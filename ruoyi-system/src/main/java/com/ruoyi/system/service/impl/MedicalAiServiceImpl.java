package com.ruoyi.system.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.system.domain.MedicalAiRecord;
import com.ruoyi.system.domain.MedicalChestXray;
import com.ruoyi.system.domain.MedicalRagKnowledge;
import com.ruoyi.system.domain.MedicalRiskPredict;
import com.ruoyi.system.domain.dto.AiChatRequest;
import com.ruoyi.system.domain.dto.AiRecordRequest;
import com.ruoyi.system.domain.dto.AiRiskRequest;
import com.ruoyi.system.mapper.MedicalAiRecordMapper;
import com.ruoyi.system.mapper.MedicalChestXrayMapper;
import com.ruoyi.system.mapper.MedicalRagKnowledgeMapper;
import com.ruoyi.system.mapper.MedicalRiskPredictMapper;
import com.ruoyi.system.service.IMedicalAiService;
import com.ruoyi.system.utils.AiHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 能力服务实现
 *
 * 职责：调用 AI 微服务 -> 校验业务码 -> 结果落库 -> 回传前端。
 * 关键点：AI 返回 data 中的每一条记录都会写入对应业务表，
 * 这样系统里积累的是**可追溯的历史数据**，论文的实验章节与演示才有依据。
 *
 * @author ruoyi
 */
@Service
public class MedicalAiServiceImpl implements IMedicalAiService {

    @Autowired
    private AiHttpUtil aiHttpUtil;

    @Autowired
    private MedicalAiRecordMapper aiRecordMapper;

    @Autowired
    private MedicalRiskPredictMapper riskPredictMapper;

    @Autowired
    private MedicalChestXrayMapper chestXrayMapper;

    @Autowired
    private MedicalRagKnowledgeMapper ragKnowledgeMapper;

    // ==================== 健康检查 ====================
    @Override
    public JsonNode health() {
        return aiHttpUtil.health();
    }

    // ==================== 模块① 智能病历 ====================
    @Override
    public JsonNode generateRecord(AiRecordRequest request) {
        Map<String, Object> body = new HashMap<>(8);
        body.put("patient_name", request.getPatientName());
        body.put("age", request.getAge());
        body.put("gender", request.getGender());
        body.put("chief_complaint", request.getChiefComplaint());
        // 调用患者已有档案数据，避免生成"通用病历"（过敏史/既往病史由前端从档案带入）
        body.put("allergy_history", request.getAllergyHistory() == null ? "" : request.getAllergyHistory());
        body.put("past_history", request.getPastHistory() == null ? "" : request.getPastHistory());
        if (request.getPatientId() != null) {
            body.put("patient_id", request.getPatientId());
        }

        JsonNode resp = aiHttpUtil.generateRecord(body);
        ensureSuccess(resp);
        JsonNode data = data(resp);

        // 落库：保存 AI 初稿（草稿态），医生修改后再采纳写入正式诊疗表
        MedicalAiRecord record = new MedicalAiRecord();
        record.setPatientId(request.getPatientId());
        record.setPatientName(request.getPatientName());
        record.setAge(request.getAge());
        record.setGender(request.getGender());
        record.setChiefComplaint(request.getChiefComplaint());
        record.setAiContent(str(data, "content", ""));
        record.setEngine(str(data, "engine", "unknown"));
        record.setModelName(str(data, "model", ""));
        record.setDegraded(data.has("degraded") && data.get("degraded").asBoolean() ? "1" : "0");
        record.setElapsedMs(lng(data, "elapsed_ms"));
        record.setStatus("0");
        record.setCreateBy("ai-service");
        aiRecordMapper.insertMedicalAiRecord(record);

        if (data instanceof ObjectNode) {
            ((ObjectNode) data).put("record_id", record.getRecordId());
        }
        return data;
    }

