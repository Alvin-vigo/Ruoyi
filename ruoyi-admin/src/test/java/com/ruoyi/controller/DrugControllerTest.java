package com.ruoyi.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.MedicalDrug;
import com.ruoyi.system.service.IMedicalDrugService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 药品信息 Controller 接口/逻辑测试。
 *
 * 采用 Mockito + 反射注入，不启动 Spring Web / 数据库 / Redis 容器，
 * 直接验证控制层的请求分发、结果包装（TableDataInfo / AjaxResult）与 toAjax 边界。
 * 属“控制层单元测试”，本机 `mvn -pl ruoyi-admin test` 即可运行。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("药品信息 Controller 接口测试（不依赖容器）")
class DrugControllerTest {

    @Mock
    private IMedicalDrugService medicalDrugService;

    private DrugController drugController;

    private MedicalDrug sample;

    @BeforeEach
    void setUp() {
        drugController = new DrugController();
        // 将 mock 服务注入 Controller 的 @Autowired 字段，规避 Spring 上下文
        ReflectionTestUtils.setField(drugController, "medicalDrugService", medicalDrugService);
        sample = new MedicalDrug();
    }

    @Test
    @DisplayName("列表接口：应调用服务并将结果包装为分页数据")
    void list_returnsTableDataInfo() {
        List<MedicalDrug> list = Arrays.asList(sample);
        when(medicalDrugService.selectMedicalDrugList(any())).thenReturn(list);

        TableDataInfo result = drugController.list(sample, 1, 10);

        assertNotNull(result);
        assertEquals(list, result.getRows());
        verify(medicalDrugService).selectMedicalDrugList(any(MedicalDrug.class));
    }

    @Test
    @DisplayName("详情接口：应返回 code=200 且 data 为服务结果")
    void getInfo_returnsSuccessWithData() {
        when(medicalDrugService.selectMedicalDrugById(1L)).thenReturn(sample);

        AjaxResult result = drugController.getInfo(1L);

        assertEquals(200, result.getCode());
        assertSame(sample, result.get("data"));
        verify(medicalDrugService).selectMedicalDrugById(anyLong());
    }

    @Test
    @DisplayName("新增：影响行数 > 0 时返回 success（code=200）")
    void add_rowsGreaterThanZero_returnsSuccess() {
        when(medicalDrugService.insertMedicalDrug(any())).thenReturn(1);

        AjaxResult result = drugController.add(sample);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("新增：影响行数 = 0 时返回 error（code=500）")
    void add_rowsZero_returnsError() {
        when(medicalDrugService.insertMedicalDrug(any())).thenReturn(0);

        AjaxResult result = drugController.add(sample);

        assertEquals(500, result.getCode());
    }

    @Test
    @DisplayName("修改：影响行数 > 0 时返回 success（code=200）")
    void edit_rowsGreaterThanZero_returnsSuccess() {
        when(medicalDrugService.updateMedicalDrug(any())).thenReturn(1);

        AjaxResult result = drugController.edit(sample);

        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除：传入 ID 数组应委托服务并返回 success")
    void remove_delegatesToService() {
        Long[] ids = {1L, 2L};
        when(medicalDrugService.deleteMedicalDrugByIds(ids)).thenReturn(2);

        AjaxResult result = drugController.remove(ids);

        assertEquals(200, result.getCode());
        verify(medicalDrugService).deleteMedicalDrugByIds(ids);
    }

    @Test
    @DisplayName("低库存预警：应返回库存小于等于阈值的药品列表")
    void lowStock_returnsWarningList() {
        List<MedicalDrug> lowStock = Arrays.asList(sample);
        when(medicalDrugService.selectMedicalDrugLowStock(10)).thenReturn(lowStock);

        AjaxResult result = drugController.lowStock(10);

        assertEquals(200, result.getCode());
        assertSame(lowStock, result.get("data"));
        verify(medicalDrugService).selectMedicalDrugLowStock(10);
    }

    @Test
    @DisplayName("近效期预警：应返回有效期临近的药品列表")
    void nearExpiry_returnsWarningList() {
        List<MedicalDrug> nearExpiry = Arrays.asList(sample);
        when(medicalDrugService.selectMedicalDrugNearExpiry(90)).thenReturn(nearExpiry);

        AjaxResult result = drugController.nearExpiry(90);

        assertEquals(200, result.getCode());
        assertSame(nearExpiry, result.get("data"));
        verify(medicalDrugService).selectMedicalDrugNearExpiry(90);
    }
}
