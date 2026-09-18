package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.MedicalDiagnosis;
import com.ruoyi.system.mapper.MedicalDiagnosisMapper;
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
 * 诊疗信息 Service 单元测试（纯 Mockito，不依赖 Spring 容器 / 数据库）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("诊疗信息 Service 单元测试")
class MedicalDiagnosisServiceImplTest {

    @Mock
    private MedicalDiagnosisMapper medicalDiagnosisMapper;

    @InjectMocks
    private MedicalDiagnosisServiceImpl medicalDiagnosisService;

    private MedicalDiagnosis sample;

    @BeforeEach
    void setUp() {
        sample = new MedicalDiagnosis();
    }

    @Test
    @DisplayName("按 ID 查询应委托 Mapper 并原样返回其结果")
    void selectById_delegatesToMapper() {
        when(medicalDiagnosisMapper.selectMedicalDiagnosisById(1L)).thenReturn(sample);

        MedicalDiagnosis result = medicalDiagnosisService.selectMedicalDiagnosisById(1L);

        assertSame(sample, result);
        verify(medicalDiagnosisMapper, times(1)).selectMedicalDiagnosisById(1L);
    }

    @Test
    @DisplayName("列表查询应委托 Mapper 并原样返回其结果")
    void selectList_delegatesToMapper() {
        List<MedicalDiagnosis> list = Arrays.asList(sample);
        when(medicalDiagnosisMapper.selectMedicalDiagnosisList(any())).thenReturn(list);

        List<MedicalDiagnosis> result = medicalDiagnosisService.selectMedicalDiagnosisList(new MedicalDiagnosis());

        assertEquals(list, result);
        verify(medicalDiagnosisMapper).selectMedicalDiagnosisList(any(MedicalDiagnosis.class));
    }

    @Test
    @DisplayName("新增应透传 Mapper 返回的影响行数")
    void insert_returnsAffectedRows() {
        when(medicalDiagnosisMapper.insertMedicalDiagnosis(any())).thenReturn(1);

        assertEquals(1, medicalDiagnosisService.insertMedicalDiagnosis(sample));
        verify(medicalDiagnosisMapper).insertMedicalDiagnosis(sample);
    }

    @Test
    @DisplayName("修改应透传 Mapper 返回的影响行数")
    void update_returnsAffectedRows() {
        when(medicalDiagnosisMapper.updateMedicalDiagnosis(any())).thenReturn(1);

        assertEquals(1, medicalDiagnosisService.updateMedicalDiagnosis(sample));
        verify(medicalDiagnosisMapper).updateMedicalDiagnosis(sample);
    }

    @Test
    @DisplayName("批量删除应委托 Mapper 批量方法并透传行数")
    void deleteByIds_delegatesToMapper() {
        Long[] ids = {1L, 2L, 3L};
        when(medicalDiagnosisMapper.deleteMedicalDiagnosisByIds(ids)).thenReturn(3);

        assertEquals(3, medicalDiagnosisService.deleteMedicalDiagnosisByIds(ids));
        verify(medicalDiagnosisMapper).deleteMedicalDiagnosisByIds(ids);
    }
}
