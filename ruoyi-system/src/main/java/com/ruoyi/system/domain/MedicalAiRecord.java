package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI 病历生成记录 medical_ai_record
 *
 * 设计说明：AI 生成的是"草稿"，医生确认修改后可采纳并写入正式诊疗表 medical_diagnosis。
 * 因此这里额外保留 AI 原文（ai_content）与医生定稿（doctor_content），
 * 便于论文中对比"AI初稿 vs 医生终稿"的差异，也避免 AI 内容直接污染正式病历。
 *
 * @author ruoyi
 */
public class MedicalAiRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    @Excel(name = "记录ID")
    private Long recordId;

    /** 患者ID（关联 medical_patient） */
    @Excel(name = "患者ID")
    private Long patientId;

    /** 患者姓名 */
    @Excel(name = "患者姓名")
    private String patientName;

    /** 年龄 */
    @Excel(name = "年龄")
    private Integer age;

    /** 性别 */
    @Excel(name = "性别")
    private String gender;

    /** 主诉 */
    @Excel(name = "主诉")
    private String chiefComplaint;

    /** AI 生成的病历原文 */
    private String aiContent;

    /** 医生修改后的内容 */
    private String doctorContent;

    /** 生成引擎：ollama / template（降级） */
    @Excel(name = "生成引擎", readConverterExp = "ollama=大模型,template=模板降级")
    private String engine;

    /** 模型名称（如 qwen2.5:7b） */
    @Excel(name = "模型")
    private String modelName;

    /** 是否降级输出（0否 1是） */
    private String degraded;

    /** 推理耗时（毫秒） */
    @Excel(name = "耗时ms")
    private Long elapsedMs;

    /** 状态（0草稿 1已采纳并写入诊疗记录 2已废弃） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=已采纳,2=已废弃")
    private String status;

    /** 采纳后对应的诊疗ID */
    private Long diagnosisId;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getAiContent() {
        return aiContent;
    }

    public void setAiContent(String aiContent) {
        this.aiContent = aiContent;
    }

    public String getDoctorContent() {
        return doctorContent;
    }

    public void setDoctorContent(String doctorContent) {
        this.doctorContent = doctorContent;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getDegraded() {
        return degraded;
    }

    public void setDegraded(String degraded) {
        this.degraded = degraded;
    }

    public Long getElapsedMs() {
        return elapsedMs;
    }

    public void setElapsedMs(Long elapsedMs) {
        this.elapsedMs = elapsedMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDiagnosisId() {
        return diagnosisId;
    }

    public void setDiagnosisId(Long diagnosisId) {
        this.diagnosisId = diagnosisId;
    }
}
