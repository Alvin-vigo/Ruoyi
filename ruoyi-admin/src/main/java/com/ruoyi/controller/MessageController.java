package com.ruoyi.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.system.domain.MedicalMessage;
import com.ruoyi.system.service.IMedicalMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 站内信消息Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/medical/message")
public class MessageController {

    @Autowired
    private IMedicalMessageService messageService;

    /**
     * 查询站内信消息列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MedicalMessage message,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalMessage> list = messageService.selectMessageList(message);
        PageInfo<MedicalMessage> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    /**
     * 获取站内信消息详情
     */
    @GetMapping(value = "/{messageId}")
    public AjaxResult getInfo(@PathVariable("messageId") Long messageId) {
        return AjaxResult.success(messageService.selectMessageById(messageId));
    }

    /**
     * 标记单条消息为已读
     */
    @PutMapping("/read/{messageId}")
    public AjaxResult read(@PathVariable("messageId") Long messageId) {
        return toAjax(messageService.markRead(messageId));
    }

    /**
     * 标记某接收人全部消息为已读
     */
    @PutMapping("/readAll")
    public AjaxResult readAll(@RequestParam("receiver") String receiver) {
        return toAjax(messageService.markAllRead(receiver));
    }

    /**
     * 查询某接收人的未读消息数
     */
    @GetMapping("/unreadCount")
    public AjaxResult unreadCount(@RequestParam("receiver") String receiver) {
        return AjaxResult.success(messageService.countUnread(receiver));
    }

    /**
     * 查询某接收人的最近若干条消息（顶部小红点弹层）
     */
    @GetMapping("/latest")
    public AjaxResult latest(@RequestParam("receiver") String receiver,
                             @RequestParam(defaultValue = "5") Integer limit) {
        int size = (limit == null || limit <= 0) ? 5 : limit;
        return AjaxResult.success(messageService.selectLatestMessages(receiver, size));
    }

    /**
     * 删除站内信消息
     */
    @DeleteMapping("/{messageIds}")
    public AjaxResult remove(@PathVariable Long[] messageIds) {
        return toAjax(messageService.deleteMessageByIds(messageIds));
    }

    private AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }
}
