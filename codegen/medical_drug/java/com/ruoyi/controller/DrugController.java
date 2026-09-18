package com.ruoyi.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.MedicalDrug;
import com.ruoyi.system.service.IMedicalDrugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 药品信息Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/medical/drug")
public class DrugController {

    @Autowired
    private IMedicalDrugService medicalDrugService;

    /**
     * 查询药品信息列表（分页）
     */
    @GetMapping("/list")
    public TableDataInfo list(MedicalDrug medicalDrug,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalDrug> list = medicalDrugService.selectMedicalDrugList(medicalDrug);
        PageInfo<MedicalDrug> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    /**
     * 获取药品信息详细信息
     */
    @GetMapping(value = "/{drugId}")
    public AjaxResult getInfo(@PathVariable("drugId") Long drugId) {
        return AjaxResult.success(medicalDrugService.selectMedicalDrugById(drugId));
    }

    /**
     * 新增药品信息
     */
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MedicalDrug medicalDrug) {
        return toAjax(medicalDrugService.insertMedicalDrug(medicalDrug));
    }

    /**
     * 修改药品信息
     */
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MedicalDrug medicalDrug) {
        return toAjax(medicalDrugService.updateMedicalDrug(medicalDrug));
    }

    /**
     * 删除药品信息
     */
    @DeleteMapping("/{drugIds}")
    public AjaxResult remove(@PathVariable Long[] drugIds) {
        return toAjax(medicalDrugService.deleteMedicalDrugByIds(drugIds));
    }

    /**
     * 导出药品信息列表
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response, MedicalDrug medicalDrug) throws IOException {
        List<MedicalDrug> list = medicalDrugService.selectMedicalDrugList(medicalDrug);
        ExcelUtil<MedicalDrug> util = new ExcelUtil<>(MedicalDrug.class);
        util.exportExcel(response, list, "药品信息数据");
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
