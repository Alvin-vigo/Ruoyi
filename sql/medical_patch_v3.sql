-- =============================================================
-- 若依医疗 · 补丁 v3（在 v2 之后执行）
-- 1) medical_diagnosis 增加 registration_id：建立「挂号 → 诊疗」的硬关联
-- 本脚本幂等，可重复执行
-- =============================================================

-- 1. 诊疗表增加挂号关联列
SET @exist_col := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'medical_diagnosis'
      AND COLUMN_NAME = 'registration_id'
);
SET @sql := IF(@exist_col = 0,
    'ALTER TABLE medical_diagnosis ADD COLUMN registration_id BIGINT(20) DEFAULT NULL COMMENT ''关联挂号ID'' AFTER patient_id, ADD KEY idx_diag_reg (registration_id)',
    'SELECT ''medical_diagnosis.registration_id 已存在，跳过'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 校验结果
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'medical_diagnosis'
  AND COLUMN_NAME = 'registration_id';
