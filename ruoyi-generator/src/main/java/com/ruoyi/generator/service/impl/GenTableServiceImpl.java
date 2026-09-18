package com.ruoyi.generator.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.generator.domain.GenTable;
import com.ruoyi.generator.domain.GenTableColumn;
import com.ruoyi.generator.mapper.GenTableColumnMapper;
import com.ruoyi.generator.mapper.GenTableMapper;
import com.ruoyi.generator.service.IGenTableService;
import com.ruoyi.generator.util.GenUtils;
import com.ruoyi.generator.util.VelocityInitializer;
import com.ruoyi.generator.util.VelocityUtils;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成 服务层实现
 *
 * @author ruoyi
 */
@Service
public class GenTableServiceImpl implements IGenTableService {
    private static final Logger log = LoggerFactory.getLogger(GenTableServiceImpl.class);

    @Autowired
    private GenTableMapper genTableMapper;

    @Autowired
    private GenTableColumnMapper genTableColumnMapper;

    /**
     * 查询代码生成业务列表
     */
    @Override
    public List<GenTable> selectGenTableList(GenTable genTable) {
        return genTableMapper.selectGenTableList(genTable);
    }

    /**
     * 查询所有表信息
     */
    @Override
    public List<GenTable> selectGenTableAll() {
        return genTableMapper.selectGenTableAll();
    }

    /**
     * 查询数据库表列表（尚未导入代码生成的表）
     */
    @Override
    public List<GenTable> selectDbTableList(GenTable genTable) {
        return genTableMapper.selectDbTableList(genTable);
    }

    /**
     * 查询代码生成业务信息
     */
    @Override
    public GenTable selectGenTableById(Long tableId) {
        GenTable genTable = genTableMapper.selectGenTableById(tableId);
        setTableFromOptions(genTable);
        List<GenTableColumn> columns = genTableColumnMapper.selectGenTableColumnListByTableId(tableId);
        for (GenTableColumn column : columns) {
            if ("1".equals(column.getIsPk())) {
                genTable.setPkColumn(column);
                break;
            }
        }
        genTable.setColumns(columns);
        return genTable;
    }

    /**
     * 修改业务表
     */
    @Override
    @Transactional
    public void updateGenTable(GenTable genTable) {
        if (genTable.getParams() != null) {
            String options = JSON.toJSONString(genTable.getParams());
            genTable.setOptions(options);
        }
        int row = genTableMapper.updateGenTable(genTable);
        if (row > 0 && genTable.getColumns() != null) {
            for (GenTableColumn column : genTable.getColumns()) {
                genTableColumnMapper.updateGenTableColumn(column);
            }
        }
    }

    /**
     * 删除业务对象
     */
    @Override
    @Transactional
    public void deleteGenTableByIds(Long[] tableIds) {
        genTableMapper.deleteGenTableByIds(tableIds);
        genTableColumnMapper.deleteGenTableColumnByTableIds(tableIds);
    }

    /**
     * 导入表结构
     */
    @Override
    @Transactional
    public void importGenTable(String[] tableNames) {
        List<GenTable> tableList = genTableMapper.selectDbTableListByNames(tableNames);
        for (GenTable genTable : tableList) {
            String tableName = genTable.getTableName();
            GenUtils.initTable(genTable, "ruoyi");
            int row = genTableMapper.insertGenTable(genTable);
            if (row > 0) {
                // 保存列信息
                List<GenTableColumn> columns = genTableMapper.selectDbTableColumnsByName(tableName);
                for (GenTableColumn column : columns) {
                    GenUtils.initColumnField(column, genTable);
                    genTableColumnMapper.insertGenTableColumn(column);
                }
            }
        }
    }

