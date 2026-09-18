package com.ruoyi.quartz.service.impl;

import com.ruoyi.quartz.domain.SysJobLog;
import com.ruoyi.quartz.mapper.SysJobLogMapper;
import com.ruoyi.quartz.service.ISysJobLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 定时任务调度日志Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SysJobLogServiceImpl implements ISysJobLogService {

    @Autowired
    private SysJobLogMapper jobLogMapper;

    /**
     * 查询定时任务调度日志列表
     */
    @Override
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog) {
        return jobLogMapper.selectJobLogList(jobLog);
    }

    /**
     * 查询定时任务调度日志
     */
    @Override
    public SysJobLog selectJobLogById(Long jobLogId) {
        return jobLogMapper.selectJobLogById(jobLogId);
    }

    /**
     * 新增定时任务调度日志
     */
    @Override
    public int addJobLog(SysJobLog jobLog) {
        return jobLogMapper.insertJobLog(jobLog);
    }

    /**
     * 批量删除定时任务调度日志
     */
    @Override
    public int deleteJobLogByIds(Long[] jobLogIds) {
        return jobLogMapper.deleteJobLogByIds(jobLogIds);
    }

    /**
     * 清空定时任务调度日志
     */
    @Override
    public int cleanJobLog() {
        return jobLogMapper.cleanJobLog();
    }
}
