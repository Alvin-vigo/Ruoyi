package com.ruoyi.system.service.impl;

import com.ruoyi.common.config.MedicalProperties;
import com.ruoyi.system.mapper.MedicalStatisticsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * 医疗数据看板 Service 单元测试。
 *
 * 采用 Mockito + 反射注入，不启动 Spring / 数据库容器，
 * 直接验证统计维度组装逻辑。属“业务层单元测试”。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("医疗数据看板 Service 单元测试（不依赖容器）")
class MedicalStatisticsServiceImplTest {

    @Mock
    private MedicalStatisticsMapper medicalStatisticsMapper;

    @Mock
    private MedicalProperties medicalProperties;

    private MedicalStatisticsServiceImpl statisticsService;

    @BeforeEach
    void setUp() {
        statisticsService = new MedicalStatisticsServiceImpl();
        ReflectionTestUtils.setField(statisticsService, "medicalStatisticsMapper", medicalStatisticsMapper);
        ReflectionTestUtils.setField(statisticsService, "medicalProperties", medicalProperties);
        // 低库存阈值来自统一配置，这里给一个确定值，避免依赖 Mockito 的默认值
        when(medicalProperties.getLowStockThreshold()).thenReturn(10);
    }

    @Test
    @DisplayName("getDashboard：应组装五个统计维度")
    void getDashboard_assemblesAllDimensions() {
        Map<String, Object> dept = new HashMap<>();
        dept.put("department", "内科");
        dept.put("count", 5);
        Map<String, Object> cat = new HashMap<>();
        cat.put("category", "0");
        cat.put("count", 8);

        when(medicalStatisticsMapper.selectPatientTotal()).thenReturn(10);
        when(medicalStatisticsMapper.selectDiagnosisTotal()).thenReturn(20);
        when(medicalStatisticsMapper.selectDrugTotal()).thenReturn(30);
        when(medicalStatisticsMapper.selectDiagnosisByDepartment()).thenReturn(Collections.singletonList(dept));
        when(medicalStatisticsMapper.selectDrugByCategory()).thenReturn(Collections.singletonList(cat));

        Map<String, Object> result = statisticsService.getDashboard(false);

        assertEquals(10, result.get("patientTotal"));
        assertEquals(20, result.get("diagnosisTotal"));
        assertEquals(30, result.get("drugTotal"));
        assertEquals(1, ((List<?>) result.get("byDepartment")).size());
        assertEquals(1, ((List<?>) result.get("byCategory")).size());
    }

    @Test
    @DisplayName("getDashboard：应包含费用汇总、费用趋势、患者性别与年龄段分布")
    void getDashboard_containsAnalysisDimensions() {
        Map<String, Object> costDept = new HashMap<>();
        costDept.put("department", "内科");
        costDept.put("totalCost", 1200.00);
        costDept.put("count", 5);
        Map<String, Object> trend = new HashMap<>();
        trend.put("month", "2024-01");
        trend.put("totalCost", 3200.00);
        trend.put("count", 12);
        Map<String, Object> gender = new HashMap<>();
        gender.put("gender", "0");
        gender.put("count", 6);
        Map<String, Object> age = new HashMap<>();
        age.put("ageGroup", "18-39岁");
        age.put("count", 4);

        when(medicalStatisticsMapper.selectPatientTotal()).thenReturn(10);
        when(medicalStatisticsMapper.selectDiagnosisTotal()).thenReturn(20);
        when(medicalStatisticsMapper.selectDrugTotal()).thenReturn(30);
        when(medicalStatisticsMapper.selectDiagnosisByDepartment()).thenReturn(Collections.emptyList());
        when(medicalStatisticsMapper.selectDrugByCategory()).thenReturn(Collections.emptyList());
        when(medicalStatisticsMapper.selectCostByDepartment()).thenReturn(Collections.singletonList(costDept));
        when(medicalStatisticsMapper.selectCostTrend()).thenReturn(Collections.singletonList(trend));
        when(medicalStatisticsMapper.selectPatientGenderStats()).thenReturn(Collections.singletonList(gender));
        when(medicalStatisticsMapper.selectPatientAgeStats()).thenReturn(Collections.singletonList(age));

        Map<String, Object> result = statisticsService.getDashboard(false);

        assertEquals(1, ((List<?>) result.get("costByDepartment")).size());
        assertEquals(1, ((List<?>) result.get("costTrend")).size());
        assertEquals(1, ((List<?>) result.get("genderStats")).size());
        assertEquals(1, ((List<?>) result.get("ageStats")).size());
    }
}
