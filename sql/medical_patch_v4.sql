-- =============================================================
-- 若依医疗 · 补丁 v4（在 v3 之后执行，可重复执行 / 幂等）
--
-- 目的：把 medical_chest_xray 从「二分类」升级到「三分类」，
--       使库结构与下列两处读写保持一致，消除 Unknown column 'probs_json'（AC-13）：
--         · ruoyi-system/src/main/java/com/ruoyi/system/domain/MedicalChestXray.java
--         · ruoyi-system/src/main/resources/mapper/MedicalChestXrayMapper.xml
--
-- 两条适用路径：
--   ① 全新导入：sql/medical_ai.sql 已改为三分类建表语句，无需执行本脚本（执行也无副作用）。
--   ② 增量升级：本脚本负责加列、扩列、回填历史数据、建索引。
--
-- ⚠ MySQL 8 不支持 ADD COLUMN IF NOT EXISTS，故统一用
--   information_schema + PREPARE/EXECUTE 实现幂等（与 v2/v3 同款写法）。
-- =============================================================

-- -------------------------------------------------------------
-- 0. 前置校验：表必须存在
-- -------------------------------------------------------------
SET @tbl_exists := (
    SELECT COUNT(*) FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'medical_chest_xray'
);
SET @sql := IF(@tbl_exists = 0,
    'SELECT ''medical_chest_xray 表不存在，请先执行 sql/medical_ai.sql'' AS error',
    'SELECT ''表已存在，开始升级...'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -------------------------------------------------------------
-- 1. 新增三分类全概率列 probs_json
--    位置放在 confidence 之后，与 Mapper 的字段顺序一致
-- -------------------------------------------------------------
SET @exist_col := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'medical_chest_xray'
      AND COLUMN_NAME = 'probs_json'
);
SET @sql := IF(@exist_col = 0,
    'ALTER TABLE medical_chest_xray ADD COLUMN probs_json VARCHAR(500) DEFAULT '''' COMMENT ''三分类全概率 JSON'' AFTER confidence',
    'SELECT ''1) medical_chest_xray.probs_json 已存在，跳过'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -------------------------------------------------------------
-- 2. 扩长 predict_result 以容纳 'Viral Pneumonia'（原 50 已够，
--    此处兼容可能被改窄的历史库，仅在长度 < 50 时执行）
-- -------------------------------------------------------------
SET @cur_len := (
    SELECT IFNULL(CHARACTER_MAXIMUM_LENGTH, 0) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'medical_chest_xray'
      AND COLUMN_NAME = 'predict_result'
);
SET @sql := IF(@cur_len > 0 AND @cur_len < 50,
    'ALTER TABLE medical_chest_xray MODIFY COLUMN predict_result VARCHAR(50) DEFAULT '''' COMMENT ''三分类结果文案（COVID-19/NORMAL/Viral Pneumonia）''',
    'SELECT ''2) medical_chest_xray.predict_result 长度已满足，跳过'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -------------------------------------------------------------
-- 3. 历史二分类数据兼容处理（仅在旧列仍存在时有意义）
--
--    决策：二分类 → 三分类不是 1:1 映射（旧标签 abnormal 无法推断是
--    COVID-19 还是 Viral Pneumonia），因此不做语义猜测，
--    而是把旧的两类概率原样搬进 probs_json，并用 legacy 标记区分，
--    前端（views/medical/ai/xray.vue）对缺失类别按 0 渲染即可。
--    旧列保留作为「兼容列」，代码已不再读写。
-- -------------------------------------------------------------
SET @has_legacy := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'medical_chest_xray'
      AND COLUMN_NAME = 'prob_normal'
);
SET @sql := IF(@has_legacy > 0,
    'UPDATE medical_chest_xray
        SET probs_json = CONCAT(''{"NORMAL":'', IFNULL(prob_normal, 0),
                                '',"ABNORMAL(历史二分类)":'', IFNULL(prob_abnormal, 0),
                                '',"legacy":true}'')
      WHERE (probs_json IS NULL OR probs_json = '''')
        AND (prob_normal IS NOT NULL OR prob_abnormal IS NOT NULL)',
    'SELECT ''3) 无历史二分类列，跳过数据回填'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -------------------------------------------------------------
-- 4. 补 predict_result 的结果索引（替代已失效的 idx_xray_label）
-- -------------------------------------------------------------
SET @exist_idx := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'medical_chest_xray'
      AND INDEX_NAME = 'idx_xray_result'
);
SET @sql := IF(@exist_idx = 0,
    'ALTER TABLE medical_chest_xray ADD KEY idx_xray_result (predict_result)',
    'SELECT ''4) idx_xray_result 已存在，跳过'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 旧索引 idx_xray_label 建立在废弃列 result_label 上，存在则删除
SET @exist_old_idx := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'medical_chest_xray'
      AND INDEX_NAME = 'idx_xray_label'
);
SET @sql := IF(@exist_old_idx > 0,
    'ALTER TABLE medical_chest_xray DROP KEY idx_xray_label',
    'SELECT ''4b) idx_xray_label 不存在，跳过'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -------------------------------------------------------------
-- 5.（可选）彻底清理废弃列 —— 默认注释，确认无历史依赖后才执行
--
--    result_label    二分类结果编码，已被 predict_result 取代
--    prob_normal     二分类「正常」概率，已被 probs_json 取代
--    prob_abnormal   二分类「异常」概率，已被 probs_json 取代
--
--    之所以默认保留：删除是不可逆操作，保留不影响正确性（代码已不读写），
--    且历史记录仍可用旧列做追溯。第 3 步已完成概率搬家。
-- -------------------------------------------------------------
-- SET @exist_col := (
--     SELECT COUNT(*) FROM information_schema.COLUMNS
--     WHERE TABLE_SCHEMA = DATABASE()
--       AND TABLE_NAME = 'medical_chest_xray'
--       AND COLUMN_NAME = 'result_label'
-- );
-- SET @sql := IF(@exist_col > 0,
--     'ALTER TABLE medical_chest_xray DROP COLUMN result_label, DROP COLUMN prob_normal, DROP COLUMN prob_abnormal',
--     'SELECT ''5) 废弃列已不存在'' AS info');
-- PREPARE stmt FROM @sql;
-- EXECUTE stmt;
-- DEALLOCATE PREPARE stmt;

-- -------------------------------------------------------------
-- 6. 校验结果：必须与 MedicalChestXrayMapper.xml 读写的列完全对齐
--    期望列：xray_id, patient_id, img_url, predict_result, confidence,
--            probs_json, device, elapsed_ms, create_by, create_time
-- -------------------------------------------------------------
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'medical_chest_xray'
ORDER BY ORDINAL_POSITION;

SELECT 'v4 补丁执行完毕。若上表中出现 probs_json 且无报错，则 AC-13 前置条件已满足。' AS result;
