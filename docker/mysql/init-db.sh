#!/bin/bash
# ============================================================
# 若依医疗 · MySQL 初始化脚本（由 mysql 官方镜像在首次初始化时自动执行）
# 按依赖顺序灌入 sql/ 目录下的全部脚本；仅首次（数据卷为空）执行一次。
# 顺序依据 doc/部署说明.md 与 doc/AI模块部署说明.md，并补齐后续补丁：
#   ry_20231130(基础表) → medical_schema(患者/诊疗/药品) → followup → message
#   → price → quartz → menu → ai_menu → drug_menu → clinic(含补丁v2/v3)
#   → medical_ai(4张AI表) → patch_v4(胸片三分类) → patch_v5(诊疗ID串行化)
# 说明：medical_clinic.sql 已合并补丁 v2/v3，故全新导入无需再单独跑 v2/v3。
# ============================================================
set -e

echo ">>> [init-db] 开始按序导入 ruoyi_medical 数据库"

for f in \
  ry_20231130.sql \
  medical_schema.sql \
  medical_followup.sql \
  medical_message.sql \
  medical_price.sql \
  quartz.sql \
  medical_menu.sql \
  medical_ai_menu.sql \
  medical_drug_menu.sql \
  medical_clinic.sql \
  medical_ai.sql \
  medical_patch_v4.sql \
  medical_patch_v5.sql ; do
  echo ">>> [init-db] applying ${f}"
  mysql --default-character-set=utf8mb4 -u root -p"$MYSQL_ROOT_PASSWORD" ruoyi_medical < "/sql/${f}"
done

echo ">>> [init-db] 导入完成"
