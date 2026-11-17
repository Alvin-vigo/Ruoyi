package com.ruoyi.system.service.impl;

import com.ruoyi.common.config.MedicalProperties;
import com.ruoyi.system.mapper.MedicalStatisticsMapper;
import com.ruoyi.system.service.IMedicalStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    @Autowired
    private MedicalProperties medicalProperties;

    /**
     * 看板缓存：默认每小时刷新一次，避免每次打开都全表 count/sum（数据量大时卡顿）
     *
     * <p>注意：这里刻意使用<b>实例</b>字段而非 static。Spring 中本类是单例 Bean，
     * 实例缓存与静态缓存在生产环境下行为完全一致；但实例作用域可以让单元测试
     * 之间互不干扰（static 会让前一个测试的缓存污染后一个测试）。
     * 并发下若多个线程同时重算，聚合结果是幂等的，重复计算无害，故不加锁。
     */
    private static final long CACHE_TTL_MS = 60 * 60 * 1000L;
    private final Map<String, Object> cache = new ConcurrentHashMap<>(16);
    private final AtomicLong cacheTs = new AtomicLong(0);

    /**
     * 获取看板统计数据（带 1h 内存缓存 + 手动刷新）
     *
     * <p>返回值是<b>快照副本</b>而非缓存对象本身：若把内部缓存直接交给调用方，
     * 调用方一旦修改就会污染后续所有请求的看板数据，且这种污染无法自愈。
     */
    @Override
    public Map<String, Object> getDashboard(boolean forceRefresh) {
        long now = System.currentTimeMillis();
        if (!forceRefresh && !cache.isEmpty() && now - cacheTs.get() < CACHE_TTL_MS) {
            // 返回副本，避免调用方改动共享缓存
            return new HashMap<>(cache);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("patientTotal", medicalStatisticsMapper.selectPatientTotal());
        data.put("diagnosisTotal", medicalStatisticsMapper.selectDiagnosisTotal());
        data.put("drugTotal", medicalStatisticsMapper.selectDrugTotal());
        // P1-5 业务闭环指标：挂号 / 处方 / 发药 / 库存预警
        data.put("registrationTotal", medicalStatisticsMapper.selectRegistrationTotal());
        data.put("prescriptionTotal", medicalStatisticsMapper.selectPrescriptionTotal());
        data.put("dispensingTotalQty", medicalStatisticsMapper.selectDispensingTotalQty());
        // 阈值统一取自 medical.low-stock-threshold，与药品预警接口、定时任务同口径
        data.put("lowStockCount", medicalStatisticsMapper.selectLowStockCount(medicalProperties.getLowStockThreshold()));
        data.put("byDepartment", medicalStatisticsMapper.selectDiagnosisByDepartment());
        data.put("byCategory", medicalStatisticsMapper.selectDrugByCategory());
        data.put("costByDepartment", medicalStatisticsMapper.selectCostByDepartment());
        data.put("costTrend", medicalStatisticsMapper.selectCostTrend());
        data.put("genderStats", medicalStatisticsMapper.selectPatientGenderStats());
        data.put("ageStats", medicalStatisticsMapper.selectPatientAgeStats());
        cache.clear();
        cache.putAll(data);
        cacheTs.set(now);
        // 同样返回副本：本方法返回的 data 不应与外部调用方共享同一个可变对象
        return new HashMap<>(data);
    }
}
