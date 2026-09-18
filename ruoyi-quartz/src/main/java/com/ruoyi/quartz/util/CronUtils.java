package com.ruoyi.quartz.util;

import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;

/**
 * cron 表达式工具类
 *
 * @author ruoyi
 */
public class CronUtils {

    /**
     * 校验 cron 表达式是否有效
     */
    public static boolean isValid(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    /**
     * 获取校验失败信息
     */
    public static String getInvalidMessage(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return null;
        } catch (ParseException e) {
            return e.getMessage();
        }
    }

    /**
     * 获取下一次执行时间
     */
    public static Date getNextExecution(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            return cron.getNextValidTimeAfter(new Date());
        } catch (ParseException e) {
            return null;
        }
    }
}
