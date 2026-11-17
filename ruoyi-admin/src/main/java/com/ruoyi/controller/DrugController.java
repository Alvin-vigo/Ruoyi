package com.ruoyi.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.config.MedicalProperties;
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

    @Autowired
    private MedicalProperties medicalProperties;

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
     * 查询低库存药品预警列表
     *
     * <p>不传 threshold 时使用医疗参数配置中的统一阈值（medical.low-stock-threshold），
     * 与统计看板、定时任务保持同一口径；传参则按调用方指定值临时覆盖。
     */
    @GetMapping("/low-stock")
    public AjaxResult lowStock(@RequestParam(required = false) Integer threshold) {
        int value = threshold != null ? threshold : medicalProperties.getLowStockThreshold();
        List<MedicalDrug> list = medicalDrugService.selectMedicalDrugLowStock(value);
        return AjaxResult.success(list);
    }

    /**
     * 查询近效期药品预警列表
     *
     * <p>不传 warnDays 时使用 medical.near-expiry-warn-days（默认 90 天）。
     */
    @GetMapping("/near-expiry")
    public AjaxResult nearExpiry(@RequestParam(required = false) Integer warnDays) {
        int value = warnDays != null ? warnDays : medicalProperties.getNearExpiryWarnDays();
        List<MedicalDrug> list = medicalDrugService.selectMedicalDrugNearExpiry(value);
        return AjaxResult.success(list);
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
    @RequestMapping(value = "/export", method = { RequestMethod.GET, RequestMethod.POST })
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
