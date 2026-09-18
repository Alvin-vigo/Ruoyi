# -*- coding: utf-8 -*-
"""
ruoyi-ai / Ollama 大模型客户端

设计要点（针对本机单机部署）：
1. 健康检查带缓存：避免每个请求都去探测 Ollama，30s 内复用上次结论；
2. 分层超时：健康检查 3s（快速失败），生成 120s（7B 在 4050 上首字慢，给足）；
3. 永不抛出：任何失败都转成 {ok: False, degraded: True, reason}，由上层走降级逻辑，
   保证 Java 侧拿到的是可读错误信息而不是 500 / 连接重置。
"""
import json
import time
from typing import Dict, Iterator, Optional, Tuple

import requests

from config import (
    LLM_ENABLED,
    LLM_HEALTH_CACHE_SEC,
    LLM_HEALTH_TIMEOUT,
    LLM_MODEL,
    LLM_TIMEOUT,
    OLLAMA_GENERATE_URL,
    OLLAMA_KEEP_ALIVE,
    OLLAMA_TAGS_URL,
)

_state: Dict[str, object] = {"checked_at": 0.0, "available": None, "reason": ""}


def health_check(force: bool = False) -> Tuple[bool, str]:
    """
    探测 Ollama 是否可用、目标模型是否已拉取。
    返回 (是否可用, 原因说明)
    """
    now = time.time()
    cached_age = now - float(_state["checked_at"])
    if (
        not force
        and _state["available"] is not None
        and cached_age < LLM_HEALTH_CACHE_SEC
    ):
        return bool(_state["available"]), str(_state["reason"])

    if not LLM_ENABLED:
        available, reason = False, "LLM_ENABLED=0，已手动禁用大模型"
    else:
        try:
            resp = requests.get(OLLAMA_TAGS_URL, timeout=LLM_HEALTH_TIMEOUT)
            if resp.status_code != 200:
                available, reason = False, f"Ollama 返回异常状态码 HTTP {resp.status_code}"
            else:
                models = [
                    m.get("name", "")
                    for m in (resp.json().get("models") or [])
                ]
                # Ollama 标签可能是 qwen2.5:7b 或 qwen2.5:7b-instruct，按基础名匹配
                base = LLM_MODEL.split(":")[0]
                hit = any(m == LLM_MODEL or m.split(":")[0] == base for m in models)
                if hit:
                    available, reason = True, f"大模型已就绪（{LLM_MODEL}）"
                else:
                    available, reason = (
                        False,
                        f"Ollama 已启动，但未找到模型 {LLM_MODEL}。"
                        f"请执行：ollama pull {LLM_MODEL}",
                    )
        except requests.exceptions.RequestException as exc:
            available, reason = (
                False,
                f"Ollama 未启动或不可达（{exc.__class__.__name__}）",
            )

    _state.update(available=available, reason=reason, checked_at=now)
    return available, reason


def reset_health_cache() -> None:
    """模型刚拉取完成时调用，强制下次重新探测"""
    _state.update(available=None, reason="", checked_at=0.0)


def generate(
    prompt: str,
    system: Optional[str] = None,
    temperature: float = 0.3,
    num_predict: int = 1024,
) -> Dict[str, object]:
    """
    调用本地大模型生成文本（一次性返回）。

    返回统一结构：
      ok        bool  是否成功
      text      str   生成文本（失败为空串）
      degraded  bool  是否走了降级（调用方据此切换模板/纯检索）
      reason    str   失败原因，前端可直接展示
      elapsed_ms int  耗时
      model     str   实际使用的模型名
    """
    available, reason = health_check()
    if not available:
        return {
            "ok": False,
            "text": "",
            "degraded": True,
            "reason": reason,
            "elapsed_ms": 0,
            "model": LLM_MODEL,
        }

    payload = {
        "model": LLM_MODEL,
        "prompt": prompt,
        "stream": False,
        "keep_alive": OLLAMA_KEEP_ALIVE,
        "options": {"temperature": temperature, "num_predict": num_predict},
    }
    if system:
        payload["system"] = system

    started = time.time()
    try:
        resp = requests.post(OLLAMA_GENERATE_URL, json=payload, timeout=LLM_TIMEOUT)
        resp.raise_for_status()
        body = resp.json()
        text = (body.get("response") or "").strip()
        if not text:
            return {
                "ok": False,
                "text": "",
                "degraded": True,
                "reason": f"大模型返回空内容（模型：{LLM_MODEL}）",
                "elapsed_ms": int((time.time() - started) * 1000),
                "model": LLM_MODEL,
            }
        return {
            "ok": True,
            "text": text,
            "degraded": False,
            "reason": "",
            "elapsed_ms": int((time.time() - started) * 1000),
            "model": LLM_MODEL,
        }
    except requests.exceptions.Timeout:
        return {
            "ok": False,
            "text": "",
            "degraded": True,
            "reason": (
                f"大模型生成超时（>{LLM_TIMEOUT:.0f}s）。"
                f"7B 模型首次加载较慢，可先执行一次 ollama run {LLM_MODEL} 预热"
            ),
            "elapsed_ms": int((time.time() - started) * 1000),
            "model": LLM_MODEL,
        }
    except Exception as exc:  # noqa: BLE001 - 兜底，绝不让异常穿透到接口层
        return {
            "ok": False,
            "text": "",
            "degraded": True,
            "reason": f"调用大模型失败：{exc}",
            "elapsed_ms": int((time.time() - started) * 1000),
            "model": LLM_MODEL,
        }


