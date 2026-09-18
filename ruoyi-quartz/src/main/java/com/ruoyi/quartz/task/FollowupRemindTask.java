package com.ruoyi.quartz.task;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.system.domain.MedicalFollowup;
import com.ruoyi.system.domain.MedicalMessage;
import com.ruoyi.system.service.IMedicalFollowupService;
import com.ruoyi.system.service.IMedicalMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 患者随访定时提醒任务：扫描逾期未完成（待随访且下次随访日期早于今天）的随访记录，
 * 向随访人推送站内信提醒。复用刚启用好的 Quartz 调度框架。
 *
 * 调用目标字符串示例：followupRemindTask.scanOverdueFollowup
 *
 * @author ruoyi
 */
@Component("followupRemindTask")
public class FollowupRemindTask {

    private static final Logger log = LoggerFactory.getLogger(FollowupRemindTask.class);

    private static final String TYPE_OVERDUE = "2";
    private static final String DEFAULT_RECEIVER = "admin";

    /**
     * 扫描逾期未完成随访并推送站内信提醒
     */
    public void scanOverdueFollowup() {
        IMedicalFollowupService followupService = SpringUtils.getBean(IMedicalFollowupService.class);
        IMedicalMessageService messageService = SpringUtils.getBean(IMedicalMessageService.class);

        List<MedicalFollowup> overdueList = followupService.selectOverdueFollowupList();
        if (overdueList == null || overdueList.isEmpty()) {
            log.info("【定时任务】随访逾期扫描：当前无逾期未完成随访");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int pushed = 0;
        for (MedicalFollowup f : overdueList) {
            // 随访人仅用于文案展示；消息接收人必须是系统登录用户名，否则消息中心按用户名过滤时收不到。
            // 生产环境可将 followup_person 与 sys_user 做映射后再决定 receiver。
            String personLabel = (f.getFollowupPerson() != null && !f.getFollowupPerson().trim().isEmpty())
                    ? f.getFollowupPerson().trim() : DEFAULT_RECEIVER;
            String receiver = DEFAULT_RECEIVER;

            // 重推规则：同一随访（业务ID）对同一接收人只推送一次提醒，避免每次扫描都重复推送。
            // 随访完成后（status 变更）后续不再命中逾期列表，自然停止；若需周期性提醒可改为按时间窗去重。
            if (messageService.existsByTypeAndRelated(TYPE_OVERDUE, f.getFollowupId(), receiver)) {
                continue;
            }

            MedicalMessage message = new MedicalMessage();
            message.setTitle("随访逾期提醒");
            String nextDate = f.getNextFollowupDate() != null ? sdf.format(f.getNextFollowupDate()) : "未知";
            String patient = f.getPatientName() != null ? f.getPatientName() : ("患者#" + f.getPatientId());
            message.setContent(String.format("患者【%s】计划于 %s 进行的随访（随访人：%s）已逾期未完成，请尽快跟进。",
                    patient, nextDate, personLabel));
            message.setType(TYPE_OVERDUE);
            message.setReceiver(receiver);
            message.setRelatedId(f.getFollowupId());
            messageService.insertMessage(message);
            pushed++;
        }
        log.info("【定时任务】随访逾期扫描：命中 {} 条，成功推送站内信 {} 条", overdueList.size(), pushed);
    }
}
