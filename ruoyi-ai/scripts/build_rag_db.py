# -*- coding: utf-8 -*-
"""
模块④ 知识库构建脚本：把医学文档切片、向量化并写入 Chroma

用法：
    python scripts/build_rag_db.py                     # 使用内置示例知识 + data/rag_docs.txt
    python scripts/build_rag_db.py --reset             # 清空重建

相比大纲的改进：
1. 大纲直接把整段文本入库，长文档检索粒度太粗。这里做了**按句切分**（默认 220 字一片，
   句边界优先），召回的是"相关的一小段"而不是一整页，回答更精准；
2. embedding 用多语言模型，对中文有效（原方案的英文模型在中文语料上几乎失效）；
3. 支持从 data/rag_docs.txt 追加你自己的资料（每行一段），便于论文里说明知识来源。
"""
import argparse
import sys
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(BASE_DIR))

from config import CHROMA_COLLECTION, CHROMA_DIR, EMBED_MODEL  # noqa: E402
from rag import split_document  # noqa: E402  # 与运行期 add_documents 共用同一切片策略

# 内置示例知识（演示够用；毕设建议换成权威来源，如诊疗指南、教材原文）
DEFAULT_DOCS = [
    "糖尿病典型症状为多饮、多食、多尿伴体重下降，即“三多一少”。"
    "空腹血糖≥7.0mmol/L，或餐后2小时血糖≥11.1mmol/L，或糖化血红蛋白≥6.5%可考虑糖尿病诊断，"
    "需在非同日复查确认。确诊后应长期监测血糖并控制饮食、规律运动。",

    "高血压诊断标准：在未服用降压药的情况下，非同日三次测量诊室血压，"
    "收缩压≥140mmHg和（或）舒张压≥90mmHg可诊断为高血压。"
    "家庭自测血压≥135/85mmHg 同样具有参考意义。",

    "血压分级：1级高血压为收缩压140-159或舒张压90-99mmHg；"
    "2级为收缩压160-179或舒张压100-109mmHg；3级为收缩压≥180或舒张压≥110mmHg。"
    "3级高血压属于高危，应立即启动药物治疗并排查靶器官损害。",

    "普通感冒多为病毒感染，常见咽痛、鼻塞、流涕、低热、咳嗽，通常5至7天自愈。"
    "以对症支持治疗为主：多休息、多饮水。体温超过38.5℃可用对乙酰氨基酚退热。"
    "若高热超过3天不退或出现明显气促，应就医排除流感或细菌感染。",

    "手足口病多见于5岁以下儿童，表现为手、足、口腔出现斑丘疹或疱疹，可伴发热。"
    "多数患儿一周左右自愈。若出现持续高热、精神差、肢体抖动、呼吸急促，"
    "提示可能发展为重症，须立即就医。",

    "急性胃肠炎常见表现为恶心、呕吐、腹痛、腹泻，多由不洁饮食或病毒感染引起。"
    "轻症可口服补液盐防止脱水，进食清淡易消化食物。"
    "出现频繁呕吐无法进水、便中带血、高热或明显脱水（尿量少、口干、乏力）时应及时就诊。",

    "咳嗽持续超过8周称为慢性咳嗽，常见病因包括上气道咳嗽综合征、咳嗽变异性哮喘、"
    "胃食管反流性咳嗽、嗜酸粒细胞性支气管炎等。建议行胸片与肺功能检查明确病因。",

    "体检发现空腹血糖受损（空腹血糖6.1至6.9mmol/L）或糖耐量减低属于糖尿病前期，"
    "尚不足以诊断糖尿病，但进展风险较高。通过减轻体重、每周至少150分钟中等强度运动、"
    "控制碳水化合物摄入，可显著降低转为糖尿病的概率。",

    "血脂异常判断：总胆固醇≥5.2mmol/L、低密度脂蛋白胆固醇≥3.4mmol/L、"
    "甘油三酯≥1.7mmol/L、高密度脂蛋白胆固醇<1.0mmol/L，满足任一项即为血脂异常。"
    "低密度脂蛋白是动脉粥样硬化性心血管疾病的关键干预靶点。",

    "发热伴胸痛、呼吸困难时，需警惕肺炎、胸膜炎、心肌梗死及肺栓塞。"
    "尤其突发单侧胸痛伴呼吸困难、咯血者，应立即就医，不可自行服药观察。",
]


def main():
    parser = argparse.ArgumentParser(description="构建 RAG 向量知识库")
    parser.add_argument("--reset", action="store_true", help="清空已有知识库后重建")
    args = parser.parse_args()

    try:
        import chromadb
        from sentence_transformers import SentenceTransformer
    except ImportError as exc:
        print(f"缺少依赖：{exc}\n请先执行：pip install -r requirements.txt")
        return 1

    docs = list(DEFAULT_DOCS)

    # 追加用户自备资料
    extra_path = BASE_DIR / "data" / "rag_docs.txt"
    if extra_path.exists():
        lines = [
            ln.strip()
            for ln in extra_path.read_text(encoding="utf-8").splitlines()
            if ln.strip() and not ln.strip().startswith("#")
        ]
        if lines:
            print(f"[数据] 从 {extra_path.name} 追加 {len(lines)} 条")
            docs.extend(lines)

    # 切片（与运行期追加知识一致的策略：超 200 字按句切片，重叠 20 字）
    chunks = []
    for doc in docs:
        chunks.extend(split_document(doc, chunk_size=200, overlap=20))
    print(f"[数据] 原始 {len(docs)} 条 -> 切片后 {len(chunks)} 个知识片段")

    print(f"[模型] 加载 embedding：{EMBED_MODEL}（首次会自动下载，约 470MB）")
    embedder = SentenceTransformer(EMBED_MODEL)

    client = chromadb.PersistentClient(path=CHROMA_DIR)
    if args.reset:
        try:
            client.delete_collection(CHROMA_COLLECTION)
            print(f"[重建] 已删除旧集合 {CHROMA_COLLECTION}")
        except Exception:  # noqa: BLE001
            pass
    collection = client.get_or_create_collection(
        name=CHROMA_COLLECTION, metadata={"hnsw:space": "cosine"}
    )

    print("[向量] 正在编码并写入……")
    vectors = embedder.encode(chunks).tolist()
    ids = [f"kb_{i:05d}" for i in range(len(chunks))]
    # 分批写入，避免一次性提交过大
    step = 64
    for i in range(0, len(chunks), step):
        collection.add(
            ids=ids[i : i + step],
            embeddings=vectors[i : i + step],
            documents=chunks[i : i + step],
        )

    print(f"\n[完成] 知识库就绪：{CHROMA_DIR}")
    print(f"[完成] 集合 {CHROMA_COLLECTION} 现有 {collection.count()} 个片段")
    print("提示：请重启 AI 微服务（python main.py）后使用 RAG 导诊。")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
