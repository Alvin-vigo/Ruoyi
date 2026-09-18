package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 站内信消息对象 medical_message
 *
 * @author ruoyi
 */
public class MedicalMessage extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    @Excel(name = "消息ID")
    private Long messageId;

    /** 消息标题 */
    @Excel(name = "消息标题")
    @NotBlank(message = "消息标题不能为空")
    @Size(max = 100, message = "消息标题长度不能超过100个字符")
    private String title;

    /** 消息内容 */
    @Excel(name = "消息内容")
    @Size(max = 500, message = "消息内容长度不能超过500个字符")
    private String content;

    /** 消息类型（0系统 1低库存预警 2随访逾期提醒） */
    @Excel(name = "消息类型", readConverterExp = "0=系统,1=低库存预警,2=随访逾期提醒")
    private String type;

    /** 接收人（系统用户名） */
    @Excel(name = "接收人")
    private String receiver;

    /** 阅读状态（0未读 1已读） */
    @Excel(name = "阅读状态", readConverterExp = "0=未读,1=已读")
    private String readStatus;

    /** 关联业务ID（随访ID/药品ID等） */
    @Excel(name = "关联业务ID")
    private Long relatedId;

    /** 查询用：开始时间 */
    private String beginTime;

    /** 查询用：结束时间 */
    private String endTime;

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
