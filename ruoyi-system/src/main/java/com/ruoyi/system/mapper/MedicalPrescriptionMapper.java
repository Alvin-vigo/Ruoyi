package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MedicalPrescription;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 处方信息Mapper接口
 *
 * @author ruoyi
 */
public interface MedicalPrescriptionMapper {

    /**
     * 查询处方信息
     *
     * @param prescriptionId 处方ID
     * @return 处方信息
     */
    public MedicalPrescription selectMedicalPrescriptionById(Long prescriptionId);

    /**
     * 查询处方信息列表
     *
     * @param medicalPrescription 处方信息
     * @return 处方信息集合
     */
    public List<MedicalPrescription> selectMedicalPrescriptionList(MedicalPrescription medicalPrescription);

    /**
     * 新增处方信息
     *
     * @param medicalPrescription 处方信息
     * @return 结果
     */
    public int insertMedicalPrescription(MedicalPrescription medicalPrescription);

    /**
     * 修改处方信息
     *
     * @param medicalPrescription 处方信息
     * @return 结果
     */
    public int updateMedicalPrescription(MedicalPrescription medicalPrescription);

    /**
     * 删除处方信息
     *
     * @param prescriptionId 处方ID
     * @return 结果
     */
    public int deleteMedicalPrescriptionById(Long prescriptionId);

    /**
     * 批量删除处方信息
     *
     * @param prescriptionIds 需要删除的处方ID数组
     * @return 结果
     */
    public int deleteMedicalPrescriptionByIds(Long[] prescriptionIds);

    /**
     * 更新处方状态
     *
     * @param id 处方ID
     * @param status 状态
     * @return 结果
     */
    public int updatePrescriptionStatus(@Param("id") Long id, @Param("status") String status);
}
