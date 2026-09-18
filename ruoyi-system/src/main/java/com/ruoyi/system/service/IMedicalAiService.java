package com.ruoyi.system.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruoyi.system.domain.MedicalAiRecord;
import com.ruoyi.system.domain.MedicalChestXray;
import com.ruoyi.system.domain.MedicalRagKnowledge;
import com.ruoyi.system.domain.MedicalRiskPredict;
import com.ruoyi.system.domain.dto.AiChatRequest;
import com.ruoyi.system.domain.dto.AiRecordRequest;
import com.ruoyi.system.domain.dto.AiRiskRequest;

import java.util.List;

/**
 * AI 能力服务接口（Java 业务层 -> FastAPI AI 微服务）
 *
 * @author ruoyi
 */
public interface IMedicalAiService {

    /** AI 服务与各模块的就绪状态，联调第一步看这个 */
    JsonNode health();

    /** 模块①：生成电子病历初稿，并保存生成记录 */
    JsonNode generateRecord(AiRecordRequest request);

    /** 模块②：疾病风险预测，并保存预测记录 */
    JsonNode riskPredict(AiRiskRequest request);

    /** 模块③：胸片影像初筛，并保存检测记录 */
    JsonNode chestXray(byte[] imageBytes, String filename, Long patientId);

    /** 模块④：RAG 导诊问答 */
    JsonNode ragChat(AiChatRequest request);

    /** 模块④：向知识库追加条目 */
    JsonNode ragAddDocs(List<String> docs);

    /** 风险模型要求的特征顺序（透传给前端动态生成表单） */
    JsonNode riskFeatures();

    // ==================== 历史记录持久化（供列表/编辑/删除） ====================

    List<MedicalAiRecord> selectAiRecordList(MedicalAiRecord record);

    int updateAiRecord(MedicalAiRecord record);

    /**
     * 保存一条已生成的 AI 病历记录（供流式生成后落库，避免二次调用大模型）。
     * 入参由前端在流式结束后回传，行为等价于 generateRecord 的落库逻辑，但不触发推理。
     */
    int saveAiRecord(MedicalAiRecord record);

    int deleteAiRecordByIds(Long[] ids);

    List<MedicalRiskPredict> selectRiskPredictList(MedicalRiskPredict predict);

    int deleteRiskPredictByIds(Long[] ids);

    List<MedicalChestXray> selectChestXrayList(MedicalChestXray xray);

    int deleteChestXrayByIds(Long[] ids);

    List<MedicalRagKnowledge> selectKnowledgeList(MedicalRagKnowledge knowledge);

    int insertKnowledge(MedicalRagKnowledge knowledge);

    int updateKnowledge(MedicalRagKnowledge knowledge);

    int deleteKnowledgeByIds(Long[] ids);

    /**
     * 把本地知识表中「未同步」的条目推送进 Chroma 向量库，并回写同步标记。
     *
     * @return 本次同步的条目数
     */
    int syncKnowledgeToVector();
}
