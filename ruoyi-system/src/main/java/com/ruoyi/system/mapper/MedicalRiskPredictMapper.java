package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MedicalRiskPredict;

import java.util.List;

/**
 * 疾病风险预测记录 数据访问层
 *
 * @author ruoyi
 */
public interface MedicalRiskPredictMapper {

    MedicalRiskPredict selectMedicalRiskPredictById(Long predictId);

    List<MedicalRiskPredict> selectMedicalRiskPredictList(MedicalRiskPredict medicalRiskPredict);

    int insertMedicalRiskPredict(MedicalRiskPredict medicalRiskPredict);

    int deleteMedicalRiskPredictById(Long predictId);

    int deleteMedicalRiskPredictByIds(Long[] ids);
}
