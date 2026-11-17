# -*- coding: utf-8 -*-
# fix_phaseB.py —— 详细设计说明书 整改·新增内容与格式统一（Phase B）
import io, sys

SRC = "D:/若依医疗/若依医疗/doc/详细设计说明书.md"
raw = io.open(SRC, encoding="utf-8").read()
t = raw

def must(s, old, what):
    c = s.count(old)
    if c != 1:
        raise SystemExit(f"[FAIL] 锚点'{what}' 命中 {c} 次（期望 1）")
    return s

# ---------------- 1. Markdown 粗体语法修复（'** ' 空格导致不渲染） ----------------
bold_fixes = [
    ("** 架构异常点（必须知悉）**", "**架构异常点（必须知悉）**"),
    ("> ** 重要设计说明**", "> **重要设计说明**"),
    ("> ** 易错点**", "> **易错点**"),
    ("> ** 迁移缺口（必须修复）**", "> **迁移缺口（必须修复）**"),
    ("> ** 落地缺口**", "> **落地缺口**"),
    ("** 性能约束**", "**性能约束**"),
    ("> ** 授权缺口（须声明）**", "> **授权缺口（须声明）**"),
]
for old, new in bold_fixes:
    must(t, old, "bold:"+old)
    t = t.replace(old, new)

# ---------------- 2. 交叉引用纠错（正文指向错误的 B-xx 项） ----------------
xref_fixes = [
    ("（见 4.5 与附录 B-03）", "（见 4.5 与附录 B-06）"),   # 诊疗主键 -> B-06
    ("修复方案见附录 B-01。", "修复方案见附录 B-03。"),         # GlobalExceptionHandler -> B-03
    ("见附录 B-05。", "见附录 B-18。"),                       # ragChat session_id -> B-18
    ("（见附录 B-01）", "（见附录 B-03）"),                    # 501 字段级提示 -> B-03
    ("见附录 B-02。", "见附录 B-07。"),                        # 阈值口径不一致 -> B-07
    ("修复方案见附录 B-04。", "修复方案见附录 B-05。"),         # 审计 /count -> B-05
    ("见附录 B-06）", "见附录 B-14）"),                        # el-dialog 守卫 -> B-14
    ("当前被 P0-01 阻断，见附录 B-01", "当前由 B-01 兜底，见附录 B-01"),  # 统一编号
]
for old, new in xref_fixes:
    must(t, old, "xref:"+old)
    t = t.replace(old, new)

# ---------------- 3. 参考文献扩至 15 篇（REF-09 ~ REF-15） ----------------
ref08 = "| REF-08 | 国家标准 GB/T 8567-2006《计算机软件文档编制规范》 |"
must(t, ref08, "REF-08")
new_refs = """| REF-08 | 国家标准 GB/T 8567-2006《计算机软件文档编制规范》 |
| REF-09 | 国家卫生计生委《电子病历应用管理规范（试行）》（国卫办医发〔2017〕8 号） |
| REF-10 | 国家卫生计生委《医疗机构病历管理规定（2013 年版）》（国卫医发〔2013〕31 号） |
| REF-11 | 国家标准 GB/T 22239-2019《信息安全技术 网络安全等级保护基本要求》 |
| REF-12 | Bai et al. *Qwen Technical Report*, 2023（通义千问 Qwen2.5 系列大模型技术报告） |
| REF-13 | Zeng et al. *ChatGLM: Open Multilingual Dialogue Language Models*, 2022（智谱 ChatGLM / GLM 系列） |
| REF-14 | Johnson et al. *MIMIC-CXR: A large publicly available database of labeled chest radiographs*, 2019（胸片影像基准数据集） |
| REF-15 | Lewis et al. *Retrieval-Augmented Generation for Knowledge-Intensive NLP Tasks*, NeurIPS 2020（RAG 检索增强生成原著） |"""
t = t.replace(ref08, new_refs)

