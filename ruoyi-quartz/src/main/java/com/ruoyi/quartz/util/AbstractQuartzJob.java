package com.ruoyi.quartz.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.quartz.domain.SysJob;
import com.ruoyi.quartz.domain.SysJobLog;
import com.ruoyi.quartz.service.ISysJobLogService;

/**
 * 抽象 Quartz 执行类，统一处理任务执行前后逻辑与日志记录
 *
 * @author ruoyi
 */
public abstract class AbstractQuartzJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(AbstractQuartzJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SysJob sysJob = (SysJob) context.getMergedJobDataMap().get(ScheduleUtils.JOB_NAME);
        try {
            before(context, sysJob);
            doExecute(context, sysJob);
            after(context, sysJob, null);
        } catch (Exception e) {
            log.error("任务执行异常 - {}：", sysJob.getJobName(), e);
            after(context, sysJob, e);
        }
    }

    protected void before(JobExecutionContext context, SysJob sysJob) {
    }

    protected void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception {
        JobInvokeUtil.invokeMethod(sysJob);
    }

    protected void after(JobExecutionContext context, SysJob sysJob, Exception e) {
        try {
            SysJobLog jobLog = new SysJobLog();
            jobLog.setJobName(sysJob.getJobName());
            jobLog.setJobGroup(sysJob.getJobGroup());
            jobLog.setInvokeTarget(sysJob.getInvokeTarget());
            jobLog.setJobMessage("任务执行" + (e == null ? "成功" : "失败"));
            jobLog.setStatus(e == null ? "0" : "1");
            if (e != null) {
                jobLog.setExceptionInfo(ExceptionUtils.getStackTrace(e));
            }
            SpringUtils.getBean(ISysJobLogService.class).addJobLog(jobLog);
        } catch (Exception ex) {
            log.error("记录任务日志失败", ex);
        }
    }
}
