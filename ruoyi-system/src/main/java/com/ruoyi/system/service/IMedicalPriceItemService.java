package com.ruoyi.system.service;

import com.ruoyi.system.domain.MedicalPriceItem;

import java.util.List;

/**
 * 医疗费用价目 业务层接口
 *
 * @author ruoyi
 */
public interface IMedicalPriceItemService {

    MedicalPriceItem selectMedicalPriceItemById(Long itemId);

    List<MedicalPriceItem> selectMedicalPriceItemList(MedicalPriceItem medicalPriceItem);

    int insertMedicalPriceItem(MedicalPriceItem medicalPriceItem);

    int updateMedicalPriceItem(MedicalPriceItem medicalPriceItem);

    int deleteMedicalPriceItemByIds(Long[] itemIds);
}
