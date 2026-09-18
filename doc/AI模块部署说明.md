# AI 模块部署与联调说明（ruoyi-ai 微服务）

> 适用版本：本文件随「四大 AI 模块」集成一同交付。
> 目标环境：Windows + Python 3.11 + NVIDIA RTX4050(6G) + 16G 内存。
> 前提：MySQL 8 已导入 `sql/medical_ai.sql`、`sql/medical_ai_menu.sql`，若依后端已按 `doc/部署说明.md` 跑通。

---

## 0. 先搞清楚整体链路

```
若依前端(Vue)  →  若依后端(SpringBoot, 8080)  →  FastAPI AI 微服务(8000)  →  Ollama(11434) / sklearn / PyTorch / Chroma
```

**三者必须都起来**，缺任何一环对应模块会报错，但系统其他部分不受影响（已做降级隔离）。

| 组件 | 端口 | 启动方式 | 缺失时的表现 |
|------|------|----------|--------------|
| 若依后端 | 8080 | 常规 SpringBoot 启动 | 整个系统不可用 |
| AI 微服务 | 8000 | `python ruoyi-ai/main.py` | 4 个 AI 菜单打开均提示"AI 服务未启动" |
| Ollama | 11434 | 装完后自动后台常驻 | ①病历走模板草稿、④问答走纯检索；②风险预测不受影响 |

---

## 1. Python 环境准备（Python 3.11）

### 1.1 创建虚拟环境（强烈建议，避免污染全局）

```powershell
cd D:\若依医疗\若依医疗\ruoyi-ai
python -m venv .venv
.\.venv\Scripts\Activate.ps1
```

> 若提示禁止运行脚本，执行：`Set-ExecutionPolicy -Scope Process -ExecutionPolicy RemoteSigned`

### 1.2 安装依赖

```powershell
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
```

**PyTorch 单独装**（与基础包分开解析，避免 pip 回溯超时）。RTX4050 用 CUDA 版：

```powershell
pip install torch torchvision --index-url https://download.pytorch.org/whl/cu121
```

无显卡时改 CPU 版：

```powershell
pip install torch torchvision --index-url https://download.pytorch.org/whl/cpu
```

> 常见坑：`fastapi` 解析图片上传依赖 `python-multipart`，requirements 里已包含。
> 若 `/api/ai/chest_xray` 返回 422，99% 是没装它。

---

## 2. 四个模型的准备（按难度从易到难）

### 2.1 模块② 风险预测 —— 立刻可用（推荐先做这个）

无需 GPU、无需大模型，秒级训练：

```powershell
python scripts\train_risk.py
```

产出：`models/model_risk.pkl` + `models/risk_features.json`。

> **重要**：`risk_features.json` 是**训练与推理的唯一真相源**。
> 前端表单、后端校验、模型推理三方都读它，杜绝"特征顺序错位"这个毕设经典翻车点。
> 改特征顺序必须重新训练，不要手工编辑 json。

若脚本提示缺少 `diabetes.csv`：脚本内置了 Pima 数据集的字段说明与自动降级逻辑，会把
已知文献分布的可复现随机数据落到这里供训练；正式论文请替换为真实公开数据集（见 §6）。

### 2.2 模块③ 胸片初筛 —— 需数据集 + 显卡

准备数据集目录：

```
ruoyi-ai/
  chest_dataset/
    train/
      normal/    *.png|jpg
      abnormal/  *.png|jpg
    val/
      normal/
      abnormal/
```

然后：

```powershell
python scripts\train_chest.py
```

产出：`models/chest_cnn.pth`。RTX4050 上按脚本默认参数（ResNet18 + 迁移学习）通常十几分钟内可完成一轮。

> 数据集建议用 NIH ChestX-ray14 的子集做二分类（有病灶→abnormal，No Finding→normal）。

### 2.3 模块④ RAG 知识库 —— 需下载 embedding 模型

```powershell
python scripts\build_rag_db.py
```

首次运行会下载 embedding 模型（约 470MB），默认用
`paraphrase-multilingual-MiniLM-L12-v2`（**支持中文**）。

> **修正说明**：原始大纲写的是 `all-MiniLM-L6-v2`，那是**纯英文**模型，
> 检索中文医学知识会严重失效，这里已换成多语言版。
> 想要更强中文效果，可设环境变量：`set EMBED_MODEL=BAAI/bge-small-zh-v1.5`

### 2.4 模块① 病历生成 —— 需 Ollama + Qwen2.5:7B

安装 Ollama（Windows 版，装完自动后台常驻，端口 11434）。

建议先把模型目录指到大盘，避免 C 盘爆满：

```powershell
setx OLLAMA_MODELS "D:\Ollama\Models"
```

重启终端后拉取模型：

```powershell
ollama pull qwen2.5:7b
```

国内加速（ModelScope 镜像）：

```powershell
$env:OLLAMA_HOST="https://modelscope.cn/api/v1/ollama"
ollama pull qwen2.5:7b
```

**没装 Ollama 也不会报错**：模块①自动降级为结构化模板草稿，模块④降级为纯检索模式。

---

