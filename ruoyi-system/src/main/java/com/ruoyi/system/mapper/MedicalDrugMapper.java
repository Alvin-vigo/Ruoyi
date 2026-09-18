package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MedicalDrug;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 药品信息Mapper接口
 *
 * @author ruoyi
 */
public interface MedicalDrugMapper {

    /**
     * 查询药品信息
     *
     * @param drugId 药品ID
     * @return 药品信息
     */
    public MedicalDrug selectMedicalDrugById(Long drugId);

    /**
     * 查询药品信息列表
     *
     * @param medicalDrug 药品信息
     * @return 药品信息集合
     */
    public List<MedicalDrug> selectMedicalDrugList(MedicalDrug medicalDrug);

    /**
     * 新增药品信息
     *
     * @param medicalDrug 药品信息
     * @return 结果
     */
    public int insertMedicalDrug(MedicalDrug medicalDrug);

    /**
     * 修改药品信息
     *
     * @param medicalDrug 药品信息
     * @return 结果
     */
    public int updateMedicalDrug(MedicalDrug medicalDrug);

    /**
     * 删除药品信息
     *
     * @param drugId 药品ID
     * @return 结果
     */
    public int deleteMedicalDrugById(Long drugId);

    /**
     * 批量删除药品信息
     *
     * @param drugIds 需要删除的药品ID数组
     * @return 结果
     */
    public int deleteMedicalDrugByIds(Long[] drugIds);

    /**
     * 查询低库存药品（库存数量小于等于阈值）
     *
     * @param threshold 库存预警阈值
     * @return 药品信息集合
     */
    public List<MedicalDrug> selectMedicalDrugLowStock(int threshold);

    /**
     * 查询近效期药品（有效期至距今天数小于等于预警天数）
     *
     * @param warnDays 效期预警天数
     * @return 药品信息集合
     */
    public List<MedicalDrug> selectMedicalDrugNearExpiry(int warnDays);

    /**
     * 扣减库存（原子操作），库存不足时返回0
     *
     * @param drugId 药品ID
     * @param quantity 扣减数量
     * @return 受影响行数，0表示库存不足
     */
    public int decrementStock(@Param("drugId") Long drugId, @Param("quantity") int quantity);

    /**
     * 回加库存（原子操作，用于退药逆向流程）
     *
     * @param drugId 药品ID
     * @param quantity 回加数量
     * @return 受影响行数
     */
    public int incrementStock(@Param("drugId") Long drugId, @Param("quantity") int quantity);
}
