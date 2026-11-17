package com.ruoyi.system.service.impl;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.MedicalDispensing;
import com.ruoyi.system.domain.MedicalDrug;
import com.ruoyi.system.domain.MedicalPatient;
import com.ruoyi.system.domain.MedicalPrescription;
import com.ruoyi.system.domain.MedicalPrescriptionItem;
import com.ruoyi.system.domain.MedicalPriceItem;
import com.ruoyi.system.mapper.MedicalDispensingMapper;
import com.ruoyi.system.mapper.MedicalDrugMapper;
import com.ruoyi.system.mapper.MedicalPrescriptionItemMapper;
import com.ruoyi.system.mapper.MedicalPrescriptionMapper;
import com.ruoyi.system.mapper.MedicalPriceItemMapper;
import com.ruoyi.system.mapper.MedicalAuditLogMapper;
import com.ruoyi.system.service.IMedicalPatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 处方信息 Service 单元测试（纯 Mockito，不依赖 Spring 容器 / 数据库）
 *
 * <p>覆盖 AC-04/AC-05（自动计价 + 过敏校验）、AC-06（发药原子扣库存 + 效期/库存拦截）、
 * AC-07（退药逆向回滚）、以及处方状态机（待发药/部分发药/已发药/作废）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("处方信息 Service 单元测试")
class MedicalPrescriptionServiceImplTest {

    @Mock
    private MedicalPrescriptionMapper prescriptionMapper;
    @Mock
    private MedicalPrescriptionItemMapper itemMapper;
    @Mock
    private MedicalDispensingMapper dispensingMapper;
    @Mock
    private MedicalDrugMapper drugMapper;
    @Mock
    private MedicalPriceItemMapper priceItemMapper;
    @Mock
    private IMedicalPatientService patientService;
    @Mock
    private RedisCache redisCache;
    @Mock
    private MedicalAuditLogMapper auditLogMapper;

    @InjectMocks
    private MedicalPrescriptionServiceImpl prescriptionService;

    private static final long FUTURE = System.currentTimeMillis() + 365L * 86400000L;
    private static final long PAST = System.currentTimeMillis() - 86400000L;

    @BeforeEach
    void setUp() {
        // 发药防重锁默认放行，个别用例再覆盖
        when(redisCache.tryLock(anyString(), anyString(), anyLong(), any())).thenReturn(true);
    }

    // ===================== 开方（自动计价 + 过敏校验） =====================

    @Test
    @DisplayName("新增处方：按价目表自动计价 + 补全患者姓名/过敏史 + 生成 CF 单号 + 过敏命中留痕")
    void addPrescription_autoPricingAndAllergy() {
        MedicalPatient p = new MedicalPatient();
        p.setPatientName("李四");
        p.setAllergyHistory("青霉素过敏");
        when(patientService.selectMedicalPatientById(300L)).thenReturn(p);

        MedicalPriceItem price = new MedicalPriceItem();
        price.setPrice(new BigDecimal("10.00"));
        when(priceItemMapper.selectPriceByDrugName("阿莫西林胶囊")).thenReturn(price);

        MedicalPrescription pres = new MedicalPrescription();
        pres.setPatientId(300L); // 不传姓名/过敏史，由服务补全
        MedicalPrescriptionItem item = new MedicalPrescriptionItem();
        item.setDrugName("阿莫西林胶囊");
        item.setQuantity(2);

        when(prescriptionMapper.insertMedicalPrescription(any())).thenReturn(1);
        when(itemMapper.insertMedicalPrescriptionItem(any())).thenReturn(1);

        int rows = prescriptionService.addPrescription(pres, Arrays.asList(item));

        assertEquals(1, rows);
        assertEquals(0, pres.getTotalAmount().compareTo(new BigDecimal("20.00")), "2 * 10.00 = 20.00，按价目表自动计价");
        assertTrue(pres.getPrescriptionNo() != null && pres.getPrescriptionNo().startsWith("CF"), "处方单号应以 CF 开头");
        assertEquals("0", pres.getPrescriptionStatus());
        assertEquals("青霉素过敏", pres.getPatientAllergy());
        assertEquals("李四", pres.getPatientName());
        assertNotNull(item.getAllergyWarning(), "患者青霉素过敏 + 开阿莫西林应给出过敏警示");
        verify(itemMapper).insertMedicalPrescriptionItem(item);
        verify(auditLogMapper).insertMedicalAuditLog(any());
    }

