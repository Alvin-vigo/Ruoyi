package com.ruoyi.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.system.domain.MedicalAiRecord;
import com.ruoyi.system.domain.MedicalChestXray;
import com.ruoyi.system.domain.MedicalRagKnowledge;
import com.ruoyi.system.domain.MedicalRiskPredict;
import com.ruoyi.system.domain.dto.AiChatRequest;
import com.ruoyi.system.domain.dto.AiRecordRequest;
import com.ruoyi.system.domain.dto.AiRiskRequest;
import com.ruoyi.system.service.IMedicalAiService;
import com.ruoyi.system.utils.AiHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 医疗 AI 能力 Controller（转发至 Python FastAPI 微服务）
 *
 * 统一异常出口：AI 服务不可用或返回非 200 时，转成 AjaxResult.error + 可读中文提示，
 * 前端直接展示 msg 即可，不会看到堆栈。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/medical/ai")
public class MedicalAiController {

    @Autowired
    private IMedicalAiService medicalAiService;

    /**
     * AI 微服务与各模块就绪状态（联调先看这个）
     */
    @GetMapping("/health")
    public AjaxResult health() {
        try {
            return AjaxResult.success(medicalAiService.health());
        } catch (AiHttpUtil.AiServiceException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 模块①：智能生成电子病历初稿
     */
    @PostMapping("/generateRecord")
    public AjaxResult generateRecord(@RequestBody AiRecordRequest request) {
        if (request.getChiefComplaint() == null || request.getChiefComplaint().trim().isEmpty()) {
            return AjaxResult.error("请输入主诉后再生成病历");
        }
        if (request.getPatientName() == null || request.getPatientName().trim().isEmpty()) {
            return AjaxResult.error("请输入患者姓名");
        }
        try {
            return AjaxResult.success("生成成功", medicalAiService.generateRecord(request));
        } catch (AiHttpUtil.AiServiceException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 模块②：常见病风险预测
     */
    @PostMapping("/riskPredict")
    public AjaxResult riskPredict(@RequestBody AiRiskRequest request) {
        try {
            return AjaxResult.success(medicalAiService.riskPredict(request));
        } catch (AiHttpUtil.AiServiceException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 风险模型要求的特征顺序，供前端动态生成表单
     */
    @GetMapping("/riskFeatures")
    public AjaxResult riskFeatures() {
        try {
            JsonNode features = medicalAiService.riskFeatures();
            if (features == null) {
                return AjaxResult.error("未能获取特征定义，请确认 risk 模型已训练并重启 AI 服务");
            }
            return AjaxResult.success(features);
        } catch (AiHttpUtil.AiServiceException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 模块③：胸片影像 AI 初筛（图片上传）
     */
    @PostMapping("/chestXray")
    public AjaxResult chestXray(@RequestParam("file") MultipartFile file,
                                @RequestParam(value = "patientId", required = false) Long patientId) {
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要上传的胸片图片");
        }
        long limitBytes = 10L * 1024 * 1024;
        if (file.getSize() > limitBytes) {
            return AjaxResult.error("图片大小不能超过 10MB");
        }
        try {
            byte[] bytes = file.getBytes();
            return AjaxResult.success(medicalAiService.chestXray(
                    bytes, file.getOriginalFilename(), patientId));
        } catch (IOException e) {
            return AjaxResult.error("读取上传图片失败：" + e.getMessage());
        } catch (AiHttpUtil.AiServiceException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 模块④：RAG 导诊问答
     */
    @PostMapping("/ragChat")
    public AjaxResult ragChat(@RequestBody AiChatRequest request) {
        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            return AjaxResult.error("请输入咨询内容");
        }
        try {
            return AjaxResult.success(medicalAiService.ragChat(request));
        } catch (AiHttpUtil.AiServiceException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 模块④：追加知识库条目
     */
    @PostMapping("/ragAddDocs")
    public AjaxResult ragAddDocs(@RequestBody List<String> docs) {
        if (docs == null || docs.isEmpty()) {
            return AjaxResult.error("请提供要入库的知识文本");
        }
        try {
            return AjaxResult.success("已写入知识库", medicalAiService.ragAddDocs(docs));
        } catch (AiHttpUtil.AiServiceException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    // ==================== 历史记录 / 知识库 持久化 ====================
    // 与模块①~④同属一个 Controller，路径统一挂在 /medical/ai 下，前端 API 层已按此契约编写。

    @GetMapping("/record/list")
    public TableDataInfo listAiRecord(MedicalAiRecord record,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalAiRecord> list = medicalAiService.selectAiRecordList(record);
        PageInfo<MedicalAiRecord> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    @PutMapping("/record")
    public AjaxResult updateAiRecord(@RequestBody MedicalAiRecord record) {
        if (record.getRecordId() == null) {
            return AjaxResult.error("缺少病历记录ID");
        }
        return AjaxResult.success(medicalAiService.updateAiRecord(record));
    }

    /**
     * 流式生成结束后由前端回传落库（content 已是完整病历文本，不再调用大模型）。
     * 入参：patientName/age/gender/chiefComplaint/patientId + aiContent/engine/modelName/degraded/elapsedMs
     */
    @PostMapping("/record/save")
    public AjaxResult saveAiRecord(@RequestBody MedicalAiRecord record) {
        if (record.getAiContent() == null || record.getAiContent().trim().isEmpty()) {
            return AjaxResult.error("病历内容为空，无法保存");
        }
        if (record.getPatientName() == null || record.getPatientName().trim().isEmpty()) {
            return AjaxResult.error("请输入患者姓名");
        }
        medicalAiService.saveAiRecord(record);
        return AjaxResult.success(record.getRecordId());
    }

    @DeleteMapping("/record/{ids}")
    public AjaxResult deleteAiRecord(@PathVariable("ids") Long[] ids) {
        return AjaxResult.success(medicalAiService.deleteAiRecordByIds(ids));
    }

    @GetMapping("/risk/list")
    public TableDataInfo listRiskPredict(MedicalRiskPredict predict,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalRiskPredict> list = medicalAiService.selectRiskPredictList(predict);
        PageInfo<MedicalRiskPredict> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    @DeleteMapping("/risk/{ids}")
    public AjaxResult deleteRiskPredict(@PathVariable("ids") Long[] ids) {
        return AjaxResult.success(medicalAiService.deleteRiskPredictByIds(ids));
    }

    @GetMapping("/xray/list")
    public TableDataInfo listChestXray(MedicalChestXray xray,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalChestXray> list = medicalAiService.selectChestXrayList(xray);
        PageInfo<MedicalChestXray> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    @DeleteMapping("/xray/{ids}")
    public AjaxResult deleteChestXray(@PathVariable("ids") Long[] ids) {
        return AjaxResult.success(medicalAiService.deleteChestXrayByIds(ids));
    }

    @GetMapping("/knowledge/list")
    public TableDataInfo listKnowledge(MedicalRagKnowledge knowledge,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalRagKnowledge> list = medicalAiService.selectKnowledgeList(knowledge);
        PageInfo<MedicalRagKnowledge> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    @PostMapping("/knowledge")
    public AjaxResult addKnowledge(@RequestBody MedicalRagKnowledge knowledge) {
        if (knowledge.getContent() == null || knowledge.getContent().trim().isEmpty()) {
            return AjaxResult.error("知识内容不能为空");
        }
        return AjaxResult.success(medicalAiService.insertKnowledge(knowledge));
    }

    @PutMapping("/knowledge")
    public AjaxResult editKnowledge(@RequestBody MedicalRagKnowledge knowledge) {
        if (knowledge.getKnowledgeId() == null) {
            return AjaxResult.error("缺少知识条目ID");
        }
        return AjaxResult.success(medicalAiService.updateKnowledge(knowledge));
    }

    @DeleteMapping("/knowledge/{ids}")
    public AjaxResult removeKnowledge(@PathVariable("ids") Long[] ids) {
        return AjaxResult.success(medicalAiService.deleteKnowledgeByIds(ids));
    }

    /**
     * 把本地未同步的知识条目推送进 Chroma 向量库（真正参与检索的副本）
     */
    @PostMapping("/knowledge/sync")
    public AjaxResult syncKnowledge() {
        try {
            int affected = medicalAiService.syncKnowledgeToVector();
            return AjaxResult.success(affected == 0
                    ? "没有待同步的知识条目"
                    : "已同步 " + affected + " 条知识到向量库");
        } catch (AiHttpUtil.AiServiceException e) {
            return AjaxResult.error("同步失败：" + e.getMessage());
        }
    }
}
