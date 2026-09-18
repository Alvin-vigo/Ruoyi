package com.ruoyi.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel注解集
 *
 * @author ruoyi
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Excel {

    /** 导出到Excel中的名字 */
    public String name() default "";

    /** 日期格式, 如: yyyy-MM-dd */
    public String dateFormat() default "";

    /** 读取内容转表达式 (如: 0=男,1=女,2=未知) */
    public String readConverterExp() default "";

    /** 导出类型（0数字 1字符串） */
    public ColumnType cellType() default ColumnType.STRING;

    /** 导出时在excel中每个列的高度 单位为字符 */
    public double height() default 14;

    /** 导出时在excel中每个列的宽 单位为字符 */
    public double width() default 16;

    /** 文字后缀,如% 90 变成90% */
    public String suffix() default "";

    /** 当值为空时,字段的默认值 */
    public String defaultValue() default "";

    /** 提示信息 */
    public String prompt() default "";

    /** 是否纵向合并 */
    public boolean combine() default false;

    /** 导出类型枚举 */
    public enum ColumnType {
        NUMERIC(0), STRING(1);

        private final int value;

        ColumnType(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }
}
