package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * 处方明细对象 medical_prescription_item
 *
 * @author ruoyi
 */
public class MedicalPrescriptionItem extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long itemId;

    /** 处方ID */
    private Long prescriptionId;

    /** 药品ID */
    private Long drugId;

    /** 药品名称 */
    private String drugName;

    /** 规格 */
    private String spec;

    /** 数量 */
    private Integer quantity;

    /** 单价 */
    private BigDecimal price;

    /** 用法用量 */
    private String dosage;

    /** 已发药数量 */
    private Integer dispensedQuantity;

    /** 过敏提示 */
    private String allergyWarning;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
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

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public Integer getDispensedQuantity() {
        return dispensedQuantity;
    }

    public void setDispensedQuantity(Integer dispensedQuantity) {
        this.dispensedQuantity = dispensedQuantity;
    }

    public String getAllergyWarning() {
        return allergyWarning;
    }

    public void setAllergyWarning(String allergyWarning) {
        this.allergyWarning = allergyWarning;
    }
}
