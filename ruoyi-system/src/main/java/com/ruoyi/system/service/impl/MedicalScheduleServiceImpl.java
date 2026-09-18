package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.MedicalSchedule;
import com.ruoyi.system.mapper.MedicalScheduleMapper;
import com.ruoyi.system.service.IMedicalScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 排班信息Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class MedicalScheduleServiceImpl implements IMedicalScheduleService {

    @Autowired
    private MedicalScheduleMapper medicalScheduleMapper;

    /**
     * 查询排班信息
     *
     * @param scheduleId 排班ID
     * @return 排班信息
     */
    @Override
    public MedicalSchedule selectMedicalScheduleById(Long scheduleId) {
        return medicalScheduleMapper.selectMedicalScheduleById(scheduleId);
    }

    /**
     * 查询排班信息列表
     *
     * @param medicalSchedule 排班信息
     * @return 排班信息集合
     */
    @Override
    public List<MedicalSchedule> selectMedicalScheduleList(MedicalSchedule medicalSchedule) {
        return medicalScheduleMapper.selectMedicalScheduleList(medicalSchedule);
    }

    /**
     * 新增排班信息
     *
     * @param medicalSchedule 排班信息
     * @return 结果
     */
    @Override
    public int insertMedicalSchedule(MedicalSchedule medicalSchedule) {
        return medicalScheduleMapper.insertMedicalSchedule(medicalSchedule);
    }

    /**
     * 修改排班信息
     *
     * @param medicalSchedule 排班信息
     * @return 结果
     */
    @Override
    public int updateMedicalSchedule(MedicalSchedule medicalSchedule) {
        return medicalScheduleMapper.updateMedicalSchedule(medicalSchedule);
    }

    /**
     * 删除排班信息
     *
     * @param scheduleId 排班ID
     * @return 结果
     */
    @Override
    public int deleteMedicalScheduleById(Long scheduleId) {
        return medicalScheduleMapper.deleteMedicalScheduleById(scheduleId);
    }

    /**
     * 批量删除排班信息
     *
     * @param scheduleIds 需要删除的排班ID数组
     * @return 结果
     */
    @Override
    public int deleteMedicalScheduleByIds(Long[] scheduleIds) {
        return medicalScheduleMapper.deleteMedicalScheduleByIds(scheduleIds);
    }
}
