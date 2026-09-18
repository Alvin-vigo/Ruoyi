package com.ruoyi.quartz.util;

import org.quartz.DisallowConcurrentExecution;

/**
 * 禁止并发执行任务（同一任务在上一次未结束时不会再次触发）
 *
 * @author ruoyi
 */
@DisallowConcurrentExecution
public class QuartzDisallowConcurrentExecution extends AbstractQuartzJob {
}