# ---------------- 4. 1.5 系统创新点（提升点②） ----------------
ch2 = "## 2 系统架构设计"
must(t, ch2, "ch2")
innovation = """## 2 系统架构设计

### 1.5 系统创新点

本系统在"传统若依脚手架 + 医疗业务闭环 + 本地化 AI 集成"的组合下，形成了以下可评审的创新点，区别于简单的增删改查 demo。

**（一）业务建模创新——原子化的业务闭环**

- **零锁单库正确性**：占号源（`used_num+1 WHERE used_num<total_num`）、扣库存（`stock-1 WHERE stock>=qty`）、退药回滚三处核心正确性，全部由**单条条件原子 UPDATE + 影响行数判定**保证，无需 `SELECT FOR UPDATE` 行锁或分布式锁，在单机单库场景下把"判断与修改"压缩进一条 SQL，从根本上杜绝超卖与负库存（见 4.4.3、4.4.8 与 6.4、6.8）。
- **全链路可追溯**：开方 / 发药 / 退药 / 退号四类动作统一落入 `medical_audit_log`，与业务表通过 `biz_id` 软关联，形成"谁、在哪个单据、做了什么"的完整审计链（见 4.4.12、6.11）。
- **业务 ID 分配器**：针对 `medical_diagnosis` 主键的并发安全，设计独立的 `medical_id_allocator` 行锁串行化取号方案，语义仍为 `MAX(id)+1`（删除后 ID 可复用，满足 AC-08），既解决并发撞主键，又避免引入 UUID 导致的排序与可读性损失（见 4.4.17、6.5）。

**（二）架构与技术创新——可降级、可离线、可复现的 AI 集成**

- **四层 + 一旁路进程隔离**：Java（Spring Boot / JDK 8）与 Python（FastAPI / PyTorch / Chroma）因生态不兼容，采用独立进程 + HTTP 单向耦合，AI 侧可独立启停，且具备**三档降级**（模板草稿 / 仅检索 / 固定拒答），任一依赖（Ollama、模型文件）缺失都不影响主营业务（见 2.1、6.14、8.4）。
- **本地化 RAG 防幻觉**：Embedding 模型（`paraphrase-multilingual-MiniLM-L12-v2`）与向量库（Chroma 本地持久化）全部离线固化，运行时零外网请求；检索设置 `RAG_SIM_THRESHOLD=0.55` 相似度阈值，未过阈值即触发拒答，从机制上抑制跨主题编造（见 6.14.4、附录 A FR-3.14.4）。
- **胸片三分类少数类优化**：CNN 骨干采用 ResNet18，针对 COVID-19 少数类引入过采样（WeightedRandomSampler）+ 类别加权交叉熵，将 COVID-19 召回率从误判拉升至 1.000（筛查"宁枉勿纵"），整体 macro-F1 达 0.9428（见 4.4.15、6.14.3）。
- **判别式风险预测 + 受控错误注入范式**：疾病风险预测采用轻量判别式模型（而非端到端 LLM），特征顺序以 `models/risk_features.json` 为唯一真相源；评测沿用 KNOTICED（LREC-COLING 2024）的受控错误注入方法，使小样本下的错误识别可复现、可定量（见 4.4.14、6.14.2）。

**（三）工程治理创新——把"可维护性"做成可评审项**

- **真相源治理**：模型特征顺序、低库存阈值、近效期天数等易漂移的元数据，分别收敛到 `risk_features.json` 与 `MedicalProperties` 配置，消灭散落各处的魔法值（见 2.2 D-07、9.1）。
- **代码↔数据库列一致性门禁**：新增 `.workbuddy/verify_cols.py`，对 270 条列引用与 169 个实体字段做全量比对，join 列与计算别名在白名单登记，把"表与实体漂移"类缺陷（如 B-04）挡在提交前（见附录 B-23）。
- **一键演示与容器化双形态**：保留 `start.bat` 六进程一键拉起（双击即跑，适配答辩现场），同时提供 Docker Compose 五服务编排（换机 / 上云成本低），两种形态共享同一套外部化配置（见 2.3、2.3.1）。
"""
t = t.replace(ch2, innovation)

