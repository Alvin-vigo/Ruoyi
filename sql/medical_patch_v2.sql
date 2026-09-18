-- ============================================================
-- 业务闭环补丁 v2：退药状态列 + 审计日志表
-- 适用场景：在已导入基础表的 ruoyi_medical 库上增量执行（仅一次）。
-- 重新全量导入时无需本文件——对应 DDL 已合并进 medical_clinic.sql。
-- 说明：MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，故用 information_schema 守卫避免重复执行报错。
-- ============================================================

USE ruoyi_medical;

-- 1、发药记录表增加 status 列（0正常 1已退），用于退药逆向流程
SET @exist := (SELECT COUNT(*) FROM information_schema.columns
               WHERE table_schema = 'ruoyi_medical'
                 AND table_name   = 'medical_dispensing'
                 AND column_name  = 'status');
SET @sql := IF(@exist = 0,
               "ALTER TABLE medical_dispensing ADD COLUMN status CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1已退）' AFTER create_time",
               'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2、医疗操作审计日志表
CREATE TABLE IF NOT EXISTS medical_audit_log (
  log_id     BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  module     VARCHAR(30)   DEFAULT ''             COMMENT '业务模块（挂号/处方/发药/退药/退号）',
  action     VARCHAR(30)   DEFAULT ''             COMMENT '操作动作',
  biz_id     BIGINT(20)    DEFAULT NULL           COMMENT '业务主键',
  biz_no     VARCHAR(50)   DEFAULT ''             COMMENT '业务单号（挂号号/处方号）',
  operator   VARCHAR(50)   DEFAULT ''             COMMENT '操作人（登录账号）',
  detail     VARCHAR(500)  DEFAULT NULL           COMMENT '操作明细',
  create_by  VARCHAR(64)   DEFAULT ''             COMMENT '创建者',
  create_time DATETIME     DEFAULT NULL           COMMENT '操作时间',
  PRIMARY KEY (log_id),
  KEY idx_audit_module (module),
  KEY idx_audit_time (create_time)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='医疗操作审计日志';
