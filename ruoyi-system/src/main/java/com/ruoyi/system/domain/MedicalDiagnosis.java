package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 诊疗信息对象 medical_diagnosis
 *
 * @author ruoyi
 */
public class MedicalDiagnosis extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 诊疗ID */
    private Long diagnosisId;

    /** 患者ID */
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    /** 患者姓名（关联查询用） */
    private String patientName;

    /** 就诊日期 */
    @NotNull(message = "就诊日期不能为空")
    private Date visitDate;

    /** 科室 */
    @NotBlank(message = "就诊科室不能为空")
    @Size(max = 50, message = "科室名称长度不能超过50个字符")
    private String department;

    /** 主治医生 */
    @NotBlank(message = "主治医生不能为空")
    @Size(max = 50, message = "医生姓名长度不能超过50个字符")
    private String doctorName;

    /** 主诉 */
    @Size(max = 500, message = "主诉长度不能超过500个字符")
    private String chiefComplaint;

    /** 现病史 */
    @Size(max = 1000, message = "现病史长度不能超过1000个字符")
    private String presentIllness;

    /** 诊断结果 */
    @NotBlank(message = "诊断结果不能为空")
    @Size(max = 500, message = "诊断结果长度不能超过500个字符")
    private String diagnosisResult;

    /** 处方/治疗方案 */
    @Size(max = 1000, message = "治疗方案长度不能超过1000个字符")
    private String treatmentPlan;

    /** 诊疗费用 */
    private BigDecimal cost;

    /** 诊疗状态（0待就诊 1就诊中 2已完成） */
    private String status;

    /** 就诊开始日期（查询用） */
    private String beginVisitDate;

    /** 就诊结束日期（查询用） */
    private String endVisitDate;

    public String getBeginVisitDate() {
        return beginVisitDate;
    }

    public void setBeginVisitDate(String beginVisitDate) {
        this.beginVisitDate = beginVisitDate;
    }

    public String getEndVisitDate() {
        return endVisitDate;
    }

    public void setEndVisitDate(String endVisitDate) {
        this.endVisitDate = endVisitDate;
    }

    public Long getDiagnosisId() {
        return diagnosisId;
    }

    public void setDiagnosisId(Long diagnosisId) {
        this.diagnosisId = diagnosisId;
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

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getPresentIllness() {
        return presentIllness;
    }

    public void setPresentIllness(String presentIllness) {
        this.presentIllness = presentIllness;
    }

    public String getDiagnosisResult() {
        return diagnosisResult;
    }

    public void setDiagnosisResult(String diagnosisResult) {
        this.diagnosisResult = diagnosisResult;
    }

    public String getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(String treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