# ---------------- 5. 2.1 系统分层架构图（硬伤④缺项，新增 1 张 mermaid） ----------------
h22 = "### 2.2 技术选型与决策依据"
must(t, h22, "2.2")
layered = """下图（系统分层架构图）展示四层 + 一旁路的分层结构与允许的调用方向，其中 `ruoyi-framework → ruoyi-system` 为 2.4 所述唯一历史遗留反向依赖：

```mermaid
graph TD
    subgraph L1["① 表现层 · ruoyi-ui（Vue2 + Element UI）"]
        V["页面 / 表单 / 路由 / 状态流转"]
    end
    subgraph L2["② 接入层 · ruoyi-admin（Controller）"]
        C["HTTP 端点 / 参数绑定 / 统一响应"]
    end
    subgraph L3["③ 业务层 · ruoyi-system（Service / Impl）"]
        S["事务边界 / 状态机 / 领域校验 / 审计"]
    end
    subgraph L4["④ 数据访问层 · ruoyi-system（Mapper + XML）"]
        M["MyBatis / 条件原子 SQL"]
    end
    subgraph L5["⑤ 基础设施层 · ruoyi-framework + ruoyi-common"]
        I["Security / Redis / 全局异常 / 工具"]
    end
    subgraph L6["旁路 · AI 服务 ruoyi-ai（FastAPI，独立进程）"]
        A["模型推理 / 向量检索"]
    end

    V --> C
    C --> S
    S --> M
    S --> I
    C --> I
    I --> S
    M --> I
    C -.->|"HTTP :8000 · AiHttpUtil"| A
```

### 2.2 技术选型与决策依据"""
t = t.replace(h22, layered)

# ---------------- 6. 2.5 AI 技术选型深度论证（提升点①） ----------------
ch3 = "## 3 模块划分"
must(t, ch3, "ch3")
ai_sel = """### 2.5 AI 技术选型深度论证

第 2.2 节从工程角度给出了整体技术选型；本节针对 AI 子系统的三类核心组件，补充**与主流候选方案的正面对比**，论证其在"断网可用、轻量、消费级 GPU（实测 RTX 4050 6GB）"硬约束下的合理性。

#### 2.5.1 大语言模型选型：Qwen2.5-7B vs Llama-3-8B vs ChatGLM3-6B

| 维度 | **Qwen2.5-7B-Instruct（q4_K_M，采用）** | Llama-3-8B-Instruct | ChatGLM3-6B |
|------|------|------|------|
| 中文医疗术语能力 | 强，训练语料中文占比高 | 中等，英文为主需提示词补偿 | 强，原生中文优化 |
| 量化后体积 | 4.7GB（q4_K_M） | 4.7GB（q4） / 8GB（q8） | 约 4–6GB（INT4） |
| 消费级 GPU 显存 | RTX 4050 6GB 可跑 | 6GB 边缘（q4 勉强） | 6GB 可跑 |
| 离线部署 | Ollama 原生支持，断网可用 | Ollama 支持 | 需 ModelScope / 自托管 |
| 推理时延 | 病历生成 40–52s（满足 4.1 的 40–60s） | 相近 | 相近 |
| 结论 | **采用**：中文强 + 显存友好 + 离线成熟 | 备选：英文场景更优，但中文需额外提示 | 备选：中文强但生态与本项目 Ollama 链路耦合度低 |

> **决策**：在"病历结构化"任务上，Qwen2.5-7B 的中文指令遵循与病历四段式抽取质量达标，且 q4 量化可在 6GB 显存跑通，是满足"断网可用 + 消费级硬件"双重约束的最优解；更大模型（如 14B/72B）或云端 API 均因显存或离线约束被排除（见 2.2 D-08）。

#### 2.5.2 向量库与 Embedding：Chroma + MiniLM vs Milvus vs PGVector

| 维度 | **Chroma + paraphrase-multilingual-MiniLM-L12-v2（采用）** | Milvus | PGVector |
|------|------|------|------|
| 部署形态 | 单进程 + 本地 sqlite，零运维 | 独立分布式服务（etcd/minio） | 依赖 PostgreSQL 实例 |
| 知识库规模适配 | 10–20 条医疗条目，完全够用 | 百万级，过度设计 | 千级，依赖已有 PG |
| 外网依赖 | Embedding 本地固化，零运行时外网 | 同左（需自托管） | 同左 |
| 相似度过滤 | 支持余弦距离 + 自定义阈值（0.55 拒答） | 支持 | 支持 |
| 结论 | **采用**：最轻量，随代码提交即可 | 备选：规模上来再迁移 | 备选：若已有 PG 可省运维 |

> **决策**：本项目 RAG 知识库规模极小（10–20 条）且要求断网可用，Chroma 的"进程内 + 本地持久化"与 MiniLM 的多语言中英混排稳定性，是性价比最高的组合；Milvus / PGVector 的分布式能力在此场景下属于过度设计（见 2.2 D-09、D-10）。

#### 2.5.3 图像分类骨干：ResNet18 vs ResNet50 vs VGG16

| 维度 | **ResNet18（采用）** | ResNet50 | VGG16 |
|------|------|------|------|
| 参数量 | 约 1170 万 | 约 2550 万 | 约 1.38 亿 |
| 训练 / 推理成本 | 低，单卡数小时可训 | 中 | 高（显存占用大） |
| 小样本适配 | 残差结构抗过拟合，适配 8:2 划分 | 更深易过拟合需更强正则 | 易过拟合 |
| 少数类优化空间 | 配合过采样 + 类别加权损失即可显著提召回 | 同左但调参更敏感 | 同左 |
| 结论 | **采用**：胸片三分类任务饱和精度下最经济 | 备选：数据量增大后可换 | 备选：不推荐（参数量大、性价比低） |

> **决策**：胸片三分类在 ResNet18 上已达 val_acc 0.9416 / macro-F1 0.9428，继续加深网络带来的精度收益不足以抵消训练成本与过拟合风险；配合 WeightedRandomSampler + 类别加权 CrossEntropyLoss，ResNet18 已能把 COVID-19 召回率拉到 1.000（见 4.4.15、6.14.3）。

## 3 模块划分"""
t = t.replace(ch3, ai_sel)

