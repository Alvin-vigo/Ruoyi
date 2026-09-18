package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 药品信息对象 medical_drug
 *
 * @author ruoyi
 */
public class MedicalDrug extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 药品ID */
    @Excel(name = "药品ID")
    private Long drugId;

    /** 药品名称 */
    @Excel(name = "药品名称")
    @NotBlank(message = "药品名称不能为空")
    @Size(max = 100, message = "药品名称长度不能超过100个字符")
    private String drugName;

    /** 药品编码 */
    @Excel(name = "药品编码")
    @Size(max = 50, message = "药品编码长度不能超过50个字符")
    private String drugCode;

    /** 药品分类（0西药 1中药 2中成药 3生物制品） */
    @Excel(name = "药品分类", readConverterExp = "0=西药,1=中药,2=中成药,3=生物制品")
    private String category;

    /** 规格 */
    @Excel(name = "规格")
    @Size(max = 100, message = "规格长度不能超过100个字符")
    private String spec;

    /** 生产厂家 */
    @Excel(name = "生产厂家")
    @Size(max = 200, message = "生产厂家长度不能超过200个字符")
    private String manufacturer;

    /** 包装单位 */
    @Excel(name = "包装单位")
    @Size(max = 20, message = "包装单位长度不能超过20个字符")
    private String unit;

    /** 进价（元） */
    @Excel(name = "进价（元）")
    @DecimalMin(value = "0.00", message = "进价不能为负数")
    private BigDecimal purchasePrice;

    /** 售价（元） */
    @Excel(name = "售价（元）")
    @DecimalMin(value = "0.00", message = "售价不能为负数")
    private BigDecimal salePrice;

    /** 库存数量 */
    @Excel(name = "库存数量")
    @PositiveOrZero(message = "库存数量不能为负数")
    private Integer stock;

    /** 生产日期 */
    @Excel(name = "生产日期", dateFormat = "yyyy-MM-dd")
    private Date productionDate;

    /** 有效期至 */
    @Excel(name = "有效期至", dateFormat = "yyyy-MM-dd")
    private Date expiryDate;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

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

    public String getDrugCode() {
        return drugCode;
    }

    public void setDrugCode(String drugCode) {
        this.drugCode = drugCode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Date getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(Date productionDate) {
        this.productionDate = productionDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
