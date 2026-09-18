package com.ruoyi.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.system.domain.MedicalSchedule;
import com.ruoyi.system.service.IMedicalScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排班信息Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/medical/schedule")
public class MedicalScheduleController {

    @Autowired
    private IMedicalScheduleService service;

    /**
     * 查询排班信息列表（分页）
     */
    @GetMapping("/list")
    public TableDataInfo list(MedicalSchedule q,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalSchedule> list = service.selectMedicalScheduleList(q);
        PageInfo<MedicalSchedule> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    /**
     * 获取排班信息详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return AjaxResult.success(service.selectMedicalScheduleById(id));
    }

    /**
     * 新增排班信息
     */
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MedicalSchedule b) {
        return toAjax(service.insertMedicalSchedule(b));
    }

    /**
     * 修改排班信息
     */
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MedicalSchedule b) {
        return toAjax(service.updateMedicalSchedule(b));
    }

    /**
     * 删除排班信息
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(service.deleteMedicalScheduleByIds(ids));
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
