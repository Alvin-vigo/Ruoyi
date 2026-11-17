package com.ruoyi.system.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 模块④ RAG 导诊问答 请求参数
 *
 * @author ruoyi
 */
public class AiChatRequest {

    /** 患者提问 */
    private String question;

    /** 检索条数，可空（由服务端取默认值） */
    private Integer topK;

    /**
     * 多轮会话ID：前端每次进入会话生成一个，点「清空」换新。
     * 留空则单轮无记忆；非空时后端透传给 FastAPI 的 rag_chat，使其能连续追问上文。
     */
    @JsonProperty("session_id")
    private String sessionId;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
