package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.MedicalDiagnosis;
import com.ruoyi.system.domain.MedicalRegistration;
import com.ruoyi.system.mapper.MedicalAuditLogMapper;
import com.ruoyi.system.mapper.MedicalRegistrationMapper;
import com.ruoyi.system.mapper.MedicalScheduleMapper;
import com.ruoyi.system.service.IMedicalDiagnosisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 挂号信息 Service 单元测试（纯 Mockito，不依赖 Spring 容器 / 数据库）
 *
 * <p>覆盖 AC-03（挂号占号源）、P1-6（退号/开始就诊审计）、挂号状态机与号源释放。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("挂号信息 Service 单元测试")
class MedicalRegistrationServiceImplTest {

    @Mock
    private MedicalRegistrationMapper registrationMapper;

    @Mock
    private MedicalScheduleMapper scheduleMapper;

    @Mock
    private MedicalAuditLogMapper auditLogMapper;

    @Mock
    private IMedicalDiagnosisService diagnosisService;

    @InjectMocks
    private MedicalRegistrationServiceImpl registrationService;

    private MedicalRegistration sample;

    @BeforeEach
    void setUp() {
        sample = new MedicalRegistration();
    }

    @Test
    @DisplayName("按 ID 查询应委托 Mapper 并原样返回结果")
    void selectById_delegatesToMapper() {
        when(registrationMapper.selectMedicalRegistrationById(1L)).thenReturn(sample);

        assertSame(sample, registrationService.selectMedicalRegistrationById(1L));
        verify(registrationMapper).selectMedicalRegistrationById(1L);
    }

    @Test
    @DisplayName("列表查询应委托 Mapper 并原样返回结果")
    void selectList_delegatesToMapper() {
        List<MedicalRegistration> list = Arrays.asList(sample);
        when(registrationMapper.selectMedicalRegistrationList(any())).thenReturn(list);

        assertEquals(list, registrationService.selectMedicalRegistrationList(new MedicalRegistration()));
        verify(registrationMapper).selectMedicalRegistrationList(any(MedicalRegistration.class));
    }

    @Test
    @DisplayName("新增：生成 GH 单号 + 默认待就诊状态 + 占号源（号源未满返回 1）")
    void insert_occupiesScheduleAndReturns() {
        sample.setScheduleId(10L);
        when(scheduleMapper.incrementUsed(10L)).thenReturn(1);
        when(registrationMapper.insertMedicalRegistration(any())).thenReturn(1);

        int rows = registrationService.insertMedicalRegistration(sample);

        assertEquals(1, rows);
        assertTrue(sample.getRegNo() != null && sample.getRegNo().startsWith("GH"), "挂号单号应以 GH 开头");
        assertEquals("0", sample.getRegStatus());
        assertNotNull(sample.getRegTime());
        verify(scheduleMapper).incrementUsed(10L);
        verify(registrationMapper).insertMedicalRegistration(sample);
    }