## 3. 启动 AI 微服务

```powershell
cd D:\若依医疗\若依医疗\ruoyi-ai
.\.venv\Scripts\Activate.ps1
python main.py
```

看到 `ruoyi-ai 启动于 http://127.0.0.1:8000` 即成功。

### 3.1 第一件事：看健康检查结果

浏览器打开：<http://127.0.0.1:8000/api/ai/health>

返回示例（重点关注每个模块的 `ready` 字段）：

```json
{
  "code": 200,
  "data": {
    "service": "ruoyi-ai",
    "llm":     {"ready": true,  "model": "qwen2.5:7b-instruct-q4_K_M", "reason": ""},
    "risk":    {"ready": true,  "features": ["age","bp_high","bp_low","bmi","glucose"]},
    "chest":   {"ready": false, "hint": "请先运行 scripts/train_chest.py"},
    "rag":     {"ready": true}
  }
}
```

也可以用交互式文档：<http://127.0.0.1:8000/docs>

---

## 4. 后端与前端对接

### 4.1 后端配置

AI 服务地址已在 `AiHttpUtil` 中用 Spring 占位符配置，默认值 `http://127.0.0.1:8000`。

如需修改，在 `ruoyi-admin/src/main/resources/application.yml` 加：

```yaml
ai:
  service:
    url: http://127.0.0.1:8000
```

### 4.2 数据库初始化

按顺序执行（**顺序不能乱**）：

```
ry_20231130.sql → medical_schema.sql → medical_followup.sql → medical_message.sql
→ medical_ai.sql → quartz.sql → medical_menu.sql → medical_ai_menu.sql
```

`medical_ai.sql` 新建 4 张表：

| 表名 | 用途 |
|------|------|
| `medical_ai_record` | AI 生成病历记录（含引擎/耗时/降级标记） |
| `medical_risk_predict` | 风险预测记录（含概率、输入特征、模型指标） |
| `medical_chest_xray` | 胸片检测记录（含置信度、推理设备、耗时） |
| `medical_rag_knowledge` | 导诊知识库（可编辑源头，同步后进 Chroma） |

> **刻意没有新建 `patient_info` 表**：项目已有 `medical_patient`，
> 再建一张患者表会导致双患者源、数据对不上——这是原始大纲里最需要纠正的一处。

### 4.3 前端

菜单已挂在前端**静态路由**上（`src/router/index.js`），无需额外配置。
侧边栏"医疗管理"下新增 5 项：AI病历书写 / 疾病风险预测 / 胸片AI初筛 / 智能导诊问答 / 导诊知识库。

---

## 5. 联调自检清单

按顺序打勾，每项都要实际点一次：

- [ ] ① AI 微服务 `/api/ai/health` 能返回 JSON，四个模块状态符合预期
- [ ] ② 若依后端启动无报错，控制台无 `AiServiceException` 堆栈
- [ ] ③ 前端「疾病风险预测」页：填写 5 项指标 → 出现概率仪表盘 → 历史表多一条记录
- [ ] ④ 前端「导诊知识库」页：新增一条知识 → 点「同步到向量库」→ 状态变"已同步"
- [ ] ⑤ 前端「智能导诊问答」页：问刚新增的知识 → 能看到引用来源
- [ ] ⑥ 前端「AI病历书写」页：输入主诉 → 生成病历 → 可编辑并保存 → 历史表可查
- [ ] ⑦ Ollama 装好后：⑥重复一次，右上角标签应显示"大模型生成"而非"模板降级"
- [ ] ⑧ 胸片模型训练完成后：上传一张胸片 → 返回正常/异常与置信度

### 常见故障速查

| 现象 | 原因 | 处理 |
|------|------|------|
| AI 页面提示"AI 微服务未启动" | 8000 端口没起 | `python main.py` |
| 风险预测报"模型未训练" | 没跑 train_risk.py | 见 §2.1 |
| 上传胸片返回 422 | 缺 python-multipart | `pip install python-multipart` |
| 问答总是"未命中知识库" | 没同步向量库 | 知识库页点同步 |
| 中文问答检索不准 | embedding 是英文模型 | 确认未用 all-MiniLM-L6-v2，见 §2.3 |
| 病历生成很慢 | 7B 模型冷启动 | 首次约 10~30s，之后显存驻留会快很多 |

---

## 6. 毕设演示建议路线

答辩环境不可控，建议**先演示不依赖大模型的部分**，把最稳的放在前面：

1. **疾病风险预测**（纯 sklearn，毫秒级，永远不翻车）—— 说明"传统机器学习落地性强"
2. **导诊问答 + 引用溯源**（依赖已下载的 embedding，稳定）—— 强调"防幻觉设计 + 证据可追溯"
3. **胸片初筛**（依赖显卡，本地离线）—— 强调"医学影像 + CNN"
4. **AI 病历书写**（依赖 Ollama，最后演示）—— 强调"本地大模型 + 隐私保护"

若现场 Ollama 出问题，可当场演示**降级能力**：模板草稿 + 纯检索照常工作，
这反而能成为答辩亮点（系统鲁棒性设计），建议主动讲而不是等老师问。

