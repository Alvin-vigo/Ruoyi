# -*- coding: utf-8 -*-
"""
模块③ 训练脚本：胸片三分类（COVID-19 / NORMAL / Viral Pneumonia）

准备数据（ImageFolder 结构）：
    data/chest_dataset/train/COVID-19/xxx.jpeg
    data/chest_dataset/train/NORMAL/xxx.jpeg
    data/chest_dataset/train/Viral Pneumonia/xxx.jpeg
    data/chest_dataset/val/<同上三类>/

用法：
    python scripts/train_chest.py --epochs 10 --batch-size 8

【类别不均衡处理（针对 COVID-19 仅 219 张的少数类问题）】
默认同时开启三种互补手段，可分别用 flag 关闭做消融对比：
  1. --oversample    WeightedRandomSampler：按类频反比重采样，使每轮各类等概率出现
  2. --weighted-loss CrossEntropyLoss(weight=类频反比)：对少数类错误施加更高惩罚
  3. --aug-strong    更强数据增强（仿射+亮度/对比度抖动），提升少数类样本多样性

评估指标：每轮打印混淆矩阵 + 各类 precision/recall/F1 + macro-F1，
最优模型按 macro-F1 保存（少数类召回直接计入目标，避免被多数类主导的 acc 掩盖）。

RTX4050 注意：6G 显存建议 batch-size<=16，遇到 CUDA out of memory 先降到 8。
"""
import argparse
import json
import sys
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(BASE_DIR))


def build_loaders(data_root: Path, batch_size: int, img_size: int, oversample: bool, aug_strong: bool):
    from torchvision import datasets, transforms

    mean = [0.485, 0.456, 0.406]
    std = [0.229, 0.224, 0.225]

    # 训练增强：基础版（翻转+小旋转）或强版（额外仿射+亮度/对比度抖动）
    train_aug = [
        transforms.Resize((img_size, img_size)),
        transforms.RandomHorizontalFlip(p=0.5),
        transforms.RandomRotation(degrees=15),
    ]
    if aug_strong:
        train_aug += [
            transforms.RandomAffine(degrees=0, translate=(0.05, 0.05), scale=(0.95, 1.05)),
            transforms.ColorJitter(brightness=0.15, contrast=0.15),
        ]
    train_aug += [transforms.ToTensor(), transforms.Normalize(mean, std)]
    train_tf = transforms.Compose(train_aug)

    val_tf = transforms.Compose(
        [
            transforms.Resize((img_size, img_size)),
            transforms.ToTensor(),
            transforms.Normalize(mean, std),
        ]
    )

    train_ds = datasets.ImageFolder(str(data_root / "train"), transform=train_tf)
    val_ds = datasets.ImageFolder(str(data_root / "val"), transform=val_tf)
    import torch

    train_loader = None
    sample_weights = None
    if oversample:
        # 按类频反比计算每个样本的采样权重，过采样少数类
        class_counts = [0] * len(train_ds.classes)
        for t in train_ds.targets:
            class_counts[t] += 1
        freq = [c if c > 0 else 1 for c in class_counts]
        inv = [1.0 / f for f in freq]
        sample_weights = [inv[t] for t in train_ds.targets]
        sampler = torch.utils.data.WeightedRandomSampler(
            weights=sample_weights, num_samples=len(train_ds.targets), replacement=True
        )
        train_loader = torch.utils.data.DataLoader(
            train_ds, batch_size=batch_size, sampler=sampler, num_workers=2, pin_memory=True
        )
    else:
        train_loader = torch.utils.data.DataLoader(
            train_ds, batch_size=batch_size, shuffle=True, num_workers=2, pin_memory=True
        )

    val_loader = torch.utils.data.DataLoader(
        val_ds, batch_size=batch_size, shuffle=False, num_workers=2, pin_memory=True
    )
    return train_loader, val_loader, train_ds.classes, class_counts if oversample else None


def evaluate(model, loader, device, n_classes, criterion=None):
    import torch

    model.eval()
    correct = total = 0
    loss_sum = 0.0
    cm = [[0] * n_classes for _ in range(n_classes)]
    with torch.no_grad():
        for imgs, labels in loader:
            imgs, labels = imgs.to(device), labels.to(device)
            outputs = model(imgs)
            if criterion is not None:
                loss_sum += criterion(outputs, labels).item() * labels.size(0)
            preds = outputs.argmax(dim=1)
            for t, p in zip(labels.cpu().tolist(), preds.cpu().tolist()):
                cm[t][p] += 1
            correct += (preds == labels).sum().item()
            total += labels.size(0)
    model.train()
    acc = correct / total if total else 0.0
    avg_loss = loss_sum / total if (criterion and total) else 0.0

    # 每类 precision / recall / F1
    prec = [0.0] * n_classes
    rec = [0.0] * n_classes
    f1 = [0.0] * n_classes
    for c in range(n_classes):
        tp = cm[c][c]
        fp = sum(cm[r][c] for r in range(n_classes) if r != c)
        fn = sum(cm[c][k] for k in range(n_classes) if k != c)
        prec[c] = tp / (tp + fp) if (tp + fp) > 0 else 0.0
        rec[c] = tp / (tp + fn) if (tp + fn) > 0 else 0.0
        f1[c] = (
            2 * prec[c] * rec[c] / (prec[c] + rec[c])
            if (prec[c] + rec[c]) > 0
            else 0.0
        )
    macro_f1 = sum(f1) / n_classes if n_classes else 0.0
    return acc, avg_loss, cm, prec, rec, f1, macro_f1