    /**
     * 预览代码
     */
    @Override
    public Map<String, String> previewCode(Long tableId) {
        Map<String, String> dataMap = new LinkedHashMap<>();
        // 根据表ID查询表信息
        GenTable table = genTableMapper.selectGenTableById(tableId);
        // 设置主子表信息
        setTableFromOptions(table);
        // 设置模板变量
        VelocityInitializer.initVelocity();
        VelocityContext context = VelocityUtils.prepareContext(table);
        // 获取模板列表
        List<String> templates = VelocityUtils.getTemplateList();
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);
            dataMap.put(template, sw.toString());
        }
        return dataMap;
    }

    /**
     * 生成代码（自定义路径）
     */
    @Override
    public byte[] generatorCode(String tableName) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        generatorCode(tableName, zip);
        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }

    /**
     * 批量生成代码（下载方式）
     */
    @Override
    public byte[] downloadCode(String[] tableNames) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (String tableName : tableNames) {
            generatorCode(tableName, zip);
        }
        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }

    /**
     * 生成代码到本地
     */
    @Override
    public void generatorCode(String[] tableNames) {
        for (String tableName : tableNames) {
            generatorCode(tableName, null);
        }
    }

    /**
     * 同步数据库
     */
    @Override
    @Transactional
    public void synchDb(String tableName) {
        GenTable genTable = genTableMapper.selectGenTableByName(tableName);
        List<GenTableColumn> dbTableColumns = genTableMapper.selectDbTableColumnsByName(tableName);
        List<GenTableColumn> tableColumns = genTableColumnMapper.selectGenTableColumnListByTableId(genTable.getTableId());
        if (dbTableColumns == null || dbTableColumns.isEmpty()) {
            throw new RuntimeException("同步数据失败，原表结构不存在");
        }
        List<String> dbTableColumnNames = dbTableColumns.stream().map(GenTableColumn::getColumnName).collect(Collectors.toList());
        for (GenTableColumn column : dbTableColumns) {
            if (!tableColumns.stream().map(GenTableColumn::getColumnName).collect(Collectors.toList()).contains(column.getColumnName())) {
                GenUtils.initColumnField(column, genTable);
                genTableColumnMapper.insertGenTableColumn(column);
            }
        }
        if (tableColumns != null && !tableColumns.isEmpty()) {
            List<Long> columnIds = tableColumns.stream().filter(column -> !dbTableColumnNames.contains(column.getColumnName())).map(GenTableColumn::getColumnId).collect(Collectors.toList());
            if (columnIds != null && !columnIds.isEmpty()) {
                genTableColumnMapper.deleteGenTableColumnByIds(columnIds.toArray(new Long[0]));
            }
        }
    }

    /**
     * 查询表信息并生成代码
     */
    private void generatorCode(String tableName, ZipOutputStream zip) {
        // 查询表信息
        GenTable table = genTableMapper.selectGenTableByName(tableName);
        setTableFromOptions(table);
        List<GenTableColumn> columns = genTableColumnMapper.selectGenTableColumnListByTableId(table.getTableId());
        for (GenTableColumn column : columns) {
            if ("1".equals(column.getIsPk())) {
                table.setPkColumn(column);
                break;
            }
        }
        table.setColumns(columns);
        // 设置模板变量信息
        VelocityInitializer.initVelocity();
        VelocityContext context = VelocityUtils.prepareContext(table);
        // 获取模板列表
        List<String> templates = VelocityUtils.getTemplateList();
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);
            try {
                // 添加到zip
                zip.putNextEntry(new ZipEntry(VelocityUtils.getFileName(template, table)));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.flush();
                zip.closeEntry();
            } catch (IOException e) {
                log.error("渲染模板失败，表名：{}，失败模板：{}，异常：", tableName, template, e);
                throw new RuntimeException("渲染模板失败，表名：" + tableName, e);
            }
        }
    }

    /**
     * 设置代码生成其他参数值
     */
    public void setTableFromOptions(GenTable genTable) {
        if (StringUtils.isNotEmpty(genTable.getOptions())) {
            JSONObject paramsObj = JSON.parseObject(genTable.getOptions());
            Map<String, String> params = new LinkedHashMap<>();
            for (String key : paramsObj.keySet()) {
                // 跳过本值，防止覆盖
                if ("treeCode".equals(key) || "treeName".equals(key) || "treeParentCode".equals(key) || "parentMenuId".equals(key)) {
                    continue;
                }
                params.put(key, paramsObj.getString(key));
            }
            genTable.setParams(params);
        }
    }
}
