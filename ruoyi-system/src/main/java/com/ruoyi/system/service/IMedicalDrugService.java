package com.ruoyi.system.service;

import com.ruoyi.system.domain.MedicalDrug;
import java.util.List;

/**
 * 药品信息Service接口
 *
 * @author ruoyi
 */
public interface IMedicalDrugService {

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
}
