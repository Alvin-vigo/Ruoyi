package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.MedicalDrug;
import com.ruoyi.system.mapper.MedicalDrugMapper;
import com.ruoyi.system.service.IMedicalDrugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 药品信息Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class MedicalDrugServiceImpl implements IMedicalDrugService {

    @Autowired
    private MedicalDrugMapper medicalDrugMapper;

    /**
     * 查询药品信息
     *
     * @param drugId 药品ID
     * @return 药品信息
     */
    @Override
    public MedicalDrug selectMedicalDrugById(Long drugId) {
        return medicalDrugMapper.selectMedicalDrugById(drugId);
    }

    /**
     * 查询药品信息列表
     *
     * @param medicalDrug 药品信息
     * @return 药品信息
     */
    @Override
    public List<MedicalDrug> selectMedicalDrugList(MedicalDrug medicalDrug) {
        return medicalDrugMapper.selectMedicalDrugList(medicalDrug);
    }

    /**
     * 新增药品信息
     *
     * @param medicalDrug 药品信息
     * @return 结果
     */
    @Override
    public int insertMedicalDrug(MedicalDrug medicalDrug) {
        return medicalDrugMapper.insertMedicalDrug(medicalDrug);
    }

    /**
     * 修改药品信息
     *
     * @param medicalDrug 药品信息
     * @return 结果
     */
    @Override
    public int updateMedicalDrug(MedicalDrug medicalDrug) {
        return medicalDrugMapper.updateMedicalDrug(medicalDrug);
    }

    /**
     * 批量删除药品信息
     *
     * @param drugIds 需要删除的药品ID
     * @return 结果
     */
    @Override
    public int deleteMedicalDrugByIds(Long[] drugIds) {
        return medicalDrugMapper.deleteMedicalDrugByIds(drugIds);
    }
}
