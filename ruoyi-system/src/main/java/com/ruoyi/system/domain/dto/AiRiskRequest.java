package com.ruoyi.system.domain.dto;

import java.util.List;

/**
 * 模块② 疾病风险预测 请求参数
 *
 * 注意：features 的顺序必须与 AI 服务 models/risk_features.json 中记录的一致，
 * 前端应先调用 /medical/ai/riskFeatures 获取真实顺序再拼装。
 *
 * @author ruoyi
 */
public class AiRiskRequest {

    /** 患者ID，可空 */
    private Long patientId;

    /** 预测病种，默认糖尿病 */
    private String diseaseName;

    /** 特征值数组，顺序必须严格匹配训练时的特征顺序 */
    private List<Double> features;

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

    public List<Double> getFeatures() {
        return features;
    }

    public void setFeatures(List<Double> features) {
        this.features = features;
    }
}
