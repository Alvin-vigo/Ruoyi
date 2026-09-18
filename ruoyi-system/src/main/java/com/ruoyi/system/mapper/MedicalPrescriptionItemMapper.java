package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MedicalPrescriptionItem;

import java.util.List;

/**
 * 处方明细Mapper接口
 *
 * @author ruoyi
 */
public interface MedicalPrescriptionItemMapper {

    /**
     * 查询处方明细
     *
     * @param itemId 明细ID
     * @return 处方明细
     */
    public MedicalPrescriptionItem selectMedicalPrescriptionItemById(Long itemId);

    /**
     * 查询处方明细列表
     *
     * @param medicalPrescriptionItem 处方明细
     * @return 处方明细集合
     */
    public List<MedicalPrescriptionItem> selectMedicalPrescriptionItemList(MedicalPrescriptionItem medicalPrescriptionItem);

    /**
     * 新增处方明细
     *
     * @param medicalPrescriptionItem 处方明细
     * @return 结果
     */
    public int insertMedicalPrescriptionItem(MedicalPrescriptionItem medicalPrescriptionItem);

    /**
     * 修改处方明细
     *
     * @param medicalPrescriptionItem 处方明细
     * @return 结果
     */
    public int updateMedicalPrescriptionItem(MedicalPrescriptionItem medicalPrescriptionItem);

    /**
     * 删除处方明细
     *
     * @param itemId 明细ID
     * @return 结果
     */
    public int deleteMedicalPrescriptionItemById(Long itemId);

    /**
     * 批量删除处方明细
     *
     * @param itemIds 需要删除的明细ID数组
     * @return 结果
     */
    public int deleteMedicalPrescriptionItemByIds(Long[] itemIds);

    /**
     * 根据处方ID查询明细列表
     *
     * @param prescriptionId 处方ID
     * @return 明细集合
     */
    public List<MedicalPrescriptionItem> selectByPrescriptionId(Long prescriptionId);

    /**
     * 更新已发药数量
     *
     * @param item 含 itemId 与 dispensedQuantity
     * @return 结果
     */
    public int updateDispensed(MedicalPrescriptionItem item);
}
