# ============================================================
# 若依医疗 · AI 微服务镜像
# 构建上下文：ruoyi-ai 目录
#   docker compose build ai
# 说明：模型权重（models/）与向量库（data/）通过卷挂载复用宿主机已训练产物，
#       不打包进镜像，镜像只含代码与 Python 依赖，构建更快、体积小。
# ============================================================
FROM python:3.11-slim

ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    HF_ENDPOINT=https://hf-mirror.com \
    PIP_NO_CACHE_DIR=1

# torch 运行时依赖 libgomp；国内用清华源加速（torch 较大，构建需耐心）
RUN apt-get update \
    && apt-get install -y --no-install-recommends libgomp1 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 复制服务代码（models/ 与 data/ 已在 .dockerignore 排除，由卷挂载提供）
COPY . .

EXPOSE 8000
# AI_HOST 由 compose 注入 0.0.0.0 以便容器外访问
CMD ["python", "main.py"]
