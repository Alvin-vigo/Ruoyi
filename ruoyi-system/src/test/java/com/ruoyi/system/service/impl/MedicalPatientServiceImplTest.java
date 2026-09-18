package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.MedicalPatient;
import com.ruoyi.system.mapper.MedicalPatientMapper;
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
 * 患者信息 Service 单元测试（纯 Mockito，不依赖 Spring 容器 / 数据库）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("患者信息 Service 单元测试")
class MedicalPatientServiceImplTest {

    @Mock
    private MedicalPatientMapper medicalPatientMapper;

    @InjectMocks
    private MedicalPatientServiceImpl medicalPatientService;

    private MedicalPatient sample;

    @BeforeEach
    void setUp() {
        // 仅作返回值占位，无需填充具体字段即可验证“委托 + 透传”语义
        sample = new MedicalPatient();
    }

    @Test
    @DisplayName("按 ID 查询应委托 Mapper 并原样返回其结果")
    void selectById_delegatesToMapper() {
        when(medicalPatientMapper.selectMedicalPatientById(1L)).thenReturn(sample);

        MedicalPatient result = medicalPatientService.selectMedicalPatientById(1L);

        assertSame(sample, result);
        verify(medicalPatientMapper, times(1)).selectMedicalPatientById(1L);
    }

    @Test
    @DisplayName("列表查询应委托 Mapper 并原样返回其结果")
    void selectList_delegatesToMapper() {
        List<MedicalPatient> list = Arrays.asList(sample);
        when(medicalPatientMapper.selectMedicalPatientList(any())).thenReturn(list);

        List<MedicalPatient> result = medicalPatientService.selectMedicalPatientList(new MedicalPatient());

        assertEquals(list, result);
        verify(medicalPatientMapper).selectMedicalPatientList(any(MedicalPatient.class));
    }

    @Test
    @DisplayName("新增应透传 Mapper 返回的影响行数")
    void insert_returnsAffectedRows() {
        when(medicalPatientMapper.insertMedicalPatient(any())).thenReturn(1);

        assertEquals(1, medicalPatientService.insertMedicalPatient(sample));
        verify(medicalPatientMapper).insertMedicalPatient(sample);
    }

    @Test
    @DisplayName("修改应透传 Mapper 返回的影响行数")
    void update_returnsAffectedRows() {
        when(medicalPatientMapper.updateMedicalPatient(any())).thenReturn(1);

        assertEquals(1, medicalPatientService.updateMedicalPatient(sample));
        verify(medicalPatientMapper).updateMedicalPatient(sample);
    }

    @Test
    @DisplayName("批量删除应委托 Mapper 批量方法并透传行数")
    void deleteByIds_delegatesToMapper() {
        Long[] ids = {1L, 2L, 3L};
        when(medicalPatientMapper.deleteMedicalPatientByIds(ids)).thenReturn(3);

        assertEquals(3, medicalPatientService.deleteMedicalPatientByIds(ids));
        verify(medicalPatientMapper).deleteMedicalPatientByIds(ids);
    }
}
