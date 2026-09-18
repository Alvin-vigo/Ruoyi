package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.MedicalMessage;
import com.ruoyi.system.mapper.MedicalMessageMapper;
import com.ruoyi.system.service.IMedicalMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 站内信消息Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class MedicalMessageServiceImpl implements IMedicalMessageService {

    @Autowired
    private MedicalMessageMapper messageMapper;

    @Override
    public int insertMessage(MedicalMessage message) {
        return messageMapper.insertMessage(message);
    }

    @Override
    public List<MedicalMessage> selectMessageList(MedicalMessage message) {
        return messageMapper.selectMessageList(message);
    }

    @Override
    public MedicalMessage selectMessageById(Long messageId) {
        return messageMapper.selectMessageById(messageId);
    }

    @Override
    public int markRead(Long messageId) {
        MedicalMessage message = new MedicalMessage();
        message.setMessageId(messageId);
        message.setReadStatus("1");
        return messageMapper.updateMessage(message);
    }

    @Override
    public int markAllRead(String receiver) {
        return messageMapper.updateReadStatusByReceiver(receiver);
    }

    @Override
    public int countUnread(String receiver) {
        return messageMapper.countUnreadByReceiver(receiver);
    }

    @Override
    public int deleteMessageByIds(Long[] messageIds) {
        return messageMapper.deleteMessageByIds(messageIds);
    }

    @Override
    public boolean existsUnreadByTypeAndRelated(String type, Long relatedId, String receiver) {
        MedicalMessage query = new MedicalMessage();
        query.setType(type);
        query.setRelatedId(relatedId);
        query.setReceiver(receiver);
        return messageMapper.countUnreadByTypeAndRelated(query) > 0;
    }

    @Override
    public boolean existsByTypeAndRelated(String type, Long relatedId, String receiver) {
        MedicalMessage query = new MedicalMessage();
        query.setType(type);
        query.setRelatedId(relatedId);
        query.setReceiver(receiver);
        return messageMapper.countByTypeAndRelated(query) > 0;
    }

    @Override
    public List<MedicalMessage> selectLatestMessages(String receiver, int limit) {
        return messageMapper.selectLatestMessages(receiver, limit);
    }
}
