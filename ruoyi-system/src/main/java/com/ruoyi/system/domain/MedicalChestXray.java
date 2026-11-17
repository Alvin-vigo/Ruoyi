package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 胸片 AI 初筛记录 medical_chest_xray（三分类：COVID-19 / NORMAL / Viral Pneumonia）
 *
 * @author ruoyi
 */
public class MedicalChestXray extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    @Excel(name = "记录ID")
    private Long xrayId;

    /** 患者ID */
    @Excel(name = "患者ID")
    private Long patientId;

    /** 影像存储路径/URL */
    @Excel(name = "影像路径")
    private String imgUrl;

    /** 判定结果文案：COVID-19 / NORMAL / Viral Pneumonia */
    @Excel(name = "判定结果")
    private String predictResult;

    /** 置信度（取判定类别的概率） */
    @Excel(name = "置信度")
    private java.math.BigDecimal confidence;

    /** 三分类全概率（JSON：{类名:概率}），用于历史追溯与演示 */
    private String probsJson;

    /** 推理设备 cuda / cpu */
    private String device;

    /** 推理耗时（毫秒） */
    @Excel(name = "耗时ms")
    private Long elapsedMs;

    public Long getXrayId() {
        return xrayId;
    }

    public void setXrayId(Long xrayId) {
        this.xrayId = xrayId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getPredictResult() {
        return predictResult;
    }

    public void setPredictResult(String predictResult) {
        this.predictResult = predictResult;
    }

    public java.math.BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(java.math.BigDecimal confidence) {
        this.confidence = confidence;
    }

    public String getProbsJson() {
        return probsJson;
    }

    public void setProbsJson(String probsJson) {
        this.probsJson = probsJson;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public Long getElapsedMs() {
        return elapsedMs;
    }

    public void setElapsedMs(Long elapsedMs) {
        this.elapsedMs = elapsedMs;
    }
}
