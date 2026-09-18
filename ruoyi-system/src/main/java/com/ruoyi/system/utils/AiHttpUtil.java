package com.ruoyi.system.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * AI 微服务调用工具（Java -> FastAPI）
 *
 * 相对大纲示例的修正：
 * 1. 大纲用 fastjson2，本项目实际依赖是 Jackson + fastjson 1.2.83，照抄会编译失败 -> 改用 Jackson；
 * 2. 大纲的 new RestTemplate() 无超时设置。大模型本地推理可能耗时数十秒，
 *    默认无限等待会导致若依请求线程长期挂起，这里显式设置连接/读取超时；
 * 3. 大纲的 AiHttpUtil 里"2.风险预测 / 3.胸片 / 4.RAG"只有注释没有实现，这里四个方法全部补齐，
 *    其中胸片走 multipart 上传（不能直接发 JSON）；
 * 4. 地址做成可配置项 ai.service.url，便于本机/服务器切换。
 *
 * @author ruoyi
 */
@Component
public class AiHttpUtil {

    @Value("${ai.service.url:http://127.0.0.1:8000}")
    private String aiBaseUrl;

    /** 连接超时：本机服务，5s 足够 */
    @Value("${ai.service.connectTimeout:5000}")
    private int connectTimeout;

    /** 读取超时：7B 本地推理较慢，默认 130s，需大于 Python 侧 LLM_TIMEOUT(120s) */
    @Value("${ai.service.readTimeout:130000}")
    private int readTimeout;

    private RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String apiPrefix;

    @PostConstruct
    public void init() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        this.restTemplate = new RestTemplate(factory);
        this.apiPrefix = aiBaseUrl.replaceAll("/+$", "") + "/api/ai";
    }

    /**
     * 模块①：智能病历生成
     * @param body patient_name / age / gender / chief_complaint / patient_id
     */
    public JsonNode generateRecord(Map<String, Object> body) {
        return postJson("/generate_record", body);
    }

    /**
     * 模块②：常见病风险预测
     * @param features 特征值数组，顺序必须与 models/risk_features.json 一致
     */
    public JsonNode riskPredict(java.util.List<Double> features, Long patientId, String diseaseName) {
        Map<String, Object> body = new java.util.HashMap<>(4);
        body.put("features", features);
        if (patientId != null) {
            body.put("patient_id", patientId);
        }
        if (diseaseName != null && !diseaseName.isEmpty()) {
            body.put("disease_name", diseaseName);
        }
        return postJson("/risk_predict", body);
    }

    /**
     * 模块③：胸片智能初筛（multipart 上传图片）
     */
    public JsonNode chestXray(byte[] imageBytes, String filename) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        parts.add("file", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(parts, headers);

        String resp = restTemplate.postForObject(apiPrefix + "/chest_xray", entity, String.class);
        return toNode(resp);
    }

    /**
     * 模块④：RAG 导诊问答
     */
    public JsonNode ragChat(String question, Integer topK) {
        Map<String, Object> body = new java.util.HashMap<>(2);
        body.put("question", question);
        if (topK != null && topK > 0) {
            body.put("top_k", topK);
        }
        return postJson("/rag_chat", body);
    }

    /**
     * 模块④：向知识库追加条目
     */
    public JsonNode ragAddDocs(java.util.List<String> docs) {
        Map<String, Object> body = new java.util.HashMap<>(1);
        body.put("docs", docs);
        return postJson("/rag/add", body);
    }

    /**
     * 模块③：删除 AI 服务落盘的物理图片（删除胸片历史记录时同步清理）
     * 失败仅记录日志，不影响上层数据库记录删除。
     */
    public void deleteUpload(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return;
        }
        try {
            Map<String, Object> body = new java.util.HashMap<>(1);
            body.put("filename", filename);
            postJson("/delete_upload", body);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AiHttpUtil.class)
                    .warn("清理胸片物理图片失败（{}）：{}", filename, e.getMessage());
        }
    }

    /**
     * 查询 AI 服务各模块就绪状态（联调第一步就该看这个）
     */
    public JsonNode health() {
        try {
            String resp = restTemplate.getForObject(apiPrefix + "/health", String.class);
            return toNode(resp);
        } catch (Exception e) {
            throw new AiServiceException("无法连接 AI 微服务（" + apiPrefix + "）：" + e.getMessage()
                    + "，请先在 ruoyi-ai 目录执行 python main.py", e);
        }
    }

    private JsonNode postJson(String path, Object body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAcceptCharset(java.util.Collections.singletonList(StandardCharsets.UTF_8));
            HttpEntity<String> entity = new HttpEntity<>(new String(json.getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8), headers);
            ResponseEntity<String> resp = restTemplate.postForEntity(apiPrefix + path, entity, String.class);
            return toNode(resp.getBody());
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceException("调用 AI 接口失败 [" + path + "]：" + e.getMessage(), e);
        }
    }

    private JsonNode toNode(String raw) {
        if (raw == null || raw.isEmpty()) {
            throw new AiServiceException("AI 微服务返回内容为空");
        }
        try {
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            throw new AiServiceException("AI 微服务返回内容无法解析：" + raw, e);
        }
    }

    /** AI 服务调用异常（统一由 Controller 捕获并转成 AjaxResult.error） */
    public static class AiServiceException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public AiServiceException(String message) {
            super(message);
        }

        public AiServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
