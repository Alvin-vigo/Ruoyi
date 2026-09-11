package com.ruoyi.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.system.domain.MedicalDiagnosis;
import com.ruoyi.system.service.IMedicalDiagnosisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 诊疗信息Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/medical/diagnosis")
public class DiagnosisController {

    @Autowired
    private IMedicalDiagnosisService medicalDiagnosisService;

    /**
     * 查询诊疗信息列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MedicalDiagnosis medicalDiagnosis,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalDiagnosis> list = medicalDiagnosisService.selectMedicalDiagnosisList(medicalDiagnosis);
        PageInfo<MedicalDiagnosis> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    /**
     * 获取诊疗信息详细信息
     */
    @GetMapping(value = "/{diagnosisId}")
    public AjaxResult getInfo(@PathVariable("diagnosisId") Long diagnosisId) {
        return AjaxResult.success(medicalDiagnosisService.selectMedicalDiagnosisById(diagnosisId));
    }

    /**
     * 根据患者ID查询诊疗记录
     */
    @GetMapping("/patient/{patientId}")
    public AjaxResult getByPatientId(@PathVariable("patientId") Long patientId) {
        List<MedicalDiagnosis> list = medicalDiagnosisService.selectMedicalDiagnosisByPatientId(patientId);
        return AjaxResult.success(list);
    }

    /**
     * 新增诊疗信息
     */
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MedicalDiagnosis medicalDiagnosis) {
        return toAjax(medicalDiagnosisService.insertMedicalDiagnosis(medicalDiagnosis));
    }

    /**
     * 修改诊疗信息
     */
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MedicalDiagnosis medicalDiagnosis) {
        return toAjax(medicalDiagnosisService.updateMedicalDiagnosis(medicalDiagnosis));
    }

    /**
     * 删除诊疗信息
     */
    @DeleteMapping("/{diagnosisIds}")
    public AjaxResult remove(@PathVariable Long[] diagnosisIds) {
        return toAjax(medicalDiagnosisService.deleteMedicalDiagnosisByIds(diagnosisIds));
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
