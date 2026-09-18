package com.ruoyi.generator.util;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.generator.config.GenConfig;
import com.ruoyi.generator.domain.GenTable;
import com.ruoyi.generator.domain.GenTableColumn;

import java.util.Arrays;

/**
 * 代码生成器 工具类
 *
 * @author ruoyi
 */
public class GenUtils {

    /**
     * 初始化表信息
     */
    public static void initTable(GenTable genTable, String operName) {
        // 使用下划线转驼峰作为类名称
        genTable.setClassName(convertToCamelCase(genTable.getTableName()));
        // 设置包名
        genTable.setPackageName(GenConfig.getPackageName());
        // 设置模块名
        genTable.setModuleName(getModuleName(GenConfig.getPackageName()));
        // 设置业务名
        genTable.setBusinessName(getBusinessName(genTable.getTableName()));
        // 设置功能名
        genTable.setFunctionName(replaceText(genTable.getTableComment()));
        // 设置作者
        genTable.setFunctionAuthor(GenConfig.getAuthor());
    }

    /**
     * 初始化列属性字段
     */
    public static void initColumnField(GenTableColumn column, GenTable table) {
        String dataType = getDbType(column.getColumnType());
        String columnName = column.getColumnName();
        column.setTableId(table.getTableId());
        column.setJavaField(StringUtils.toCamelCase(columnName));
        // 设置默认java类型
        column.setJavaType(getJavaType(dataType));
        column.setQueryType("EQ");

        // 设置主键（根据自增或字段名匹配）
        if (StringUtils.isNotEmpty(column.getExtra()) && "auto_increment".equals(column.getExtra())
                || Arrays.asList(GenConfig.getPk()).contains(columnName)) {
            column.setIsPk("1");
        } else {
            column.setIsPk("0");
        }
        // 设置自增
        if (StringUtils.isNotEmpty(column.getExtra()) && "auto_increment".equals(column.getExtra())) {
            column.setIsIncrement("1");
        } else {
            column.setIsIncrement("0");
        }

        // 设置字段默认值
        column.setIsRequired("0");
        column.setIsInsert("1");
        column.setIsEdit("1");
        column.setIsList("1");
        column.setIsQuery("0");
        column.setHtmlType("input");

        // 文本类型使用模糊查询
        if (Arrays.asList(GenConfig.getQueryLike()).contains(columnName)) {
            column.setQueryType("LIKE");
            column.setIsQuery("1");
        }
        // 状态字段使用下拉框
        if (Arrays.asList(GenConfig.getDict()).contains(columnName)) {
            column.setHtmlType("select");
        }
    }

    /**
     * 获取数据库类型
     */
    public static String getDbType(String columnType) {
        if (StringUtils.indexOf(columnType, "(") > 0) {
            return StringUtils.substringBefore(columnType, "(");
        }
        return columnType;
    }

    /**
     * 列名转换成Java属性名
     */
    public static String convertToCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        // 快速检查
        if (name == null || name.isEmpty()) {
            return "";
        } else if (!name.contains("_")) {
            // 不含下划线，仅将首字母大写
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        // 用下划线分割，首字母大写
        String[] camels = name.split("_");
        for (String camel : camels) {
            if (camel.isEmpty()) {
                continue;
            }
            result.append(camel.substring(0, 1).toUpperCase());
            result.append(camel.substring(1).toLowerCase());
        }
        return result.toString();
    }

    /**
     * 获取模块名
     */
    public static String getModuleName(String packageName) {
        int lastIndex = packageName.lastIndexOf(".");
        String name = StringUtils.substring(packageName, lastIndex + 1);
        return name;
    }

    /**
     * 获取业务名
     */
    public static String getBusinessName(String tableName) {
        int lastIndex = tableName.lastIndexOf("_");
        int nameLength = tableName.length();
        String name = StringUtils.substring(tableName, lastIndex + 1, nameLength);
        return name;
    }

    /**
     * 表名转换成Java类名（首字母大写的驼峰命名）
     */
    public static String tableToJava(String tableName) {
        String autoRemovePre = GenConfig.getAutoRemovePre();
        String tablePrefix = GenConfig.getTablePrefix();
        if ("true".equals(autoRemovePre) && StringUtils.isNotEmpty(tablePrefix)) {
            String[] searchList = StringUtils.split(tablePrefix, ",");
            tableName = replaceFirst(tableName, searchList);
        }
        return convertToCamelCase(tableName);
    }

    /**
     * 替换前缀
     */
    public static String replaceFirst(String replacement, String[] searchList) {
        String text = replacement;
        for (String searchString : searchList) {
            if (replacement.startsWith(searchString)) {
                text = replacement.replaceFirst(searchString, "");
                break;
            }
        }
        return text;
    }

    /**
     * 关键字替换
     */
    public static String replaceText(String text) {
        return StringUtils.replaceEach(text, new String[] { "表", "的" }, new String[] { "", "" });
    }

    /**
     * 字符串转数组
     */
    public static String[] toStrArray(String str) {
        return StringUtils.split(str, ",");
    }

    /**
     * 根据数据库类型名获取Java类型
     */
    public static String getJavaType(String dataType) {
        switch (dataType) {
            case "tinyint":
            case "smallint":
            case "mediumint":
            case "int":
            case "integer":
                return "Integer";
            case "bigint":
                return "Long";
            case "float":
                return "Float";
            case "double":
                return "Double";
            case "decimal":
            case "numeric":
            case "money":
                return "BigDecimal";
            case "date":
            case "time":
            case "year":
            case "datetime":
            case "timestamp":
                return "Date";
            default:
                return "String";
        }
    }
}
