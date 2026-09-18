package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MedicalAiRecord;

import java.util.List;

/**
 * AI 病历生成记录 数据访问层
 *
 * @author ruoyi
 */
public interface MedicalAiRecordMapper {

    MedicalAiRecord selectMedicalAiRecordById(Long recordId);

    List<MedicalAiRecord> selectMedicalAiRecordList(MedicalAiRecord medicalAiRecord);

    int insertMedicalAiRecord(MedicalAiRecord medicalAiRecord);

    int updateMedicalAiRecord(MedicalAiRecord medicalAiRecord);

    int deleteMedicalAiRecordById(Long recordId);

    int deleteMedicalAiRecordByIds(Long[] ids);
}
