package com.ruoyi.quartz.util;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import com.ruoyi.quartz.domain.SysJob;

/**
 * 定时任务工具类
 *
 * @author ruoyi
 */
public class ScheduleUtils {
    public static final String JOB_NAME = "TASK_";

    /**
     * 获取 JobKey
     */
    public static JobKey getJobKey(Long jobId, String jobGroup) {
        return JobKey.jobKey(JOB_NAME + jobId, jobGroup);
    }

    /**
     * 获取 TriggerKey
     */
    public static TriggerKey getTriggerKey(Long jobId, String jobGroup) {
        return TriggerKey.triggerKey(JOB_NAME + jobId, jobGroup);
    }

    /**
     * 创建定时任务
     *
     * 说明：本项目 Quartz 使用 JDBC 持久化任务存储（LocalDataSourceJobStore），
     * QRTZ_* 表中的任务会跨应用重启保留。因此注册前必须先判断任务是否已存在，
     * 若存在则先删除再注册，避免重启时因 ObjectAlreadyExistsException 导致启动失败。
     */
    public static void createScheduleJob(Scheduler scheduler, SysJob job) throws SchedulerException {
        JobKey jobKey = getJobKey(job.getJobId(), job.getJobGroup());
        TriggerKey triggerKey = getTriggerKey(job.getJobId(), job.getJobGroup());

        // 幂等保护：已存在则先清理旧的任务与触发器，再重新注册
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }

        Class<? extends Job> jobClass = getQuartzJobClass(job);
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobKey).build();

        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
        cronScheduleBuilder = handleCronScheduleMisfirePolicy(job, cronScheduleBuilder);

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(cronScheduleBuilder).build();

        jobDetail.getJobDataMap().put(JOB_NAME, job);
        scheduler.scheduleJob(jobDetail, trigger);

        // 如果任务状态为暂停，则暂停任务
        if ("1".equals(job.getStatus())) {
            pauseJob(scheduler, job);
        }
    }

    /**
     * 根据并发标识获取任务执行类
     */
    private static Class<? extends Job> getQuartzJobClass(SysJob sysJob) {
        boolean isConcurrent = "0".equals(sysJob.getConcurrent());
        return isConcurrent ? QuartzJobExecution.class : QuartzDisallowConcurrentExecution.class;
    }

    /**
     * 暂停任务
     */
    public static void pauseJob(Scheduler scheduler, SysJob job) throws SchedulerException {
        scheduler.pauseJob(getJobKey(job.getJobId(), job.getJobGroup()));
    }

    /**
     * 恢复任务
     */
    public static void resumeJob(Scheduler scheduler, SysJob job) throws SchedulerException {
        scheduler.resumeJob(getJobKey(job.getJobId(), job.getJobGroup()));
    }

    /**
     * 立即执行一次任务
     */
    public static void run(Scheduler scheduler, SysJob job) throws SchedulerException {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(JOB_NAME, job);
        scheduler.triggerJob(getJobKey(job.getJobId(), job.getJobGroup()), dataMap);
    }

    /**
     * 删除任务
     */
    public static void deleteScheduleJob(Scheduler scheduler, SysJob job) throws SchedulerException {
        scheduler.deleteJob(getJobKey(job.getJobId(), job.getJobGroup()));
    }

    /**
     * 处理计划执行错误策略
     */
    private static CronScheduleBuilder handleCronScheduleMisfirePolicy(SysJob job, CronScheduleBuilder cb)
            throws SchedulerException {
        String misfirePolicy = job.getMisfirePolicy();
        if (misfirePolicy == null) {
            return cb;
        }
        // 与若依官方口径保持一致：0默认 1立即触发执行 2触发一次执行 3不触发立即执行
        switch (misfirePolicy) {
            case "0":
                return cb;
            case "1":
                return cb.withMisfireHandlingInstructionIgnoreMisfires();
            case "2":
                return cb.withMisfireHandlingInstructionFireAndProceed();
            case "3":
                return cb.withMisfireHandlingInstructionDoNothing();
            default:
                return cb;
        }
    }
}
