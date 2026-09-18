# -*- coding: utf-8 -*-
"""
ruoyi-ai / FastAPI 服务入口

对外端口 127.0.0.1:8000，Java 后端通过 HTTP 调用。
统一响应信封 {code, msg, data} 与若依 AjaxResult 对齐（200 成功 / 500 失败）。

降级策略（本机单机部署，任一环节不可用时服务不能崩）：
  模块① 病历  ：大模型不可用时输出结构化模板草稿（degraded=True）
  模块② 风险  ：模型未训练时报 500 并给出训练命令（有模型即可真实运行，无需 GPU）
  模块③ 胸片  ：模型未训练/无 torch 时报 500 并给出训练命令
  模块④ RAG   ：向量库未命中直接拒绝回答（防幻觉）；大模型不可用降级为纯检索模式
"""
import io
import json
import threading
import time
import traceback
import uuid
from typing import Any, Dict, Iterator

from fastapi import FastAPI, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from fastapi.staticfiles import StaticFiles
from PIL import Image

import chest as chest_service
import rag as rag_service
import risk as risk_service
from config import (
    APP_HOST,
    APP_PORT,
    LLM_MODEL,
    RISK_FEATURE_NAMES,
    UPLOAD_DIR,
)
from fallback import build_fallback_record
from llm import generate_stream, health_check, warmup
from prompts import SYSTEM_RECORD, build_record_prompt
from schemas import DeleteUploadReq, MedicalRecordReq, RagAddDocReq, RagQueryReq, RiskReq

app = FastAPI(title="医疗AI微服务", description="若依医疗AI综合平台 - AI能力层", version="1.0.0")

# 挂载胸片图片静态目录。
# 推理后原图保存在 data/uploads 下，接口返回 img_url="/ai-uploads/xxx.png"，
# 没有这段挂载的话前端 <img> 一律 404，历史检测记录里会看不到图。
# 前端 ruoyi-ui/vue.config.js 已配置 /ai-uploads -> http://127.0.0.1:8000 的 devServer 代理。
UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
app.mount("/ai-uploads", StaticFiles(directory=str(UPLOAD_DIR)), name="ai-uploads")

# 允许若依前端/后端跨域调用（本机联调）
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


def ok(data: Any = None, msg: str = "操作成功") -> Dict[str, Any]:
    return {"code": 200, "msg": msg, "data": data}


def fail(msg: str, code: int = 500, data: Any = None) -> Dict[str, Any]:
    return {"code": code, "msg": msg, "data": data}


def _sse(obj: Dict[str, Any]) -> str:
    """把对象打成一行 SSE 帧：data: {json}\n\n"""
    return "data: " + json.dumps(obj, ensure_ascii=False) + "\n\n"


# ===================== 模块① 智能病历辅助书写（流式 SSE） =====================
@app.post("/api/ai/generate_record_stream")
def generate_record_stream(req: MedicalRecordReq):
    """
    流式生成病历（SSE，text/event-stream）。前端用 fetch + ReadableStream 逐帧渲染，
    实现"打字机"效果，规避一次性等待 10-30s 的卡顿观感。

    帧格式（每行一条）：
      {"type":"delta","text":"...增量文本..."}   逐 token 推送
      {"type":"done","content":"全文","engine":"ollama|template",
       "model":"...","degraded":bool,"elapsed_ms":int,
       "patient_name":"...","patient_id":int}

    Ollama 不可用时整体降级为模板草稿（仍走 SSE，保证前端逻辑统一）。
    """
    prompt = build_record_prompt(req)

    def event_generator() -> Iterator[str]:
        available, reason = health_check()
        if not available:
            # 降级：一次性把模板草稿作为一段 delta 推给前端
            fb = build_fallback_record(req, reason)
            fb_text = fb.get("content", "")
            yield _sse({"type": "delta", "text": fb_text})
            yield _sse({
                "type": "done",
                "content": fb_text,
                "engine": "template",
                "model": LLM_MODEL,
                "degraded": True,
                "elapsed_ms": fb.get("elapsed_ms", 0),
                "patient_name": req.patient_name,
                "patient_id": req.patient_id,
            })
            return

        full: list = []
        for token, meta in generate_stream(prompt, system=SYSTEM_RECORD, temperature=0.3):
            if meta:
                # 收尾帧：携带全文与元信息
                yield _sse({
                    "type": "done",
                    "content": "".join(full),
                    "engine": "ollama",
                    "model": meta.get("model", LLM_MODEL),
                    "degraded": False,
                    "elapsed_ms": meta.get("elapsed_ms", 0),
                    "patient_name": req.patient_name,
                    "patient_id": req.patient_id,
                })
                continue
            full.append(token)
            yield _sse({"type": "delta", "text": token})

    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"},
    )


