package com.ruoyi.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.system.domain.MedicalAuditLog;
import com.ruoyi.system.mapper.MedicalAuditLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 操作审计日志Controller（只读：仅提供查询，审计记录由各业务 Service 写入）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/medical/audit")
public class MedicalAuditLogController {

    @Autowired
    private MedicalAuditLogMapper auditLogMapper;

    /**
     * 查询审计日志列表（分页）
     */
    @GetMapping("/list")
    public TableDataInfo list(MedicalAuditLog q,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalAuditLog> list = auditLogMapper.selectMedicalAuditLogList(q);
        PageInfo<MedicalAuditLog> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    /**
     * 健康检查用：返回审计日志总条数
     */
    @GetMapping("/count")
    public AjaxResult count() {
        return AjaxResult.success(auditLogMapper.selectMedicalAuditLogList(new MedicalAuditLog()).size());
    }
}
