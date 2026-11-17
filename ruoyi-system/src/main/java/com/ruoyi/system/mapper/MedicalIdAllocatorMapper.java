package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MedicalIdAllocator;
import org.apache.ibatis.annotations.Param;

/**
 * 业务 ID 分配器 数据层
 *
 * @author ruoyi
 */
public interface MedicalIdAllocatorMapper {

    /**
     * 按业务标识加行锁读取分配器行（SELECT ... FOR UPDATE）
     *
     * <p>必须在事务内调用；行锁在事务提交 / 回滚时释放。
     * 该锁是所有 ID 分配请求的串行化点。
     *
     * @param bizKey 业务标识
     * @return 分配器行，不存在时返回 null
     */
    MedicalIdAllocator selectForUpdate(@Param("bizKey") String bizKey);

    /**
     * 记录最近一次分配出的 ID（仅审计用途）
     *
     * @param bizKey 业务标识
     * @param lastId 本次分配出的 ID
     * @return 影响行数
     */
    int updateLastId(@Param("bizKey") String bizKey, @Param("lastId") Long lastId);
}