    @Test
    @DisplayName("新增处方：无过敏史时过敏警示为空")
    void addPrescription_noAllergy_noWarning() {
        MedicalPatient p = new MedicalPatient();
        p.setPatientName("王五");
        p.setAllergyHistory(""); // 无过敏史
        when(patientService.selectMedicalPatientById(301L)).thenReturn(p);
        when(priceItemMapper.selectPriceByDrugName(anyString())).thenReturn(null);

        MedicalPrescription pres = new MedicalPrescription();
        pres.setPatientId(301L);
        MedicalPrescriptionItem item = new MedicalPrescriptionItem();
        item.setDrugName("维生素C");
        item.setQuantity(1);
        when(prescriptionMapper.insertMedicalPrescription(any())).thenReturn(1);
        when(itemMapper.insertMedicalPrescriptionItem(any())).thenReturn(1);

        prescriptionService.addPrescription(pres, Arrays.asList(item));

        assertNull(item.getAllergyWarning(), "无过敏史不应产生过敏警示");
    }

    // ===================== 发药（原子扣库存 + 拦截） =====================

    @Test
    @DisplayName("发药：空明细直接返回，不触发任何库存/审计操作")
    void dispense_empty_noop() {
        prescriptionService.dispense(1001L, null, "op");
        prescriptionService.dispense(1001L, Collections.emptyList(), "op");

        verify(redisCache, never()).tryLock(anyString(), anyString(), anyLong(), any());
        verify(drugMapper, never()).decrementStock(anyLong(), anyInt());
        verify(auditLogMapper, never()).insertMedicalAuditLog(any());
    }

    @Test
    @DisplayName("发药：正常扣库存 + 落发药记录 + 状态置已发药(2)")
    void dispense_happyPath_setsDispensed() {
        MedicalPrescriptionItem dbItem = new MedicalPrescriptionItem();
        dbItem.setItemId(1L);
        dbItem.setDrugId(9L);
        dbItem.setDrugName("阿莫西林胶囊");
        dbItem.setQuantity(2);
        dbItem.setDispensedQuantity(0);
        when(itemMapper.selectMedicalPrescriptionItemById(1L)).thenReturn(dbItem);

        MedicalDrug drug = new MedicalDrug();
        drug.setDrugId(9L);
        drug.setDrugName("阿莫西林胶囊");
        drug.setExpiryDate(new Date(FUTURE));
        when(drugMapper.selectMedicalDrugById(9L)).thenReturn(drug);
        when(drugMapper.decrementStock(9L, 2)).thenReturn(1);

        // 全部明细已发完 -> 状态 2
        MedicalPrescriptionItem done = new MedicalPrescriptionItem();
        done.setQuantity(2);
        done.setDispensedQuantity(2);
        when(itemMapper.selectByPrescriptionId(1001L)).thenReturn(Arrays.asList(done));

        MedicalPrescriptionItem dispenseItem = new MedicalPrescriptionItem();
        dispenseItem.setItemId(1L);
        dispenseItem.setQuantity(2);

        prescriptionService.dispense(1001L, Arrays.asList(dispenseItem), "op");

        verify(drugMapper).decrementStock(9L, 2);
        verify(dispensingMapper).insertMedicalDispensing(any());
        verify(itemMapper).updateDispensed(any());
        verify(prescriptionMapper).updatePrescriptionStatus(1001L, "2");
        verify(auditLogMapper).insertMedicalAuditLog(any());
    }

