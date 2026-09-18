package com.ruoyi.generator.mapper;

import com.ruoyi.generator.domain.GenTableColumn;

import java.util.List;

/**
 * 业务表字段Mapper接口
 *
 * @author ruoyi
 */
public interface GenTableColumnMapper {

    /**
     * 查询业务表字段列表
     *
     * @param tableId 业务表ID
     * @return 业务表字段集合
     */
    public List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId);

    /**
     * 新增业务表字段
     *
     * @param genTableColumn 业务表字段信息
     * @return 结果
     */
    public int insertGenTableColumn(GenTableColumn genTableColumn);

    /**
     * 修改业务表字段
     *
     * @param genTableColumn 业务表字段信息
     * @return 结果
     */
    public int updateGenTableColumn(GenTableColumn genTableColumn);

    /**
     * 删除业务表字段
     *
     * @param columnIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteGenTableColumnByIds(Long[] columnIds);

    /**
     * 批量删除业务表字段
     *
     * @param tableIds 需要删除的表ID
     * @return 结果
     */
    public int deleteGenTableColumnByTableIds(Long[] tableIds);
}
