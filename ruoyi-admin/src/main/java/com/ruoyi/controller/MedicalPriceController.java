package com.ruoyi.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.system.domain.MedicalPriceItem;
import com.ruoyi.system.service.IMedicalPriceItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医疗费用价目 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/medical/price")
public class MedicalPriceController {

    @Autowired
    private IMedicalPriceItemService medicalPriceItemService;

    /**
     * 查询费用价目列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MedicalPriceItem item,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalPriceItem> list = medicalPriceItemService.selectMedicalPriceItemList(item);
        PageInfo<MedicalPriceItem> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    /**
     * 获取费用价目详细信息
     */
    @GetMapping(value = "/{itemId}")
    public AjaxResult getInfo(@PathVariable("itemId") Long itemId) {
        return AjaxResult.success(medicalPriceItemService.selectMedicalPriceItemById(itemId));
    }

    /**
     * 新增费用价目
     */
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MedicalPriceItem item) {
        return toAjax(medicalPriceItemService.insertMedicalPriceItem(item));
    }

    /**
     * 修改费用价目
     */
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MedicalPriceItem item) {
        return toAjax(medicalPriceItemService.updateMedicalPriceItem(item));
    }

    /**
     * 删除费用价目
     */
    @DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds) {
        return toAjax(medicalPriceItemService.deleteMedicalPriceItemByIds(itemIds));
    }

    /**
     * 将操作结果转换为 AjaxResult
     */
    private AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }
}
