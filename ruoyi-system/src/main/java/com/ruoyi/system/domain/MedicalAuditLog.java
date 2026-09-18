package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.io.Serializable;

/**
 * 医疗操作审计日志对象 medical_audit_log
 * 记录关键业务操作（开方 / 发药 / 退药 / 退号 / 改库存）的谁、何时、对什么、做了什么，用于医疗合规留痕。
 *
 * @author ruoyi
 */
public class MedicalAuditLog extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 业务模块（挂号 / 处方 / 发药 / 退药 / 退号） */
    private String module;

    /** 操作动作 */
    private String action;

    /** 业务主键 */
    private Long bizId;

    /** 业务单号（挂号号 / 处方号） */
    private String bizNo;

    /** 操作人（登录账号） */
    private String operator;

    /** 操作明细 */
    private String detail;

    /** 查询用：创建时间起 */
    private String beginTime;

    /** 查询用：创建时间止 */
    private String endTime;

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getBizId() {
        return bizId;
    }

    public void setBizId(Long bizId) {
        this.bizId = bizId;
    }

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