    @Test
    @DisplayName("发药：部分发药后状态置部分发药(1)")
    void dispense_partial_setsPartial() {
        MedicalPrescriptionItem dbItem = new MedicalPrescriptionItem();
        dbItem.setItemId(1L);
        dbItem.setDrugId(9L);
        dbItem.setDrugName("阿莫西林胶囊");
        dbItem.setQuantity(2);
        dbItem.setDispensedQuantity(0);
        when(itemMapper.selectMedicalPrescriptionItemById(1L)).thenReturn(dbItem);

        MedicalDrug drug = new MedicalDrug();
        drug.setDrugId(9L);
        drug.setDrugName("阿莫西林胶囊");
        drug.setExpiryDate(new Date(FUTURE));
        when(drugMapper.selectMedicalDrugById(9L)).thenReturn(drug);
        when(drugMapper.decrementStock(9L, 2)).thenReturn(1);

        MedicalPrescriptionItem done = new MedicalPrescriptionItem();
        done.setQuantity(2);
        done.setDispensedQuantity(2);
        MedicalPrescriptionItem todo = new MedicalPrescriptionItem();
        todo.setQuantity(5);
        todo.setDispensedQuantity(0);
        when(itemMapper.selectByPrescriptionId(1005L)).thenReturn(Arrays.asList(done, todo));

        MedicalPrescriptionItem dispenseItem = new MedicalPrescriptionItem();
        dispenseItem.setItemId(1L);
        dispenseItem.setQuantity(2);

        prescriptionService.dispense(1005L, Arrays.asList(dispenseItem), "op");

        verify(prescriptionMapper).updatePrescriptionStatus(1005L, "1");
    }