    // ==================== 模块② 风险预测 ====================
    @Override
    public JsonNode riskPredict(AiRiskRequest request) {
        if (request.getFeatures() == null || request.getFeatures().isEmpty()) {
            throw new AiHttpUtil.AiServiceException("请提供体检指标特征值");
        }
        JsonNode resp = aiHttpUtil.riskPredict(
                request.getFeatures(), request.getPatientId(), request.getDiseaseName());
        ensureSuccess(resp);
        JsonNode data = data(resp);

        MedicalRiskPredict entity = new MedicalRiskPredict();
        entity.setPatientId(request.getPatientId());
        entity.setDiseaseName(str(data, "disease_name", request.getDiseaseName()));
        entity.setRiskProb(dec(data, "prob"));
        entity.setResultLabel(String.valueOf(lngInt(data, "label")));
        entity.setRiskLevel(str(data, "risk_level", ""));
        entity.setAdvice(str(data, "advice", ""));
        entity.setThreshold(dec(data, "threshold"));
        JsonNode featuresNode = data.get("features");
        entity.setFeatureJson(featuresNode != null ? featuresNode.toString() : request.getFeatures().toString());
        JsonNode metricsNode = data.get("model_metrics");
        entity.setModelMetrics(metricsNode != null ? metricsNode.toString() : "");
        entity.setCreateBy("ai-service");
        riskPredictMapper.insertMedicalRiskPredict(entity);

        if (data instanceof ObjectNode) {
            ((ObjectNode) data).put("predict_id", entity.getPredictId());
        }
        return data;
    }

    // ==================== 模块③ 胸片初筛 ====================
    @Override
    public JsonNode chestXray(byte[] imageBytes, String filename, Long patientId) {
        JsonNode resp = aiHttpUtil.chestXray(imageBytes, filename);
        ensureSuccess(resp);
        JsonNode data = data(resp);

        MedicalChestXray entity = new MedicalChestXray();
        entity.setPatientId(patientId);
        entity.setImgUrl(str(data, "img_url", ""));
        entity.setPredictResult(str(data, "result", ""));
        entity.setResultLabel(str(data, "result_label", ""));
        entity.setConfidence(dec(data, "confidence"));
        entity.setProbNormal(dec(data, "prob_normal"));
        entity.setProbAbnormal(dec(data, "prob_abnormal"));
        entity.setDevice(str(data, "device", ""));
        entity.setElapsedMs(lng(data, "elapsed_ms"));
        entity.setCreateBy("ai-service");
        chestXrayMapper.insertMedicalChestXray(entity);

        if (data instanceof ObjectNode) {
            ((ObjectNode) data).put("xray_id", entity.getXrayId());
        }
        return data;
    }

    // ==================== 模块④ RAG 导诊 ====================
    @Override
    public JsonNode ragChat(AiChatRequest request) {
        JsonNode resp = aiHttpUtil.ragChat(request.getQuestion(), request.getTopK());
        ensureSuccess(resp);
        return data(resp);
    }

    @Override
    public JsonNode ragAddDocs(List<String> docs) {
        JsonNode resp = aiHttpUtil.ragAddDocs(docs);
        // 写入向量库后同步标记知识表（幂等：已同步的不会被选中）
        ensureSuccess(resp);
        return data(resp);
    }

    @Override
    public JsonNode riskFeatures() {
        JsonNode resp = aiHttpUtil.health();
        ensureSuccess(resp);
        JsonNode riskNode = resp.path("data").path("risk");
        return riskNode.has("features") ? riskNode.get("features") : null;
    }

    // ==================== 历史记录持久化 ====================
    @Override
    public List<MedicalAiRecord> selectAiRecordList(MedicalAiRecord record) {
        return aiRecordMapper.selectMedicalAiRecordList(record);
    }

    @Override
    public int updateAiRecord(MedicalAiRecord record) {
        return aiRecordMapper.updateMedicalAiRecord(record);
    }

    @Override
    public int saveAiRecord(MedicalAiRecord record) {
        if (record.getStatus() == null || record.getStatus().isEmpty()) {
            record.setStatus("0");
        }
        if (record.getCreateBy() == null || record.getCreateBy().isEmpty()) {
            record.setCreateBy("ai-service");
        }
        aiRecordMapper.insertMedicalAiRecord(record);
        return 1;
    }

    @Override
    public int deleteAiRecordByIds(Long[] ids) {
        return aiRecordMapper.deleteMedicalAiRecordByIds(ids);
    }

