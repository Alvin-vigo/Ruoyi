package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ruoyi.system.domain.MedicalAuditLog;
import com.ruoyi.system.domain.MedicalDiagnosis;
import com.ruoyi.system.domain.MedicalRegistration;
import com.ruoyi.system.mapper.MedicalAuditLogMapper;
import com.ruoyi.system.mapper.MedicalRegistrationMapper;
import com.ruoyi.system.mapper.MedicalScheduleMapper;
import com.ruoyi.system.service.IMedicalDiagnosisService;
import com.ruoyi.system.service.IMedicalRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 挂号信息Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class MedicalRegistrationServiceImpl implements IMedicalRegistrationService {

    @Autowired
    private MedicalRegistrationMapper medicalRegistrationMapper;

    @Autowired
    private MedicalScheduleMapper medicalScheduleMapper;

    @Autowired
    private MedicalAuditLogMapper auditLogMapper;

    @Autowired
    private IMedicalDiagnosisService diagnosisService;

    /**
     * 查询挂号信息
     *
     * @param registrationId 挂号ID
     * @return 挂号信息
     */
    @Override
    public MedicalRegistration selectMedicalRegistrationById(Long registrationId) {
        return medicalRegistrationMapper.selectMedicalRegistrationById(registrationId);
    }

    /**
     * 查询挂号信息列表
     *
     * @param medicalRegistration 挂号信息
     * @return 挂号信息集合
     */
    @Override
    public List<MedicalRegistration> selectMedicalRegistrationList(MedicalRegistration medicalRegistration) {
        return medicalRegistrationMapper.selectMedicalRegistrationList(medicalRegistration);
    }

    /**
     * 新增挂号信息（占用号源，号源已满抛异常）
     *
     * @param medicalRegistration 挂号信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertMedicalRegistration(MedicalRegistration medicalRegistration) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String datePart = sdf.format(new Date());
        String randomPart = String.format("%04d", new Random().nextInt(9000) + 1000);
        medicalRegistration.setRegNo("GH" + datePart + randomPart);
        if (medicalRegistration.getRegStatus() == null || medicalRegistration.getRegStatus().isEmpty()) {
            medicalRegistration.setRegStatus("0");
        }
        if (medicalRegistration.getRegTime() == null) {
            medicalRegistration.setRegTime(new Date());
        }
        if (medicalRegistration.getScheduleId() != null) {
            int rows = medicalScheduleMapper.incrementUsed(medicalRegistration.getScheduleId());
            if (rows == 0) {
                throw new ServiceException("号源已满，无法挂号");
            }
        }
        return medicalRegistrationMapper.insertMedicalRegistration(medicalRegistration);
    }

    /**
     * 修改挂号信息
     *
     * @param medicalRegistration 挂号信息
     * @return 结果
     */
    @Override
    public int updateMedicalRegistration(MedicalRegistration medicalRegistration) {
        return medicalRegistrationMapper.updateMedicalRegistration(medicalRegistration);
    }

    /**
     * 删除挂号信息
     *
     * @param registrationId 挂号ID
     * @return 结果
     */
    @Override
    public int deleteMedicalRegistrationById(Long registrationId) {
        return medicalRegistrationMapper.deleteMedicalRegistrationById(registrationId);
    }

    /**
     * 批量删除挂号信息
     *
     * @param registrationIds 需要删除的挂号ID数组
     * @return 结果
     */
    @Override
    public int deleteMedicalRegistrationByIds(Long[] registrationIds) {
        return medicalRegistrationMapper.deleteMedicalRegistrationByIds(registrationIds);
    }

    /**
     * 退号：置状态为已退号并释放号源
     *
     * @param registrationId 挂号ID
     * @return 结果
     */
    @Override
    @Transactional
    public int refund(Long registrationId) {
        MedicalRegistration reg = medicalRegistrationMapper.selectMedicalRegistrationById(registrationId);
        if (reg == null) {
            return 0;
        }
        reg.setRegStatus("2");
        medicalRegistrationMapper.updateMedicalRegistration(reg);
        if (reg.getScheduleId() != null) {
            medicalScheduleMapper.decrementUsed(reg.getScheduleId());
        }
        // P1-6 关键操作审计：退号留痕
        audit("退号", "退号", registrationId, reg.getRegNo(), "释放号源，恢复可挂号名额");
        return 1;
    }

    /**
     * 开始就诊（P1-3 串联）：由挂号记录自动生成一条诊疗草稿
     *
     * 预填患者、科室、医生、就诊时间，诊断结果占位为「待完善」；
     * 同时把挂号状态从「待就诊」置为「已就诊」，并落审计日志。
     *
     * @param registrationId 挂号ID
     * @return 新生成的诊疗记录ID
     */
    @Override
    @Transactional
    public Long startVisit(Long registrationId) {
        MedicalRegistration reg = medicalRegistrationMapper.selectMedicalRegistrationById(registrationId);
        if (reg == null) {
            throw new ServiceException("挂号记录不存在：registrationId=" + registrationId);
        }
        if (!"0".equals(reg.getRegStatus())) {
            throw new ServiceException("只有「待就诊」状态的挂号才能开始就诊（当前状态：" + reg.getRegStatus() + "）");
        }
        if (reg.getPatientId() == null) {
            throw new ServiceException("该挂号记录缺少患者信息，无法生成诊疗记录");
        }
        // medical_diagnosis.department / doctor_name 均为 NOT NULL，缺了会导致插入失败
        if (reg.getDept() == null || reg.getDept().isEmpty()
                || reg.getDoctorName() == null || reg.getDoctorName().isEmpty()) {
            throw new ServiceException("该挂号记录缺少科室或接诊医生信息，无法生成诊疗记录");
        }

        MedicalDiagnosis draft = new MedicalDiagnosis();
        draft.setPatientId(reg.getPatientId());
        draft.setPatientName(reg.getPatientName());
        draft.setRegistrationId(reg.getRegistrationId());
        draft.setVisitDate(new Date());
        draft.setDepartment(reg.getDept());
        draft.setDoctorName(reg.getDoctorName());
        draft.setStatus("1");
        // 诊断结果为 NOT NULL，草稿阶段用占位值，医生在诊疗管理页补全
        draft.setDiagnosisResult("待完善");
        draft.setChiefComplaint("");
        draft.setPresentIllness("");
        draft.setTreatmentPlan("");
        diagnosisService.insertMedicalDiagnosis(draft);

        reg.setRegStatus("1");
        medicalRegistrationMapper.updateMedicalRegistration(reg);

        audit("接诊", "开始就诊", registrationId, reg.getRegNo(),
                "由挂号自动生成诊疗草稿，diagnosisId=" + draft.getDiagnosisId());
        return draft.getDiagnosisId();
    }

    /**
     * 写入操作审计日志。审计失败不影响主业务流程。
     */
    private void audit(String module, String action, Long bizId, String bizNo, String detail) {
        try {
            MedicalAuditLog log = new MedicalAuditLog();
            String user = "system";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                user = auth.getName();
            }
            log.setModule(module);
            log.setAction(action);
            log.setBizId(bizId);
            log.setBizNo(bizNo);
            log.setOperator(user);
            log.setDetail(detail);
            log.setCreateBy(user);
            auditLogMapper.insertMedicalAuditLog(log);
        } catch (Exception e) {
            // 审计不影响主流程
        }
    }
}
