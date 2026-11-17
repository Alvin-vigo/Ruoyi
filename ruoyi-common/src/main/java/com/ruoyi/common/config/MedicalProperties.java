package com.ruoyi.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 医疗业务参数配置（前缀 medical，见 application.yml）
 *
 * <p>存在目的：把散落在各处的业务阈值收敛为单一真相源。
 * 在引入本类之前，「低库存阈值」存在三套口径——
 * 统计看板硬编码 50、药品预警接口默认 10、定时任务默认 10，
 * 导致看板上的「库存预警数」与药品页的预警列表数量对不上。
 *
 * <p>凡是业务阈值都应在此声明，禁止在 Service / Controller / Task 里写死数字。
 *
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "medical")
public class MedicalProperties {

    /** 低库存预警阈值：库存数量小于等于该值即视为需要补货 */
    private int lowStockThreshold = 10;

    /** 近效期预警天数：距有效期不足该天数即预警（仅提示，不拦截发药） */
    private int nearExpiryWarnDays = 90;

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public int getNearExpiryWarnDays() {
        return nearExpiryWarnDays;
    }

    public void setNearExpiryWarnDays(int nearExpiryWarnDays) {
        this.nearExpiryWarnDays = nearExpiryWarnDays;
    }
}
