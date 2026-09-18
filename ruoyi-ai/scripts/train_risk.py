# -*- coding: utf-8 -*-
"""
模块② 训练脚本：常见病（糖尿病）风险预测

用法：
    python scripts/train_risk.py                     # 自动准备数据并训练
    python scripts/train_risk.py --csv 你的数据.csv   # 使用自备数据

相比大纲的关键修正：
1. 大纲写输入特征含"收缩压"，但公开常用的 Pima 数据集只有舒张压 —— 两者对不上。
   本脚本以**数据实际列**为准训练，并把特征顺序写入 models/risk_features.json；
   推理端读取同一个 json 做校验，杜绝"训练/推理特征顺序错位"。
2. Pima 数据集用 0 表示缺失（Glucose/BloodPressure/BMI/Insulin 的 0 是哨兵值而非真实值），
   直接喂给模型会把均值带偏。这里先把 0 置 NaN 再用中位数填充。
3. 用 Pipeline(StandardScaler + LogisticRegression)，避免量纲差异导致收敛不良。
4. 输出准确率/AUC/混淆矩阵，直接可搬进论文实验结果章节。
"""
import argparse
import json
import sys
import urllib.request
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(BASE_DIR))

# Pima Indians Diabetes（UCI 公开数据集，常用于教学与毕设）
PIMA_URL = (
    "https://raw.githubusercontent.com/plotly/datasets/master/diabetes.csv"
)
PIMA_FALLBACK_URL = (
    "https://raw.githubusercontent.com/npradaschnor/Pima-Indians-Diabetes-Dataset/"
    "master/diabetes.csv"
)


def ensure_pima(target: Path) -> Path:
    """下载 Pima 数据集到本地（已存在则跳过）"""
    target.parent.mkdir(parents=True, exist_ok=True)
    if target.exists() and target.stat().st_size > 1000:
        print(f"[数据] 已存在 {target}，跳过下载")
        return target

    for url in (PIMA_URL, PIMA_FALLBACK_URL):
        try:
            print(f"[数据] 正在下载 {url}")
            urllib.request.urlretrieve(url, target)
            print(f"[数据] 已保存到 {target}")
            return target
        except Exception as exc:  # noqa: BLE001
            print(f"[数据] 下载失败：{exc}，尝试下一个源")
    raise RuntimeError(
        "自动下载失败。请手动下载 diabetes.csv 放到 "
        f"{target}，或在 Kaggle 搜索 'Pima Indians Diabetes Database'。"
    )


def load_frame(csv_path: Path):
    import pandas as pd

    df = pd.read_csv(csv_path)
    df.columns = [c.strip() for c in df.columns]
    return df


def main():
    parser = argparse.ArgumentParser(description="训练糖尿病风险预测模型")
    parser.add_argument("--csv", default="", help="自备 CSV 路径（需含 label 列）")
    parser.add_argument("--target", default="Outcome", help="标签列名，默认 Outcome")
    args = parser.parse_args()

    try:
        import joblib
        import pandas as pd
        from sklearn.linear_model import LogisticRegression
        from sklearn.metrics import (
            accuracy_score,
            confusion_matrix,
            f1_score,
            precision_score,
            recall_score,
            roc_auc_score,
        )
        from sklearn.model_selection import train_test_split
        from sklearn.pipeline import Pipeline
        from sklearn.preprocessing import StandardScaler
    except ImportError as exc:
        print(f"缺少依赖：{exc}\n请先执行：pip install -r requirements.txt")
        return 1

    if args.csv:
        csv_path = Path(args.csv)
        if not csv_path.exists():
            print(f"找不到数据文件：{csv_path}")
            return 1
    else:
        # 默认读取项目根目录下 data/AI 疾病风险预测模块/diabetes.csv
        # （该文件已随项目数据集一并准备；缺失时自动从公开源下载）
        default_csv = BASE_DIR.parent / "data" / "AI 疾病风险预测模块" / "diabetes.csv"
        csv_path = ensure_pima(default_csv)

    df = load_frame(csv_path)
    print(f"[数据] 载入 {len(df)} 条记录，列：{list(df.columns)}")

    target_col = args.target
    if target_col not in df.columns:
        # 常见别名兜底
        for alias in ("Outcome", "label", "Label", "target", "diabetes"):
            if alias in df.columns:
                target_col = alias
                break
        else:
            print(f"数据集中找不到标签列（尝试过 {target_col} 及常见别名）")
            return 1

    # ---- Pima 的 0 是缺失哨兵值，必须处理 ----
    sentinel_cols = [
        c
        for c in ("Glucose", "BloodPressure", "SkinThickness", "Insulin", "BMI")
        if c in df.columns
    ]
    if sentinel_cols:
        df[sentinel_cols] = df[sentinel_cols].replace(0, pd.NA)
        df[sentinel_cols] = df[sentinel_cols].fillna(df[sentinel_cols].median())
        print(f"[清洗] 已将 {sentinel_cols} 中的 0 视为缺失并用中位数填充")

    feature_cols = [c for c in df.columns if c != target_col]
    X = df[feature_cols]
    y = df[target_col]

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )

    pipeline = Pipeline(
        [
            ("scaler", StandardScaler()),
            ("clf", LogisticRegression(max_iter=2000, random_state=42)),
        ]
    )
    pipeline.fit(X_train, y_train)

    y_pred = pipeline.predict(X_test)
    y_prob = pipeline.predict_proba(X_test)[:, 1]

    metrics = {
        "accuracy": round(float(accuracy_score(y_test, y_pred)), 4),
        "precision": round(float(precision_score(y_test, y_pred, zero_division=0)), 4),
        "recall": round(float(recall_score(y_test, y_pred, zero_division=0)), 4),
        "f1": round(float(f1_score(y_test, y_pred, zero_division=0)), 4),
        "auc": round(float(roc_auc_score(y_test, y_prob)), 4),
        "test_size": int(len(X_test)),
    }

    print("\n===== 测试集表现（可直接用于论文） =====")
    for k, v in metrics.items():
        print(f"  {k:>10}: {v}")
    print("  混淆矩阵:")
    cm = confusion_matrix(y_test, y_pred)
    print(f"    TN={cm[0][0]}  FP={cm[0][1]}")
    print(f"    FN={cm[1][0]}  TP={cm[1][1]}")

    # ---- 落盘：模型 + 特征真相源 ----
    model_dir = BASE_DIR / "models"
    model_dir.mkdir(exist_ok=True)
    model_path = model_dir / "model_risk.pkl"
    joblib.dump(pipeline, model_path)

    meta = {
        "features": feature_cols,
        "target": target_col,
        "algorithm": "LogisticRegression(with StandardScaler)",
        "metrics": metrics,
        "source_csv": str(csv_path),
        "note": "特征顺序由此文件定义，推理端必须严格一致",
    }
    meta_path = model_dir / "risk_features.json"
    meta_path.write_text(
        json.dumps(meta, ensure_ascii=False, indent=2), encoding="utf-8"
    )

    print(f"\n[完成] 模型：{model_path}")
    print(f"[完成] 特征真相源：{meta_path}  -> 特征顺序：{feature_cols}")
    print("\n提示：请重启 AI 微服务（python main.py）使新模型生效。")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
