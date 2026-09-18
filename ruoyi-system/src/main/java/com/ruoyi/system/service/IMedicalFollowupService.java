package com.ruoyi.system.service;

import com.ruoyi.system.domain.MedicalFollowup;

import java.util.List;

/**
 * 患者随访记录Service接口
 *
 * @author ruoyi
 */
public interface IMedicalFollowupService {

    /**
     * 查询患者随访记录
     *
     * @param followupId 随访ID
     * @return 随访记录
     */
    public MedicalFollowup selectMedicalFollowupById(Long followupId);

    /**
     * 查询患者随访记录列表
     *
     * @param medicalFollowup 随访记录
     * @return 随访记录集合
     */
    public List<MedicalFollowup> selectMedicalFollowupList(MedicalFollowup medicalFollowup);

    /**
     * 新增患者随访记录
     *
     * @param medicalFollowup 随访记录
     * @return 结果
     */
    public int insertMedicalFollowup(MedicalFollowup medicalFollowup);

    /**
     * 修改患者随访记录
     *
     * @param medicalFollowup 随访记录
     * @return 结果
     */
    public int updateMedicalFollowup(MedicalFollowup medicalFollowup);

    /**
     * 批量删除患者随访记录
     *
     * @param followupIds 需要删除的随访ID数组
     * @return 结果
     */
    public int deleteMedicalFollowupByIds(Long[] followupIds);

    /**
     * 根据患者ID查询随访记录
     *
     * @param patientId 患者ID
     * @return 随访记录集合
     */
    public List<MedicalFollowup> selectMedicalFollowupByPatientId(Long patientId);

    /**
     * 查询逾期未完成随访（状态为待随访且下次随访日期早于今天）
     *
     * @return 逾期随访记录集合
     */
    public List<MedicalFollowup> selectOverdueFollowupList();
}