# ===================== 健康检查 =====================
@app.get("/api/ai/health")
async def health() -> Dict[str, Any]:
    """一次性看清四个模块各处于什么状态，联调时先看这个"""
    llm_ok, llm_reason = health_check(force=True)
    return ok(
        {
            "service": "ruoyi-ai",
            "llm": {"ready": llm_ok, "model": LLM_MODEL, "reason": llm_reason},
            "record": {"engine": "ollama" if llm_ok else "template"},
            "risk": risk_service.status(),
            "chest": chest_service.status(),
            "rag": rag_service.status(),
        }
    )


# ===================== 模块① 智能病历辅助书写 =====================
@app.post("/api/ai/generate_record")
async def generate_record(req: MedicalRecordReq) -> Dict[str, Any]:
    started = time.time()
    try:
        from llm import generate

        prompt = build_record_prompt(req)
        result = generate(prompt, system=SYSTEM_RECORD, temperature=0.3)

        if result["ok"]:
            return ok(
                {
                    "content": result["text"],
                    "engine": "ollama",
                    "model": result["model"],
                    "degraded": False,
                    "elapsed_ms": result["elapsed_ms"],
                    "patient_name": req.patient_name,
                    "patient_id": req.patient_id,
                }
            )

        # 大模型不可用 -> 降级到结构化模板
        fallback = build_fallback_record(req, str(result["reason"]))
        fallback["elapsed_ms"] = int((time.time() - started) * 1000)
        return ok(fallback, msg=str(result["reason"]))
    except Exception as exc:  # noqa: BLE001
        traceback.print_exc()
        return fail(f"病历生成失败：{exc}")


# ===================== 模块② 常见病风险预测 =====================
@app.post("/api/ai/risk_predict")
async def risk_predict(req: RiskReq) -> Dict[str, Any]:
    try:
        result = risk_service.predict(req.features)
        result.update({"disease_name": req.disease_name, "patient_id": req.patient_id})
        return ok(result)
    except risk_service.ModelNotReady as exc:
        return fail(str(exc))
    except ValueError as exc:
        return fail(str(exc))
    except Exception as exc:  # noqa: BLE001
        traceback.print_exc()
        return fail(f"风险预测失败：{exc}")


# ===================== 模块③ 胸片智能初筛 =====================
@app.post("/api/ai/chest_xray")
async def chest_xray(file: UploadFile = File(...)) -> Dict[str, Any]:
    started = time.time()
    if not (file.content_type or "").startswith("image/"):
        return fail(f"请上传图片文件，当前类型：{file.content_type}")

    try:
        # 先一次性读出字节：UploadFile.file 读过一次后指针到末尾，后续存图会失败
        raw = await file.read()
    except Exception as exc:  # noqa: BLE001
        return fail(f"读取上传文件失败：{exc}")
    if not raw:
        return fail("上传文件内容为空")

    # 安全加固（point 14）：扩展名白名单 + 单文件大小上限，
    # 防止攻击者上传超大文件或非图像内容撑爆磁盘/绕过类型校验。
    MAX_UPLOAD_BYTES = 10 * 1024 * 1024
    ALLOWED_EXT = {"png", "jpg", "jpeg", "bmp", "dcm", "gif"}
    suffix = (file.filename or "").rsplit(".", 1)[-1].lower()
    if suffix not in ALLOWED_EXT:
        return fail(f"不支持的文件类型：.{suffix or '未知'}，仅允许 png/jpg/jpeg/bmp/dcm")
    if len(raw) > MAX_UPLOAD_BYTES:
        return fail("上传文件过大，单张胸片图片不能超过 10MB")

    try:
        image = Image.open(io.BytesIO(raw))
        image.load()
    except Exception as exc:  # noqa: BLE001
        return fail(f"图片解析失败：{exc}")

    try:
        result = chest_service.predict(image)
    except chest_service.ModelNotReady as exc:
        return fail(str(exc))
    except Exception as exc:  # noqa: BLE001
        traceback.print_exc()
        return fail(f"胸片识别失败：{exc}")

    # 留存待查记录（供历史追溯，Java 侧可落 img_url）
    suffix = (file.filename or "xray.png").rsplit(".", 1)[-1].lower()
    filename = f"{uuid.uuid4().hex}.{suffix if suffix in {'png', 'jpg', 'jpeg'} else 'png'}"
    saved_path = UPLOAD_DIR / filename
    try:
        Image.open(io.BytesIO(raw)).convert("RGB").save(saved_path)
    except Exception:  # noqa: BLE001 - 存图失败不影响已有推理结果
        saved_path = None

    result.update(
        {
            "filename": saved_path.name if saved_path else "",
            "img_url": f"/ai-uploads/{saved_path.name}" if saved_path else "",
            "elapsed_ms": int((time.time() - started) * 1000),
        }
    )
    return ok(result)