    @Test
    @DisplayName("新增：号源已满（incrementUsed 返回 0）抛异常且不落库")
    void insert_scheduleFull_throws() {
        sample.setScheduleId(10L);
        when(scheduleMapper.incrementUsed(10L)).thenReturn(0);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> registrationService.insertMedicalRegistration(sample));
        assertTrue(ex.getMessage().contains("号源已满"));
        verify(registrationMapper, never()).insertMedicalRegistration(any());
    }

    @Test
    @DisplayName("修改应委托 Mapper")
    void update_delegatesToMapper() {
        when(registrationMapper.updateMedicalRegistration(any())).thenReturn(1);
        assertEquals(1, registrationService.updateMedicalRegistration(sample));
    }

    @Test
    @DisplayName("单条/批量删除应委托 Mapper")
    void delete_delegatesToMapper() {
        when(registrationMapper.deleteMedicalRegistrationById(5L)).thenReturn(1);
        when(registrationMapper.deleteMedicalRegistrationByIds(any())).thenReturn(2);

        assertEquals(1, registrationService.deleteMedicalRegistrationById(5L));
        assertEquals(2, registrationService.deleteMedicalRegistrationByIds(new Long[]{1L, 2L}));
    }

    @Test
    @DisplayName("退号：记录不存在返回 0 且不更新")
    void refund_notFound_returns0() {
        when(registrationMapper.selectMedicalRegistrationById(9L)).thenReturn(null);

        assertEquals(0, registrationService.refund(9L));
        verify(registrationMapper, never()).updateMedicalRegistration(any());
        verify(scheduleMapper, never()).decrementUsed(anyLong());
    }

    @Test
    @DisplayName("退号：置已退号 + 释放号源 + 审计留痕")
    void refund_happyPath() {
        sample.setRegistrationId(9L);
        sample.setRegNo("GH202401010001");
        sample.setRegStatus("1");
        sample.setScheduleId(10L);
        when(registrationMapper.selectMedicalRegistrationById(9L)).thenReturn(sample);
        when(registrationMapper.updateMedicalRegistration(any())).thenReturn(1);
        when(scheduleMapper.decrementUsed(10L)).thenReturn(1);

        int r = registrationService.refund(9L);

        assertEquals(1, r);
        assertEquals("2", sample.getRegStatus());
        verify(scheduleMapper).decrementUsed(10L);
        verify(auditLogMapper).insertMedicalAuditLog(any());
    }

    @Test
    @DisplayName("开始就诊：挂号不存在抛异常")
    void startVisit_notFound_throws() {
        when(registrationMapper.selectMedicalRegistrationById(1L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> registrationService.startVisit(1L));
    }

    @Test
    @DisplayName("开始就诊：非待就诊状态抛异常")
    void startVisit_wrongStatus_throws() {
        sample.setRegStatus("1");
        when(registrationMapper.selectMedicalRegistrationById(1L)).thenReturn(sample);

        ServiceException ex = assertThrows(ServiceException.class, () -> registrationService.startVisit(1L));
        assertTrue(ex.getMessage().contains("待就诊"));
    }

    @Test
    @DisplayName("开始就诊：缺少患者信息抛异常")
    void startVisit_noPatient_throws() {
        sample.setRegStatus("0");
        sample.setDept("内科");
        sample.setDoctorName("张医生");
        when(registrationMapper.selectMedicalRegistrationById(1L)).thenReturn(sample);

        assertThrows(ServiceException.class, () -> registrationService.startVisit(1L));
    }

    @Test
    @DisplayName("开始就诊：缺少科室或医生抛异常")
    void startVisit_noDept_throws() {
        sample.setRegStatus("0");
        sample.setPatientId(100L);
        when(registrationMapper.selectMedicalRegistrationById(1L)).thenReturn(sample);

        assertThrows(ServiceException.class, () -> registrationService.startVisit(1L));
    }

    @Test
    @DisplayName("开始就诊：正常生成诊疗草稿、置已就诊并返回 diagnosisId")
    void startVisit_happyPath() {
        sample.setRegistrationId(1L);
        sample.setRegNo("GH202401010001");
        sample.setRegStatus("0");
        sample.setPatientId(100L);
        sample.setPatientName("张三");
        sample.setDept("内科");
        sample.setDoctorName("张医生");
        when(registrationMapper.selectMedicalRegistrationById(1L)).thenReturn(sample);
        when(registrationMapper.updateMedicalRegistration(any())).thenReturn(1);
        // 模拟诊疗服务在草稿上回填主键（与 MedicalDiagnosisServiceImpl 的分配器行为一致）
        when(diagnosisService.insertMedicalDiagnosis(any())).thenAnswer(inv -> {
            MedicalDiagnosis d = inv.getArgument(0);
            d.setDiagnosisId(555L);
            return 1;
        });

        Long did = registrationService.startVisit(1L);

        assertEquals(Long.valueOf(555L), did);
        assertEquals("1", sample.getRegStatus());
        verify(diagnosisService).insertMedicalDiagnosis(any());

        ArgumentCaptor<MedicalDiagnosis> cap = ArgumentCaptor.forClass(MedicalDiagnosis.class);
        verify(diagnosisService).insertMedicalDiagnosis(cap.capture());
        MedicalDiagnosis draft = cap.getValue();
        assertEquals(100L, draft.getPatientId().longValue());
        assertEquals("内科", draft.getDepartment());
        assertEquals("张三", draft.getPatientName());
        assertEquals("待完善", draft.getDiagnosisResult());
        assertEquals("1", draft.getStatus());
        verify(auditLogMapper).insertMedicalAuditLog(any());
    }
}
