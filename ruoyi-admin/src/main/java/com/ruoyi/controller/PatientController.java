package com.ruoyi.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.MedicalPatient;
import com.ruoyi.system.service.IMedicalPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 患者信息Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/medical/patient")
public class PatientController {

    @Autowired
    private IMedicalPatientService medicalPatientService;

    /**
     * 查询患者信息列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MedicalPatient medicalPatient,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalPatient> list = medicalPatientService.selectMedicalPatientList(medicalPatient);
        PageInfo<MedicalPatient> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    /**
     * 获取患者信息详细信息
     */
    @GetMapping(value = "/{patientId}")
    public AjaxResult getInfo(@PathVariable("patientId") Long patientId) {
        return AjaxResult.success(medicalPatientService.selectMedicalPatientById(patientId));
    }

    /**
     * 新增患者信息
     */
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MedicalPatient medicalPatient) {
        if ("1".equals(medicalPatientService.checkIdCardUnique(medicalPatient))) {
            return AjaxResult.error("新增患者'" + medicalPatient.getPatientName() + "'失败，身份证号已存在");
        }
        return toAjax(medicalPatientService.insertMedicalPatient(medicalPatient));
    }

    /**
     * 修改患者信息
     */
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MedicalPatient medicalPatient) {
        if ("1".equals(medicalPatientService.checkIdCardUnique(medicalPatient))) {
            return AjaxResult.error("修改患者'" + medicalPatient.getPatientName() + "'失败，身份证号已存在");
        }
        return toAjax(medicalPatientService.updateMedicalPatient(medicalPatient));
    }

    /**
     * 删除患者信息
     */
    @DeleteMapping("/{patientIds}")
    public AjaxResult remove(@PathVariable Long[] patientIds) {
        return toAjax(medicalPatientService.deleteMedicalPatientByIds(patientIds));
    }

    /**
     * 导出患者信息
     */
    @RequestMapping(value = "/export", method = { RequestMethod.GET, RequestMethod.POST })
    public void export(HttpServletResponse response, MedicalPatient medicalPatient) throws IOException {
        List<MedicalPatient> list = medicalPatientService.selectMedicalPatientList(medicalPatient);
        ExcelUtil<MedicalPatient> util = new ExcelUtil<>(MedicalPatient.class);
        util.exportExcel(response, list, "患者信息数据");
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
