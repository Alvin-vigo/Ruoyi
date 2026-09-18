# -*- coding: utf-8 -*-
"""
ruoyi-ai / 模块④ RAG 导诊（Chroma 向量检索 + 检索增强）

与大纲的关键差异：
1. embedding 换成多语言模型（config.py 已修正），中文检索才生效；
2. 检索为空时不再硬取 ret["documents"][0] 而 KeyError/IndexError，改为返回空列表，
   由上层决定是"提示知识库无命中"还是"拒绝让大模型编造"；
3. 检索与生成解耦：即使大模型不可用，retrieve_only 模式仍返回命中的知识原文，
   这正好是论文里"有无大模型"的天然对比基线；
4. 支持运行期追加知识条目，不必每次重建向量库。
"""
from typing import Dict, List

from config import CHROMA_COLLECTION, CHROMA_DIR, EMBED_MODEL, RAG_TOP_K, RAG_SIM_THRESHOLD

from llm import generate
from prompts import SYSTEM_RAG, build_rag_prompt, join_context

# ===================== 多轮对话上下文管理 =====================
# 学生单机演示无需 Redis，用进程内字典维护 session_id -> 历史消息列表。
# 超过 SESSION_MAX_TURNS 轮自动截断最旧的部分，控制 prompt 长度与成本。
SESSION_MAX_TURNS = int(__import__("os").getenv("RAG_SESSION_MAX_TURNS", "5"))
SESSIONS: Dict[str, List[Dict[str, str]]] = {}


def _push_turn(session_id: str, role: str, content: str) -> None:
    if not session_id:
        return
    hist = SESSIONS.setdefault(session_id, [])
    hist.append({"role": role, "content": content})
    # 仅保留最近 SESSION_MAX_TURNS 轮（一轮 = 用户+助手各一条，故 *2）
    cap = SESSION_MAX_TURNS * 2
    if len(hist) > cap:
        del hist[: len(hist) - cap]


def _history_text(session_id: str) -> str:
    if not session_id:
        return ""
    hist = SESSIONS.get(session_id, [])
    if not hist:
        return ""
    lines = [f"{'患者' if m['role'] == 'user' else '导诊'}：{m['content']}" for m in hist]
    return "\n".join(lines)


class RagNotReady(Exception):
    """向量库/embedding 模型尚未就绪"""


_client = None
_collection = None
_embedder = None
_load_attempted = False
_load_error = ""


def _ensure():
    global _client, _collection, _embedder, _load_attempted, _load_error
    if _collection is not None:
        return _collection, _embedder
    if _load_attempted:
        raise RagNotReady(_load_error)

    _load_attempted = True
    _load_error = ""
    try:
        import chromadb  # type: ignore
        from sentence_transformers import SentenceTransformer  # type: ignore
    except ImportError as exc:  # pragma: no cover
        _load_error = (
            f"缺少 RAG 依赖（chromadb/sentence-transformers）：{exc}，"
            f"请 pip install -r requirements.txt"
        )
        raise RagNotReady(_load_error) from exc

    try:
        _client = chromadb.PersistentClient(path=CHROMA_DIR)
        _collection = _client.get_or_create_collection(name=CHROMA_COLLECTION)
        _embedder = SentenceTransformer(EMBED_MODEL)
    except Exception as exc:  # noqa: BLE001
        _load_error = f"向量库初始化失败：{exc}"
        raise RagNotReady(_load_error) from exc
    return _collection, _embedder


def status() -> Dict[str, object]:
    """
    只探测向量库本身，**不加载 embedding 模型**。

    重要：早期实现在 status() 里走 _ensure() → SentenceTransformer(EMBED_MODEL)，
    首次调用会触发几百 MB 的模型下载，在国内网络下直接把 /api/ai/health 卡死
    （huggingface.co 超时 / 证书校验失败）。健康检查必须秒回，
    所以这里只连 Chroma 取 collection.count()，模型加载推迟到真正检索时。
    """
    ready = False
    count = 0
    hint = "请先运行 scripts/build_rag_db.py 构建知识库"
    try:
        import chromadb  # type: ignore

        client = chromadb.PersistentClient(path=CHROMA_DIR)
        collection = client.get_collection(name=CHROMA_COLLECTION)
        count = collection.count()
        ready = count > 0
        hint = "" if ready else "知识库已创建但为空，请运行 scripts/build_rag_db.py 写入知识"
    except ImportError:
        hint = "缺少 RAG 依赖（chromadb），请 pip install -r requirements.txt"
    except Exception:  # noqa: BLE001 - 库不存在或尚未构建，均属正常未就绪状态
        pass
    return {
        "ready": ready,
        "collection": CHROMA_COLLECTION,
        "embed_model": EMBED_MODEL,
        "doc_count": count,
        "hint": hint,
    }


def retrieve(question: str, top_k: int = RAG_TOP_K) -> Dict[str, object]:
    """纯向量检索，不依赖大模型"""
    collection, embedder = _ensure()
    query_vec = embedder.encode(question).tolist()
    result = collection.query(query_embeddings=[query_vec], n_results=top_k)

    raw_docs = (result.get("documents") or [[]])[0]
    raw_dist = (result.get("distances") or [[]])[0]
    raw_ids = (result.get("ids") or [[]])[0]

    # 相似度阈值过滤：Chroma cosine 距离越小越相关；超过阈值视为不相关，
    # 过滤后 hit 为空会触发上层拒答分支，避免跨主题瞎编（医学场景防幻觉）。
    kept = [
        (d, doc, i)
        for d, doc, i in zip(raw_dist, raw_docs, raw_ids)
        if float(d) < RAG_SIM_THRESHOLD
    ]
    if kept:
        distances, documents, ids = zip(*kept)
    else:
        distances, documents, ids = [], [], []

    return {
        "documents": list(documents),
        "distances": [round(float(d), 4) for d in distances],
        "ids": list(ids),
        "hit": len(documents) > 0,
    }