def main():
    parser = argparse.ArgumentParser(description="训练胸片三分类模型")
    parser.add_argument("--data", default=str(BASE_DIR / "data" / "chest_dataset"))
    parser.add_argument("--epochs", type=int, default=10)
    parser.add_argument("--batch-size", type=int, default=8)
    parser.add_argument("--lr", type=float, default=1e-4)
    parser.add_argument("--img-size", type=int, default=224)
    parser.add_argument("--oversample", action="store_true", default=True, help="过采样少数类(默认开)")
    parser.add_argument("--no-oversample", dest="oversample", action="store_false", help="关闭过采样")
    parser.add_argument("--weighted-loss", action="store_true", default=True, help="类别加权 loss(默认开)")
    parser.add_argument("--no-weighted-loss", dest="weighted_loss", action="store_false", help="关闭加权 loss")
    parser.add_argument("--aug-strong", action="store_true", default=True, help="强增强(默认开)")
    parser.add_argument("--no-aug-strong", dest="aug_strong", action="store_false", help="关闭强增强")
    args = parser.parse_args()

    try:
        import torch
        import torchvision.models as models
    except ImportError as exc:
        print(f"缺少 torch/torchvision：{exc}\n请先安装（CUDA 版见 requirements.txt 注释）")
        return 1

    data_root = Path(args.data)
    if not (data_root / "train").exists():
        print(
            f"未找到训练目录：{data_root / 'train'}\n"
            f"请按以下结构放置数据：\n"
            f"  {data_root}/train/COVID-19/*.jpg\n"
            f"  {data_root}/train/NORMAL/*.jpg\n"
            f"  {data_root}/train/Viral Pneumonia/*.jpg\n"
            f"  {data_root}/val/<同上三类>/\n"
        )
        return 1

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"[设备] {device}" + (f" ({torch.cuda.get_device_name(0)})" if device.type == "cuda" else ""))

    train_loader, val_loader, classes, class_counts = build_loaders(
        data_root, args.batch_size, args.img_size, args.oversample, args.aug_strong
    )
    n_classes = len(classes)
    print(f"[数据] 类别：{classes}（类别顺序已随模型保存至 models/chest_classes.json，后端推理自动对齐）")
    if class_counts:
        print(f"[数据] 训练各类数量：{dict(zip(classes, class_counts))}（已启用过采样平衡）")
    print(f"[数据] 训练 {len(train_loader.dataset)} 张，验证 {len(val_loader.dataset)} 张")
    print(
        f"[策略] oversample={args.oversample} weighted_loss={args.weighted_loss} aug_strong={args.aug_strong}"
    )

    # 类别加权 loss：权重 = 1/类频（或总样本/(n*类频)），缓解少数类
    criterion = torch.nn.CrossEntropyLoss()
    if args.weighted_loss:
        # 用训练集真实计数（无过采样时为原始分布）计算逆频权重
        real_counts = [0] * n_classes
        for t in train_loader.dataset.targets:
            real_counts[t] += 1
        total_n = sum(real_counts) or 1
        weights = [total_n / (n_classes * max(c, 1)) for c in real_counts]
        w_tensor = torch.tensor(weights, dtype=torch.float32, device=device)
        criterion = torch.nn.CrossEntropyLoss(weight=w_tensor)
        print(f"[loss] 类别权重：{dict(zip(classes, [round(w, 3) for w in weights]))}")

    model = models.resnet18(num_classes=n_classes).to(device)
    optimizer = torch.optim.Adam(model.parameters(), lr=args.lr)

    best_macro_f1 = 0.0
    best_acc = 0.0
    for epoch in range(args.epochs):
        model.train()
        running = 0.0
        for imgs, labels in train_loader:
            imgs, labels = imgs.to(device), labels.to(device)
            optimizer.zero_grad()
            loss = criterion(model(imgs), labels)
            loss.backward()
            optimizer.step()
            running += loss.item() * labels.size(0)

        train_loss = running / len(train_loader.dataset)
        acc, val_loss, cm, prec, rec, f1, macro_f1 = evaluate(
            model, val_loader, device, n_classes, criterion
        )
        print(
            f"epoch {epoch + 1:>2}/{args.epochs}  "
            f"train_loss={train_loss:.4f}  val_loss={val_loss:.4f}  "
            f"val_acc={acc:.4f}  macro_F1={macro_f1:.4f}"
        )
        # 每类明细
        for i, c in enumerate(classes):
            print(
                f"    {c:16s} P={prec[i]:.3f}  R={rec[i]:.3f}  F1={f1[i]:.3f}"
            )
        # 混淆矩阵（行=真实 列=预测）
        print("    混淆矩阵 [行:真实 / 列:预测]:")
        header = "      " + "".join(f"{c[:6]:>8s}" for c in classes)
        print(header)
        for i, c in enumerate(classes):
            print(f"    {c[:6]:>6s}" + "".join(f"{cm[i][j]:>8d}" for j in range(n_classes)))

        # 按 macro-F1 保存最优（少数类召回直接计入目标）
        if macro_f1 > best_macro_f1:
            best_macro_f1 = macro_f1
            best_acc = acc
            model_dir = BASE_DIR / "models"
            model_dir.mkdir(exist_ok=True)
            torch.save(model.state_dict(), model_dir / "chest_cnn.pth")
            with open(model_dir / "chest_classes.json", "w", encoding="utf-8") as _f:
                json.dump(classes, _f, ensure_ascii=False, indent=2)

    print(f"\n[完成] 最优模型已保存：{BASE_DIR / 'models' / 'chest_cnn.pth'}")
    print(f"[完成] 最优 macro-F1={best_macro_f1:.4f}  对应 val_acc={best_acc:.4f}")
    print("提示：请重启 AI 微服务（python main.py）使新模型生效。")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
