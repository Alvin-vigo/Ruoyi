package com.ruoyi.controller;

import com.ruoyi.system.domain.MedicalPrescriptionItem;

import java.util.List;

/**
 * 发药请求对象
 *
 * @author ruoyi
 */
public class DispenseRequest {

    /** 处方ID */
    private Long prescriptionId;

    /** 本次发药明细（含 itemId 与 quantity） */
    private List<MedicalPrescriptionItem> items;

    /** 操作人 */
    private String operator;

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public List<MedicalPrescriptionItem> getItems() {
        return items;
    }

    public void setItems(List<MedicalPrescriptionItem> items) {
        this.items = items;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
