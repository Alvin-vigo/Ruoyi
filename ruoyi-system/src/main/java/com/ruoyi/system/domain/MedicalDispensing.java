package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 发药记录对象 medical_dispensing
 *
 * @author ruoyi
 */
public class MedicalDispensing extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 发药ID */
    private Long dispensingId;

    /** 处方ID */
    private Long prescriptionId;

    /** 明细ID */
    private Long itemId;

    /** 药品ID */
    private Long drugId;

    /** 药品名称 */
    private String drugName;

    /** 发药数量 */
    private Integer quantity;

    /** 操作人 */
    private String operator;

    /** 发药时间 */
    private Date dispensingTime;

    public Long getDispensingId() {
        return dispensingId;
    }

    public void setDispensingId(Long dispensingId) {
        this.dispensingId = dispensingId;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getDrugId() {
        return drugId;
    }

    public void setDrugId(Long drugId) {
        this.drugId = drugId;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Date getDispensingTime() {
        return dispensingTime;
    }

    public void setDispensingTime(Date dispensingTime) {
        this.dispensingTime = dispensingTime;
    }

    /** 状态（0正常 1已退） */
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
