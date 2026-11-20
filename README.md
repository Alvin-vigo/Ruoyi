# 若依医疗管理系统（RuoYi-Medical）

一套基于若依（RuoYi）快速开发框架**裁剪**而来的轻量级医疗业务管理系统，覆盖“患者就诊 → 医生诊疗 → 药房发药 → 随访管理”全流程，并集成 **AI 辅助能力**（智能病历、风险预测、胸片初筛、智能导诊）。纯本地部署，**不依赖任何外部公网服务**。

---

## ✨ 功能特性

- **业务闭环**：挂号（占号源）→ 开始就诊 → 诊疗记录 → 开方（自动计价 / 过敏校验）→ 发药（效期 / 库存校验）→ 退药回滚，关键操作全程审计留痕。
- **患者与药品**：患者建档（身份证 / 手机号格式校验）、药品库存与近效期预警。
- **运营看板**：13 项聚合指标，其中核心 4 项为挂号量 / 处方量 / 发药总量 / 库存预警（另含患者、诊疗、药品总量及科室、分类、费用、性别、年龄等分布）。
- **AI 智能模块**：
  - 📝 病历生成（Ollama 本地大模型，约 40–52 秒，离线可降级）
  - 📊 糖尿病风险预测（已训练模型，AUC≈0.813，8 项临床特征）
  - 🩻 胸片三分类识别（COVID-19 / NORMAL / Viral Pneumonia，macro-F1≈0.9428 / val_acc≈0.9416；COVID-19 召回 1.000、precision≈0.83，属筛查场景下的"宁枉勿纵"取舍）
  - 💬 智能导诊（本地知识库 RAG 检索增强，库外问题自动拒答防幻觉）

---

## 🏗️ 系统架构

```
浏览器 (Vue2 + Element UI, :8081)
        │  HTTP / JSON
        ▼
后端 (Spring Boot / RuoYi 裁剪版, :8080)
        │  ├─ MySQL 8.0  (ruoyi_medical, :3306)
        │  └─ Redis      (Memurai, :6379, 令牌)
        │
        │  HTTP / JSON (AI 网关 AiHttpUtil)
        ▼
AI 服务 (FastAPI, :8000)
        ├─ Ollama 大模型 (:11434, qwen2.5:7b)
        ├─ 风险预测 / 胸片 (PyTorch + sklearn)
        └─ 智能导诊 (Chroma 向量库 + 本地嵌入)
```

---

## 🧰 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot、Spring Security、JWT、MyBatis、Quartz、Redis |
| 前端 | Vue2、Element UI、Axios |
| AI | FastAPI、PyTorch、scikit-learn、Chroma、Ollama、sentence-transformers |
| 数据 | MySQL 8.0（utf8mb4）、Redis（Memurai 兼容） |

---

## ⚙️ 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 8 | 后端运行 |
| Maven | 3.9.x | 后端构建（`-Dmaven.test.skip=true` 可跳过测试） |
| Node.js | 16.x | 前端 dev / build |
| Python | 3.13 | AI 服务（`ruoyi-ai` venv，CPU；胸片训练用 `chest-train` venv，GPU） |
| MySQL | 8.0 | 库名 `ruoyi_medical` |
| Redis | 兼容实现 Memurai | 端口 6379 |
| Ollama | 含 `qwen2.5:7b-instruct-q4_K_M` | 端口 11434 |

---

## 🚀 快速开始

### 方式一：一键启动（推荐演示）
直接**双击**项目根目录的 `start.bat`（它会依次拉起 MySQL → Memurai → 后端 → AI 服务 → 前端）。

> 脚本**不再自动启动 Ollama**；如需 AI 病历生成 / 智能导诊的大模型能力，请手动执行 `D:\AI\Ollama\ollama.exe serve`。未启动时相应功能自动降级（病历走模板草稿、导诊走纯检索），其余功能不受影响。

> ⚠️ 注意：不要从脚本 / 无头 Shell 整跑 `start.bat`（`start ""` 弹出的子窗口进程在无头环境不存活）；开发联调用下面的分步方式。

### 方式二：分步启动（开发）
依次启动，且后端务必显式指定端口：

```bash
# 1. 缓存（Memurai）
D:/Memurai/memurai.exe

# 2. 数据库（MySQL 8.0，库 ruoyi_medical 已导入 SQL）

# 3. 后端（务必 --server.port=8080，防止环境变量注入随机端口）
D:/jdk8/bin/java -jar ruoyi-admin/target/ruoyi-admin-1.0.0.jar --server.port=8080

# 4. AI 服务
cd ruoyi-ai
C:/Users/ALVIN/.workbuddy/binaries/python/envs/ruoyi-ai/Scripts/python.exe -m uvicorn main:app --host 127.0.0.1 --port 8000

# 5. Ollama（可选，仅 AI 病历生成 / 导诊问答需要；未启动则自动降级）
D:/AI/Ollama/ollama.exe serve

# 6. 前端
cd ruoyi-ui && D:/nodejs16/npm.cmd run dev    # 开发模式 :8081
# 或 D:/nodejs16/npm.cmd run build:prod        # 生产构建
```

### 方式三：容器化部署（Docker，跨机 / 生产推荐）

> 适用：希望一条命令拉起整套环境，或迁移到 Linux 服务器。产物由 B-16 交付（`docker-compose.yml` + 三个 `Dockerfile` + `docker/mysql/init-db.sh`）。**本开发沙箱无 Docker，未实测 `docker build` / 启动，首次请在装有 Docker 的主机上执行验证**。

```bash
# 1. （可选）在 compose 同级放 .env 覆盖默认口令；模板见 .env.example
#    cp .env.example .env   然后编辑其中的 DB_PASSWORD

# 2. 构建并拉起全套（mysql / redis / backend / frontend / ai）
docker compose up -d --build

# 3. 访问前端
#    浏览器打开 http://localhost:8081
```

