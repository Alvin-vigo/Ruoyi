package com.ruoyi.common.core.page;

import com.github.pagehelper.page.PageMethod;

/**
 * 分页工具类，基于 PageHelper
 *
 * @author ruoyi
 */
public class TableSupport {

    /**
     * 封装分页参数并开始分页
     *
     * @param pageDomain 分页参数
     */
    public static void startPage(PageDomain pageDomain) {
        if (pageDomain == null) {
            return;
        }
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        if (pageNum != null && pageSize != null) {
            String orderBy = buildOrderBy(pageDomain);
            if (orderBy != null && !orderBy.isEmpty()) {
                PageMethod.startPage(pageNum, pageSize, orderBy);
            } else {
                PageMethod.startPage(pageNum, pageSize);
            }
        }
    }

    /**
     * 构建排序 SQL 片段
     *
     * @param pageDomain 分页参数
     * @return 排序字符串
     */
    private static String buildOrderBy(PageDomain pageDomain) {
        String orderByColumn = pageDomain.getOrderByColumn();
        String isAsc = pageDomain.getIsAsc();
        if (orderByColumn == null || orderByColumn.isEmpty()) {
            return null;
        }
        if (isAsc == null || isAsc.isEmpty()) {
            isAsc = "asc";
        }
        return orderByColumn + " " + isAsc;
    }

    /**
     * 构建分页响应对象
     *
     * @param list 数据列表
     * @param total 总记录数
     * @return 分页结果
     */
    public static TableDataInfo getDataTable(java.util.List<?> list, long total) {
        return new TableDataInfo(list, total);
    }
}
