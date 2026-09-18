package com.ruoyi.system.service;

import java.util.Map;

/**
 * 医疗数据看板统计Service接口
 *
 * @author ruoyi
 */
public interface IMedicalStatisticsService {

    /**
     * 获取看板统计数据
     *
     * @param forceRefresh true 时跳过缓存强制重算
     * @return 包含患者总数、诊疗总数、药品总数、科室分布、药品分类分布
     */
    Map<String, Object> getDashboard(boolean forceRefresh);
}
