@echo off
chcp 65001 >nul
title 若依医疗 一键启动
setlocal

:: ============ 路径配置（如与你本机不符，改这里即可） ============
set PROJECT_DIR=D:\若依医疗\若依医疗
set JAVA_EXE=D:\jdk8\bin\java.exe
set NPM_EXE=D:\nodejs16\npm.cmd
set AI_PYTHON=C:\Users\ALVIN\.workbuddy\binaries\python\envs\ruoyi-ai\Scripts\python.exe
set MEMURAI_EXE=D:\Memurai\memurai.exe
:: MySQL 服务名：常见为 MySQL80 或 MySQL，下面会自动回退
set MYSQL_SVC=MySQL80

echo ============================================
echo   若依医疗一体化管理系统 - 一键启动
echo ============================================
echo.

:: 1) 启动 MySQL（若依元数据库 ruoyi_medical）
echo [1/5] 启动 MySQL 服务...
net start %MYSQL_SVC% || net start MySQL || echo [WARN] MySQL 服务未启动，请手动启动后继续

:: 2) 启动 Redis（本机用 Memurai 兼容，端口 6379，存储登录 token）
echo [2/5] 启动 Redis(Memurai)...
if exist "%MEMURAI_EXE%" (
  start "" "%MEMURAI_EXE%"
) else (
  net start Memurai || echo [WARN] 未找到 Memurai(%MEMURAI_EXE%)，请确认路径或改用 net start Memurai
)

:: 等数据库/缓存就绪
echo [..] 等待 MySQL / Redis 就绪（8 秒）...
timeout /t 8 /nobreak >nul

:: 3) 启动后端 Spring Boot（已预编译则直接跑 jar，否则先构建；--server.port 固定 8080，防环境变量干扰）
echo [3/5] 启动后端服务(8080)...
cd /d "%PROJECT_DIR%"
if exist ruoyi-admin\target\ruoyi-admin-1.0.0.jar (
  start "" "%JAVA_EXE%" -jar ruoyi-admin\target\ruoyi-admin-1.0.0.jar --server.port=8080
) else (
  echo [INFO] 未找到预编译 jar，开始构建（首次约几分钟，请耐心等待）...
  call mvnw.cmd clean install -DskipTests
  start "" "%JAVA_EXE%" -jar ruoyi-admin\target\ruoyi-admin-1.0.0.jar --server.port=8080
)

:: 4) 启动前端 Vue 开发服务(8081 端口，dev 模式含代理)
echo [4/5] 启动前端服务(dev)...
start "" cmd /k "cd /d "%PROJECT_DIR%\ruoyi-ui" && "%NPM_EXE%" run dev"

:: 5) 启动 AI 微服务 FastAPI(8000)
echo [5/5] 启动 AI 微服务(8000)...
start "" cmd /k "cd /d "%PROJECT_DIR%\ruoyi-ai" && "%AI_PYTHON%" -m uvicorn main:app --host 127.0.0.1 --port 8000"

echo.
echo ============================================
echo  全部服务已尝试启动，请查看各弹出的窗口：
echo    后端  : http://127.0.0.1:8080
echo    前端  : http://127.0.0.1:8081
echo    AI    : http://127.0.0.1:8000/docs
echo.
echo  说明：本脚本不再自动启动 Ollama(11434)。
echo  如需 AI 病历生成 / 导诊问答的大模型能力，请手动启动：
echo    D:\AI\Ollama\ollama.exe serve
echo  未启动时相关功能自动降级（病历走模板草稿、导诊走纯检索），不影响其余功能。
echo ============================================
echo.
pause