---

## 7. 原始大纲的改动说明（答辩可能被问，务必清楚）

| 大纲原文 | 问题 | 本项目的处理 |
|----------|------|--------------|
| 新建 `patient_info` 表 | 与已有 `medical_patient` 冲突，双患者源 | 复用 `medical_patient`，只新增 4 张 AI 侧表 |
| RAG 用 `all-MiniLM-L6-v2` | 纯英文模型，检索中文失效 | 改多语言 `paraphrase-multilingual-MiniLM-L12-v2`，可切 bge-small-zh |
| 服务顶层 `joblib.load`/`torch.load` | 模型缺失直接崩溃起不来 | 懒加载 + 优雅降级，缺失时服务照常起并给出训练提示 |
| Ollama `requests.post` 无超时 | 挂了会卡死请求线程 | 统一超时 + 重试 + 降级模板/纯检索 |
| Java `AiHttpUtil` 只有方法签名 | `//2.//3.//4.` 是空注释 | 4 个方法全部实现，含图片 multipart 上传 |
| Java 示例用 fastjson2 | 本项目实际是 fastjson 1.2.83 + Jackson，照抄会编译失败 | 改用 Jackson + RestTemplate（复用已有依赖，不引入新 jar） |
| 特征顺序前后端各写一遍 | 训练/推理错位是毕设经典翻车点 | `risk_features.json` 单一真相源，前端表单动态生成 |
| RAG 知识库写死在脚本里 | 改一条知识就要重建整个向量库 | 加知识库管理页 +「同步到向量库」按钮，支持运行期增量入库 |


---

## 8. 本机已配置实录（2026-09-17 实跑通过）

### 8.1 Ollama

| 项 | 值 |
|---|---|
| 安装位置 | `D:\AI\Ollama\ollama.exe`（已在 PATH） |
| 模型目录 | `D:\AI\OllamaModels` |
| 已拉模型 | **`qwen2.5:7b-instruct-q4_K_M`**（4.7GB，Q4_K_M 量化） |
| 服务端口 | 11434（**默认不常驻**，需手动起 serve） |

启动（任选一种）：

```
set OLLAMA_MODELS=D:\AI\OllamaModels
D:\AI\Ollama\ollama.exe serve
```

验证：`curl http://127.0.0.1:11434/api/tags`

> ⚠️ `ruoyi-ai/config.py` 的 `LLM_MODEL` 默认值**必须是完整 tag** `qwen2.5:7b-instruct-q4_K_M`。
> 早期写成 `qwen2.5:7b` 时，健康检查按基础名 `qwen2.5` 能匹配通过（假阳性），
> 但真正生成时按全名请求会 404。已修正。

### 8.2 真实推理实测

```
POST http://127.0.0.1:8000/api/ai/generate_record
{"patient_name":"张三","age":45,"gender":"男",
 "chief_complaint":"发热咳嗽3天，伴咽痛",
 "allergy_history":"青霉素过敏","past_history":"高血压5年"}
```

返回：`engine=ollama`、`model=qwen2.5:7b-instruct-q4_K_M`、`degraded=false`、
`elapsed_ms≈40000`，输出完整的「现病史 / 体格检查 / 初步诊断 / 医嘱建议」四段结构。

首次请求含模型加载约 40–50 秒（7B 量化版载入），之后同会话内秒级返回。

### 8.3 健康检查（/api/ai/health）当前状态

```json
{
  "llm":   {"ready": true,  "model": "qwen2.5:7b-instruct-q4_K_M"},
  "risk":  {"ready": true,  "metrics": {"accuracy":0.7078,"auc":0.813}},
  "chest": {"ready": false, "hint": "请先运行 scripts/train_chest.py"},
  "rag":   {"ready": false, "doc_count": 0, "hint": "知识库已创建但为空"}
}
```

> ⚠️ **健康检查不会触发 embedding 模型下载**。早期实现在 `rag.status()` 里加载
> `SentenceTransformer`，首次调用会去 HuggingFace 拉几百 MB，
> 国内网络下 `WinError 10060` 超时 / 证书校验失败，把 `/api/ai/health` 直接卡死。
> 现已改为只连 Chroma 取 `collection.count()`，模型加载推迟到真正检索时。

### 8.4 RAG embedding 模型下载（国内网络）

`ruoyi-ai/config.py` 顶部已内置国内镜像：

```python
os.environ.setdefault("HF_ENDPOINT", "https://hf-mirror.com")
```

注意：**必须先 `import config` 再 import `sentence_transformers`**，否则这句来不及生效。

预下载可单独执行（约 470MB，视网络 10–30 分钟）：

```
cd D:\若依医疗\若依医疗\ruoyi-ai
set HF_ENDPOINT=https://hf-mirror.com
python -c "import config; from sentence_transformers import SentenceTransformer; SentenceTransformer(config.EMBED_MODEL)"
```

下载完成后再跑 `python scripts/build_rag_db.py` 构建知识库，RAG 才会 `ready=true`。
未下载也不影响其余三个模块：模块①走 Ollama、模块②风险模型已训练、模块④自动降级。
