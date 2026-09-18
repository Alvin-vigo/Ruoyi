-- ----------------------------
-- 若依医疗管理系统数据库脚本
-- ----------------------------
-- 说明：本脚本在已选数据库（ruoyi_medical，见 application.yml 的 druid url）中创建医疗业务表，
--       请在导入前确认当前连接的目标数据库为项目配置的数据库。

-- ----------------------------
-- 患者信息表
-- ----------------------------
DROP TABLE IF EXISTS medical_patient;
CREATE TABLE medical_patient (
  patient_id       BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '患者ID',
  patient_name     VARCHAR(50)   NOT NULL              COMMENT '患者姓名',
  gender           CHAR(1)       DEFAULT '2'           COMMENT '性别（0男 1女 2未知）',
  birth_date       DATE          DEFAULT NULL         COMMENT '出生日期',
  id_card          VARCHAR(18)   DEFAULT ''            COMMENT '身份证号',
  phone            VARCHAR(11)   DEFAULT ''            COMMENT '联系电话',
  address          VARCHAR(200)  DEFAULT ''            COMMENT '住址',
  blood_type       VARCHAR(10)   DEFAULT ''            COMMENT '血型（A/B/O/AB）',
  allergy_history  VARCHAR(500)  DEFAULT ''            COMMENT '过敏史',
  past_history     VARCHAR(500)  DEFAULT ''            COMMENT '既往病史',
  status           CHAR(1)       DEFAULT '0'           COMMENT '状态（0正常 1住院 2出院 3死亡）',
  create_by        VARCHAR(64)   DEFAULT ''            COMMENT '创建者',
  create_time      DATETIME      DEFAULT NULL          COMMENT '创建时间',
  update_by        VARCHAR(64)   DEFAULT ''            COMMENT '更新者',
  update_time      DATETIME      DEFAULT NULL          COMMENT '更新时间',
  remark           VARCHAR(500)  DEFAULT NULL          COMMENT '备注',
  PRIMARY KEY (patient_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='患者信息表';

-- ----------------------------
-- 诊疗信息表
-- ----------------------------
DROP TABLE IF EXISTS medical_diagnosis;
CREATE TABLE medical_diagnosis (
  diagnosis_id      BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '诊疗ID',
  patient_id        BIGINT(20)    NOT NULL              COMMENT '患者ID',
  registration_id   BIGINT(20)    DEFAULT NULL           COMMENT '关联挂号ID',
  visit_date        DATETIME      NOT NULL             COMMENT '就诊日期',
  department        VARCHAR(50)   NOT NULL             COMMENT '就诊科室',
  doctor_name       VARCHAR(50)   NOT NULL             COMMENT '主治医生',
  chief_complaint   VARCHAR(500)  DEFAULT ''            COMMENT '主诉',
  present_illness   VARCHAR(1000) DEFAULT ''            COMMENT '现病史',
  diagnosis_result  VARCHAR(500)  NOT NULL             COMMENT '诊断结果',
  treatment_plan    VARCHAR(1000) DEFAULT ''            COMMENT '治疗方案',
  cost              DECIMAL(10,2) DEFAULT 0.00          COMMENT '诊疗费用',
  status            CHAR(1)       DEFAULT '0'           COMMENT '状态（0待就诊 1就诊中 2已完成）',
  create_by         VARCHAR(64)   DEFAULT ''            COMMENT '创建者',
  create_time       DATETIME      DEFAULT NULL          COMMENT '创建时间',
  update_by         VARCHAR(64)   DEFAULT ''            COMMENT '更新者',
  update_time       DATETIME      DEFAULT NULL          COMMENT '更新时间',
  remark            VARCHAR(500)  DEFAULT NULL          COMMENT '备注',
  PRIMARY KEY (diagnosis_id),
  KEY idx_patient_id (patient_id),
  KEY idx_diag_reg (registration_id),
  KEY idx_visit_date (visit_date)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='诊疗信息表';

-- ----------------------------
-- 药品信息表
-- ----------------------------
DROP TABLE IF EXISTS medical_drug;
CREATE TABLE medical_drug (
  drug_id         BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '药品ID',
  drug_name       VARCHAR(100)  NOT NULL               COMMENT '药品名称',
  drug_code       VARCHAR(50)   DEFAULT ''             COMMENT '药品编码',
  category        CHAR(1)       DEFAULT '0'            COMMENT '药品分类（0西药 1中药 2中成药 3生物制品）',
  spec            VARCHAR(100)  DEFAULT ''             COMMENT '规格',
  manufacturer    VARCHAR(200)  DEFAULT ''             COMMENT '生产厂家',
  unit            VARCHAR(20)   DEFAULT ''             COMMENT '包装单位',
  purchase_price  DECIMAL(10,2) DEFAULT 0.00           COMMENT '进价（元）',
  sale_price      DECIMAL(10,2) DEFAULT 0.00           COMMENT '售价（元）',
  stock           INT(11)       DEFAULT 0              COMMENT '库存数量',
  production_date DATE          DEFAULT NULL           COMMENT '生产日期',
  expiry_date     DATE          DEFAULT NULL           COMMENT '有效期至',
  status          CHAR(1)       DEFAULT '0'            COMMENT '状态（0正常 1停用）',
  create_by       VARCHAR(64)   DEFAULT ''             COMMENT '创建者',
  create_time     DATETIME      DEFAULT NULL           COMMENT '创建时间',
  update_by       VARCHAR(64)   DEFAULT ''             COMMENT '更新者',
  update_time     DATETIME      DEFAULT NULL           COMMENT '更新时间',
  remark          VARCHAR(500)  DEFAULT NULL           COMMENT '备注',
  PRIMARY KEY (drug_id),
  KEY idx_drug_name (drug_name),
  KEY idx_category (category)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='药品信息表';

-- ----------------------------
-- 初始化测试数据
-- ----------------------------

-- 患者数据
INSERT INTO medical_patient (patient_name, gender, birth_date, id_card, phone, address, blood_type, allergy_history, status, create_by, create_time, remark)
VALUES
('张三', '0', '1990-05-15', '110101199005151234', '13800138001', '北京市朝阳区建国路88号', 'A', '青霉素过敏', '0', 'admin', NOW(), '普通感冒患者'),
('李四', '1', '1985-10-20', '110101198510202345', '13800138002', '北京市海淀区中关村大街1号', 'O', '无', '0', 'admin', NOW(), '高血压复诊患者'),
('王五', '0', '1978-03-08', '110101197803083456', '13800138003', '北京市西城区西长安街10号', 'B', '磺胺类药物过敏', '1', 'admin', NOW(), '住院患者'),
('赵六', '1', '1995-12-25', '110101199512254567', '13800138004', '北京市东城区东单北大街5号', 'AB', '海鲜过敏', '0', 'admin', NOW(), '体检患者'),
('钱七', '0', '2000-07-01', '110101200007015678', '13800138005', '北京市丰台区南三环西路6号', 'A', '无', '2', 'admin', NOW(), '已出院患者');

-- 诊疗记录数据
INSERT INTO medical_diagnosis (patient_id, visit_date, department, doctor_name, chief_complaint, present_illness, diagnosis_result, treatment_plan, cost, status, create_by, create_time, remark)
VALUES
(1, '2024-01-15 09:30:00', '内科', '李医生', '发热、咳嗽3天', '患者3天前受凉后出现发热，体温最高38.5℃，伴咳嗽、咳痰', '急性上呼吸道感染', '口服感冒药，多饮水，注意休息', 150.00, '2', 'admin', NOW(), '首次就诊'),
(2, '2024-01-16 10:00:00', '心内科', '王医生', '头晕伴心悸1周', '患者1周来反复头晕，活动后心悸，既往高血压病史5年', '原发性高血压2级', '调整降压药物，每日监测血压', 280.00, '2', 'admin', NOW(), '高血压复诊'),
(3, '2024-01-17 14:00:00', '呼吸科', '张医生', '胸痛伴呼吸困难2天', '患者2天前无明显诱因出现胸痛，深呼吸时加重，伴呼吸困难', '肺炎（右侧）', '住院抗感染治疗，完善胸部CT检查', 3500.00, '1', 'admin', NOW(), '住院治疗中'),
(4, '2024-01-18 08:30:00', '体检中心', '陈医生', '年度体检', '常规年度健康体检', '体检未见明显异常', '保持健康生活方式，定期体检', 500.00, '2', 'admin', NOW(), '年度体检'),
(5, '2024-01-10 15:00:00', '外科', '刘医生', '急性腹痛6小时', '患者6小时前突发上腹部疼痛，伴恶心呕吐', '急性阑尾炎', '急诊手术切除阑尾，术后抗感染治疗', 8000.00, '2', 'admin', NOW(), '已手术出院');

-- 药品数据
-- 注意：效期一律设为未来日期，否则「发药效期校验」会把所有药品都拦掉、主流程演示不下去。
-- 仅板蓝根颗粒刻意保留过期日期，专门用于演示「过期药自动拦截」。
INSERT INTO medical_drug (drug_name, drug_code, category, spec, manufacturer, unit, purchase_price, sale_price, stock, production_date, expiry_date, status, create_by, create_time, remark)
VALUES
('阿莫西林胶囊',   'YP20240101001', '0', '0.25g*24粒/盒',  '华北制药股份有限公司', '盒', 8.50,   15.80,  500, '2026-01-10', '2028-01-09', '0', 'admin', NOW(), '青霉素类抗生素'),
('布洛芬缓释胶囊', 'YP20240101002', '0', '0.3g*20粒/盒',   '中美天津史克制药',     '盒', 12.00,  22.50,  300, '2026-02-15', '2028-02-14', '0', 'admin', NOW(), '解热镇痛类非处方药'),
('板蓝根颗粒',     'YP20240101003', '2', '10g*20袋/盒',    '广州白云山药业',       '盒', 9.80,   18.00,  420, '2024-03-01', '2025-08-31', '0', 'admin', NOW(), '已过期：用于演示发药效期拦截'),
('枸杞子',         'YP20240101004', '1', '250g/袋',        '宁夏百瑞源枸杞',       '袋', 25.00,  45.00,  150, '2026-04-20', '2028-04-19', '0', 'admin', NOW(), '药食同源中药材'),
('重组人胰岛素注射液', 'YP20240101005', '3', '300IU/支',   '诺和诺德（中国）',     '支', 88.00,  136.00, 80,  '2026-05-05', '2027-05-04', '0', 'admin', NOW(), '需2-8℃冷藏保存'),
('复方甘草片',     'YP20240101006', '0', '100片/瓶',       '太极集团',             '瓶', 5.60,   9.90,   0,   '2025-12-01', '2027-11-30', '1', 'admin', NOW(), '库存为零已停用');

-- ----------------------------
-- 代码生成器业务表
-- ----------------------------
DROP TABLE IF EXISTS gen_table;
CREATE TABLE gen_table (
  table_id          BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '编号',
  table_name        VARCHAR(200)  DEFAULT ''            COMMENT '表名称',
  table_comment     VARCHAR(500)  DEFAULT ''            COMMENT '表描述',
  class_name        VARCHAR(100)  DEFAULT ''            COMMENT '实体类名称',
  tpl_category      VARCHAR(200)  DEFAULT 'crud'        COMMENT '使用的模板（crud）',
  package_name      VARCHAR(100)  DEFAULT ''            COMMENT '生成包路径',
  module_name       VARCHAR(30)   DEFAULT ''            COMMENT '生成模块名',
  business_name     VARCHAR(30)   DEFAULT ''            COMMENT '生成业务名',
  function_name     VARCHAR(50)   DEFAULT ''            COMMENT '生成功能名',
  function_author   VARCHAR(50)   DEFAULT ''            COMMENT '生成作者',
  options           VARCHAR(1000) DEFAULT ''            COMMENT '其它生成选项信息',
  create_by         VARCHAR(64)   DEFAULT ''            COMMENT '创建者',
  create_time       DATETIME      DEFAULT NULL          COMMENT '创建时间',
  update_by         VARCHAR(64)   DEFAULT ''            COMMENT '更新者',
  update_time       DATETIME      DEFAULT NULL          COMMENT '更新时间',
  remark            VARCHAR(500)  DEFAULT NULL          COMMENT '备注',
  PRIMARY KEY (table_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='代码生成业务表';

-- ----------------------------
-- 代码生成器业务表字段
-- ----------------------------
DROP TABLE IF EXISTS gen_table_column;
CREATE TABLE gen_table_column (
  column_id         BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '编号',
  table_id          BIGINT(20)    DEFAULT NULL          COMMENT '归属表编号',
  column_name       VARCHAR(200)  DEFAULT ''            COMMENT '列名称',
  column_comment    VARCHAR(500)  DEFAULT ''            COMMENT '列描述',
  column_type       VARCHAR(200)  DEFAULT ''            COMMENT '列数据类型',
  java_type         VARCHAR(500)  DEFAULT ''            COMMENT 'JAVA类型',
  java_field        VARCHAR(200)  DEFAULT ''            COMMENT 'JAVA字段名',
  is_pk             CHAR(1)       DEFAULT '0'           COMMENT '是否主键（1是）',
  is_increment      CHAR(1)       DEFAULT '0'           COMMENT '是否自增（1是）',
  is_required       CHAR(1)       DEFAULT '0'           COMMENT '是否必填（1是）',
  is_insert         CHAR(1)       DEFAULT '1'           COMMENT '是否为插入字段（1是）',
  is_edit           CHAR(1)       DEFAULT '1'           COMMENT '是否编辑字段（1是）',
  is_list           CHAR(1)       DEFAULT '1'           COMMENT '是否列表字段（1是）',
  is_query          CHAR(1)       DEFAULT '0'           COMMENT '是否查询字段（1是）',
  query_type        VARCHAR(200)  DEFAULT 'EQ'          COMMENT '查询方式（EQ等于、LIKE模糊）',
  html_type         VARCHAR(200)  DEFAULT 'input'       COMMENT '显示类型',
  dict_type         VARCHAR(200)  DEFAULT ''            COMMENT '字典类型',
  sort              INT(11)       DEFAULT NULL          COMMENT '排序',
  create_by         VARCHAR(64)   DEFAULT ''            COMMENT '创建者',
  create_time       DATETIME      DEFAULT NULL          COMMENT '创建时间',
  PRIMARY KEY (column_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='代码生成业务表字段';
