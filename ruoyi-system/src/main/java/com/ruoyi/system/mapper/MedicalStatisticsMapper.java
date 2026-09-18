package com.ruoyi.system.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 医疗数据看板统计Mapper接口
 *
 * @author ruoyi
 */
public interface MedicalStatisticsMapper {

    /**
     * 统计患者总数
     */
    int selectPatientTotal();

    /**
     * 统计诊疗记录总数
     */
    int selectDiagnosisTotal();

    /**
     * 统计药品总数
     */
    int selectDrugTotal();

    /**
     * 统计挂号总量
     */
    int selectRegistrationTotal();

    /**
     * 统计处方总量
     */
    int selectPrescriptionTotal();

    /**
     * 统计发药总量（发药记录 quantity 汇总）
     */
    int selectDispensingTotalQty();

    /**
     * 统计库存预警数量（库存小于等于阈值）
     *
     * @param threshold 库存预警阈值
     */
    int selectLowStockCount(@Param("threshold") int threshold);

    /**
     * 按科室统计诊疗数量
     */
    List<Map<String, Object>> selectDiagnosisByDepartment();

    /**
     * 按药品分类统计数量
     */
    List<Map<String, Object>> selectDrugByCategory();

    /**
     * 按科室统计诊疗费用（汇总金额与诊疗数量）
     */
    List<Map<String, Object>> selectCostByDepartment();

    /**
     * 按月统计诊疗费用趋势
     */
    List<Map<String, Object>> selectCostTrend();

    /**
     * 患者性别分布
     */
    List<Map<String, Object>> selectPatientGenderStats();

    /**
     * 患者年龄段分布
     */
    List<Map<String, Object>> selectPatientAgeStats();
}
