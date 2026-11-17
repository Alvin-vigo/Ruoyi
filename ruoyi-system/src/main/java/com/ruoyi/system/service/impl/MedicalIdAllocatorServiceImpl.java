package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.MedicalIdAllocator;
import com.ruoyi.system.mapper.MedicalDiagnosisMapper;
import com.ruoyi.system.mapper.MedicalIdAllocatorMapper;
import com.ruoyi.system.service.IMedicalIdAllocatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 业务 ID 分配器 服务实现
 *
 * <p><b>为什么需要这张表</b>：诊疗 ID 要求「删除后可复用」（AC-08），即取 MAX(id)+1，
 * 而 MySQL AUTO_INCREMENT 只增不回收，两者语义冲突；但又不能让多个请求同时读到同一个
 * MAX 值，否则 INSERT 会撞主键。本实现用一个独立单行表做行锁，把并发分配串行化。
 *
 * <p><b>为什么用 Propagation.REQUIRED 而不是 REQUIRES_NEW</b>（关键）：
 * 若分配走独立新事务，锁会在「取号」这一步提交时立刻释放，而调用方的 INSERT 尚未提交。
 * 此时另一个事务抢到锁，仍会读到同一个 MAX 值，照样撞主键。
 * 因此必须让分配加入调用方的同一个事务（REQUIRED），使行锁一直持有到 INSERT 提交，
 * 后到的并发请求才能在自己的读视图里看见前一条记录。
 *
 * <p><b>为什么最大值要用当前读（FOR UPDATE）</b>：
 * MySQL 默认 REPEATABLE READ 下，普通 SELECT 走 MVCC 一致性读，可能读到事务开启时的旧快照。
 * 加锁读会绕过快照直读最新已提交版本，配合上面的行锁顺序即可保证取值正确。
 *
 * @author ruoyi
 */
@Service
public class MedicalIdAllocatorServiceImpl implements IMedicalIdAllocatorService {

    /** 诊疗业务的分配器标识，对应 medical_id_allocator.biz_key */
    public static final String BIZ_KEY_DIAGNOSIS = "diagnosis";

    @Autowired
    private MedicalIdAllocatorMapper idAllocatorMapper;

    @Autowired
    private MedicalDiagnosisMapper diagnosisMapper;

    /**
     * 原子地分配下一个诊疗 ID
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Long allocateDiagnosisId() {
        // 1) 取行锁：所有并发分配在此串行化。行锁直到外层事务提交才释放。
        MedicalIdAllocator allocator = idAllocatorMapper.selectForUpdate(BIZ_KEY_DIAGNOSIS);
        if (allocator == null) {
            throw new ServiceException("诊疗 ID 分配器未初始化，请执行 sql/medical_patch_v5.sql 后重试");
        }

        // 2) 当前读取最大值（不走 MVCC 快照）
        Long maxId = diagnosisMapper.selectMaxIdForUpdate();
        long nextId = (maxId == null ? 0L : maxId) + 1L;

        // 3) 记账（仅供排障，不参与下次计算，以免破坏「删除后复用」语义）
        idAllocatorMapper.updateLastId(BIZ_KEY_DIAGNOSIS, nextId);

        return nextId;
    }
}
