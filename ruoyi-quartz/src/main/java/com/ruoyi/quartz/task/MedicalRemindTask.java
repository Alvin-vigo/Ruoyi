package com.ruoyi.quartz.task;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.system.domain.MedicalDrug;
import com.ruoyi.system.domain.MedicalMessage;
import com.ruoyi.system.service.IMedicalDrugService;
import com.ruoyi.system.service.IMedicalMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 医疗业务集成示例定时任务：低库存扫描预警
 *
 * @author ruoyi
 */
@Component("medicalRemindTask")
public class MedicalRemindTask {

    private static final Logger log = LoggerFactory.getLogger(MedicalRemindTask.class);

    private static final String TYPE_LOW_STOCK = "1";
    private static final String DEFAULT_RECEIVER = "admin";

    /**
     * 扫描低库存药品（阈值可配，默认10），用于定时预警，并推送站内信
     *
     * @param params 可选数字参数，作为库存预警阈值
     */
    public void scanLowStock(String params) {
        int threshold = 10;
        try {
            if (params != null && !params.trim().isEmpty()) {
                threshold = Integer.parseInt(params.trim());
            }
        } catch (NumberFormatException e) {
            log.warn("低库存阈值解析失败，使用默认值 {}", threshold);
        }
        IMedicalDrugService drugService = SpringUtils.getBean(IMedicalDrugService.class);
        List<MedicalDrug> lowStockList = drugService.selectMedicalDrugLowStock(threshold);
        log.info("【定时任务】低库存扫描（阈值 {}）：命中 {} 个药品", threshold, lowStockList.size());

        if (lowStockList.isEmpty()) {
            return;
        }

        IMedicalMessageService messageService = SpringUtils.getBean(IMedicalMessageService.class);
        int pushed = 0;
        for (MedicalDrug drug : lowStockList) {
            // 去重：同一药品已存在未读预警则不重复推送
            if (messageService.existsUnreadByTypeAndRelated(TYPE_LOW_STOCK, drug.getDrugId(), DEFAULT_RECEIVER)) {
                continue;
            }
            MedicalMessage message = new MedicalMessage();
            message.setTitle("药品低库存预警");
            message.setContent(String.format("药品【%s】当前库存 %d，低于预警阈值 %d，请及时补货。",
                    drug.getDrugName(), drug.getStock(), threshold));
            message.setType(TYPE_LOW_STOCK);
            message.setReceiver(DEFAULT_RECEIVER);
            message.setRelatedId(drug.getDrugId());
            messageService.insertMessage(message);
            pushed++;
        }
        log.info("【定时任务】低库存预警站内信推送：成功 {} 条", pushed);
    }
}