# ===================== 模块④ RAG 导诊问答（多轮上下文） =====================
@app.post("/api/ai/rag_chat")
async def rag_chat(req: RagQueryReq) -> Dict[str, Any]:
    try:
        result = rag_service.ask(req.question, top_k=req.top_k or 3, session_id=req.session_id or "")
    except rag_service.RagNotReady as exc:
        return fail(str(exc))
    except Exception as exc:  # noqa: BLE001
        traceback.print_exc()
        return fail(f"导诊问答失败：{exc}")
    return ok(result)


@app.post("/api/ai/rag/add")
async def rag_add(req: RagAddDocReq) -> Dict[str, Any]:
    """运行期追加知识条目，免去反复重建向量库"""
    try:
        res = rag_service.add_documents(req.docs)
        return ok(res, msg=f"已写入 {res['added']} 条知识")
    except rag_service.RagNotReady as exc:
        return fail(str(exc))
    except Exception as exc:  # noqa: BLE001
        traceback.print_exc()
        return fail(f"知识写入失败：{exc}")


@app.get("/api/ai/risk_features")
async def risk_features() -> Dict[str, Any]:
    """供前端表单动态生成输入项，避免前端写死导致顺序错乱"""
    return ok({"features": RISK_FEATURE_NAMES})


# ===================== 模块③ 胸片：删除物理图片 =====================
@app.post("/api/ai/delete_upload")
async def delete_upload(req: DeleteUploadReq) -> Dict[str, Any]:
    """
    删除 /ai-uploads 下的物理图片（删除胸片历史记录时同步清理，避免垃圾文件堆积）。
    仅接受文件名、强制防目录穿越；失败不影响上层数据库记录删除。
    """
    filename = (req.filename or "").strip()
    if not filename or "/" in filename or "\\" in filename or ".." in filename:
        return fail("非法文件名")
    try:
        target = (UPLOAD_DIR / filename).resolve()
        if target != UPLOAD_DIR.resolve() and UPLOAD_DIR.resolve() not in target.parents:
            return fail("非法路径")
        if target.exists() and target.is_file():
            target.unlink()
            return ok({"deleted": True})
        return ok({"deleted": False, "reason": "文件不存在"})
    except Exception as exc:  # noqa: BLE001
        return fail(f"删除失败：{exc}")


@app.on_event("startup")
def _startup_warmup():
    """
    后台预热：把本地大模型常驻显存（配合 OLLAMA_KEEP_ALIVE），
    让首次真实推理不再经历数十秒的模型加载。Ollama 未启动时只记录原因，
    不阻塞启动、不抛异常（演示前手动拉起 Ollama 即可）。
    """
    threading.Thread(target=warmup, daemon=True).start()


if __name__ == "__main__":
    import uvicorn

    print(f"ruoyi-ai 启动于 http://{APP_HOST}:{APP_PORT}  (接口文档: /docs)")
    uvicorn.run(app, host=APP_HOST, port=APP_PORT)
