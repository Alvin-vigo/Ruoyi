package com.ruoyi.quartz.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.quartz.domain.SysJobLog;
import com.ruoyi.quartz.service.ISysJobLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 定时任务调度日志Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/job/log")
public class SysJobLogController {

    @Autowired
    private ISysJobLogService jobLogService;

    /**
     * 查询定时任务调度日志列表
     */
    @GetMapping("/list")
    public TableDataInfo list(SysJobLog jobLog,
                              @RequestParam(defaultValue = "1") int pageNum,
                              @RequestParam(defaultValue = "10") int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<SysJobLog> list = jobLogService.selectJobLogList(jobLog);
        PageInfo<SysJobLog> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    /**
     * 获取定时任务调度日志详细信息
     */
    @GetMapping(value = "/{jobLogId}")
    public AjaxResult getInfo(@PathVariable("jobLogId") Long jobLogId) {
        return AjaxResult.success(jobLogService.selectJobLogById(jobLogId));
    }

    /**
     * 删除定时任务调度日志
     */
    @DeleteMapping("/{jobLogIds}")
    public AjaxResult remove(@PathVariable Long[] jobLogIds) {
        return toAjax(jobLogService.deleteJobLogByIds(jobLogIds));
    }

    /**
     * 清空定时任务调度日志
     */
    @DeleteMapping("/clean")
    public AjaxResult clean() {
        return toAjax(jobLogService.cleanJobLog());
    }

    /**
     * 导出定时任务调度日志
     */
    @RequestMapping(value = "/export", method = { RequestMethod.GET, RequestMethod.POST })
    public void export(HttpServletResponse response, SysJobLog jobLog) throws IOException {
        List<SysJobLog> list = jobLogService.selectJobLogList(jobLog);
        ExcelUtil<SysJobLog> util = new ExcelUtil<>(SysJobLog.class);
        util.exportExcel(response, list, "定时任务日志");
    }

    /**
     * 将操作结果转换为 AjaxResult
     */
    private AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }
}
