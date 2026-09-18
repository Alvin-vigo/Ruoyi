package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 排班信息对象 medical_schedule
 *
 * @author ruoyi
 */
public class MedicalSchedule extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 排班ID */
    private Long scheduleId;

    /** 科室 */
    private String dept;

    /** 医生姓名 */
    private String doctorName;

    /** 排班日期 */
    private Date scheduleDate;

    /** 时段（0上午 1下午 2晚上） */
    private String period;

    /** 号源总数 */
    private Integer totalNum;

    /** 已用号数 */
    private Integer usedNum;

    /** 状态（0正常 1停诊） */
    private String status;

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Date getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(Date scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Integer getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(Integer totalNum) {
        this.totalNum = totalNum;
    }

    public Integer getUsedNum() {
        return usedNum;
    }

    public void setUsedNum(Integer usedNum) {
        this.usedNum = usedNum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
