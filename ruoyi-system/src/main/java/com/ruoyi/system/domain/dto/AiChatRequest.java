package com.ruoyi.system.domain.dto;

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
}
