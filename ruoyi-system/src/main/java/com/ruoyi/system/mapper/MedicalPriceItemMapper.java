package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MedicalPriceItem;

import java.util.List;

/**
 * 医疗费用价目 数据访问层
 *
 * @author ruoyi
 */
public interface MedicalPriceItemMapper {

    MedicalPriceItem selectMedicalPriceItemById(Long itemId);

    /**
     * 按药品名称查启用中的药品价目（自动计价用）
     *
     * @param drugName 药品名称（与价目表 item_name 一致）
     * @return 价目对象
     */
    MedicalPriceItem selectPriceByDrugName(String drugName);

    List<MedicalPriceItem> selectMedicalPriceItemList(MedicalPriceItem medicalPriceItem);

    int insertMedicalPriceItem(MedicalPriceItem medicalPriceItem);

    int updateMedicalPriceItem(MedicalPriceItem medicalPriceItem);

    int deleteMedicalPriceItemById(Long itemId);

    int deleteMedicalPriceItemByIds(Long[] itemIds);
}
