package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 医疗费用价目对象 medical_price_item
 *
 * @author ruoyi
 */
public class MedicalPriceItem extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 价目ID */
    @Excel(name = "价目ID")
    private Long itemId;

    /** 项目类别（挂号费/诊查费/检查费/化验费/治疗费/手术费/护理费/床位费/药品/其它） */
    @Excel(name = "项目类别")
    @NotBlank(message = "项目类别不能为空")
    private String itemType;

    /** 项目名称 */
    @Excel(name = "项目名称")
    @NotBlank(message = "项目名称不能为空")
    private String itemName;

    /** 规格/说明 */
    @Excel(name = "规格说明")
    private String spec;

    /** 计价单位（次/日/盒/瓶/套…） */
    @Excel(name = "计价单位")
    private String unit;

    /** 价格（元） */
    @Excel(name = "价格", cellType = Excel.ColumnType.NUMERIC, prompt = "单位：元")
    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    /** 医保标记（0自费 1医保） */
    @Excel(name = "医保标记", readConverterExp = "0=自费,1=医保")
    private String insuranceFlag;

    /** 状态（0启用 1停用） */
    @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
    private String status;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getInsuranceFlag() {
        return insuranceFlag;
    }

    public void setInsuranceFlag(String insuranceFlag) {
        this.insuranceFlag = insuranceFlag;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
