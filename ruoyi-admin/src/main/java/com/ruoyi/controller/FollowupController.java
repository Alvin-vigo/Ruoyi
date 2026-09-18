package com.ruoyi.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.MedicalFollowup;
import com.ruoyi.system.service.IMedicalFollowupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 患者随访记录Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/medical/followup")
public class FollowupController {

    @Autowired
    private IMedicalFollowupService medicalFollowupService;

    /**
     * 查询患者随访记录列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MedicalFollowup medicalFollowup,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalFollowup> list = medicalFollowupService.selectMedicalFollowupList(medicalFollowup);
        PageInfo<MedicalFollowup> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    /**
     * 获取患者随访记录详细信息
     */
    @GetMapping(value = "/{followupId}")
    public AjaxResult getInfo(@PathVariable("followupId") Long followupId) {
        return AjaxResult.success(medicalFollowupService.selectMedicalFollowupById(followupId));
    }

    /**
     * 根据患者ID查询随访记录
     */
    @GetMapping("/patient/{patientId}")
    public AjaxResult getByPatientId(@PathVariable("patientId") Long patientId) {
        List<MedicalFollowup> list = medicalFollowupService.selectMedicalFollowupByPatientId(patientId);
        return AjaxResult.success(list);
    }

    /**
     * 新增患者随访记录
     */
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MedicalFollowup medicalFollowup) {
        return toAjax(medicalFollowupService.insertMedicalFollowup(medicalFollowup));
    }

    /**
     * 修改患者随访记录
     */
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MedicalFollowup medicalFollowup) {
        return toAjax(medicalFollowupService.updateMedicalFollowup(medicalFollowup));
    }

    /**
     * 删除患者随访记录
     */
    @DeleteMapping("/{followupIds}")
    public AjaxResult remove(@PathVariable Long[] followupIds) {
        return toAjax(medicalFollowupService.deleteMedicalFollowupByIds(followupIds));
    }

    /**
     * 导出患者随访记录
     */
    @RequestMapping(value = "/export", method = { RequestMethod.GET, RequestMethod.POST })
    public void export(HttpServletResponse response, MedicalFollowup medicalFollowup) throws IOException {
        List<MedicalFollowup> list = medicalFollowupService.selectMedicalFollowupList(medicalFollowup);
        ExcelUtil<MedicalFollowup> util = new ExcelUtil<>(MedicalFollowup.class);
        util.exportExcel(response, list, "患者随访记录");
    }

    /**
     * 将操作结果转换为 AjaxResult
     *
     * @param rows 操作行数
     * @return 结果
     */
    private AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }
}
