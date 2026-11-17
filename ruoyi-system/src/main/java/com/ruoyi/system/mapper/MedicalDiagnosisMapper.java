package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MedicalDiagnosis;

import java.util.List;
import java.util.Map;

/**
 * 诊疗信息Mapper接口
 *
 * @author ruoyi
 */
public interface MedicalDiagnosisMapper {

    /**
     * 查询诊疗信息
     *
     * @param diagnosisId 诊疗ID
     * @return 诊疗信息
     */
    public MedicalDiagnosis selectMedicalDiagnosisById(Long diagnosisId);

    /**
     * 查询诊疗信息列表
     *
     * @param medicalDiagnosis 诊疗信息
     * @return 诊疗信息集合
     */
    public List<MedicalDiagnosis> selectMedicalDiagnosisList(MedicalDiagnosis medicalDiagnosis);

    /**
     * 查询下一个可用的诊疗ID（当前最大ID+1，删除后ID可复用）
     *
     * @return 下一个诊疗ID
     */
    public Long selectNextDiagnosisId();

    /**
     * 当前读（SELECT ... FOR UPDATE）取当前最大诊疗ID
     *
     * <p>与 selectNextDiagnosisId 的区别：本方法加锁并直读最新已提交版本，
     * 不走 MVCC 快照，用于在事务内配合行锁做原子 ID 分配（AC-08）。
     * 空表时返回 null，由调用方按 0 处理。
     *
     * @return 当前最大诊疗ID，表为空时返回 null
     */
    public Long selectMaxIdForUpdate();

    /**
     * 新增诊疗信息
     *
     * @param medicalDiagnosis 诊疗信息
     * @return 结果
     */
    public int insertMedicalDiagnosis(MedicalDiagnosis medicalDiagnosis);

    /**
     * 修改诊疗信息
     *
     * @param medicalDiagnosis 诊疗信息
     * @return 结果
     */
    public int updateMedicalDiagnosis(MedicalDiagnosis medicalDiagnosis);

    /**
     * 删除诊疗信息
     *
     * @param diagnosisId 诊疗ID
     * @return 结果
     */
    public int deleteMedicalDiagnosisById(Long diagnosisId);

    /**
     * 批量删除诊疗信息
     *
     * @param diagnosisIds 需要删除的诊疗ID数组
     * @return 结果
     */
    public int deleteMedicalDiagnosisByIds(Long[] diagnosisIds);

    /**
     * 根据患者ID查询诊疗记录
     *
     * @param patientId 患者ID
     * @return 诊疗信息集合
     */
    public List<MedicalDiagnosis> selectMedicalDiagnosisByPatientId(Long patientId);

    /**
     * 统计诊疗费用汇总（总数、总额、均值）
     *
     * @param medicalDiagnosis 筛选条件
     * @return 汇总结果 Map（totalCount / totalCost / avgCost）
     */
    public Map<String, Object> selectMedicalDiagnosisFeeSummary(MedicalDiagnosis medicalDiagnosis);
}
