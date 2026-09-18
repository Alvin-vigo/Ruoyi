package com.ruoyi.system.service;

import com.ruoyi.system.domain.MedicalRegistration;

import java.util.List;

/**
 * 挂号信息Service接口
 *
 * @author ruoyi
 */
public interface IMedicalRegistrationService {

    /**
     * 查询挂号信息
     *
     * @param registrationId 挂号ID
     * @return 挂号信息
     */
    public MedicalRegistration selectMedicalRegistrationById(Long registrationId);

    /**
     * 查询挂号信息列表
     *
     * @param medicalRegistration 挂号信息
     * @return 挂号信息集合
     */
    public List<MedicalRegistration> selectMedicalRegistrationList(MedicalRegistration medicalRegistration);

    /**
     * 新增挂号信息（含号源占用，号源已满抛异常）
     *
     * @param medicalRegistration 挂号信息
     * @return 结果
     */
    public int insertMedicalRegistration(MedicalRegistration medicalRegistration);

    /**
     * 修改挂号信息
     *
     * @param medicalRegistration 挂号信息
     * @return 结果
     */
    public int updateMedicalRegistration(MedicalRegistration medicalRegistration);

    /**
     * 删除挂号信息
     *
     * @param registrationId 挂号ID
     * @return 结果
     */
    public int deleteMedicalRegistrationById(Long registrationId);

    /**
     * 批量删除挂号信息
     *
     * @param registrationIds 需要删除的挂号ID数组
     * @return 结果
     */
    public int deleteMedicalRegistrationByIds(Long[] registrationIds);

    /**
     * 退号：置状态为已退号并释放号源
     *
     * @param registrationId 挂号ID
     * @return 结果
     */
    /**
     * 退号：置状态为已退号并释放号源
     *
     * @param registrationId 挂号ID
     * @return 结果
     */
    public int refund(Long registrationId);

    /**
     * 开始就诊：由挂号记录生成一条诊疗草稿，并置挂号状态为已就诊
     *
     * @param registrationId 挂号ID
     * @return 生成的诊疗记录ID
     */
    public Long startVisit(Long registrationId);
}