    @Override
    public List<MedicalRiskPredict> selectRiskPredictList(MedicalRiskPredict predict) {
        return riskPredictMapper.selectMedicalRiskPredictList(predict);
    }

    @Override
    public int deleteRiskPredictByIds(Long[] ids) {
        return riskPredictMapper.deleteMedicalRiskPredictByIds(ids);
    }

    @Override
    public List<MedicalChestXray> selectChestXrayList(MedicalChestXray xray) {
        return chestXrayMapper.selectMedicalChestXrayList(xray);
    }

    @Override
    public int deleteChestXrayByIds(Long[] ids) {
        // 先取出待删记录的图片地址，删库后再清理 AI 服务落盘的物理图片（防垃圾文件堆积）
        List<MedicalChestXray> records = chestXrayMapper.selectMedicalChestXrayByIds(ids);
        int rows = chestXrayMapper.deleteMedicalChestXrayByIds(ids);
        if (records != null) {
            for (MedicalChestXray r : records) {
                String url = r.getImgUrl();
                if (url != null && !url.isEmpty() && url.contains("/")) {
                    String name = url.substring(url.lastIndexOf('/') + 1);
                    aiHttpUtil.deleteUpload(name);
                }
            }
        }
        return rows;
    }

    @Override
    public List<MedicalRagKnowledge> selectKnowledgeList(MedicalRagKnowledge knowledge) {
        return ragKnowledgeMapper.selectMedicalRagKnowledgeList(knowledge);
    }

    @Override
    public int insertKnowledge(MedicalRagKnowledge knowledge) {
        // 新增内容默认未同步，等待人工点「同步到向量库」
        knowledge.setSynced("0");
        return ragKnowledgeMapper.insertMedicalRagKnowledge(knowledge);
    }

    @Override
    public int updateKnowledge(MedicalRagKnowledge knowledge) {
        // 正文变更后需重新入向量库，否则 RAG 仍用旧副本检索
        knowledge.setSynced("0");
        return ragKnowledgeMapper.updateMedicalRagKnowledge(knowledge);
    }

    @Override
    public int deleteKnowledgeByIds(Long[] ids) {
        return ragKnowledgeMapper.deleteMedicalRagKnowledgeByIds(ids);
    }

    @Override
    public int syncKnowledgeToVector() {
        List<MedicalRagKnowledge> pending = ragKnowledgeMapper.selectUnsyncedKnowledge();
        if (pending.isEmpty()) {
            return 0;
        }
        List<String> docs = new ArrayList<>(pending.size());
        Long[] ids = new Long[pending.size()];
        for (int i = 0; i < pending.size(); i++) {
            MedicalRagKnowledge item = pending.get(i);
            // 标题拼进正文，可显著提升检索命中（纯正文常缺关键医学名词）
            String doc = (item.getTitle() == null || item.getTitle().trim().isEmpty())
                    ? item.getContent()
                    : item.getTitle() + "：" + item.getContent();
            docs.add(doc);
            ids[i] = item.getKnowledgeId();
        }
        JsonNode resp = aiHttpUtil.ragAddDocs(docs);
        ensureSuccess(resp);
        return ragKnowledgeMapper.markSynced(ids);
    }

    // ==================== 私有辅助 ====================
    private void ensureSuccess(JsonNode resp) {
        if (resp == null) {
            throw new AiHttpUtil.AiServiceException("AI 微服务无响应");
        }
        JsonNode codeNode = resp.get("code");
        if (codeNode == null || codeNode.asInt() != 200) {
            String msg = resp.has("msg") ? resp.get("msg").asText() : "AI 服务返回未知错误";
            throw new AiHttpUtil.AiServiceException(msg);
        }
    }

    private JsonNode data(JsonNode resp) {
        JsonNode data = resp.get("data");
        return data == null ? resp : data;
    }

    private String str(JsonNode node, String field, String def) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? def : v.asText();
    }

    private BigDecimal dec(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.decimalValue();
    }

    private Long lng(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asLong();
    }

    private int lngInt(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? 0 : v.asInt();
    }
}
