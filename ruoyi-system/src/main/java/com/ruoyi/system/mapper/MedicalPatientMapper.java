package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MedicalPatient;

import java.util.List;

/**
 * 患者信息Mapper接口
 *
 * @author ruoyi
 */
public interface MedicalPatientMapper {

    /**
     * 查询患者信息
     *
     * @param patientId 患者ID
     * @return 患者信息
     */
    public MedicalPatient selectMedicalPatientById(Long patientId);

    /**
     * 查询患者信息列表
     *
     * @param medicalPatient 患者信息
     * @return 患者信息集合
     */
    public List<MedicalPatient> selectMedicalPatientList(MedicalPatient medicalPatient);

    /**
     * 新增患者信息
     *
     * @param medicalPatient 患者信息
     * @return 结果
     */
    public int insertMedicalPatient(MedicalPatient medicalPatient);

    /**
     * 修改患者信息
     *
     * @param medicalPatient 患者信息
     * @return 结果
     */
    public int updateMedicalPatient(MedicalPatient medicalPatient);

    /**
     * 删除患者信息
     *
     * @param patientId 患者ID
     * @return 结果
     */
    public int deleteMedicalPatientById(Long patientId);

    /**
     * 批量删除患者信息
     *
     * @param patientIds 需要删除的患者ID数组
     * @return 结果
     */
    public int deleteMedicalPatientByIds(Long[] patientIds);
}
