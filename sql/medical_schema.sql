-- ----------------------------
-- 若依医疗管理系统数据库脚本
-- ----------------------------

-- 创建数据库
CREATE DATABASE IF NOT EXISTS ruoyi_medical DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE ruoyi_medical;

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
  KEY idx_visit_date (visit_date)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='诊疗信息表';

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
