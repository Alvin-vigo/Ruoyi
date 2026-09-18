package com.ruoyi.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.IMedicalStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 医疗数据看板统计Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/medical/statistics")
public class StatisticsController {

    @Autowired
    private IMedicalStatisticsService medicalStatisticsService;

    /**
     * 获取医疗数据看板统计
     * @param refresh true 时跳过缓存强制重算（手动点击刷新）
     */
    @GetMapping("/dashboard")
    public AjaxResult dashboard(@RequestParam(required = false) Boolean refresh) {
        Map<String, Object> data = medicalStatisticsService.getDashboard(refresh != null && refresh);
        return AjaxResult.success(data);
    }
}
