-- ============================================================
-- 业务闭环：挂号 / 处方 / 发药（P0-①）+ 用药安全（P0-②）+ RBAC 角色（P0-④）
-- 执行顺序：在 ruoyi_medical 库下，medical_menu.sql 之后执行本文件。
-- 说明：本脚本幂等（DROP TABLE IF EXISTS / NOT EXISTS），可重复执行。
-- ============================================================

-- ----------------------------
-- 1、号源排班表
-- ----------------------------
DROP TABLE IF EXISTS medical_schedule;
CREATE TABLE medical_schedule (
  schedule_id   BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '排班ID',
  dept          VARCHAR(50)   NOT NULL               COMMENT '科室',
  doctor_name   VARCHAR(50)   NOT NULL               COMMENT '出诊医生',
  schedule_date DATE          NOT NULL               COMMENT '排班日期',
  period        CHAR(1)       DEFAULT '0'            COMMENT '时段（0上午 1下午）',
  total_num     INT(11)       DEFAULT 0              COMMENT '号源总数',
  used_num      INT(11)       DEFAULT 0              COMMENT '已挂号数',
  status        CHAR(1)       DEFAULT '0'            COMMENT '状态（0出诊 1停诊）',
  create_by     VARCHAR(64)   DEFAULT ''             COMMENT '创建者',
  create_time   DATETIME      DEFAULT NULL           COMMENT '创建时间',
  update_by     VARCHAR(64)   DEFAULT ''             COMMENT '更新者',
  update_time   DATETIME      DEFAULT NULL           COMMENT '更新时间',
  remark        VARCHAR(500)  DEFAULT NULL           COMMENT '备注',
  PRIMARY KEY (schedule_id),
  UNIQUE KEY uk_sched (dept, doctor_name, schedule_date, period),
  KEY idx_sched_date (schedule_date)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='号源排班表';

-- ----------------------------
-- 2、挂号表
-- ----------------------------
DROP TABLE IF EXISTS medical_registration;
CREATE TABLE medical_registration (
  registration_id BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '挂号ID',
  reg_no         VARCHAR(30)   NOT NULL               COMMENT '挂号号（GH+日期+序列）',
  patient_id     BIGINT(20)    NOT NULL               COMMENT '患者ID',
  patient_name   VARCHAR(50)   DEFAULT ''             COMMENT '患者姓名（冗余，便于展示）',
  schedule_id    BIGINT(20)    DEFAULT NULL           COMMENT '关联号源排班ID',
  dept           VARCHAR(50)   DEFAULT ''             COMMENT '科室',
  doctor_name    VARCHAR(50)   DEFAULT ''             COMMENT '接诊医生',
  reg_type       CHAR(1)       DEFAULT '0'            COMMENT '挂号类型（0普通 1专家 2急诊，对应价目挂号费类别）',
  reg_fee        DECIMAL(10,2) DEFAULT 0.00          COMMENT '挂号费（元）',
  reg_status     CHAR(1)       DEFAULT '0'            COMMENT '状态（0待就诊 1已就诊 2已退号）',
  reg_time       DATETIME      DEFAULT NULL           COMMENT '挂号时间',
  create_by      VARCHAR(64)   DEFAULT ''             COMMENT '创建者',
  create_time    DATETIME      DEFAULT NULL           COMMENT '创建时间',
  update_by      VARCHAR(64)   DEFAULT ''             COMMENT '更新者',
  update_time    DATETIME      DEFAULT NULL           COMMENT '更新时间',
  remark         VARCHAR(500)  DEFAULT NULL           COMMENT '备注',
  PRIMARY KEY (registration_id),
  UNIQUE KEY uk_reg_no (reg_no),
  KEY idx_reg_patient (patient_id),
  KEY idx_reg_schedule (schedule_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='挂号表';

-- ----------------------------
-- 3、处方主表
-- ----------------------------
DROP TABLE IF EXISTS medical_prescription;
CREATE TABLE medical_prescription (
  prescription_id   BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '处方ID',
  prescription_no   VARCHAR(30)   NOT NULL               COMMENT '处方号（CF+日期+序列）',
  registration_id   BIGINT(20)    DEFAULT NULL           COMMENT '关联挂号ID',
  patient_id        BIGINT(20)    NOT NULL               COMMENT '患者ID',
  patient_name      VARCHAR(50)   DEFAULT ''             COMMENT '患者姓名（冗余）',
  doctor_name       VARCHAR(50)   DEFAULT ''             COMMENT '开方医生',
  dept              VARCHAR(50)   DEFAULT ''             COMMENT '科室',
  diagnosis         VARCHAR(500)  DEFAULT ''             COMMENT '诊断',
  total_amount      DECIMAL(10,2) DEFAULT 0.00          COMMENT '处方总金额（元）',
  prescription_status CHAR(1)     DEFAULT '0'            COMMENT '状态（0待发药 1部分发药 2已发药 3已作废）',
  create_by         VARCHAR(64)   DEFAULT ''             COMMENT '创建者',
  create_time       DATETIME      DEFAULT NULL           COMMENT '创建时间',
  update_by         VARCHAR(64)   DEFAULT ''             COMMENT '更新者',
  update_time       DATETIME      DEFAULT NULL           COMMENT '更新时间',
  remark            VARCHAR(500)  DEFAULT NULL           COMMENT '备注',
  PRIMARY KEY (prescription_id),
  UNIQUE KEY uk_pres_no (prescription_no),
  KEY idx_pres_patient (patient_id),
  KEY idx_pres_reg (registration_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='处方主表';

-- ----------------------------
-- 4、处方明细表
-- ----------------------------
DROP TABLE IF EXISTS medical_prescription_item;
CREATE TABLE medical_prescription_item (
  item_id            BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  prescription_id    BIGINT(20)    NOT NULL               COMMENT '处方ID',
  drug_id            BIGINT(20)    NOT NULL               COMMENT '药品ID',
  drug_name          VARCHAR(100)  DEFAULT ''             COMMENT '药品名称（冗余）',
  spec               VARCHAR(100)  DEFAULT ''             COMMENT '规格',
  quantity           INT(11)       NOT NULL DEFAULT 0     COMMENT '开方数量',
  price              DECIMAL(10,2) DEFAULT 0.00          COMMENT '单价（元）',
  dosage             VARCHAR(100)  DEFAULT ''             COMMENT '用法用量',
  dispensed_quantity INT(11)       DEFAULT 0              COMMENT '已发数量',
  allergy_warning    VARCHAR(200)  DEFAULT NULL           COMMENT '用药安全警示（如：患者既往青霉素过敏）',
  create_by          VARCHAR(64)   DEFAULT ''             COMMENT '创建者',
  create_time        DATETIME      DEFAULT NULL           COMMENT '创建时间',
  PRIMARY KEY (item_id),
  KEY idx_item_pres (prescription_id),
  KEY idx_item_drug (drug_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='处方明细表';

-- ----------------------------
-- 5、发药记录表
-- ----------------------------
DROP TABLE IF EXISTS medical_dispensing;
CREATE TABLE medical_dispensing (
  dispensing_id   BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '发药ID',
  prescription_id BIGINT(20)   NOT NULL               COMMENT '处方ID',
  item_id         BIGINT(20)   NOT NULL               COMMENT '处方明细ID',
  drug_id         BIGINT(20)   NOT NULL               COMMENT '药品ID',
  drug_name       VARCHAR(100) DEFAULT ''             COMMENT '药品名称（冗余）',
  quantity        INT(11)       NOT NULL DEFAULT 0     COMMENT '本次发药数量',
  operator        VARCHAR(50)   DEFAULT ''             COMMENT '发药人',
  dispensing_time DATETIME      DEFAULT NULL           COMMENT '发药时间',
  status          CHAR(1)       DEFAULT '0'            COMMENT '状态（0正常 1已退）',
  create_by       VARCHAR(64)   DEFAULT ''             COMMENT '创建者',
  create_time     DATETIME      DEFAULT NULL           COMMENT '创建时间',
  PRIMARY KEY (dispensing_id),
  KEY idx_dispense_pres (prescription_id),
  KEY idx_dispense_drug (drug_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='发药记录表';

-- ----------------------------
-- 6、医疗操作审计日志表（开方/发药/退药/退号/改库存 留痕，合规要求）
-- ----------------------------
DROP TABLE IF EXISTS medical_audit_log;
CREATE TABLE medical_audit_log (
  log_id      BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  module      VARCHAR(30)   DEFAULT ''             COMMENT '业务模块（挂号/处方/发药/退药/退号）',
  action      VARCHAR(30)   DEFAULT ''             COMMENT '操作动作',
  biz_id      BIGINT(20)    DEFAULT NULL           COMMENT '业务主键',
  biz_no      VARCHAR(50)   DEFAULT ''             COMMENT '业务单号（挂号号/处方号）',
  operator    VARCHAR(50)   DEFAULT ''             COMMENT '操作人（登录账号）',
  detail      VARCHAR(500)  DEFAULT NULL           COMMENT '操作明细',
  create_by   VARCHAR(64)   DEFAULT ''             COMMENT '创建者',
  create_time DATETIME      DEFAULT NULL           COMMENT '操作时间',
  PRIMARY KEY (log_id),
  KEY idx_audit_module (module),
  KEY idx_audit_time (create_time)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='医疗操作审计日志';

-- ============================================================
-- 种子数据（演示用，可重复执行）
-- ============================================================

-- 药品（若尚未存在则插入，便于开方/发药演示；与过敏史关键词匹配：青霉素类 / 磺胺类）
INSERT INTO medical_drug (drug_name, drug_code, category, spec, manufacturer, unit, purchase_price, sale_price, stock, status, create_by, create_time, remark)
SELECT '阿莫西林胶囊', 'YP001', '0', '0.25g*24粒/盒', '华北制药', '盒', 10.00, 15.80, 100, '0', 'admin', NOW(), '青霉素类'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM medical_drug WHERE drug_name='阿莫西林胶囊');
INSERT INTO medical_drug (drug_name, drug_code, category, spec, manufacturer, unit, purchase_price, sale_price, stock, status, create_by, create_time, remark)
SELECT '复方磺胺甲噁唑片', 'YP002', '0', '0.48g*100片/瓶', '太极集团', '瓶', 6.00, 9.90, 80, '0', 'admin', NOW(), '磺胺类'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM medical_drug WHERE drug_name='复方磺胺甲噁唑片');
INSERT INTO medical_drug (drug_name, drug_code, category, spec, manufacturer, unit, purchase_price, sale_price, stock, status, create_by, create_time, remark)
SELECT '布洛芬缓释胶囊', 'YP003', '0', '0.3g*20粒/盒', '中美天津史克', '盒', 15.00, 22.50, 60, '0', 'admin', NOW(), '解热镇痛'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM medical_drug WHERE drug_name='布洛芬缓释胶囊');
INSERT INTO medical_drug (drug_name, drug_code, category, spec, manufacturer, unit, purchase_price, sale_price, stock, status, create_by, create_time, remark)
SELECT '盐酸二甲双胍片', 'YP004', '0', '0.5g*30片/盒', '中美上海施贵宝', '盒', 12.00, 18.00, 50, '0', 'admin', NOW(), '降糖药'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM medical_drug WHERE drug_name='盐酸二甲双胍片');

-- 号源排班（演示：内科/外科 未来两天的上午/下午号源）
INSERT INTO medical_schedule (dept, doctor_name, schedule_date, period, total_num, used_num, status, create_by, create_time)
SELECT '内科', '张医生', CURDATE(), '0', 30, 0, '0', 'admin', NOW()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM medical_schedule WHERE dept='内科' AND doctor_name='张医生' AND schedule_date=CURDATE() AND period='0');
INSERT INTO medical_schedule (dept, doctor_name, schedule_date, period, total_num, used_num, status, create_by, create_time)
SELECT '内科', '张医生', CURDATE(), '1', 30, 0, '0', 'admin', NOW()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM medical_schedule WHERE dept='内科' AND doctor_name='张医生' AND schedule_date=CURDATE() AND period='1');
INSERT INTO medical_schedule (dept, doctor_name, schedule_date, period, total_num, used_num, status, create_by, create_time)
SELECT '外科', '李医生', CURDATE(), '0', 20, 0, '0', 'admin', NOW()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM medical_schedule WHERE dept='外科' AND doctor_name='李医生' AND schedule_date=CURDATE() AND period='0');
INSERT INTO medical_schedule (dept, doctor_name, schedule_date, period, total_num, used_num, status, create_by, create_time)
SELECT '内科', '王医生', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '0', 30, 0, '0', 'admin', NOW()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM medical_schedule WHERE dept='内科' AND doctor_name='王医生' AND schedule_date=DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND period='0');

-- ============================================================
-- 菜单：挂号 / 处方发药 挂到「医疗管理」目录下（静态路由由前端 router 控制可见性）
-- ============================================================
SET @med = (SELECT menu_id FROM sys_menu WHERE path='medical' AND menu_type='M' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '号源排班', @med, 8, 'schedule', 'medical/schedule/index', 1, 0, 'C', '0', '0', 'medical:schedule:list', 'el-icon-date', 'admin', NOW(), '号源排班菜单'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path='schedule');
SET @schedMenu = (SELECT menu_id FROM sys_menu WHERE path='schedule' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '排班新增', @schedMenu, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:schedule:add', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@schedMenu AND perms='medical:schedule:add');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '排班修改', @schedMenu, 2, '#', '', 1, 0, 'F', '0', '0', 'medical:schedule:edit', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@schedMenu AND perms='medical:schedule:edit');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '排班删除', @schedMenu, 3, '#', '', 1, 0, 'F', '0', '0', 'medical:schedule:remove', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@schedMenu AND perms='medical:schedule:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '挂号管理', @med, 9, 'registration', 'medical/registration/index', 1, 0, 'C', '0', '0', 'medical:registration:list', 'el-icon-tickets', 'admin', NOW(), '挂号管理菜单'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path='registration');
SET @regMenu = (SELECT menu_id FROM sys_menu WHERE path='registration' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '挂号', @regMenu, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:registration:add', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@regMenu AND perms='medical:registration:add');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '退号', @regMenu, 2, '#', '', 1, 0, 'F', '0', '0', 'medical:registration:refund', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@regMenu AND perms='medical:registration:refund');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '处方发药', @med, 10, 'prescription', 'medical/prescription/index', 1, 0, 'C', '0', '0', 'medical:prescription:list', 'el-icon-first-aid-kit', 'admin', NOW(), '处方与发药菜单'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path='prescription');
SET @presMenu = (SELECT menu_id FROM sys_menu WHERE path='prescription' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '开方', @presMenu, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:prescription:add', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@presMenu AND perms='medical:prescription:add');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '发药', @presMenu, 2, '#', '', 1, 0, 'F', '0', '0', 'medical:prescription:dispense', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@presMenu AND perms='medical:prescription:dispense');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '处方作废', @presMenu, 3, '#', '', 1, 0, 'F', '0', '0', 'medical:prescription:cancel', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id=@presMenu AND perms='medical:prescription:cancel');

-- ============================================================
-- RBAC：新增 医生 / 护士 / 药剂师 三角色，并授予「医疗管理」整棵子树的菜单权限
-- （admin 仍保有全部；三角色仅能看到业务菜单，看不到系统管理，体现权限分级）
-- ============================================================
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, update_by, update_time, remark)
SELECT 3, '医生', 'doctor', 3, 1, '0', '0', 'admin', NOW(), '', NULL, '门诊医生角色'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_id=3);
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, update_by, update_time, remark)
SELECT 4, '护士', 'nurse', 4, 1, '0', '0', 'admin', NOW(), '', NULL, '护士角色'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_id=4);
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, update_by, update_time, remark)
SELECT 5, '药剂师', 'pharmacist', 5, 1, '0', '0', 'admin', NOW(), '', NULL, '药剂师角色'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_id=5);

-- 授予三角色：医疗管理目录(M) + 其下所有二级菜单(C) + 按钮(F)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM (SELECT 3 AS role_id UNION SELECT 4 UNION SELECT 5) r
CROSS JOIN (
  SELECT menu_id FROM sys_menu
  WHERE menu_id = @med
     OR parent_id = @med
     OR parent_id IN (SELECT menu_id FROM sys_menu WHERE parent_id = @med)
) m
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=m.menu_id);