def split_document(text: str, chunk_size: int = 200, overlap: int = 20) -> List[str]:
    """
    长文本切片（RAG 检索粒度控制）：
    单条知识超过 200 字时按句边界切分为 100-200 字的小片段，相邻片段重叠 20 字，
    保证语义连贯且检索命中更精准；短于阈值的整条入库。
    """
    import re

    text = (text or "").strip()
    if not text:
        return []
    if len(text) <= chunk_size:
        return [text]

    sentences = [s.strip() for s in re.split(r"(?<=[。！？；\n])", text) if s.strip()]
    chunks, buf = [], ""
    for sent in sentences:
        if len(buf) + len(sent) <= chunk_size:
            buf += sent
        else:
            if buf:
                chunks.append(buf)
            # 单句本身超长，强制按 chunk_size 硬切
            if len(sent) > chunk_size:
                step = chunk_size - overlap
                for i in range(0, len(sent), step):
                    chunks.append(sent[i : i + chunk_size])
                buf = ""
            else:
                buf = sent
    if buf:
        chunks.append(buf)
    # 对相邻片段做重叠拼接，避免生硬截断丢失上下文
    if overlap > 0 and len(chunks) > 1:
        merged = [chunks[0]]
        for ch in chunks[1:]:
            prev = merged[-1]
            merged[-1] = prev + ch[:overlap]
            merged.append(ch[overlap:] if len(ch) > overlap else ch)
        chunks = [c for c in merged if c]
    return chunks


def add_documents(docs: List[str]) -> Dict[str, object]:
    """向知识库追加条目（长文本先切片，保证检索粒度）"""
    collection, embedder = _ensure()
    if not docs:
        return {"added": 0}

    import time

    final_docs: List[str] = []
    ids: List[str] = []
    base = int(time.time() * 1000)
    idx = 0
    for doc in docs:
        for chunk in split_document(doc):
            final_docs.append(chunk)
            ids.append(f"doc_{base}_{idx}")
            idx += 1

    if not final_docs:
        return {"added": 0, "ids": []}
    vectors = embedder.encode(final_docs).tolist()
    collection.add(ids=ids, embeddings=vectors, documents=final_docs)
    return {"added": len(final_docs), "ids": ids}


def ask(question: str, top_k: int = RAG_TOP_K, session_id: str = "") -> Dict[str, object]:
    """
    带多轮上下文的导诊问答编排（替代 main.py 里散落的 retrieve+generate 逻辑）：
    1. 向量检索知识库；
    2. 命中为空 -> 直接拒答（防幻觉），并把这次问答记入会话历史；
    3. 命中 -> 拼接【对话历史】+【参考资料】交给大模型，生成后更新会话历史。
    返回与旧 rag_chat 一致的信封结构，并回显 session_id。
    """
    import time

    started = time.time()
    hits = retrieve(question, top_k=top_k)

    # 知识库无命中：直接拒答，绝不交给大模型自由发挥（医学场景防幻觉）
    if not hits["hit"]:
        _push_turn(session_id, "user", question)
        _push_turn(session_id, "assistant", "（知识库未命中，已拒绝生成回答）")
        return {
            "session_id": session_id,
            "answer": "本地知识库未检索到相关内容，为避免误导，暂不给出结论。建议前往医院相应科室就诊。",
            "references": [],
            "hit": False,
            "degraded": False,
            "engine": "rag-retrieve-only",
            "elapsed_ms": int((time.time() - started) * 1000),
        }

    # 命中：把历史对话拼进 prompt，实现"追问不丢上文"
    history = _history_text(session_id)
    base_prompt = build_rag_prompt(question, join_context(hits["documents"]))
    if history:
        prompt = f"【对话历史】\n{history}\n\n{base_prompt}"
    else:
        prompt = base_prompt

    _push_turn(session_id, "user", question)
    result = generate(prompt, system=SYSTEM_RAG, temperature=0.2)

    if result["ok"]:
        answer = result["text"]
        _push_turn(session_id, "assistant", answer)
        return {
            "session_id": session_id,
            "answer": answer,
            "references": hits["documents"],
            "reference_scores": hits["distances"],
            "hit": True,
            "degraded": False,
            "engine": "rag+ollama",
            "elapsed_ms": result["elapsed_ms"],
        }

    # 大模型不可用 -> 纯检索模式：返回命中知识原文（论文对比基线）
    fallback_answer = "【纯检索模式】大模型未启用，以下为知识库原文：\n\n" + join_context(hits["documents"])
    _push_turn(session_id, "assistant", fallback_answer)
    return {
        "session_id": session_id,
        "answer": fallback_answer,
        "references": hits["documents"],
        "reference_scores": hits["distances"],
        "hit": True,
        "degraded": True,
        "engine": "rag-retrieve-only",
        "note": str(result["reason"]),
        "elapsed_ms": int((time.time() - started) * 1000),
    }
