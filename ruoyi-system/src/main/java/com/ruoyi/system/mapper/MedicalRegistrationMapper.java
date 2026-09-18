package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MedicalRegistration;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 挂号信息Mapper接口
 *
 * @author ruoyi
 */
public interface MedicalRegistrationMapper {

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
     * 新增挂号信息
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
     * 更新挂号状态
     *
     * @param id 挂号ID
     * @param status 状态
     * @return 结果
     */
    public int updateRegStatus(@Param("id") Long id, @Param("status") String status);
}
