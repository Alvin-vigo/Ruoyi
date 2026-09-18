package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MedicalAuditLog;
import org.apache.ibatis.annotations.Param;

/**
 * 医疗操作审计日志Mapper接口
 *
 * @author ruoyi
 */
public interface MedicalAuditLogMapper {

    /**
     * 新增审计日志
     *
     * @param medicalAuditLog 审计日志
     * @return 结果
     */
    public int insertMedicalAuditLog(MedicalAuditLog medicalAuditLog);

    /**
     * 查询审计日志列表
     *
     * @param medicalAuditLog 查询条件（module / action / bizNo / operator 可选）
     * @return 审计日志集合
     */
    public java.util.List<MedicalAuditLog> selectMedicalAuditLogList(MedicalAuditLog medicalAuditLog);
}
