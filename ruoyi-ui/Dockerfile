# ============================================================
# 若依医疗 · 前端镜像（多阶段构建）
# 构建上下文：ruoyi-ui 目录
#   docker compose build frontend
# ============================================================

# ---------- 构建阶段：Node16 安装依赖并打生产包 ----------
FROM node:16 AS build
WORKDIR /app

# 优先用 lockfile 精确还原依赖
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# 对应 package.json 中的 build:prod（vue-cli-service build）
RUN npm run build:prod

# ---------- 运行阶段：nginx 托管 dist 并代理后端 ----------
FROM nginx:alpine
# 允许胸片原图上传（FastAPI 接收 multipart）
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
