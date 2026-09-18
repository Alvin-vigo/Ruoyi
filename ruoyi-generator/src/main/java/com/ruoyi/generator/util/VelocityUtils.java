package com.ruoyi.generator.util;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.generator.config.GenConfig;
import com.ruoyi.generator.domain.GenTable;
import com.ruoyi.generator.domain.GenTableColumn;
import org.apache.velocity.VelocityContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 模板处理工具类
 *
 * @author ruoyi
 */
public class VelocityUtils {

    /** 项目空间路径 */
    private static final String PROJECT_PATH = "main/java/com/ruoyi";

    /** mybatis空间路径 */
    private static final String MYBATIS_PATH = "main/resources/mapper";

    /** 前端空间路径 */
    private static final String VUE_PATH = "ruoyi-ui";

    /**
     * 设置模板变量信息
     */
    public static VelocityContext prepareContext(GenTable genTable) {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("tableName", genTable.getTableName());
        velocityContext.put("functionName", StringUtils.isNotEmpty(genTable.getFunctionName()) ? genTable.getFunctionName() : "【请设置功能名称】");
        velocityContext.put("ClassName", genTable.getClassName());
        velocityContext.put("className", StringUtils.uncapitalize(genTable.getClassName()));
        velocityContext.put("moduleName", genTable.getModuleName());
        velocityContext.put("businessName", genTable.getBusinessName());
        velocityContext.put("basePackage", GenConfig.getPackageName());
        velocityContext.put("packageName", genTable.getPackageName());
        velocityContext.put("author", genTable.getFunctionAuthor());
        velocityContext.put("datetime", DateUtils.getNowDate());
        velocityContext.put("pkColumn", genTable.getPkColumn());
        velocityContext.put("importList", getImportList(genTable.getColumns()));
        velocityContext.put("columns", genTable.getColumns());
        velocityContext.put("table", genTable);
        return velocityContext;
    }

    /**
     * 获取模板信息
     */
    public static List<String> getTemplateList() {
        List<String> templates = new ArrayList<>();
        templates.add("vm/java/domain.java.vm");
        templates.add("vm/java/mapper.java.vm");
        templates.add("vm/java/service.java.vm");
        templates.add("vm/java/serviceImpl.java.vm");
        templates.add("vm/java/controller.java.vm");
        templates.add("vm/xml/mapper.xml.vm");
        templates.add("vm/vue/api.js.vm");
        templates.add("vm/vue/index.vue.vm");
        templates.add("vm/sql/sql.vm");
        return templates;
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, GenTable genTable) {
        // 文件名称
        String fileName = "";
        // 包路径
        String packageName = genTable.getPackageName();
        // 模块名
        String moduleName = genTable.getModuleName();
        // 大写类名
        String className = genTable.getClassName();
        // 业务名称
        String businessName = genTable.getBusinessName().toLowerCase();

        String javaPath = PROJECT_PATH + "/" + StringUtils.replace(packageName, ".", "/") + "/";
        String mybatisPath = MYBATIS_PATH + "/" + moduleName + "/";
        String vuePath = "vue/";

        if (template.contains("domain.java.vm")) {
            fileName = StringUtils.format("{}/domain/{}.java", javaPath, className);
        } else if (template.contains("mapper.java.vm")) {
            fileName = StringUtils.format("{}/mapper/{}Mapper.java", javaPath, className);
        } else if (template.contains("service.java.vm")) {
            fileName = StringUtils.format("{}/service/I{}Service.java", javaPath, className);
        } else if (template.contains("serviceImpl.java.vm")) {
            fileName = StringUtils.format("{}/service/impl/{}ServiceImpl.java", javaPath, className);
        } else if (template.contains("controller.java.vm")) {
            fileName = StringUtils.format("{}/controller/{}Controller.java", javaPath, className);
        } else if (template.contains("mapper.xml.vm")) {
            fileName = StringUtils.format("{}{}Mapper.xml", mybatisPath, className);
        } else if (template.contains("api.js.vm")) {
            fileName = StringUtils.format("{}api/{}/{}.js", vuePath, moduleName, businessName);
        } else if (template.contains("index.vue.vm")) {
            fileName = StringUtils.format("{}views/{}/{}/index.vue", vuePath, moduleName, businessName);
        } else if (template.contains("sql.vm")) {
            fileName = businessName + "Menu.sql";
        }
        return fileName;
    }

    /**
     * 根据列类型获取导入包路径
     */
    public static Set<String> getImportList(List<GenTableColumn> columns) {
        Set<String> importList = new HashSet<>();
        for (GenTableColumn column : columns) {
            if ("Date".equals(column.getJavaType())) {
                importList.add("java.util.Date");
            } else if ("BigDecimal".equals(column.getJavaType())) {
                importList.add("java.math.BigDecimal");
            }
        }
        return importList;
    }
}
