package com.ruoyi.quartz.mapper;

import com.ruoyi.quartz.domain.SysJobLog;

import java.util.List;

/**
 * 定时任务调度日志Mapper接口
 *
 * @author ruoyi
 */
public interface SysJobLogMapper {

    /**
     * 查询定时任务调度日志列表
     */
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog);

    /**
     * 查询定时任务调度日志
     */
    public SysJobLog selectJobLogById(Long jobLogId);

    /**
     * 新增定时任务调度日志
     */
    public int insertJobLog(SysJobLog jobLog);

    /**
     * 删除定时任务调度日志
     */
    public int deleteJobLogById(Long jobLogId);

    /**
     * 批量删除定时任务调度日志
     */
    public int deleteJobLogByIds(Long[] jobLogIds);

    /**
     * 清空定时任务调度日志
     */
    public int cleanJobLog();
}
