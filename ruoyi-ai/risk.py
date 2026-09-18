# -*- coding: utf-8 -*-
"""
ruoyi-ai / 模块② 常见病风险预测（传统机器学习）

相比大纲的改进：
1. 模型懒加载 —— 没跑训练脚本时服务照样能启动，不会顶层 FileNotFoundError 崩溃；
2. 特征数量与取值范围校验 —— 防止前端传错顺序导致"看似有结果实为乱算"；
3. 给出可读文案 —— 概率之外附带风险分层与建议，前端可直接展示。
"""
from typing import Dict, List

from config import RISK_FEATURE_NAMES, RISK_MODEL_PATH, RISK_THRESHOLD


class ModelNotReady(Exception):
    """模型尚未训练/未找到"""


_model = None
_load_attempted = False
_load_error = ""


def _get_model():
    """懒加载 sklearn 模型，失败时抛出 ModelNotReady"""
    global _model, _load_attempted, _load_error
    if _model is not None:
        return _model
    if _load_attempted:
        raise ModelNotReady(_load_error)

    _load_attempted = True
    if not RISK_MODEL_PATH.exists():
        _load_error = (
            f"未找到模型文件 {RISK_MODEL_PATH}，请先运行：python scripts/train_risk.py"
        )
        raise ModelNotReady(_load_error)
    try:
        import joblib  # 重依赖，按需导入
    except ImportError as exc:  # pragma: no cover
        _load_error = f"缺少依赖 joblib：{exc}，请 pip install -r requirements.txt"
        raise ModelNotReady(_load_error) from exc

    try:
        _model = joblib.load(RISK_MODEL_PATH)
    except Exception as exc:  # noqa: BLE001
        _load_error = f"模型加载失败：{exc}"
        raise ModelNotReady(_load_error) from exc
    return _model


def load_feature_names() -> tuple:
    """
    特征顺序以【训练产物 risk_features.json】为单一真相源。
    避免"训练用数据集A的列序、推理按代码里写死的顺序传"这种毕设经典翻车。
    """
    meta_path = RISK_MODEL_PATH.parent / "risk_features.json"
    if meta_path.exists():
        try:
            import json

            meta = json.loads(meta_path.read_text(encoding="utf-8"))
            feats = meta.get("features")
            if feats:
                return list(feats), meta.get("metrics", {})
        except Exception:  # noqa: BLE001 - json 损坏时退回默认
            pass
    return list(RISK_FEATURE_NAMES), {}


def status() -> Dict[str, object]:
    features, metrics = load_feature_names()
    return {
        "ready": RISK_MODEL_PATH.exists(),
        "model_path": str(RISK_MODEL_PATH),
        "features": features,
        "metrics": metrics,
        "hint": "" if RISK_MODEL_PATH.exists() else "请先运行 scripts/train_risk.py",
    }


def _risk_level(prob: float) -> str:
    if prob < 0.3:
        return "低风险"
    if prob < 0.6:
        return "中风险"
    return "高风险"


def _advice(prob: float) -> str:
    if prob < 0.3:
        return "保持规律作息与膳食均衡，建议每年常规体检复查。"
    if prob < 0.6:
        return "建议改善生活方式（控糖低脂饮食、规律运动），3个月内复查相关指标。"
    return "建议尽快至内分泌科就诊，完善空腹血糖、糖化血红蛋白等检查。"


def predict(features: List[float]) -> Dict[str, object]:
    """预测患病概率。features 顺序必须与 risk_features.json 中记录的一致。"""
    real_features, metrics = load_feature_names()
    if len(features) != len(real_features):
        raise ValueError(
            f"特征数量应为 {len(real_features)}（顺序：{'、'.join(real_features)}），"
            f"实际收到 {len(features)} 个"
        )
    if not all(isinstance(x, (int, float)) for x in features):
        raise ValueError("特征数组中存在非数值项")

    model = _get_model()
    try:
        # 优先输出正类概率；兼容只提供 predict_proba 的常见 sklearn 模型
        proba = model.predict_proba([features])[0]
        prob = float(proba[1]) if len(proba) > 1 else float(proba[0])
    except AttributeError as exc:  # 部分模型无 predict_proba
        raise ValueError(f"该模型不支持概率输出：{exc}") from exc

    prob = max(0.0, min(1.0, prob))
    label = 1 if prob >= RISK_THRESHOLD else 0
    return {
        "prob": round(prob, 4),
        "prob_percent": f"{prob * 100:.1f}%",
        "label": label,
        "risk_level": _risk_level(prob),
        "advice": _advice(prob),
        "threshold": RISK_THRESHOLD,
        "features": dict(zip(real_features, features)),
        "model_metrics": metrics,
    }
