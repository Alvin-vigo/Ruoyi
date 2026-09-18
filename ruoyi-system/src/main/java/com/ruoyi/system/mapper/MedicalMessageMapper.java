package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.MedicalMessage;

import java.util.List;

/**
 * 站内信消息Mapper接口
 *
 * @author ruoyi
 */
public interface MedicalMessageMapper {

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
     * 标记已读（更新阅读状态）
     */
    int updateMessage(MedicalMessage message);

    /**
     * 统计某接收人的未读消息数
     */
    int countUnreadByReceiver(String receiver);

    /**
     * 将某接收人的全部未读消息置为已读
     */
    int updateReadStatusByReceiver(String receiver);

    /**
     * 批量删除站内信消息
     */
    int deleteMessageByIds(Long[] messageIds);

    /**
     * 判断某接收人是否已存在同类型且关联同一业务ID的未读消息（防止定时任务重复推送）
     */
    int countUnreadByTypeAndRelated(MedicalMessage message);

    /**
     * 判断某接收人是否已存在同类型且关联同一业务ID的任意消息（用于重推规则：同一业务只提醒一次）
     */
    int countByTypeAndRelated(MedicalMessage message);

    /**
     * 查询某接收人的最近若干条消息（用于顶部小红点弹层）
     */
    List<MedicalMessage> selectLatestMessages(String receiver, int limit);
}
