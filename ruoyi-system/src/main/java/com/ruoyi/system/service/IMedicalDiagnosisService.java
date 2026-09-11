package com.ruoyi.system.service;

import com.ruoyi.system.domain.MedicalDiagnosis;

import java.util.List;

/**
 * 诊疗信息Service接口
 *
 * @author ruoyi
 */
public interface IMedicalDiagnosisService {

    /**
     * 查询诊疗信息
     *
     * @param diagnosisId 诊疗ID
     * @return 诊疗信息
     */
    public MedicalDiagnosis selectMedicalDiagnosisById(Long diagnosisId);

    /**
     * 查询诊疗信息列表
     *
     * @param medicalDiagnosis 诊疗信息
     * @return 诊疗信息集合
     */
    public List<MedicalDiagnosis> selectMedicalDiagnosisList(MedicalDiagnosis medicalDiagnosis);

    /**
     * 新增诊疗信息
     *
     * @param medicalDiagnosis 诊疗信息
     * @return 结果
     */
    public int insertMedicalDiagnosis(MedicalDiagnosis medicalDiagnosis);

    /**
     * 修改诊疗信息
     *
     * @param medicalDiagnosis 诊疗信息
     * @return 结果
     */
    public int updateMedicalDiagnosis(MedicalDiagnosis medicalDiagnosis);

    /**
     * 批量删除诊疗信息
     *
     * @param diagnosisIds 需要删除的诊疗ID数组
     * @return 结果
     */
    public int deleteMedicalDiagnosisByIds(Long[] diagnosisIds);

    /**
     * 根据患者ID查询诊疗记录
     *
     * @param patientId 患者ID
     * @return 诊疗信息集合
     */
    public List<MedicalDiagnosis> selectMedicalDiagnosisByPatientId(Long patientId);
}
