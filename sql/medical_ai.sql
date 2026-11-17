-- AI 模块数据表（医疗AI综合平台）
-- 导入顺序：在本目录所有业务脚本之后再执行本文件
--
-- 设计原则：
--   1. 不新建患者表，沿用既有 medical_patient.patient_id 作为外键语义（逻辑关联，不建物理外键）；
--   2. 每张 AI 结果表都保留"入参快照 + 结果 + 引擎/耗时"，保证结论可追溯；
--   3. medical_ai_record 保存的是 AI 草稿，采纳后由业务代码写入正式诊疗表 medical_diagnosis。

DROP TABLE IF EXISTS medical_ai_record;
create table medical_ai_record (
  record_id        BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  patient_id       BIGINT DEFAULT NULL COMMENT '患者ID（可空，支持临时书写）',
  patient_name     VARCHAR(50)  DEFAULT '' COMMENT '患者姓名',
  age              INT DEFAULT NULL COMMENT '年龄',
  gender           VARCHAR(10)  DEFAULT '' COMMENT '性别',
  chief_complaint  VARCHAR(500) DEFAULT '' COMMENT '主诉',
  ai_content       TEXT COMMENT 'AI生成的病历原文',
  doctor_content   TEXT COMMENT '医生修改后的内容',
  engine           VARCHAR(30)  DEFAULT '' COMMENT '生成引擎（ollama=大模型 template=模板降级）',
  model_name       VARCHAR(50)  DEFAULT '' COMMENT '模型名称，如 qwen2.5:7b',
  degraded         CHAR(1)      DEFAULT '0' COMMENT '是否降级输出（0否 1是）',
  elapsed_ms       BIGINT DEFAULT NULL COMMENT '推理耗时（毫秒）',
  status           CHAR(1)      DEFAULT '0' COMMENT '状态（0草稿 1已采纳 2已废弃）',
  diagnosis_id     BIGINT DEFAULT NULL COMMENT '采纳后对应的诊疗ID',
  create_by        VARCHAR(64)  DEFAULT '' COMMENT '创建者',
  create_time      DATETIME DEFAULT NULL COMMENT '创建时间',
  remark           VARCHAR(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (record_id),
  KEY idx_ai_record_patient (patient_id),
  KEY idx_ai_record_status (status, engine)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment = 'AI病历生成记录表';

DROP TABLE IF EXISTS medical_risk_predict;
create table medical_risk_predict (
  predict_id       BIGINT NOT NULL AUTO_INCREMENT COMMENT '预测记录ID',
  patient_id       BIGINT DEFAULT NULL COMMENT '患者ID（可空）',
  disease_name     VARCHAR(50)  DEFAULT '' COMMENT '预测病种',
  feature_json     VARCHAR(1000) DEFAULT '' COMMENT '入参特征快照（带特征名）',
  risk_prob        DECIMAL(6,4) DEFAULT NULL COMMENT '患病概率 0~1',
  result_label     CHAR(1)      DEFAULT '0' COMMENT '结论标签（0低风险 1高风险）',
  risk_level       VARCHAR(20)  DEFAULT '' COMMENT '风险等级文案',
  advice           VARCHAR(500) DEFAULT '' COMMENT '健康建议',
  threshold        DECIMAL(6,4) DEFAULT NULL COMMENT '判定阈值',
  model_metrics    VARCHAR(500) DEFAULT '' COMMENT '模型指标快照（accuracy/auc等）',
  create_by        VARCHAR(64)  DEFAULT '' COMMENT '创建者',
  create_time      DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (predict_id),
  KEY idx_risk_patient (patient_id),
  KEY idx_risk_result (result_label, disease_name)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment = '疾病风险预测记录表';

-- =============================================================
-- 胸片 AI 初筛记录表（三分类：COVID-19 / NORMAL / Viral Pneumonia）
-- ⚠ 结构必须与此二处严格一致，否则会出现 Unknown column（AC-13）：
--    1) ruoyi-system/src/main/java/com/ruoyi/system/domain/MedicalChestXray.java
--    2) ruoyi-system/src/main/resources/mapper/MedicalChestXrayMapper.xml
-- 历史二分类列（result_label / prob_normal / prob_abnormal）已于三分类改造中废弃，
-- 全新安装不再创建；老库升级由 sql/medical_patch_v4.sql 处理。
-- =============================================================
DROP TABLE IF EXISTS medical_chest_xray;
create table medical_chest_xray (
  xray_id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  patient_id       BIGINT DEFAULT NULL COMMENT '患者ID',
  img_url          VARCHAR(255) DEFAULT '' COMMENT '影像访问路径（/ai-uploads/xxx.png）',
  predict_result   VARCHAR(50)  DEFAULT '' COMMENT '三分类结果文案（COVID-19/NORMAL/Viral Pneumonia）',
  confidence       DECIMAL(6,4) DEFAULT NULL COMMENT '最大类置信度',
  probs_json       VARCHAR(500) DEFAULT '' COMMENT '三分类全概率 JSON，如 {"COVID-19":0.01,"NORMAL":0.97,"Viral Pneumonia":0.02}',
  device           VARCHAR(20)  DEFAULT '' COMMENT '推理设备（cuda/cpu）',
  elapsed_ms       BIGINT DEFAULT NULL COMMENT '推理耗时（毫秒）',
  create_by        VARCHAR(64)  DEFAULT '' COMMENT '创建者',
  create_time      DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (xray_id),
  KEY idx_xray_patient (patient_id),
  KEY idx_xray_result (predict_result)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment = '胸片AI初筛记录表（三分类）';

DROP TABLE IF EXISTS medical_rag_knowledge;
create table medical_rag_knowledge (
  knowledge_id     BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  title            VARCHAR(200) DEFAULT '' COMMENT '标题',
  content          TEXT COMMENT '知识正文',
  category         VARCHAR(50)  DEFAULT '' COMMENT '分类（常见病/用药/检查/保健）',
  synced           CHAR(1)      DEFAULT '0' COMMENT '是否已同步至向量库（0未同步 1已同步）',
  create_by        VARCHAR(64)  DEFAULT '' COMMENT '创建者',
  create_time      DATETIME DEFAULT NULL COMMENT '创建时间',
  remark           VARCHAR(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (knowledge_id),
  KEY idx_rag_synced (synced),
  KEY idx_rag_category (category)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment = 'RAG导诊知识条目表';

commit;