def generate_stream(
    prompt: str,
    system: Optional[str] = None,
    temperature: float = 0.3,
    num_predict: int = 1024,
) -> Iterator[Tuple[str, Dict[str, object]]]:
    """
    流式调用本地大模型，逐 token 产出文本片段，供 SSE 打字机效果。

    产出为 (token_text, final_meta) 序列：前面的元素 token_text 为增量文本，
    最后一个元素 final_meta 是一个 dict（含 done/model/degraded/elapsed_ms 等元信息），
    其 token_text 为空串，调用方据此判断收尾。

    若 Ollama 不可用，不产出任何 token，仅在 final_meta 中标记 degraded=True + reason。
    """
    available, reason = health_check()
    started = time.time()
    if not available:
        yield "", {
            "done": True,
            "ok": False,
            "degraded": True,
            "reason": reason,
            "model": LLM_MODEL,
            "elapsed_ms": 0,
        }
        return

    payload = {
        "model": LLM_MODEL,
        "prompt": prompt,
        "stream": True,
        "keep_alive": OLLAMA_KEEP_ALIVE,
        "options": {"temperature": temperature, "num_predict": num_predict},
    }
    if system:
        payload["system"] = system

    model_name = LLM_MODEL
    try:
        resp = requests.post(
            OLLAMA_GENERATE_URL, json=payload, stream=True, timeout=LLM_TIMEOUT
        )
        resp.raise_for_status()
        for line in resp.iter_lines(decode_unicode=True):
            if not line:
                continue
            try:
                obj = json.loads(line)
            except json.JSONDecodeError:
                continue
            token = obj.get("response") or ""
            if token:
                yield token, {}
            if obj.get("done"):
                model_name = obj.get("model") or model_name
                break
    except requests.exceptions.Timeout:
        yield "", {
            "done": True,
            "ok": False,
            "degraded": True,
            "reason": f"大模型生成超时（>{LLM_TIMEOUT:.0f}s）",
            "model": model_name,
            "elapsed_ms": int((time.time() - started) * 1000),
        }
        return
    except Exception as exc:  # noqa: BLE001 - 兜底
        yield "", {
            "done": True,
            "ok": False,
            "degraded": True,
            "reason": f"调用大模型失败：{exc}",
            "model": model_name,
            "elapsed_ms": int((time.time() - started) * 1000),
        }
        return

    yield "", {
        "done": True,
        "ok": True,
        "degraded": False,
        "reason": "",
        "model": model_name,
        "elapsed_ms": int((time.time() - started) * 1000),
    }


def warmup() -> Dict[str, object]:
    """
    服务启动时预热：探测 Ollama 是否在线，若在线则用极小 prompt 触发一次推理，
    把模型常驻显存（配合 OLLAMA_KEEP_ALIVE）。失败（Ollama 未启动）只记录原因，
    绝不抛异常导致服务起不来——本地单机部署，演示前手动拉起 Ollama 即可。
    """
    available, reason = health_check(force=True)
    if not available:
        return {"warmed_up": False, "reason": reason}
    try:
        # 最小推理：1 个 token 足以把模型加载进显存
        probe = generate("ping", temperature=0.0, num_predict=1)
        return {
            "warmed_up": probe["ok"],
            "model": probe.get("model"),
            "reason": probe.get("reason", ""),
        }
    except Exception as exc:  # noqa: BLE001
        return {"warmed_up": False, "reason": f"预热失败：{exc}"}
