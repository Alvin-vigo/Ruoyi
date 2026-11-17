package com.ruoyi.system.service;

/**
 * 业务 ID 分配器 服务层
 *
 * @author ruoyi
 */
public interface IMedicalIdAllocatorService {

    /**
     * 原子地分配下一个诊疗 ID。
     *
     * <p>语义：返回值 = 当前 medical_diagnosis 表中最大 diagnosis_id + 1。
     * 因此删除最大那条记录后，下一次新增会复用该 ID（满足 AC-08），
     * 这与 MySQL AUTO_INCREMENT 只增不回收的默认行为相反。
     *
     * <p>原子性：借助 medical_id_allocator 的行锁把所有分配请求串行化，
     * 并对最大值采用当前读（FOR UPDATE），避免 REPEATABLE READ 下读到过期快照。
     *
     * @return 下一个可用的 diagnosis_id
     */
    Long allocateDiagnosisId();
}
