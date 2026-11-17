package com.ruoyi.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.IMedicalStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 医疗数据看板 Controller 接口测试。
 *
 * 采用 Mockito + 反射注入，不启动 Spring Web / 数据库 / Redis 容器，
 * 直接验证控制层对看板统计结果的包装。属“控制层单元测试”。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("医疗数据看板 Controller 接口测试（不依赖容器）")
class StatisticsControllerTest {

    @Mock
    private IMedicalStatisticsService medicalStatisticsService;

    private StatisticsController statisticsController;

    @BeforeEach
    void setUp() {
        statisticsController = new StatisticsController();
        ReflectionTestUtils.setField(statisticsController, "medicalStatisticsService", medicalStatisticsService);
    }

    @Test
    @DisplayName("看板统计：应返回包含患者/诊疗/药品总数及分布的 Map")
    void dashboard_returnsStatisticsMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("patientTotal", 10);
        data.put("diagnosisTotal", 20);
        data.put("drugTotal", 30);
        data.put("byDepartment", Collections.emptyList());
        data.put("byCategory", Collections.emptyList());
        // getDashboard 需要 forceRefresh 形参；不传 refresh 时 Controller 传 false
        when(medicalStatisticsService.getDashboard(false)).thenReturn(data);

        AjaxResult result = statisticsController.dashboard(null);

        assertEquals(200, result.getCode());
        Map<String, Object> returned = (Map<String, Object>) result.get("data");
        assertNotNull(returned);
        assertEquals(10, returned.get("patientTotal"));
        assertEquals(20, returned.get("diagnosisTotal"));
        assertEquals(30, returned.get("drugTotal"));
        verify(medicalStatisticsService).getDashboard(false);
    }

    @Test
    @DisplayName("看板统计：refresh=true 时应把强制刷新标志透传给 Service")
    void dashboard_passesForceRefreshFlag() {
        Map<String, Object> data = new HashMap<>();
        data.put("patientTotal", 1);
        when(medicalStatisticsService.getDashboard(true)).thenReturn(data);

        AjaxResult result = statisticsController.dashboard(true);

        assertEquals(200, result.getCode());
        assertEquals(1, ((Map<String, Object>) result.get("data")).get("patientTotal"));
        verify(medicalStatisticsService).getDashboard(true);
    }
}
