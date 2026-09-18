package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.MedicalFollowup;
import com.ruoyi.system.mapper.MedicalFollowupMapper;
import com.ruoyi.system.service.IMedicalFollowupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 患者随访记录Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class MedicalFollowupServiceImpl implements IMedicalFollowupService {

    @Autowired
    private MedicalFollowupMapper medicalFollowupMapper;

    /**
     * 查询患者随访记录
     *
     * @param followupId 随访ID
     * @return 随访记录
     */
    @Override
    public MedicalFollowup selectMedicalFollowupById(Long followupId) {
        return medicalFollowupMapper.selectMedicalFollowupById(followupId);
    }

    /**
     * 查询患者随访记录列表
     *
     * @param medicalFollowup 随访记录
     * @return 随访记录集合
     */
    @Override
    public List<MedicalFollowup> selectMedicalFollowupList(MedicalFollowup medicalFollowup) {
        return medicalFollowupMapper.selectMedicalFollowupList(medicalFollowup);
    }

    /**
     * 新增患者随访记录
     *
     * @param medicalFollowup 随访记录
     * @return 结果
     */
    @Override
    public int insertMedicalFollowup(MedicalFollowup medicalFollowup) {
        return medicalFollowupMapper.insertMedicalFollowup(medicalFollowup);
    }

    /**
     * 修改患者随访记录
     *
     * @param medicalFollowup 随访记录
     * @return 结果
     */
    @Override
    public int updateMedicalFollowup(MedicalFollowup medicalFollowup) {
        return medicalFollowupMapper.updateMedicalFollowup(medicalFollowup);
    }

    /**
     * 批量删除患者随访记录
     *
     * @param followupIds 需要删除的随访ID数组
     * @return 结果
     */
    @Override
    public int deleteMedicalFollowupByIds(Long[] followupIds) {
        return medicalFollowupMapper.deleteMedicalFollowupByIds(followupIds);
    }

    /**
     * 根据患者ID查询随访记录
     *
     * @param patientId 患者ID
     * @return 随访记录集合
     */
    @Override
    public List<MedicalFollowup> selectMedicalFollowupByPatientId(Long patientId) {
        return medicalFollowupMapper.selectMedicalFollowupByPatientId(patientId);
    }

    /**
     * 查询逾期未完成随访
     *
     * @return 逾期随访记录集合
     */
    @Override
    public List<MedicalFollowup> selectOverdueFollowupList() {
        return medicalFollowupMapper.selectOverdueFollowupList();
    }
}
