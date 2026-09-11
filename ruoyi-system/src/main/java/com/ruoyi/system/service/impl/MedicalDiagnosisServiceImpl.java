package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.MedicalDiagnosis;
import com.ruoyi.system.mapper.MedicalDiagnosisMapper;
import com.ruoyi.system.service.IMedicalDiagnosisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 诊疗信息Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class MedicalDiagnosisServiceImpl implements IMedicalDiagnosisService {

    @Autowired
    private MedicalDiagnosisMapper medicalDiagnosisMapper;

    /**
     * 查询诊疗信息
     *
     * @param diagnosisId 诊疗ID
     * @return 诊疗信息
     */
    @Override
    public MedicalDiagnosis selectMedicalDiagnosisById(Long diagnosisId) {
        return medicalDiagnosisMapper.selectMedicalDiagnosisById(diagnosisId);
    }

    /**
     * 查询诊疗信息列表
     *
     * @param medicalDiagnosis 诊疗信息
     * @return 诊疗信息
     */
    @Override
    public List<MedicalDiagnosis> selectMedicalDiagnosisList(MedicalDiagnosis medicalDiagnosis) {
        return medicalDiagnosisMapper.selectMedicalDiagnosisList(medicalDiagnosis);
    }

    /**
     * 新增诊疗信息
     *
     * @param medicalDiagnosis 诊疗信息
     * @return 结果
     */
    @Override
    public int insertMedicalDiagnosis(MedicalDiagnosis medicalDiagnosis) {
        return medicalDiagnosisMapper.insertMedicalDiagnosis(medicalDiagnosis);
    }

    /**
     * 修改诊疗信息
     *
     * @param medicalDiagnosis 诊疗信息
     * @return 结果
     */
    @Override
    public int updateMedicalDiagnosis(MedicalDiagnosis medicalDiagnosis) {
        return medicalDiagnosisMapper.updateMedicalDiagnosis(medicalDiagnosis);
    }

    /**
     * 批量删除诊疗信息
     *
     * @param diagnosisIds 需要删除的诊疗ID
     * @return 结果
     */
    @Override
    public int deleteMedicalDiagnosisByIds(Long[] diagnosisIds) {
        return medicalDiagnosisMapper.deleteMedicalDiagnosisByIds(diagnosisIds);
    }

    /**
     * 根据患者ID查询诊疗记录
     *
     * @param patientId 患者ID
     * @return 诊疗信息集合
     */
    @Override
    public List<MedicalDiagnosis> selectMedicalDiagnosisByPatientId(Long patientId) {
        return medicalDiagnosisMapper.selectMedicalDiagnosisByPatientId(patientId);
    }
}
