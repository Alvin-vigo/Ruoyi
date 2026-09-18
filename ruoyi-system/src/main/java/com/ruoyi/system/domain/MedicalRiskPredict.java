package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 疾病风险预测记录 medical_risk_predict
 *
 * 保存每次预测的入参快照与结果，便于历史追溯与论文实验统计。
 *
 * @author ruoyi
 */
public class MedicalRiskPredict extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 预测记录ID */
    @Excel(name = "记录ID")
    private Long predictId;

    /** 患者ID（可空，支持不关联患者的离线测算） */
    @Excel(name = "患者ID")
    private Long patientId;

    /** 预测病种 */
    @Excel(name = "预测病种")
    private String diseaseName;

    /** 入参特征 JSON 快照，如 {"Glucose":148,...} */
    private String featureJson;

    /** 患病概率 0~1 */
    @Excel(name = "患病概率")
    private java.math.BigDecimal riskProb;

    /** 风险标签（0低风险 1高风险） */
    @Excel(name = "结论", readConverterExp = "0=低风险,1=高风险")
    private String resultLabel;

    /** 风险分层文案：低风险/中风险/高风险 */
    @Excel(name = "风险等级")
    private String riskLevel;

    /** 模型给出的健康建议 */
    private String advice;

    /** 判定阈值 */
    private java.math.BigDecimal threshold;

    /** 模型指标快照（accuracy/auc 等 JSON） */
    private String modelMetrics;

    public Long getPredictId() {
        return predictId;
    }

    public void setPredictId(Long predictId) {
        this.predictId = predictId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String getFeatureJson() {
        return featureJson;
    }

    public void setFeatureJson(String featureJson) {
        this.featureJson = featureJson;
    }

    public java.math.BigDecimal getRiskProb() {
        return riskProb;
    }

    public void setRiskProb(java.math.BigDecimal riskProb) {
        this.riskProb = riskProb;
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public void setResultLabel(String resultLabel) {
        this.resultLabel = resultLabel;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public java.math.BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(java.math.BigDecimal threshold) {
        this.threshold = threshold;
    }

    public String getModelMetrics() {
        return modelMetrics;
    }

    public void setModelMetrics(String modelMetrics) {
        this.modelMetrics = modelMetrics;
    }
}