    @Test
    @DisplayName("发药：过期药品禁止发药（P0-1 医疗安全红线），不扣库存")
    void dispense_expiredDrug_throws() {
        MedicalPrescriptionItem dbItem = new MedicalPrescriptionItem();
        dbItem.setItemId(1L);
        dbItem.setDrugId(9L);
        dbItem.setDrugName("阿莫西林胶囊");
        dbItem.setQuantity(2);
        dbItem.setDispensedQuantity(0);
        when(itemMapper.selectMedicalPrescriptionItemById(1L)).thenReturn(dbItem);

        MedicalDrug drug = new MedicalDrug();
        drug.setDrugId(9L);
        drug.setDrugName("阿莫西林胶囊");
        drug.setExpiryDate(new Date(PAST));
        when(drugMapper.selectMedicalDrugById(9L)).thenReturn(drug);

        MedicalPrescriptionItem dispenseItem = new MedicalPrescriptionItem();
        dispenseItem.setItemId(1L);
        dispenseItem.setQuantity(1);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> prescriptionService.dispense(1002L, Arrays.asList(dispenseItem), "op"));
        assertTrue(ex.getMessage().contains("已过期"));
        verify(drugMapper, never()).decrementStock(anyLong(), anyInt());
        verify(dispensingMapper, never()).insertMedicalDispensing(any());
    }

    @Test
    @DisplayName("发药：库存不足（原子扣减返回0）抛异常")
    void dispense_stockInsufficient_throws() {
        MedicalPrescriptionItem dbItem = new MedicalPrescriptionItem();
        dbItem.setItemId(1L);
        dbItem.setDrugId(9L);
        dbItem.setDrugName("阿莫西林胶囊");
        dbItem.setQuantity(2);
        dbItem.setDispensedQuantity(0);
        when(itemMapper.selectMedicalPrescriptionItemById(1L)).thenReturn(dbItem);

        MedicalDrug drug = new MedicalDrug();
        drug.setDrugId(9L);
        drug.setDrugName("阿莫西林胶囊");
        drug.setExpiryDate(new Date(FUTURE));
        when(drugMapper.selectMedicalDrugById(9L)).thenReturn(drug);
        when(drugMapper.decrementStock(9L, 1)).thenReturn(0); // 库存不足

        MedicalPrescriptionItem dispenseItem = new MedicalPrescriptionItem();
        dispenseItem.setItemId(1L);
        dispenseItem.setQuantity(1);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> prescriptionService.dispense(1003L, Arrays.asList(dispenseItem), "op"));
        assertTrue(ex.getMessage().contains("库存不足"));
        verify(drugMapper).decrementStock(9L, 1);
        verify(drugMapper, never()).incrementStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("发药：发药数量超过开方数量抛异常（在查药品前拦截）")
    void dispense_qtyExceeds_throws() {
        MedicalPrescriptionItem dbItem = new MedicalPrescriptionItem();
        dbItem.setItemId(1L);
        dbItem.setDrugId(9L);
        dbItem.setDrugName("阿莫西林胶囊");
        dbItem.setQuantity(2);
        dbItem.setDispensedQuantity(0);
        when(itemMapper.selectMedicalPrescriptionItemById(1L)).thenReturn(dbItem);

        MedicalPrescriptionItem dispenseItem = new MedicalPrescriptionItem();
        dispenseItem.setItemId(1L);
        dispenseItem.setQuantity(5); // > 2

        ServiceException ex = assertThrows(ServiceException.class,
                () -> prescriptionService.dispense(1004L, Arrays.asList(dispenseItem), "op"));
        assertTrue(ex.getMessage().contains("发药数量超过开方数量"));
        verify(drugMapper, never()).selectMedicalDrugById(anyLong());
        verify(drugMapper, never()).decrementStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("发药：Redis 分布式锁获取失败（被他人持有）直接拒绝，不扣库存")
    void dispense_redisLockHeld_throws() {
        when(redisCache.tryLock(anyString(), anyString(), anyLong(), any())).thenReturn(false);

        MedicalPrescriptionItem dispenseItem = new MedicalPrescriptionItem();
        dispenseItem.setItemId(1L);
        dispenseItem.setQuantity(1);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> prescriptionService.dispense(1006L, Arrays.asList(dispenseItem), "op"));
        assertTrue(ex.getMessage().contains("操作过于频繁"));
        verify(drugMapper, never()).decrementStock(anyLong(), anyInt());
    }

    // ===================== 退药（逆向回滚） =====================

    @Test
    @DisplayName("退药：回加库存 + 置发药记录为已退 + 处方恢复待发药(0) + 审计")
    void returnDispensing_happyPath_rollsBack() {
        MedicalDispensing rec = new MedicalDispensing();
        rec.setPrescriptionId(2001L);
        rec.setItemId(1L);
        rec.setDrugId(9L);
        rec.setQuantity(3);
        rec.setStatus("0"); // 未退，属于可退
        when(dispensingMapper.selectByPrescriptionId(2001L)).thenReturn(Arrays.asList(rec));
        when(drugMapper.incrementStock(9L, 3)).thenReturn(1);

        MedicalPrescriptionItem item = new MedicalPrescriptionItem();
        item.setDispensedQuantity(3);
        when(itemMapper.selectMedicalPrescriptionItemById(1L)).thenReturn(item);

        prescriptionService.returnDispensing(2001L, "op");

        verify(drugMapper).incrementStock(9L, 3);
        verify(dispensingMapper).updateMedicalDispensing(any());
        verify(itemMapper).updateDispensed(any());
        verify(prescriptionMapper).updatePrescriptionStatus(2001L, "0");
        verify(auditLogMapper).insertMedicalAuditLog(any());
    }

    @Test
    @DisplayName("退药：无已发药记录抛异常，不回滚库存")
    void returnDispensing_noActive_throws() {
        MedicalDispensing rec = new MedicalDispensing();
        rec.setStatus("1"); // 已退，不计入可退
        when(dispensingMapper.selectByPrescriptionId(2002L)).thenReturn(Arrays.asList(rec));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> prescriptionService.returnDispensing(2002L, "op"));
        assertTrue(ex.getMessage().contains("无已发药记录"));
        verify(drugMapper, never()).incrementStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("作废处方：状态置 3")
    void cancel_setsStatus3() {
        prescriptionService.cancel(3001L);
        verify(prescriptionMapper).updatePrescriptionStatus(3001L, "3");
    }

    @Test
    @DisplayName("查询处方明细委托 Mapper")
    void selectItems_delegates() {
        List<MedicalPrescriptionItem> list = new ArrayList<>();
        when(itemMapper.selectByPrescriptionId(1L)).thenReturn(list);
        assertEquals(list, prescriptionService.selectItems(1L));
        verify(itemMapper).selectByPrescriptionId(1L);
    }
}
