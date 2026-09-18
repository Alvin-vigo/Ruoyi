package com.ruoyi.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.system.domain.MedicalRegistration;
import com.ruoyi.system.service.IMedicalRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 挂号信息Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/medical/registration")
public class MedicalRegistrationController {

    @Autowired
    private IMedicalRegistrationService service;

    /**
     * 查询挂号信息列表（分页）
     */
    @GetMapping("/list")
    public TableDataInfo list(MedicalRegistration q,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalRegistration> list = service.selectMedicalRegistrationList(q);
        PageInfo<MedicalRegistration> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    /**
     * 获取挂号信息详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return AjaxResult.success(service.selectMedicalRegistrationById(id));
    }

    /**
     * 新增挂号信息（含号源占用）
     */
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MedicalRegistration b) {
        return toAjax(service.insertMedicalRegistration(b));
    }

    /**
     * 修改挂号信息
     */
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MedicalRegistration b) {
        return toAjax(service.updateMedicalRegistration(b));
    }

    /**
     * 删除挂号信息
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(service.deleteMedicalRegistrationByIds(ids));
    }

    /**
     * 开始就诊（P1-3 串联）：由挂号记录生成一条诊疗草稿，返回诊疗ID
     */
    @PostMapping("/start-visit/{registrationId}")
    public AjaxResult startVisit(@PathVariable Long registrationId) {
        return AjaxResult.success(service.startVisit(registrationId));
    }

    /**
     * 退号
     */
    @PostMapping("/refund/{registrationId}")
    public AjaxResult refund(@PathVariable Long registrationId) {
        return toAjax(service.refund(registrationId));
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
