-- ----------------------------
-- 药品信息表建表脚本（作业3：代码生成器实战）
-- 兼容 RuoYi-Vue v3.8.7 / MySQL 5.7+ / 8.0
-- 使用方式：先选择目标业务库（如 ry-vue），再执行本脚本
-- ----------------------------

-- ----------------------------
-- 1. 药品信息表
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
-- 2. 初始化测试数据
-- ----------------------------
INSERT INTO medical_drug (drug_name, drug_code, category, spec, manufacturer, unit, purchase_price, sale_price, stock, production_date, expiry_date, status, create_by, create_time, remark)
VALUES
('阿莫西林胶囊',   'YP20240101001', '0', '0.25g*24粒/盒',  '华北制药股份有限公司', '盒', 8.50,   15.80,  500, '2024-01-10', '2026-01-09', '0', 'admin', NOW(), '青霉素类抗生素'),
('布洛芬缓释胶囊', 'YP20240101002', '0', '0.3g*20粒/盒',   '中美天津史克制药',     '盒', 12.00,  22.50,  300, '2024-02-15', '2026-02-14', '0', 'admin', NOW(), '解热镇痛类非处方药'),
('板蓝根颗粒',     'YP20240101003', '2', '10g*20袋/盒',    '广州白云山药业',       '盒', 9.80,   18.00,  420, '2024-03-01', '2025-08-31', '0', 'admin', NOW(), '清热解毒中成药'),
('枸杞子',         'YP20240101004', '1', '250g/袋',        '宁夏百瑞源枸杞',       '袋', 25.00,  45.00,  150, '2024-04-20', '2025-04-19', '0', 'admin', NOW(), '药食同源中药材'),
('重组人胰岛素注射液', 'YP20240101005', '3', '300IU/支',   '诺和诺德（中国）',     '支', 88.00,  136.00, 80,  '2024-05-05', '2025-05-04', '0', 'admin', NOW(), '需2-8℃冷藏保存'),
('复方甘草片',     'YP20240101006', '0', '100片/瓶',       '太极集团',             '瓶', 5.60,   9.90,   0,   '2023-12-01', '2025-11-30', '1', 'admin', NOW(), '库存为零已停用');
