# -*- coding: utf-8 -*-
"""
模块③ 训练脚本：胸片二分类（正常 / 异常）

准备数据（ImageFolder 结构）：
    data/chest_dataset/train/NORMAL/xxx.jpeg
    data/chest_dataset/train/ABNORMAL/xxx.jpeg
    data/chest_dataset/val/NORMAL/...
    data/chest_dataset/val/ABNORMAL/...

用法：
    python scripts/train_chest.py --epochs 10 --batch-size 16

【数据集选择建议 —— 对大纲的重要修正】
大纲推荐 NIH ChestX-ray14：全量约 42GB（12 个 tar 包），下载与整理成本极高，
对本科毕设来说性价比很差。推荐改用：
    Kaggle《Chest X-Ray Images (Pneumonia)》
    https://www.kaggle.com/datasets/paulthimmanna/chest-xray-images
    - 约 5,856 张、已划分 train/val/test、本身就是二分类（NORMAL / PNEUMONIA）
    - 约 1.2GB，RTX4050 上单轮两三分钟，10 轮约半小时可出结果
    - 直接把其 train/ 与 val/ 目录放到 data/chest_dataset/ 下即可开跑
若坚持用 ChestX-ray14，需自行把 14 类病灶标签合并为"正常/异常"二分类。

RTX4050 注意：6G 显存建议 batch-size<=16，遇到 CUDA out of memory 先降到 8。
"""
import argparse
import sys
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(BASE_DIR))


def build_loaders(data_root: Path, batch_size: int, img_size: int):
    from torchvision import datasets, transforms

    mean = [0.485, 0.456, 0.406]
    std = [0.229, 0.224, 0.225]

    train_tf = transforms.Compose(
        [
            transforms.Resize((img_size, img_size)),
            transforms.RandomHorizontalFlip(p=0.5),
            transforms.RandomRotation(degrees=8),
            transforms.ToTensor(),
            transforms.Normalize(mean, std),
        ]
    )
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

    train_loader = torch.utils.data.DataLoader(
        train_ds, batch_size=batch_size, shuffle=True, num_workers=2, pin_memory=True
    )
    val_loader = torch.utils.data.DataLoader(
        val_ds, batch_size=batch_size, shuffle=False, num_workers=2, pin_memory=True
    )
    return train_loader, val_loader, train_ds.classes


def evaluate(model, loader, device, criterion=None):
    import torch

    model.eval()
    correct = total = 0
    loss_sum = 0.0
    with torch.no_grad():
        for imgs, labels in loader:
            imgs, labels = imgs.to(device), labels.to(device)
            outputs = model(imgs)
            if criterion is not None:
                loss_sum += criterion(outputs, labels).item() * labels.size(0)
            preds = outputs.argmax(dim=1)
            correct += (preds == labels).sum().item()
            total += labels.size(0)
    model.train()
    acc = correct / total if total else 0.0
    avg_loss = loss_sum / total if (criterion and total) else 0.0
    return acc, avg_loss


def main():
    parser = argparse.ArgumentParser(description="训练胸片二分类模型")
    parser.add_argument("--data", default=str(BASE_DIR / "data" / "chest_dataset"))
    parser.add_argument("--epochs", type=int, default=10)
    parser.add_argument("--batch-size", type=int, default=16)
    parser.add_argument("--lr", type=float, default=1e-4)
    parser.add_argument("--img-size", type=int, default=224)
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
            f"  {data_root}/train/NORMAL/*.jpg\n"
            f"  {data_root}/train/ABNORMAL/*.jpg\n"
            f"  {data_root}/val/NORMAL/*.jpg\n"
            f"  {data_root}/val/ABNORMAL/*.jpg\n"
            f"推荐数据集：Kaggle Chest X-Ray Images (Pneumonia)，约 1.2GB。"
        )
        return 1

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"[设备] {device}" + (f" ({torch.cuda.get_device_name(0)})" if device.type == "cuda" else ""))

    train_loader, val_loader, classes = build_loaders(data_root, args.batch_size, args.img_size)
    print(f"[数据] 类别：{classes}（类别索引需与线上 ABNORMAL=1 / NORMAL=0 对齐）")
    print(f"[数据] 训练 {len(train_loader.dataset)} 张，验证 {len(val_loader.dataset)} 张")

    model = models.resnet18(num_classes=len(classes)).to(device)
    criterion = torch.nn.CrossEntropyLoss()
    optimizer = torch.optim.Adam(model.parameters(), lr=args.lr)

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
        val_acc, val_loss = evaluate(model, val_loader, device, criterion)
        print(
            f"epoch {epoch + 1:>2}/{args.epochs}  "
            f"train_loss={train_loss:.4f}  val_loss={val_loss:.4f}  val_acc={val_acc:.4f}"
        )
        if val_acc > best_acc:
            best_acc = val_acc

    model_dir = BASE_DIR / "models"
    model_dir.mkdir(exist_ok=True)
    save_path = model_dir / "chest_cnn.pth"
    torch.save(model.state_dict(), save_path)
    print(f"\n[完成] 权重已保存：{save_path}")
    print(f"[完成] 最佳验证准确率：{best_acc:.4f}（可直接用于论文）")
    print("提示：请重启 AI 微服务（python main.py）使新模型生效。")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
