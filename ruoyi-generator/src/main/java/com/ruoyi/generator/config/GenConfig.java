package com.ruoyi.generator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 读取代码生成相关配置
 *
 * @author ruoyi
 */
@Component
public class GenConfig {
    /** 作者 */
    public static String author;

    /** 生成包路径 */
    public static String packageName;

    /** 自动去除表前缀，默认是true */
    public static String autoRemovePre;

    /** 表前缀 */
    public static String tablePrefix;

    public static String getAuthor() {
        return author;
    }

    @Value("${gen.author:ruoyi}")
    public void setAuthor(String author) {
        GenConfig.author = author;
    }

    public static String getPackageName() {
        return packageName;
    }

    @Value("${gen.packageName:com.ruoyi.system}")
    public void setPackageName(String packageName) {
        GenConfig.packageName = packageName;
    }

    public static String getAutoRemovePre() {
        return autoRemovePre;
    }

    @Value("${gen.autoRemovePre:false}")
    public void setAutoRemovePre(String autoRemovePre) {
        GenConfig.autoRemovePre = autoRemovePre;
    }

    public static String getTablePrefix() {
        return tablePrefix;
    }

    @Value("${gen.tablePrefix:}")
    public void setTablePrefix(String tablePrefix) {
        GenConfig.tablePrefix = tablePrefix;
    }

    /**
     * 获取主键字段数组
     */
    public static String[] getPk() {
        return new String[] { "id", "_id" };
    }

    /**
     * 获取默认模糊查询字段数组
     */
    public static String[] getQueryLike() {
        return new String[] { "name", "title", "content", "remark" };
    }

    /**
     * 获取默认字典字段数组
     */
    public static String[] getDict() {
        return new String[] { "status", "type", "sex", "gender" };
    }
}
