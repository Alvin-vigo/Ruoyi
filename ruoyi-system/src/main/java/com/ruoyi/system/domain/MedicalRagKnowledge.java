package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * RAG 导诊知识条目 medical_rag_knowledge
 *
 * 用于从后台维护知识内容（增删改查），再同步进 Chroma 向量库。
 * 注意：真正参与向量检索的是 Chroma 内的副本，此表是它的可编辑源头。
 *
 * @author ruoyi
 */
public class MedicalRagKnowledge extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @Excel(name = "编号")
    private Long knowledgeId;

    /** 标题 */
    @Excel(name = "标题")
    private String title;

    /** 知识正文 */
    @Excel(name = "内容")
    private String content;

    /** 分类：如 常见病 / 用药 / 检查 / 保健 */
    @Excel(name = "分类")
    private String category;

    /** 是否已同步至向量库（0未同步 1已同步） */
    @Excel(name = "同步状态", readConverterExp = "0=未同步,1=已同步")
    private String synced;

    /** 备注 */
    private String remark;

    public Long getKnowledgeId() {
        return knowledgeId;
    }

    public void setKnowledgeId(Long knowledgeId) {
        this.knowledgeId = knowledgeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSynced() {
        return synced;
    }

    public void setSynced(String synced) {
        this.synced = synced;
    }

    @Override
    public String getRemark() {
        return remark;
    }

    @Override
    public void setRemark(String remark) {
        this.remark = remark;
    }
}
