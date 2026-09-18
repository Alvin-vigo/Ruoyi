package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MedicalDrug;
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
}
