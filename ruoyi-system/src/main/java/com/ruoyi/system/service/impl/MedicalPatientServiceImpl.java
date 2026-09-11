package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.MedicalPatient;
import com.ruoyi.system.mapper.MedicalPatientMapper;
import com.ruoyi.system.service.IMedicalPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 患者信息Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class MedicalPatientServiceImpl implements IMedicalPatientService {

    @Autowired
    private MedicalPatientMapper medicalPatientMapper;

    /**
     * 查询患者信息
     *
     * @param patientId 患者ID
     * @return 患者信息
     */
    @Override
    public MedicalPatient selectMedicalPatientById(Long patientId) {
        return medicalPatientMapper.selectMedicalPatientById(patientId);
    }

    /**
     * 查询患者信息列表
     *
     * @param medicalPatient 患者信息
     * @return 患者信息
     */
    @Override
    public List<MedicalPatient> selectMedicalPatientList(MedicalPatient medicalPatient) {
        return medicalPatientMapper.selectMedicalPatientList(medicalPatient);
    }

    /**
     * 新增患者信息
     *
     * @param medicalPatient 患者信息
     * @return 结果
     */
    @Override
    public int insertMedicalPatient(MedicalPatient medicalPatient) {
        return medicalPatientMapper.insertMedicalPatient(medicalPatient);
    }

    /**
     * 修改患者信息
     *
     * @param medicalPatient 患者信息
     * @return 结果
     */
    @Override
    public int updateMedicalPatient(MedicalPatient medicalPatient) {
        return medicalPatientMapper.updateMedicalPatient(medicalPatient);
    }

    /**
     * 批量删除患者信息
     *
     * @param patientIds 需要删除的患者ID
     * @return 结果
     */
    @Override
    public int deleteMedicalPatientByIds(Long[] patientIds) {
        return medicalPatientMapper.deleteMedicalPatientByIds(patientIds);
    }
}
