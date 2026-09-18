package com.ruoyi.quartz.mapper;

import com.ruoyi.quartz.domain.SysJob;

import java.util.List;

/**
 * 定时任务调度信息Mapper接口
 *
 * @author ruoyi
 */
public interface SysJobMapper {

    /**
     * 查询定时任务调度信息列表
     */
    public List<SysJob> selectJobList(SysJob job);

    /**
     * 查询定时任务调度信息
     */
    public SysJob selectJobById(Long jobId);

    /**
     * 新增定时任务调度信息
     */
    public int insertJob(SysJob job);

    /**
     * 修改定时任务调度信息
     */
    public int updateJob(SysJob job);

    /**
     * 删除定时任务调度信息
     */
    public int deleteJobById(Long jobId);

    /**
     * 批量删除定时任务调度信息
     */
    public int deleteJobByIds(Long[] jobIds);
}
