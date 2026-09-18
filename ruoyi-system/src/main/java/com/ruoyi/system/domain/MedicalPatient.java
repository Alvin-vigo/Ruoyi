package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 患者信息对象 medical_patient
 *
 * @author ruoyi
 */
public class MedicalPatient extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 患者ID */
    @Excel(name = "患者ID")
    private Long patientId;

    /** 患者姓名 */
    @Excel(name = "患者姓名")
    @NotBlank(message = "患者姓名不能为空")
    @Size(max = 50, message = "患者姓名长度不能超过50个字符")
    private String patientName;

    /** 患者性别（0男 1女 2未知） */
    @Excel(name = "患者性别", readConverterExp = "0=男,1=女,2=未知")
    private String gender;

    /** 出生日期 */
    @Excel(name = "出生日期", dateFormat = "yyyy-MM-dd")
    private Date birthDate;

    /** 身份证号 */
    @Excel(name = "身份证号")
    @Pattern(regexp = "^$|^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$",
            message = "身份证号格式不正确")
    private String idCard;

    /** 联系电话 */
    @Excel(name = "联系电话")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 住址 */
    @Excel(name = "住址")
    @Size(max = 200, message = "住址长度不能超过200个字符")
    private String address;

    /** 血型（A/B/O/AB） */
    @Excel(name = "血型")
    private String bloodType;

    /** 过敏史 */
    @Excel(name = "过敏史")
    @Size(max = 500, message = "过敏史长度不能超过500个字符")
    private String allergyHistory;

    /** 既往病史 */
    @Excel(name = "既往病史")
    @Size(max = 500, message = "既往病史长度不能超过500个字符")
    private String pastHistory;

    /** 患者状态（0正常 1住院 2出院 3死亡） */
    @Excel(name = "患者状态", readConverterExp = "0=正常,1=住院,2=出院,3=死亡")
    private String status;

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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getAllergyHistory() {
        return allergyHistory;
    }

    public void setAllergyHistory(String allergyHistory) {
        this.allergyHistory = allergyHistory;
    }

    public String getPastHistory() {
        return pastHistory;
    }

    public void setPastHistory(String pastHistory) {
        this.pastHistory = pastHistory;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
