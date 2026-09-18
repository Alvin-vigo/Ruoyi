package com.ruoyi.system.domain.dto;

/**
 * 模块① 智能病历生成 请求参数
 *
 * @author ruoyi
 */
public class AiRecordRequest {

    /** 患者ID（可空，支持不关联档案的临时书写） */
    private Long patientId;

    /** 患者姓名 */
    private String patientName;

    /** 年龄 */
    private Integer age;

    /** 性别：男/女 */
    private String gender;

    /** 主诉 */
    private String chiefComplaint;

    /** 过敏史（从患者档案带入，生成时规避风险用药） */
    private String allergyHistory;

    /** 既往病史（从患者档案带入，个性化生成） */
    private String pastHistory;

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
}
