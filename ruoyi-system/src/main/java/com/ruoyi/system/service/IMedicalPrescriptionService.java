package com.ruoyi.system.service;

import com.ruoyi.system.domain.MedicalPrescription;
import com.ruoyi.system.domain.MedicalPrescriptionItem;

import java.util.List;

/**
 * 处方信息Service接口
 *
 * @author ruoyi
 */
public interface IMedicalPrescriptionService {

    /**
     * 查询处方信息
     *
     * @param prescriptionId 处方ID
     * @return 处方信息
     */
    public MedicalPrescription selectMedicalPrescriptionById(Long prescriptionId);

    /**
     * 查询处方信息列表
     *
     * @param medicalPrescription 处方信息
     * @return 处方信息集合
     */
    public List<MedicalPrescription> selectMedicalPrescriptionList(MedicalPrescription medicalPrescription);

    /**
     * 新增处方信息（含明细与过敏校验，事务）
     *
     * @param pres 处方
     * @param items 处方明细
     * @return 结果
     */
    public int addPrescription(MedicalPrescription pres, List<MedicalPrescriptionItem> items);

    /**
     * 修改处方信息
     *
     * @param medicalPrescription 处方信息
     * @return 结果
     */
    public int updateMedicalPrescription(MedicalPrescription medicalPrescription);

    /**
     * 删除处方信息
     *
     * @param prescriptionId 处方ID
     * @return 结果
     */
    public int deleteMedicalPrescriptionById(Long prescriptionId);

    /**
     * 批量删除处方信息
     *
     * @param prescriptionIds 需要删除的处方ID数组
     * @return 结果
     */
    public int deleteMedicalPrescriptionByIds(Long[] prescriptionIds);

    /**
     * 查询处方下的明细列表
     *
     * @param prescriptionId 处方ID
     * @return 明细集合
     */
    public List<MedicalPrescriptionItem> selectItems(Long prescriptionId);

    /**
     * 发药（含库存扣减与明细更新，事务）
     *
     * @param prescriptionId 处方ID
     * @param dispenseItems 本次发药明细（含 itemId 与 quantity）
     * @param operator 操作人
     */
    public void dispense(Long prescriptionId, List<MedicalPrescriptionItem> dispenseItems, String operator);

    /**
     * 退药（逆向流程：原子回滚库存、置发药记录为已退、回退处方状态，事务）
     *
     * @param prescriptionId 处方ID
     * @param operator 操作人
     */
    public void returnDispensing(Long prescriptionId, String operator);

    /**
     * 作废处方
     *
     * @param prescriptionId 处方ID
     */
    public void cancel(Long prescriptionId);
}
