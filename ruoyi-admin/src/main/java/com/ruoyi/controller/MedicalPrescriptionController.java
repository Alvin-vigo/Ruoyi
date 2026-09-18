package com.ruoyi.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.system.domain.MedicalPrescription;
import com.ruoyi.system.domain.MedicalPrescriptionItem;
import com.ruoyi.system.service.IMedicalPrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处方信息Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/medical/prescription")
public class MedicalPrescriptionController {

    @Autowired
    private IMedicalPrescriptionService service;

    /**
     * 查询处方信息列表（分页）
     */
    @GetMapping("/list")
    public TableDataInfo list(MedicalPrescription q,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalPrescription> list = service.selectMedicalPrescriptionList(q);
        PageInfo<MedicalPrescription> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    /**
     * 获取处方信息详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return AjaxResult.success(service.selectMedicalPrescriptionById(id));
    }

    /**
     * 新增处方信息（含明细）
     */
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MedicalPrescription b) {
        return toAjax(service.addPrescription(b, b.getItems()));
    }

    /**
     * 修改处方信息
     */
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MedicalPrescription b) {
        return toAjax(service.updateMedicalPrescription(b));
    }

    /**
     * 删除处方信息
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(service.deleteMedicalPrescriptionByIds(ids));
    }

    /**
     * 发药
     */
    @PostMapping("/dispense")
    public AjaxResult dispense(@Validated @RequestBody DispenseRequest request) {
        service.dispense(request.getPrescriptionId(), request.getItems(), request.getOperator());
        return AjaxResult.success();
    }

    /**
     * 作废处方
     */
    @PostMapping("/cancel/{prescriptionId}")
    public AjaxResult cancel(@PathVariable Long prescriptionId) {
        service.cancel(prescriptionId);
        return AjaxResult.success();
    }

    /**
     * 退药（逆向流程：回滚库存）
     */
    @PostMapping("/return/{prescriptionId}")
    public AjaxResult returnDispensing(@PathVariable Long prescriptionId,
                                       @RequestParam(required = false) String operator) {
        service.returnDispensing(prescriptionId, operator);
        return AjaxResult.success();
    }

    /**
     * 查询处方明细列表
     */
    @GetMapping("/items/{prescriptionId}")
    public AjaxResult items(@PathVariable Long prescriptionId) {
        return AjaxResult.success(service.selectItems(prescriptionId));
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
