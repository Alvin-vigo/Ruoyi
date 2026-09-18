# -*- coding: utf-8 -*-
"""
ruoyi-ai / 模块③ 胸片智能初筛（ResNet18 二分类）

任务定义（与评审整改一致）：
  将 ChestX-ray14 的 14 类肺部病变（Atelectasis、Cardiomegaly、Effusion、Infiltration、
  Mass、Nodule、Pneumonia、Pneumothorax、Consolidation、Edema、Emphysema、Fibrosis、
  Pleural_Thickening、Hernia）统一归并为"异常"一类，
  与"正常"做**有无病灶的二分类初筛**，不做具体疾病鉴别（疾病鉴别超出本模型能力，需放射科医师）。

相比大纲的改进：
1. torch 属于重量级依赖（含 CUDA），改为推理时才懒加载 —— 本机没装 torch 时，
   其余三个模块仍可正常对外服务；
2. 模型结构（num_classes=2）与 train_chest.py 严格对齐，避免 load_state_dict 形状不匹配；
3. 输出双类概率而非单一置信度，便于前端画"正常/异常"对比条；
4. 明确声明这是**辅助筛查**工具，结论不构成诊断依据。
"""
from typing import Dict

from config import CHEST_CLASSES, CHEST_IMG_SIZE, CHEST_MODEL_PATH, CHEST_THRESHOLD


class ModelNotReady(Exception):
    """模型尚未训练/未找到"""


_model = None
_device = None
_load_attempted = False
_load_error = ""


def _get_model():
    global _model, _device, _load_attempted, _load_error
    if _model is not None:
        return _model, _device
    if _load_attempted:
        raise ModelNotReady(_load_error)

    _load_attempted = True
    if not CHEST_MODEL_PATH.exists():
        _load_error = (
            f"未找到模型权重 {CHEST_MODEL_PATH}，请先运行：python scripts/train_chest.py"
        )
        raise ModelNotReady(_load_error)
    try:
        import torch
        import torchvision.models as models
    except ImportError as exc:  # pragma: no cover
        _load_error = f"缺少依赖 torch/torchvision：{exc}，请 pip install -r requirements.txt"
        raise ModelNotReady(_load_error) from exc

    _device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    try:
        model = models.resnet18(num_classes=len(CHEST_CLASSES))
        state = torch.load(CHEST_MODEL_PATH, map_location=_device)
        model.load_state_dict(state)
        model.to(_device)
        model.eval()
        _model = model
    except Exception as exc:  # noqa: BLE001
        _load_error = f"模型加载失败（结构可能不匹配）：{exc}"
        raise ModelNotReady(_load_error) from exc
    return _model, _device


def status() -> Dict[str, object]:
    return {
        "ready": CHEST_MODEL_PATH.exists(),
        "model_path": str(CHEST_MODEL_PATH),
        "classes": CHEST_CLASSES,
        "hint": "" if CHEST_MODEL_PATH.exists() else "请先运行 scripts/train_chest.py",
    }


def predict(image) -> Dict[str, object]:
    """
    image: PIL.Image.Image（RGB）
    返回双类概率与判定结果
    """
    model, device = _get_model()
    import torch
    from torchvision import transforms

    transform = transforms.Compose(
        [
            transforms.Resize((CHEST_IMG_SIZE, CHEST_IMG_SIZE)),
            transforms.ToTensor(),
        ]
    )
    tensor = transform(image.convert("RGB")).unsqueeze(0).to(device)
    with torch.no_grad():
        logits = model(tensor)
        probs = torch.softmax(logits, dim=1)[0]

    prob_normal = float(probs[0])
    prob_abnormal = float(probs[1])
    abnormal = prob_abnormal >= CHEST_THRESHOLD

    return {
        "result": CHEST_CLASSES[1] if abnormal else CHEST_CLASSES[0],
        "result_label": "abnormal" if abnormal else "normal",
        "confidence": round(prob_abnormal if abnormal else prob_normal, 4),
        "prob_normal": round(prob_normal, 4),
        "prob_abnormal": round(prob_abnormal, 4),
        "threshold": CHEST_THRESHOLD,
        "device": str(device),
        "disclaimer": "本结果为AI辅助筛查，不能替代放射科医师诊断。",
    }
