package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.MedicalDiagnosis;
import com.ruoyi.system.mapper.MedicalDiagnosisMapper;
import com.ruoyi.system.service.IMedicalDiagnosisService;
import com.ruoyi.system.service.IMedicalIdAllocatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 诊疗信息Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class MedicalDiagnosisServiceImpl implements IMedicalDiagnosisService {

    @Autowired
    private MedicalDiagnosisMapper medicalDiagnosisMapper;

    @Autowired
    private IMedicalIdAllocatorService idAllocatorService;

    /**
     * 查询诊疗信息
     *
     * @param diagnosisId 诊疗ID
     * @return 诊疗信息
     */
    @Override
    public MedicalDiagnosis selectMedicalDiagnosisById(Long diagnosisId) {
        return medicalDiagnosisMapper.selectMedicalDiagnosisById(diagnosisId);
    }

    /**
     * 查询诊疗信息列表
     *
     * @param medicalDiagnosis 诊疗信息
     * @return 诊疗信息
     */
    @Override
    public List<MedicalDiagnosis> selectMedicalDiagnosisList(MedicalDiagnosis medicalDiagnosis) {
        return medicalDiagnosisMapper.selectMedicalDiagnosisList(medicalDiagnosis);
    }

    /**
     * 新增诊疗信息
     *
     * @param medicalDiagnosis 诊疗信息
     * @return 结果
     */
    /**
     * 新增诊疗信息
     *
     * <p>未指定主键时，走 {@link IMedicalIdAllocatorService#allocateDiagnosisId()} 原子分配，
     * 取值语义仍为「当前最大 ID + 1」，因此删除记录后新记录可复用被删的 ID（AC-08）。
     * 本方法加 {@code @Transactional} 是必需的：分配器依赖同一个事务持有行锁直到 INSERT 提交，
     * 否则并发请求会读到同一个 MAX 值并撞主键。
     *
     * @param medicalDiagnosis 诊疗信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMedicalDiagnosis(MedicalDiagnosis medicalDiagnosis) {
        if (medicalDiagnosis.getDiagnosisId() == null) {
            medicalDiagnosis.setDiagnosisId(idAllocatorService.allocateDiagnosisId());
        }
        return medicalDiagnosisMapper.insertMedicalDiagnosis(medicalDiagnosis);
    }

    /**
     * 修改诊疗信息
     *
     * @param medicalDiagnosis 诊疗信息
     * @return 结果
     */
    @Override
    public int updateMedicalDiagnosis(MedicalDiagnosis medicalDiagnosis) {
        return medicalDiagnosisMapper.updateMedicalDiagnosis(medicalDiagnosis);
    }

    /**
     * 批量删除诊疗信息
     *
     * @param diagnosisIds 需要删除的诊疗ID
     * @return 结果
     */
    @Override
    public int deleteMedicalDiagnosisByIds(Long[] diagnosisIds) {
        return medicalDiagnosisMapper.deleteMedicalDiagnosisByIds(diagnosisIds);
    }

    /**
     * 根据患者ID查询诊疗记录
     *
     * @param patientId 患者ID
     * @return 诊疗信息集合
     */
    @Override
    public List<MedicalDiagnosis> selectMedicalDiagnosisByPatientId(Long patientId) {
        return medicalDiagnosisMapper.selectMedicalDiagnosisByPatientId(patientId);
    }

    /**
     * 统计诊疗费用汇总（总数、总额、均值）
     *
     * @param medicalDiagnosis 筛选条件
     * @return 汇总结果 Map（totalCount / totalCost / avgCost）
     */
    @Override
    public Map<String, Object> selectMedicalDiagnosisFeeSummary(MedicalDiagnosis medicalDiagnosis) {
        return medicalDiagnosisMapper.selectMedicalDiagnosisFeeSummary(medicalDiagnosis);
    }
}
