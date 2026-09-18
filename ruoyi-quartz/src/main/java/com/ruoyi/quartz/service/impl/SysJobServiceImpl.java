package com.ruoyi.quartz.service.impl;

import com.ruoyi.quartz.domain.SysJob;
import com.ruoyi.quartz.mapper.SysJobMapper;
import com.ruoyi.quartz.service.ISysJobService;
import com.ruoyi.quartz.util.ScheduleUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 定时任务调度信息Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SysJobServiceImpl implements ISysJobService {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private SysJobMapper jobMapper;

    /**
     * 应用启动时，将 sys_job 表中所有任务注册到调度器。
     * 依赖 ScheduleUtils.createScheduleJob 的幂等保护（已存在则先删再注册），
     * 因此重复启动不会因持久化的 QRTZ_* 任务而抛 ObjectAlreadyExistsException。
     */
    @PostConstruct
    public void init() {
        try {
            List<SysJob> jobList = jobMapper.selectJobList(new SysJob());
            for (SysJob job : jobList) {
                ScheduleUtils.createScheduleJob(scheduler, job);
            }
        } catch (Exception e) {
            // 启动时任务加载失败不应阻断整个应用启动，仅记录日志
            org.slf4j.LoggerFactory.getLogger(SysJobServiceImpl.class)
                    .error("定时任务初始化加载失败", e);
        }
    }

    /**
     * 查询定时任务调度信息列表
     */
    @Override
    public List<SysJob> selectJobList(SysJob job) {
        return jobMapper.selectJobList(job);
    }

    /**
     * 查询定时任务调度信息
     */
    @Override
    public SysJob selectJobById(Long jobId) {
        return jobMapper.selectJobById(jobId);
    }

    /**
     * 新增定时任务调度信息（同时创建调度任务）
     */
    @Override
    public int insertJob(SysJob job) throws Exception {
        int rows = jobMapper.insertJob(job);
        if (rows > 0) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
        return rows;
    }

    /**
     * 修改定时任务调度信息（同时重新调度）
     */
    @Override
    public int updateJob(SysJob job) throws Exception {
        SysJob oldJob = jobMapper.selectJobById(job.getJobId());
        int rows = jobMapper.updateJob(job);
        if (rows > 0) {
            ScheduleUtils.deleteScheduleJob(scheduler, oldJob);
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
        return rows;
    }

    /**
     * 批量删除定时任务调度信息（同时删除调度任务）
     */
    @Override
    public int deleteJobByIds(Long[] jobIds) throws Exception {
        for (Long jobId : jobIds) {
            SysJob job = jobMapper.selectJobById(jobId);
            ScheduleUtils.deleteScheduleJob(scheduler, job);
        }
        return jobMapper.deleteJobByIds(jobIds);
    }

    /**
     * 修改任务状态（暂停 / 恢复）
     */
    @Override
    public int changeStatus(SysJob job) throws Exception {
        if ("0".equals(job.getStatus())) {
            resumeJob(job);
        } else if ("1".equals(job.getStatus())) {
            pauseJob(job);
        }
        return 1;
    }

    private void resumeJob(SysJob job) throws SchedulerException {
        ScheduleUtils.resumeJob(scheduler, job);
        job.setStatus("0");
        jobMapper.updateJob(job);
    }

    private void pauseJob(SysJob job) throws SchedulerException {
        ScheduleUtils.pauseJob(scheduler, job);
        job.setStatus("1");
        jobMapper.updateJob(job);
    }

    /**
     * 立即执行一次任务
     */
    @Override
    public void run(SysJob job) throws Exception {
        // 前端"执行一次"可能只传 jobId，这里按 jobId 回查数据库，
        // 保证 invokeTarget / cron 等字段完整，避免任务执行时反射拿到空目标
        if (job != null && job.getJobId() != null) {
            SysJob dbJob = jobMapper.selectJobById(job.getJobId());
            if (dbJob != null) {
                job = dbJob;
            }
        }
        ScheduleUtils.run(scheduler, job);
    }
}
