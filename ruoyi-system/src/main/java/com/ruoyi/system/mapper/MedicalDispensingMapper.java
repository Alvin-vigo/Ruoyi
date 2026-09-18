package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MedicalDispensing;

import java.util.List;

/**
 * 发药记录Mapper接口
 *
 * @author ruoyi
 */
public interface MedicalDispensingMapper {

    /**
     * 查询发药记录
     *
     * @param dispensingId 发药ID
     * @return 发药记录
     */
    public MedicalDispensing selectMedicalDispensingById(Long dispensingId);

    /**
     * 查询发药记录列表
     *
     * @param medicalDispensing 发药记录
     * @return 发药记录集合
     */
    public List<MedicalDispensing> selectMedicalDispensingList(MedicalDispensing medicalDispensing);

    /**
     * 新增发药记录
     *
     * @param medicalDispensing 发药记录
     * @return 结果
     */
    public int insertMedicalDispensing(MedicalDispensing medicalDispensing);

    /**
     * 修改发药记录
     *
     * @param medicalDispensing 发药记录
     * @return 结果
     */
    public int updateMedicalDispensing(MedicalDispensing medicalDispensing);

    /**
     * 删除发药记录
     *
     * @param dispensingId 发药ID
     * @return 结果
     */
    public int deleteMedicalDispensingById(Long dispensingId);

    /**
     * 批量删除发药记录
     *
     * @param dispensingIds 需要删除的发药ID数组
     * @return 结果
     */
    public int deleteMedicalDispensingByIds(Long[] dispensingIds);

    /**
     * 按处方ID查询发药记录
     *
     * @param prescriptionId 处方ID
     * @return 发药记录集合
     */
    public List<MedicalDispensing> selectByPrescriptionId(Long prescriptionId);
}
