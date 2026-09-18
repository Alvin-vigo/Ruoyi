package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.MedicalPriceItem;
import com.ruoyi.system.mapper.MedicalPriceItemMapper;
import com.ruoyi.system.service.IMedicalPriceItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 医疗费用价目 业务层实现
 *
 * @author ruoyi
 */
@Service
public class MedicalPriceItemServiceImpl implements IMedicalPriceItemService {

    @Autowired
    private MedicalPriceItemMapper medicalPriceItemMapper;

    @Override
    public MedicalPriceItem selectMedicalPriceItemById(Long itemId) {
        return medicalPriceItemMapper.selectMedicalPriceItemById(itemId);
    }

    @Override
    public List<MedicalPriceItem> selectMedicalPriceItemList(MedicalPriceItem medicalPriceItem) {
        return medicalPriceItemMapper.selectMedicalPriceItemList(medicalPriceItem);
    }

    @Override
    public int insertMedicalPriceItem(MedicalPriceItem medicalPriceItem) {
        return medicalPriceItemMapper.insertMedicalPriceItem(medicalPriceItem);
    }

    @Override
    public int updateMedicalPriceItem(MedicalPriceItem medicalPriceItem) {
        return medicalPriceItemMapper.updateMedicalPriceItem(medicalPriceItem);
    }

    @Override
    public int deleteMedicalPriceItemByIds(Long[] itemIds) {
        return medicalPriceItemMapper.deleteMedicalPriceItemByIds(itemIds);
    }
}
