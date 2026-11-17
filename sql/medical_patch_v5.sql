-- =============================================================
-- 若依医疗 · 补丁 v5（可重复执行 / 幂等）
--
-- 目的：为「诊疗 ID 原子分配」提供串行化载体表 medical_id_allocator。
--       解决 MAX(diagnosis_id)+1 在并发下撞主键的问题，同时**保留**
--       「删除记录后新增可复用被删 ID」的验收语义（AC-08）。
--
-- 设计要点：
--   1) 表只有一行（biz_key='diagnosis'），作为 SELECT ... FOR UPDATE 的行锁载体。
--      所有取号请求在此串行化，行锁持有到调用方事务提交为止。
--   2) last_id 仅做审计记账，不参与下一次分配计算——一旦用它递增，
--      「删除最大 ID 后复用」的语义就丢了，这是本方案与自增计数器的关键区别。
--   3) 真正参与计算的是 medical_diagnosis 的当前最大值（详见
--      MedicalIdAllocatorServiceImpl.allocateDiagnosisId()）。
--
-- 关联代码：
--   · ruoyi-system/src/main/java/com/ruoyi/system/domain/MedicalIdAllocator.java
--   · ruoyi-system/src/main/java/com/ruoyi/system/mapper/MedicalIdAllocatorMapper.java
--   · ruoyi-system/src/main/resources/mapper/MedicalIdAllocatorMapper.xml
--   · ruoyi-system/src/main/java/com/ruoyi/system/service/impl/MedicalIdAllocatorServiceImpl.java
-- =============================================================

-- -------------------------------------------------------------
-- 1. 建表（MySQL 8 不支持 CREATE TABLE IF NOT EXISTS 的列级 IF NOT EXISTS，
--    但 CREATE TABLE IF NOT EXISTS 本身可用）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS medical_id_allocator (
  biz_key     VARCHAR(50) NOT NULL                COMMENT '业务标识（当前仅 diagnosis）',
  last_id     BIGINT(20)  NOT NULL DEFAULT 0      COMMENT '最近一次分配出的ID（仅审计/排障，不参与下次分配）',
  update_time DATETIME    DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (biz_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务ID分配器（行锁串行化，保证 MAX+1 语义原子性）';

-- -------------------------------------------------------------
-- 2. 初始化诊疗业务的分配行
--    ON DUPLICATE KEY UPDATE 保证重复执行不会报错、也不会覆盖已有记账
-- -------------------------------------------------------------
INSERT INTO medical_id_allocator (biz_key, last_id, update_time)
VALUES ('diagnosis', 0, NOW())
ON DUPLICATE KEY UPDATE update_time = update_time;

-- -------------------------------------------------------------
-- 3. 校验结果：应能看到 biz_key='diagnosis' 的一行
-- -------------------------------------------------------------
SELECT biz_key, last_id, update_time FROM medical_id_allocator ORDER BY biz_key;

SELECT 'v5 补丁执行完毕。medical_id_allocator 已就绪，诊疗新增将走原子 ID 分配。' AS result;
