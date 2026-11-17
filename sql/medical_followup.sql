-- 若依医疗管理 患者随访记录表
-- 在导入 medical_schema.sql 之后执行本文件

DROP TABLE IF EXISTS medical_followup;
CREATE TABLE medical_followup (
  followup_id        BIGINT NOT NULL AUTO_INCREMENT COMMENT '随访ID',
  patient_id         BIGINT NOT NULL COMMENT '患者ID',
  followup_date      DATETIME DEFAULT NULL COMMENT '随访日期',
  followup_type      CHAR(1) DEFAULT '0' COMMENT '随访方式（0电话 1微信 2上门）',
  content            VARCHAR(1000) DEFAULT '' COMMENT '随访内容',
  next_followup_date DATE DEFAULT NULL COMMENT '下次随访日期',
  status             CHAR(1) DEFAULT '0' COMMENT '随访状态（0待随访 1已完成）',
  followup_person    VARCHAR(50) DEFAULT '' COMMENT '随访人',
  create_by          VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time        DATETIME DEFAULT NULL COMMENT '创建时间',
  update_by          VARCHAR(64) DEFAULT '' COMMENT '更新者',
  update_time        DATETIME DEFAULT NULL COMMENT '更新时间',
  remark             VARCHAR(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (followup_id),
  KEY idx_patient_id (patient_id),
  KEY idx_followup_date (followup_date)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者随访记录表';

-- 示例数据（可选）
INSERT INTO medical_followup (patient_id, followup_date, followup_type, content, next_followup_date, status, followup_person, create_time)
VALUES
  (1, NOW(), '0', '电话回访，患者恢复良好，叮嘱按时服药。', DATE_ADD(NOW(), INTERVAL 30 DAY), '1', '护士站', NOW()),
  (2, NOW(), '1', '微信随访，血压控制平稳，无需调整用药。', DATE_ADD(NOW(), INTERVAL 14 DAY), '0', '责任医师', NOW());
