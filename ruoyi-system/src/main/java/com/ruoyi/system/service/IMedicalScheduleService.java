package com.ruoyi.system.service;

import com.ruoyi.system.domain.MedicalSchedule;

import java.util.List;

/**
 * 排班信息Service接口
 *
 * @author ruoyi
 */
public interface IMedicalScheduleService {

    /**
     * 查询排班信息
     *
     * @param scheduleId 排班ID
     * @return 排班信息
     */
    public MedicalSchedule selectMedicalScheduleById(Long scheduleId);

    /**
     * 查询排班信息列表
     *
     * @param medicalSchedule 排班信息
     * @return 排班信息集合
     */
    public List<MedicalSchedule> selectMedicalScheduleList(MedicalSchedule medicalSchedule);

    /**
     * 新增排班信息
     *
     * @param medicalSchedule 排班信息
     * @return 结果
     */
    public int insertMedicalSchedule(MedicalSchedule medicalSchedule);

    /**
     * 修改排班信息
     *
     * @param medicalSchedule 排班信息
     * @return 结果
     */
    public int updateMedicalSchedule(MedicalSchedule medicalSchedule);

    /**
     * 删除排班信息
     *
     * @param scheduleId 排班ID
     * @return 结果
     */
    public int deleteMedicalScheduleById(Long scheduleId);

    /**
     * 批量删除排班信息
     *
     * @param scheduleIds 需要删除的排班ID数组
     * @return 结果
     */
    public int deleteMedicalScheduleByIds(Long[] scheduleIds);
}
