package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MedicalRagKnowledge;

import java.util.List;

/**
 * RAG 导诊知识条目 数据访问层
 *
 * @author ruoyi
 */
public interface MedicalRagKnowledgeMapper {

    MedicalRagKnowledge selectMedicalRagKnowledgeById(Long knowledgeId);

    List<MedicalRagKnowledge> selectMedicalRagKnowledgeList(MedicalRagKnowledge knowledge);

    /** 查询尚未同步进向量库的条目 */
    List<MedicalRagKnowledge> selectUnsyncedKnowledge();

    int insertMedicalRagKnowledge(MedicalRagKnowledge knowledge);

    int updateMedicalRagKnowledge(MedicalRagKnowledge knowledge);

    /** 批量标记为已同步 */
    int markSynced(Long[] ids);

    int deleteMedicalRagKnowledgeById(Long knowledgeId);

    int deleteMedicalRagKnowledgeByIds(Long[] ids);
}
