package com.ruoyi.system.service;

import com.ruoyi.system.domain.MedicalMessage;

import java.util.List;

/**
 * 站内信消息Service接口
 *
 * @author ruoyi
 */
public interface IMedicalMessageService {

    /**
     * 新增站内信消息
     */
    int insertMessage(MedicalMessage message);

    /**
     * 查询站内信消息列表
     */
    List<MedicalMessage> selectMessageList(MedicalMessage message);

    /**
     * 查询站内信消息详情
     */
    MedicalMessage selectMessageById(Long messageId);

    /**
     * 标记单条消息为已读
     */
    int markRead(Long messageId);

    /**
     * 标记某接收人全部消息为已读
     */
    int markAllRead(String receiver);

    /**
     * 统计某接收人的未读消息数
     */
    int countUnread(String receiver);

    /**
     * 批量删除站内信消息
     */
    int deleteMessageByIds(Long[] messageIds);

    /**
     * 判断某接收人是否已存在同类型且关联同一业务ID的未读消息（用于定时任务去重）
     */
    boolean existsUnreadByTypeAndRelated(String type, Long relatedId, String receiver);

    /**
     * 判断某接收人是否已存在同类型且关联同一业务ID的任意消息（重推规则：同一业务仅提醒一次）
     */
    boolean existsByTypeAndRelated(String type, Long relatedId, String receiver);

    /**
     * 查询某接收人的最近若干条消息（用于顶部小红点弹层）
     */
    List<MedicalMessage> selectLatestMessages(String receiver, int limit);
}
