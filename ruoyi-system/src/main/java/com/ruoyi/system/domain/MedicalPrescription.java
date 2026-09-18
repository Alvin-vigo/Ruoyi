package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * 处方信息对象 medical_prescription
 * 说明：patientAllergy 与 items 为业务冗余/子表字段，数据库无对应列，insert 时不写入。
 *
 * @author ruoyi
 */
public class MedicalPrescription extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 处方ID */
    private Long prescriptionId;

    /** 处方编号 */
    private String prescriptionNo;

    /** 挂号ID */
    private Long registrationId;

    /** 患者ID */
    private Long patientId;

    /** 患者姓名 */
    private String patientName;

    /** 医生姓名 */
    private String doctorName;

    /** 科室 */
    private String dept;

    /** 诊断 */
    private String diagnosis;

    /** 处方总金额 */
    private BigDecimal totalAmount;

    /** 处方状态（0待发药 1部分发药 2已发药 3已作废） */
    private String prescriptionStatus;

    /** 患者过敏史（冗余字段，数据库无对应列） */
    private String patientAllergy;

    /** 处方明细列表（子表，数据库无对应列） */
    private List<MedicalPrescriptionItem> items;

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getPrescriptionNo() {
        return prescriptionNo;
    }

    public void setPrescriptionNo(String prescriptionNo) {
        this.prescriptionNo = prescriptionNo;
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
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

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPrescriptionStatus() {
        return prescriptionStatus;
    }

    public void setPrescriptionStatus(String prescriptionStatus) {
        this.prescriptionStatus = prescriptionStatus;
    }

    public String getPatientAllergy() {
        return patientAllergy;
    }

    public void setPatientAllergy(String patientAllergy) {
        this.patientAllergy = patientAllergy;
    }

    public List<MedicalPrescriptionItem> getItems() {
        return items;
    }

    public void setItems(List<MedicalPrescriptionItem> items) {
        this.items = items;
    }
}
