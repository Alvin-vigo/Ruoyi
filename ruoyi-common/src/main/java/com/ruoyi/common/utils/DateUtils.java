package com.ruoyi.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具类
 *
 * @author ruoyi
 */
public class DateUtils {

    /** 时间格式（yyyy-MM-dd HH:mm:ss） */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /** 日期格式（yyyy-MM-dd） */
    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    /**
     * 获取当前日期时间字符串
     *
     * @return 当前日期时间字符串
     */
    public static String getNowDate() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 获取当前日期字符串
     *
     * @return 当前日期字符串
     */
    public static String getTodayDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    /**
     * 获取指定格式的当前时间字符串
     *
     * @param format 时间格式
     * @return 当前时间字符串
     */
    public static String dateTimeNow(final String format) {
        return parseDateToStr(format, new Date());
    }

    /**
     * 格式化日期为字符串
     *
     * @param format 格式
     * @param date   日期
     * @return 格式化后的字符串
     */
    public static String parseDateToStr(final String format, final Date date) {
        return new SimpleDateFormat(format).format(date);
    }
}
