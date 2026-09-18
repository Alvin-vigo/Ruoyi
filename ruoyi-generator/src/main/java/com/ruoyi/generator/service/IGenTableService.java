package com.ruoyi.generator.service;

import com.ruoyi.generator.domain.GenTable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成 服务层
 *
 * @author ruoyi
 */
public interface IGenTableService {

    /**
     * 查询代码生成业务列表
     *
     * @param genTable 代码生成业务信息
     * @return 业务列表
     */
    public List<GenTable> selectGenTableList(GenTable genTable);

    /**
     * 查询所有表信息
     *
     * @return 所有表信息
     */
    public List<GenTable> selectGenTableAll();

    /**
     * 查询数据库表列表（尚未导入代码生成的表）
     *
     * @param genTable 查询条件
     * @return 数据库表集合
     */
    public List<GenTable> selectDbTableList(GenTable genTable);

    /**
     * 查询代码生成业务信息
     *
     * @param tableId 业务表ID
     * @return 业务信息
     */
    public GenTable selectGenTableById(Long tableId);

    /**
     * 修改业务表
     *
     * @param genTable 业务表信息
     */
    public void updateGenTable(GenTable genTable);

    /**
     * 删除业务信息
     *
     * @param tableIds 需要删除的表数组
     */
    public void deleteGenTableByIds(Long[] tableIds);

    /**
     * 导入表结构
     *
     * @param tableNames 表名数组
     */
    public void importGenTable(String[] tableNames);

    /**
     * 预览代码
     *
     * @param tableId 表编号
     * @return 预览数据列表
     */
    public Map<String, String> previewCode(Long tableId);

    /**
     * 生成代码（自定义路径）
     *
     * @param tableName 表名称
     * @return 数据
     */
    public byte[] generatorCode(String tableName);

    /**
     * 同步数据库
     *
     * @param tableName 表名称
     */
    public void synchDb(String tableName);

    /**
     * 批量生成代码（下载方式）
     *
     * @param tableNames 表数组
     * @return 数据
     */
    public byte[] downloadCode(String[] tableNames);

    /**
     * 批量生成代码（自定义路径）
     *
     * @param tableNames 表数组
     */
    public void generatorCode(String[] tableNames);
}
