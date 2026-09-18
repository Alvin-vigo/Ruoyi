package com.ruoyi.system.service.impl;

import com.ruoyi.system.mapper.MedicalStatisticsMapper;
import com.ruoyi.system.service.IMedicalStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 医疗数据看板统计Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class MedicalStatisticsServiceImpl implements IMedicalStatisticsService {

    @Autowired
    private MedicalStatisticsMapper medicalStatisticsMapper;

    /** 看板缓存：默认每小时刷新一次，避免每次打开都全表 count/sum（数据量大时卡顿） */
    private static final long CACHE_TTL_MS = 60 * 60 * 1000L;
    private static final Map<String, Object> cache = new HashMap<>(8);
    private static final AtomicLong cacheTs = new AtomicLong(0);

    /**
     * 获取看板统计数据（带 1h 内存缓存 + 手动刷新）
     */
    @Override
    public Map<String, Object> getDashboard(boolean forceRefresh) {
        long now = System.currentTimeMillis();
        if (!forceRefresh && !cache.isEmpty() && now - cacheTs.get() < CACHE_TTL_MS) {
            return cache;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("patientTotal", medicalStatisticsMapper.selectPatientTotal());
        data.put("diagnosisTotal", medicalStatisticsMapper.selectDiagnosisTotal());
        data.put("drugTotal", medicalStatisticsMapper.selectDrugTotal());
        // P1-5 业务闭环指标：挂号 / 处方 / 发药 / 库存预警
        data.put("registrationTotal", medicalStatisticsMapper.selectRegistrationTotal());
        data.put("prescriptionTotal", medicalStatisticsMapper.selectPrescriptionTotal());
        data.put("dispensingTotalQty", medicalStatisticsMapper.selectDispensingTotalQty());
        data.put("lowStockCount", medicalStatisticsMapper.selectLowStockCount(50));
        data.put("byDepartment", medicalStatisticsMapper.selectDiagnosisByDepartment());
        data.put("byCategory", medicalStatisticsMapper.selectDrugByCategory());
        data.put("costByDepartment", medicalStatisticsMapper.selectCostByDepartment());
        data.put("costTrend", medicalStatisticsMapper.selectCostTrend());
        data.put("genderStats", medicalStatisticsMapper.selectPatientGenderStats());
        data.put("ageStats", medicalStatisticsMapper.selectPatientAgeStats());
        cache.clear();
        cache.putAll(data);
        cacheTs.set(now);
        return data;
    }
}
