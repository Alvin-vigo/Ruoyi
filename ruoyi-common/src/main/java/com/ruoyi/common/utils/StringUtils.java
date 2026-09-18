package com.ruoyi.common.utils;

/**
 * 字符串工具类
 *
 * @author ruoyi
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    /**
     * * 判断一个字符串是否为空串
     *
     * @param str String
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(String str) {
        return isNull(str) || "".equals(str.trim());
    }

    /**
     * * 判断一个字符串是否为非空串
     *
     * @param str String
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * * 判断一个对象是否为空
     *
     * @param object Object
     * @return true：为空 false：非空
     */
    public static boolean isNull(Object object) {
        return object == null;
    }

    /**
     * * 判断一个对象是否非空
     *
     * @param object Object
     * @return true：非空 false：空
     */
    public static boolean isNotNull(Object object) {
        return !isNull(object);
    }

    /**
     * 下划线转驼峰命名（首字母小写）
     * 例：sys_user_name -> sysUserName
     */
    public static final char SEPARATOR = '_';

    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == SEPARATOR) {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 格式化文本, {} 表示占位符
     *
     * @param template 文本模板
     * @param params   参数列表
     * @return 格式化后的文本
     */
    public static String format(String template, Object... params) {
        if (isEmpty(template) || params == null || params.length == 0) {
            return template;
        }
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (Object param : params) {
            int placeholderIndex = template.indexOf("{}", index);
            if (placeholderIndex == -1) {
                sb.append(template.substring(index));
                return sb.toString();
            }
            sb.append(template, index, placeholderIndex);
            sb.append(param);
            index = placeholderIndex + 2;
        }
        sb.append(template.substring(index));
        return sb.toString();
    }
}
