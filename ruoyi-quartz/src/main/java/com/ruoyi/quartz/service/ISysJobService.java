package com.ruoyi.quartz.service;

import com.ruoyi.quartz.domain.SysJob;

import java.util.List;

/**
 * 定时任务调度信息Service接口
 *
 * @author ruoyi
 */
public interface ISysJobService {

    /**
     * 查询定时任务调度信息列表
     */
    public List<SysJob> selectJobList(SysJob job);

    /**
     * 查询定时任务调度信息
     */
    public SysJob selectJobById(Long jobId);

    /**
     * 新增定时任务调度信息（同时创建调度任务）
     */
    public int insertJob(SysJob job) throws Exception;

    /**
     * 修改定时任务调度信息（同时重新调度）
     */
    public int updateJob(SysJob job) throws Exception;

    /**
     * 批量删除定时任务调度信息（同时删除调度任务）
     */
    public int deleteJobByIds(Long[] jobIds) throws Exception;

    /**
     * 修改任务状态（暂停 / 恢复）
     */
    public int changeStatus(SysJob job) throws Exception;

    /**
     * 立即执行一次任务
     */
    public void run(SysJob job) throws Exception;
}
