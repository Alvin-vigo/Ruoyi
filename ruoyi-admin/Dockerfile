# ============================================================
# 若依医疗 · 后端镜像（多阶段构建）
# 构建上下文必须指向仓库根目录：
#   docker compose build backend      # compose 已配置 context: .
# 或手动：
#   docker build -f ruoyi-admin/Dockerfile -t ruoyi-medical-backend .
# ============================================================

# ---------- 构建阶段：用 Maven + JDK8 把整个多模块工程打成可执行 jar ----------
FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /build

# 先拷贝源码（含各模块 pom），再构建；-am 自动构建 ruoyi-admin 依赖的兄弟模块
COPY . .
# 跳过单测：测试需要 MySQL/Redis 环境，CI/容器构建阶段只验证可编译可打包
RUN mvn -B clean package -DskipTests -pl ruoyi-admin -am

# ---------- 运行阶段：仅带 JRE8，镜像更小 ----------
FROM eclipse-temurin:8-jre
WORKDIR /app

COPY --from=build /build/ruoyi-admin/target/ruoyi-admin-1.0.0.jar app.jar

# DB_HOST / REDIS_HOST / AI_SERVICE_URL 等由 docker-compose 注入；
# application.yml 已用 ${DB_HOST:localhost} 等占位符，缺省行为与改造前一致。
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
