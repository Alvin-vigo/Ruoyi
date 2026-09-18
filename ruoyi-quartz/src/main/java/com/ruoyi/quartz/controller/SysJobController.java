package com.ruoyi.quartz.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.quartz.domain.SysJob;
import com.ruoyi.quartz.service.ISysJobService;
import com.ruoyi.quartz.util.CronUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 定时任务调度信息Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/job")
public class SysJobController {

    @Autowired
    private ISysJobService jobService;

    /**
     * 查询定时任务调度信息列表
     */
    @GetMapping("/list")
    public TableDataInfo list(SysJob job,
                              @RequestParam(defaultValue = "1") int pageNum,
                              @RequestParam(defaultValue = "10") int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<SysJob> list = jobService.selectJobList(job);
        PageInfo<SysJob> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    /**
     * 获取定时任务调度信息详细信息
     */
    @GetMapping(value = "/{jobId}")
    public AjaxResult getInfo(@PathVariable("jobId") Long jobId) {
        return AjaxResult.success(jobService.selectJobById(jobId));
    }

    /**
     * 新增定时任务调度信息
     */
    @PostMapping
    public AjaxResult add(@RequestBody SysJob job) throws Exception {
        return toAjax(jobService.insertJob(job));
    }

    /**
     * 修改定时任务调度信息
     */
    @PutMapping
    public AjaxResult edit(@RequestBody SysJob job) throws Exception {
        return toAjax(jobService.updateJob(job));
    }

    /**
     * 删除定时任务调度信息
     */
    @DeleteMapping("/{jobIds}")
    public AjaxResult remove(@PathVariable Long[] jobIds) throws Exception {
        return toAjax(jobService.deleteJobByIds(jobIds));
    }

    /**
     * 修改任务状态（暂停 / 恢复）
     */
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysJob job) throws Exception {
        return toAjax(jobService.changeStatus(job));
    }

    /**
     * 立即执行一次任务
     */
    @PutMapping("/run")
    public AjaxResult run(@RequestBody SysJob job) throws Exception {
        jobService.run(job);
        return AjaxResult.success();
    }

    /**
     * 校验 cron 表达式
     */
    @GetMapping("/checkCronExpression")
    public AjaxResult checkCronExpression(@RequestParam("cronExpression") String cronExpression) {
        boolean valid = CronUtils.isValid(cronExpression);
        return AjaxResult.success(valid);
    }

    /**
     * 导出定时任务调度信息
     */
    @RequestMapping(value = "/export", method = { RequestMethod.GET, RequestMethod.POST })
    public void export(HttpServletResponse response, SysJob job) throws IOException {
        List<SysJob> list = jobService.selectJobList(job);
        ExcelUtil<SysJob> util = new ExcelUtil<>(SysJob.class);
        util.exportExcel(response, list, "定时任务");
    }

    /**
     * 将操作结果转换为 AjaxResult
     */
    private AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }
}
