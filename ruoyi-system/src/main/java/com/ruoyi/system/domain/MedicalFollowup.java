package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 患者随访记录对象 medical_followup
 *
 * @author ruoyi
 */
public class MedicalFollowup extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 随访ID */
    @Excel(name = "随访ID")
    private Long followupId;

    /** 患者ID */
    @Excel(name = "患者ID")
    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    /** 患者姓名（关联查询用） */
    @Excel(name = "患者姓名")
    private String patientName;

    /** 随访日期 */
    @Excel(name = "随访日期", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "随访日期不能为空")
    private Date followupDate;

    /** 随访方式（0电话 1微信 2上门） */
    @Excel(name = "随访方式", readConverterExp = "0=电话,1=微信,2=上门")
    @Size(max = 1, message = "随访方式长度不能超过1个字符")
    private String followupType;

    /** 随访内容 */
    @Excel(name = "随访内容")
    @Size(max = 1000, message = "随访内容长度不能超过1000个字符")
    private String content;

    /** 下次随访日期 */
    @Excel(name = "下次随访日期", dateFormat = "yyyy-MM-dd")
    private Date nextFollowupDate;

    /** 随访状态（0待随访 1已完成） */
    @Excel(name = "随访状态", readConverterExp = "0=待随访,1=已完成")
    private String status;

    /** 随访人 */
    @Excel(name = "随访人")
    @Size(max = 50, message = "随访人长度不能超过50个字符")
    private String followupPerson;

    /** 随访开始日期（查询用） */
    private String beginFollowupDate;

    /** 随访结束日期（查询用） */
    private String endFollowupDate;

    /** 快捷筛选标识（查询用：all/overdue/today/week/done） */
    private String quickFilter;

    /** 逾期天数（计算列：下次随访日期早于今天的天数，非逾期为 null） */
    private Integer overdueDays;

    public Long getFollowupId() {
        return followupId;
    }

    public void setFollowupId(Long followupId) {
        this.followupId = followupId;
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

    public Date getFollowupDate() {
        return followupDate;
    }

    public void setFollowupDate(Date followupDate) {
        this.followupDate = followupDate;
    }

    public String getFollowupType() {
        return followupType;
    }

    public void setFollowupType(String followupType) {
        this.followupType = followupType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getNextFollowupDate() {
        return nextFollowupDate;
    }

    public void setNextFollowupDate(Date nextFollowupDate) {
        this.nextFollowupDate = nextFollowupDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFollowupPerson() {
        return followupPerson;
    }

    public void setFollowupPerson(String followupPerson) {
        this.followupPerson = followupPerson;
    }

    public String getBeginFollowupDate() {
        return beginFollowupDate;
    }

    public void setBeginFollowupDate(String beginFollowupDate) {
        this.beginFollowupDate = beginFollowupDate;
    }

    public String getEndFollowupDate() {
        return endFollowupDate;
    }

    public void setEndFollowupDate(String endFollowupDate) {
        this.endFollowupDate = endFollowupDate;
    }

    public String getQuickFilter() {
        return quickFilter;
    }

    public void setQuickFilter(String quickFilter) {
        this.quickFilter = quickFilter;
    }

    public Integer getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(Integer overdueDays) {
        this.overdueDays = overdueDays;
    }
}
