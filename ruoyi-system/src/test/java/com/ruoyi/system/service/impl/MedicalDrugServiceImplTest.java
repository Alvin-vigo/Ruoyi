package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.MedicalDrug;
import com.ruoyi.system.mapper.MedicalDrugMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 药品信息 Service 单元测试（纯 Mockito，不依赖 Spring 容器 / 数据库）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("药品信息 Service 单元测试")
class MedicalDrugServiceImplTest {

    @Mock
    private MedicalDrugMapper medicalDrugMapper;

    @InjectMocks
    private MedicalDrugServiceImpl medicalDrugService;

    private MedicalDrug sample;

    @BeforeEach
    void setUp() {
        sample = new MedicalDrug();
    }

    @Test
    @DisplayName("按 ID 查询应委托 Mapper 并原样返回其结果")
    void selectById_delegatesToMapper() {
        when(medicalDrugMapper.selectMedicalDrugById(1L)).thenReturn(sample);

        MedicalDrug result = medicalDrugService.selectMedicalDrugById(1L);

        assertSame(sample, result);
        verify(medicalDrugMapper, times(1)).selectMedicalDrugById(1L);
    }

    @Test
    @DisplayName("列表查询应委托 Mapper 并原样返回其结果")
    void selectList_delegatesToMapper() {
        List<MedicalDrug> list = Arrays.asList(sample);
        when(medicalDrugMapper.selectMedicalDrugList(any())).thenReturn(list);

        List<MedicalDrug> result = medicalDrugService.selectMedicalDrugList(new MedicalDrug());

        assertEquals(list, result);
        verify(medicalDrugMapper).selectMedicalDrugList(any(MedicalDrug.class));
    }

    @Test
    @DisplayName("新增应透传 Mapper 返回的影响行数")
    void insert_returnsAffectedRows() {
        when(medicalDrugMapper.insertMedicalDrug(any())).thenReturn(1);

        assertEquals(1, medicalDrugService.insertMedicalDrug(sample));
        verify(medicalDrugMapper).insertMedicalDrug(sample);
    }

    @Test
    @DisplayName("修改应透传 Mapper 返回的影响行数")
    void update_returnsAffectedRows() {
        when(medicalDrugMapper.updateMedicalDrug(any())).thenReturn(1);

        assertEquals(1, medicalDrugService.updateMedicalDrug(sample));
        verify(medicalDrugMapper).updateMedicalDrug(sample);
    }

    @Test
    @DisplayName("批量删除应委托 Mapper 批量方法并透传行数")
    void deleteByIds_delegatesToMapper() {
        Long[] ids = {1L, 2L, 3L};
        when(medicalDrugMapper.deleteMedicalDrugByIds(ids)).thenReturn(3);

        assertEquals(3, medicalDrugService.deleteMedicalDrugByIds(ids));
        verify(medicalDrugMapper).deleteMedicalDrugByIds(ids);
    }
}
