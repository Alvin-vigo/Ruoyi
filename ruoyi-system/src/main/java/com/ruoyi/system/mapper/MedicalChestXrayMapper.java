package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MedicalChestXray;

import java.util.List;

/**
 * 胸片 AI 初筛记录 数据访问层
 *
 * @author ruoyi
 */
public interface MedicalChestXrayMapper {

    MedicalChestXray selectMedicalChestXrayById(Long xrayId);

    List<MedicalChestXray> selectMedicalChestXrayByIds(Long[] ids);

    List<MedicalChestXray> selectMedicalChestXrayList(MedicalChestXray medicalChestXray);

    int insertMedicalChestXray(MedicalChestXray medicalChestXray);

    int deleteMedicalChestXrayById(Long xrayId);

    int deleteMedicalChestXrayByIds(Long[] ids);
}
