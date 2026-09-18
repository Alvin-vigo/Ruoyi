# -*- coding: utf-8 -*-
"""
ruoyi-ai / 全局配置

所有路径、模型名、超时集中在此，支持环境变量覆盖。
兼容 Python 3.11；本机装好依赖后无需改动即可运行。
"""
import os
from pathlib import Path

# 国内网络直连 huggingface.co 会超时挂死（WinError 10060），默认走 HF 镜像；
# 如需改回官方源，设环境变量 HF_ENDPOINT=https://huggingface.co
os.environ.setdefault("HF_ENDPOINT", "https://hf-mirror.com")

# ---------------- 基础路径 ----------------
BASE_DIR = Path(__file__).resolve().parent
MODEL_DIR = Path(os.getenv("AI_MODEL_DIR", str(BASE_DIR / "models")))
DATA_DIR = Path(os.getenv("AI_DATA_DIR", str(BASE_DIR / "data")))
UPLOAD_DIR = Path(os.getenv("AI_UPLOAD_DIR", str(DATA_DIR / "uploads")))

for _p in (MODEL_DIR, DATA_DIR, UPLOAD_DIR):
    _p.mkdir(parents=True, exist_ok=True)

# ---------------- Ollama 本地大模型 ----------------
# 装好 Ollama 并 pull qwen2.5:7b 后，模块①病历生成与模块④RAG 走真实大模型。
OLLAMA_BASE = os.getenv("OLLAMA_BASE", "http://127.0.0.1:11434").rstrip("/")
OLLAMA_GENERATE_URL = f"{OLLAMA_BASE}/api/generate"
OLLAMA_TAGS_URL = f"{OLLAMA_BASE}/api/tags"

LLM_MODEL = os.getenv("LLM_MODEL", "qwen2.5:7b-instruct-q4_K_M")
# 7B 在 RTX4050 上首字较慢，默认给足 120s；健康检查要快，单独设短超时
LLM_TIMEOUT = float(os.getenv("LLM_TIMEOUT", "120"))
LLM_HEALTH_TIMEOUT = float(os.getenv("LLM_HEALTH_TIMEOUT", "3"))
LLM_HEALTH_CACHE_SEC = float(os.getenv("LLM_HEALTH_CACHE_SEC", "30"))
LLM_ENABLED = os.getenv("LLM_ENABLED", "1").lower() not in ("0", "false", "no")
# 模型常驻显存时长：默认 30 分钟，避免每次调用都重新把 4.7GB 模型加载进显存
# （首字延迟从数十秒降到秒级）。设为 0 立即卸载，负数（如 -1）永久常驻。
OLLAMA_KEEP_ALIVE = os.getenv("OLLAMA_KEEP_ALIVE", "30m")

# ---------------- RAG 向量检索 ----------------
# 【重要修正】大纲原用 all-MiniLM-L6-v2 —— 那是纯英文模型，
# 检索中文病历/中文知识库会严重失效。改为多语言模型（支持中文）。
# 如需更强的中文效果，可设环境变量 EMBED_MODEL=BAAI/bge-small-zh-v1.5
# 优先使用已下载到本地的模型目录（离线可用，运行时不再访问 HuggingFace）；
# 目录不存在时退回 Hub 名称（首次会下载 ~470MB，国内建议先配好 HF_ENDPOINT 镜像）
EMBED_MODEL = os.getenv("EMBED_MODEL", "")
if not EMBED_MODEL:
    _local_embed = MODEL_DIR / "paraphrase-multilingual-MiniLM-L12-v2"
    EMBED_MODEL = str(_local_embed) if _local_embed.is_dir() else (
        "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
    )
CHROMA_DIR = os.getenv("CHROMA_DIR", str(DATA_DIR / "chroma_db"))
CHROMA_COLLECTION = os.getenv("CHROMA_COLLECTION", "medical_knowledge")
RAG_TOP_K = int(os.getenv("RAG_TOP_K", "3"))
# RAG 切片策略：单条知识超过 RAG_CHUNK_SIZE 字时按句切分，相邻片段重叠 RAG_CHUNK_OVERLAP 字
RAG_CHUNK_SIZE = int(os.getenv("RAG_CHUNK_SIZE", "200"))
RAG_CHUNK_OVERLAP = int(os.getenv("RAG_CHUNK_OVERLAP", "20"))
# RAG 相似度阈值（Chroma cosine 距离，即 1-余弦相似度；越小越相关）。
# 超过该距离的检索结果视为不相关，触发拒答分支，避免跨主题瞎编（医学场景防幻觉）。
RAG_SIM_THRESHOLD = float(os.getenv("RAG_SIM_THRESHOLD", "0.55"))

# ---------------- 模型文件 ----------------
RISK_MODEL_PATH = Path(os.getenv("RISK_MODEL_PATH", str(MODEL_DIR / "model_risk.pkl")))
CHEST_MODEL_PATH = Path(os.getenv("CHEST_MODEL_PATH", str(MODEL_DIR / "chest_cnn.pth")))

# ---------------- 模块② 风险预测 ----------------
# 【必须与 train_risk.py 中的特征顺序严格一致】，否则推理结果无意义
# 必须与 models/risk_features.json（模型训练时的真实特征顺序）严格一致，
# 否则推理端收到的特征数量/顺序与模型不匹配会直接 500。此前曾误写成 5 个简化特征。
RISK_FEATURE_NAMES = ["Pregnancies", "Glucose", "BloodPressure", "SkinThickness", "Insulin", "BMI", "DiabetesPedigreeFunction", "Age"]
RISK_THRESHOLD = float(os.getenv("RISK_THRESHOLD", "0.5"))

# ---------------- 模块③ 胸片 ----------------
CHEST_CLASSES = ["正常", "异常"]
CHEST_THRESHOLD = float(os.getenv("CHEST_THRESHOLD", "0.5"))
CHEST_IMG_SIZE = int(os.getenv("CHEST_IMG_SIZE", "224"))

# ---------------- 服务 ----------------
APP_HOST = os.getenv("AI_HOST", "127.0.0.1")
APP_PORT = int(os.getenv("AI_PORT", "8000"))