要点：
- 前端由 Nginx 托管 `dist/`，并把 `/medical`、`/login`、`/logout`、`/getInfo`、`/monitor`、`/system`、`/tool`、`/gen` 代理到 `backend:8080`，`/ai-uploads` 代理到 `ai:8000`（路径直转，**无 `/prod-api` 前缀**，与前端 `baseURL` 一致）。
- 后端 `application.yml` 已支持 `DB_HOST` / `DB_PORT` / `REDIS_HOST` / `REDIS_PORT` / `AI_SERVICE_URL` 环境变量；容器间通过服务名互通，无需改代码。
- MySQL 首次启动由 `docker/mysql/init-db.sh` 按依赖顺序灌入 `sql/` 下全部脚本（顺序见「部署说明」）。**模型权重（`ruoyi-ai/models/`）与向量库（`ruoyi-ai/data/`）通过卷挂载复用宿主机已训练产物，不打包进镜像**。
- Ollama 跑在宿主机，`ai` 服务经 `host.docker.internal:11434` 访问；未启动时模块①（病历）与④（RAG）自动降级。

**默认账号**：`admin`，口令见 `.env.example`（首次部署请自行修改；当前未启用验证码）。

---

## 📁 目录结构

```
若依医疗/
├── ruoyi-admin/          后端启动模块（打包为 ruoyi-admin-1.0.0.jar）
├── ruoyi-system/        业务 domain / mapper / service / controller
├── ruoyi-common/        公共工具
├── ruoyi-framework/     安全、Redis、跨域等基础设施
├── ruoyi-generator/     代码生成（裁剪保留）
├── ruoyi-quartz/        定时任务
├── ruoyi-ui/            前端 Vue2 工程（src/views/medical/*）
├── ruoyi-ai/            AI 服务（FastAPI + 模型 + 数据）
│   ├── main.py          网关入口
│   ├── chest.py         胸片识别
│   ├── rag.py           智能导诊
│   ├── config.py        配置（特征顺序、类别、模型路径）
│   ├── models/          训练好的模型与特征 / 类别 JSON
│   └── data/chroma_db/  本地知识库向量
├── doc/                 项目文档（需求 / 接口 / 部署 / 演示清单）
├── docker-compose.yml   容器编排（mysql/redis/backend/frontend/ai 五服务）
├── docker/              MySQL 初始化脚本（init-db.sh，按序灌库）
├── ruoyi-admin/Dockerfile   后端多阶段构建镜像
├── ruoyi-ui/Dockerfile      前端构建 + Nginx 托管镜像
├── ruoyi-ai/Dockerfile      AI 服务镜像（模型 / 向量库走卷挂载）
└── start.bat            一键启动脚本
```

---

## 📚 模块说明

| 模块 | 说明 |
|------|------|
| 患者管理 | 建档、身份证 / 手机号校验 |
| 挂号管理 | 挂号占号源、退号、开始就诊 |
| 排班管理 | 医生出诊排班与号源 |
| 诊疗管理 | 诊疗草稿、诊断结果、AI 辅助写病历 |
| 药品管理 | 药品目录、库存、近效期预警 |
| 处方发药 | 开方（计价 / 过敏校验）、发药、退药 |
| 随访管理 | 随访计划与记录 |
| 站内信 | 消息发送与未读统计 |
| 价目管理 | 诊疗项目价目表 |
| 审计日志 | 关键操作留痕查询 |
| 统计看板 | 运营指标聚合 |
| 定时任务 | Quartz 任务调度 |

---

## 🤖 AI 能力说明

- **病历生成**：调用本地 Ollama `qwen2.5:7b` 生成结构化病历；Ollama 未就绪时降级为模板草稿。
- **风险预测**：输入 8 项临床特征，输出风险等级与概率（模型 `models/model_risk.pkl`）。
- **胸片识别**：三分类 CNN（`models/chest_cnn.pth`），返回结果 + 置信度 + 三类概率，并落库。
- **智能导诊**：本地知识库 RAG（`medical_rag_knowledge` 当前 18 条导诊 / 常见病知识，建库脚本内置 10 条种子，可在「导诊知识库」页增补并同步到向量库），相似度低于 0.55 阈值时拒答防幻觉。

> ⚠️ AI 结果均为**辅助参考**，不替代专业医生诊断。

---

## 📄 文档导航

- [项目需求文档](doc/项目需求文档.md)
- [详细设计说明书](doc/详细设计说明书.md)
- [运行演示流程与检查清单](doc/运行演示流程与检查清单.html)
- [接口文档](doc/接口文档.md)
- [部署说明](doc/部署说明.md)
- [AI 模块部署说明](doc/AI模块部署说明.md)
- [测试记录](doc/测试记录.md)
- [优化升级方案](doc/优化升级方案_精炼分析.html)

---

## ⚠️ 已知边界

1. RBAC 仅到**菜单级**（角色 → 菜单权限），未做按钮 / 数据级隔离。
2. **未启用验证码**（按需求裁剪）。
3. 胸片少数类（COVID-19）precision≈0.83，极端样本可能误判，持续优化中。
4. 演示数据含故意过期的“板蓝根颗粒”与种子业务数据，用于演示拦截与看板。
5. 容器化编排（B-16 的 `docker-compose.yml` + 三个 `Dockerfile`）已随项目提供，**尚未在本开发环境实测 `docker build` / 启动**，首次部署请在装有 Docker 的主机上执行 `docker compose up -d --build` 验证。

---

## 📜 声明

本项目为教学 / 演示用途，患者信息均为模拟数据；AI 模块输出仅作辅助，不承担任何医疗诊断责任。