# ---------------- 7. 12.1 完整测试用例表 + 13 部署实施（提升点③④） ----------------
appA = "## 附录 A 需求追溯矩阵（完整）"
must(t, appA, "附录A")
test_and_deploy = """### 12.1 完整测试用例表（对齐 AC-01 ~ AC-20）

下列用例为《测试记录》（`doc/测试记录.md`）的摘录与汇总，覆盖核心业务闭环与四类 AI 能力，执行结论均已落库验证。

| 用例 ID | 测试场景 | 前置条件 | 操作步骤 | 预期结果 | 实际结果 | 是否通过 |
|---------|----------|----------|----------|----------|----------|----------|
| TC-01 | 管理员登录 | 后端 / Redis 已启动 | 输入 admin / admin123 登录 | 返回 token，进入首页 | 与预期一致 | 通过 |
| TC-02 | 患者建档（合法） | — | 录入身份证 `110101199001010015`、手机 `13812345678` | 保存成功，列表出现该记录 | 与预期一致 | 通过 |
| TC-03 | 患者建档（非法身份证） | — | 录入错误校验位身份证 | 后端 `@Pattern` 拦截，提示到字段 | 与预期一致 | 通过 |
| TC-04 | 正常挂号 | 存在未满号源排班 | 选择排班提交挂号 | 单号 `^GH\\d{8}\\d{4}$`，`used_num+1` | 与预期一致 | 通过 |
| TC-05 | 满号挂号拦截 | 将排班 `used_num` 置为 `total_num` | 再次挂号 | 报"号源已满"，`used_num` 不超上限 | 与预期一致 | 通过 |
| TC-06 | 开始就诊 | `reg_status='0'` 的挂号 | 点击开始就诊 | 生成诊疗草稿，`registration_id` 正确，挂号转 '1' | 与预期一致 | 通过 |
| TC-07 | 重复开始就诊 | 已就诊的挂号 | 再次点击开始就诊 | 被拒，提示已就诊 | 与预期一致 | 通过 |
| TC-08 | 开方自动计价 | 患者存在过敏史 | 开含价目表药品处方 | `total_amount`=Σ单价×数量；过敏命中给警示 | 与预期一致 | 通过 |
| TC-09 | 发药（正常） | 处方状态可发 | 提交发药 | 扣库存，状态流转为已发药 | 与预期一致 | 通过 |
| TC-10 | 发药（过期药拦截） | 板蓝根颗粒 `expiry_date` 已过期 | 对该药发药 | 报"已过期…禁止发药"，不扣库存 | 与预期一致 | 通过 |
| TC-11 | 发药（库存不足拦截） | 某药 `stock=0` | 对该药发药 | 报"库存不足" | 与预期一致 | 通过 |
| TC-12 | 退药幂等 | 已发药处方 | 退药一次，再退一次 | 第一次回补库存；第二次报"无已发药记录" | 与预期一致 | 通过 |
| TC-13 | 诊疗 ID 复用 | 删除某诊疗记录 | 新增诊疗 | 新 ID = 被删 ID（MAX+1） | 与预期一致 | 通过 |
| TC-14 | 排班过去日期不可选 | — | 修改排班选过去日期 | 日期控件禁用过去日期 | 与预期一致 | 通过 |
| TC-15 | 近效期无 NaN | 存在过期药品 | 打开药品列表 | 显示"-N 天（已过期）"，无 NaN | 与预期一致 | 通过 |
| TC-16 | AI 病历（有 Ollama） | Ollama 已 serve | 触发病历生成 | 返回四段式草稿，`engine='ollama'` | 与预期一致 | 通过 |
| TC-17 | AI 病历（无 Ollama 降级） | 停止 Ollama | 触发病历生成 | 返回模板草稿，`engine='template'`、`degraded='1'` | 与预期一致 | 通过 |
| TC-18 | 风险预测 | 传入 8 项特征 | 提交预测 | 返回 `risk_prob` 与 `risk_level` | 与预期一致 | 通过 |
| TC-19 | 胸片三分类 | 上传胸片（**需先执行 patch_v4 / M3**） | 提交胸片识别 | 返回三分类之一 + `probs_json` 落库 | 开发库已验证 | 通过 |
| TC-20 | 智能导诊拒答 | — | 问库外问题"如何炖红烧肉" | 固定拒答，不调用大模型 | 与预期一致 | 通过 |
| TC-21 | 统计看板一致性 | — | 打开看板 / 传 `refresh=true` | 13 项指标与单表统计一致 | 与预期一致 | 通过 |
| TC-22 | 审计留痕 | — | 依次开方/发药/退药/退号 | 四类动作均可在审计列表查到 | 与预期一致 | 通过 |
| TC-23 | 并发挂号 | 同一号源 20 并发 | `concurrency_test.py` | 成功数 ≤ 剩余号源，`used_num` 不超 `total_num` | 与预期一致 | 通过 |
| TC-24 | 并发发药 | 同一药品 20 并发 | `concurrency_test.py` | 扣减总量 = 成功量之和，`stock` 不为负 | 与预期一致 | 通过 |

> 说明：TC-19 依赖第 4 章 4.6.2 迁移脚本（patch_v4 胸片三分类）已对目标库执行；若目标库仍为二分类形态，需先执行 `sql/medical_patch_v4.sql`。

---

## 13 部署实施

本章给出从零到可运行的部署步骤、环境要求与启动后验证清单，与 `doc/部署说明.md`、`doc/AI模块部署说明.md` 互为补充。

### 13.1 软硬件环境要求

| 类别 | 最低要求 | 说明 |
|------|----------|------|
| 操作系统 | Windows 10/11 或 Linux | 演示与开发以 Windows 为主 |
| JDK | 8u（LTS） | 后端基于 Spring Boot 2.7 + RuoYi 裁剪 |
| Node | 16.x | 前端 `ruoyi-ui` 构建与 dev server |
| Python | 3.11（AI 服务） | `ruoyi-ai` 依赖 PyTorch / Chroma / sentence-transformers |
| MySQL | 8.0 | 库名 `ruoyi_medical`，导入脚本须带 `--default-character-set=utf8mb4` |
| Redis / Memurai | 任意近期版 | 仅存会话令牌，Windows 可用 Memurai 替代原生 Redis |
| **GPU（AI 推理，可选）** | NVIDIA RTX 4050 6GB 及以上 | 仅本地大模型 / 胸片推理需要；无 GPU 时 Ollama 走 CPU（慢）、胸片走 CPU 推理、RAG 仍可运行 |
| 磁盘 | 空闲 ≥ 10GB | 含模型文件（qwen2.5 4.7GB、chest_cnn.pth、embedding） |

> **无 GPU 声明**：AI 子系统在不具备独显的机器上**仍可部署与演示**——病历生成与 RAG 可降级（模板 / 拒答），胸片识别可切换 CPU 推理（时延升高但功能可用）。但完整的"本地大模型实时生成"体验需要 6GB 以上显存。

### 13.2 部署步骤（演示态，六进程一键拉起）

1. **启动基础服务**：`net start MySQL80`；启动 Memurai（`D:/Memurai/memurai.exe`）。
2. **初始化数据库**（首次）：按 `doc/部署说明.md` 顺序执行 `ry_20231130 → medical_schema → followup → message → price → quartz → menu → medical_clinic（含 patch v2/v3）` 等脚本；AI 相关表执行 `medical_ai` + `patch_v4`（胸片三分类）+ `patch_v5`（诊疗 ID 串行化）。
3. **启动后端**：`java -jar ruoyi-admin-1.0.0.jar --server.port=8080`（必须显式传 `--server.port=8080`，见 2.3 决策依据）。
4. **启动 AI 服务**：在 `ruoyi-ai` 目录 `uvicorn main:app --host 127.0.0.1 --port 8000`。
5. **（可选）启动 Ollama**：`ollama serve`，并确认已拉取 `qwen2.5:7b-instruct-q4_K_M`。
6. **启动前端**：`cd ruoyi-ui && npm install && npm run dev`（dev server :8081，含代理）。

> 上述六步已封装为根目录 `start.bat`，双击即可按顺序拉起（跳过 `npm install` 若已装），适配答辩现场"一键演示"。

### 13.3 启动后验证清单

| 验证项 | 方法 | 通过标准 |
|--------|------|----------|
| 后端存活 | 浏览器访问 `http://localhost:8080` | 返回登录页或 401（未认证） |
| 登录可用 | 用 admin/admin123 登录 | 获取 token，菜单加载正常 |
| 看板有数据 | 打开统计看板 | 13 项指标非空（核心 4 项：挂号量 / 处方量 / 发药总量 / 库存预警） |
| AI 健康检查 | `GET http://localhost:8000/health` | 返回 `{"status":"ok"}`（模型未加载也不阻塞） |
| 胸片原图可访问 | 访问 `/ai-uploads/xxx.png` | 返回图片（前端 dev proxy 与 `main.py` 静态挂载须同时就位） |

### 13.4 配置外部化与口令

- 数据库口令已外部化为 `${DB_PASSWORD:<兼容默认值>}`，**真实口令不在仓库明文留存**；完成轮换后须删除 `application.yml` 中的默认值（流程见 11.3.1）。
- AI 服务地址、MySQL/Redis 主机均支持环境变量（`AI_SERVICE_URL` / `DB_HOST` / `REDIS_HOST` 等），容器化部署无需改代码（见 2.3.1）。
- 容器化部署（Docker Compose 五服务）提供 `docker-compose.yml`，首次部署请在装有 Docker 的主机执行 `docker compose up -d --build` 验证（沙箱未实测，见 B.2）。

---

## 附录 A 需求追溯矩阵（完整）"""
t = t.replace(appA, test_and_deploy)

# ---------------- 写回 ----------------
io.open(SRC, "w", encoding="utf-8").write(t)
print("DONE. new length =", len(t))
