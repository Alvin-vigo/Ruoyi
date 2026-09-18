package com.ruoyi.common.utils.poi;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel相关处理
 *
 * @author ruoyi
 */
public class ExcelUtil<T> {
    private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);

    /** 实体对象 */
    public Class<T> clazz;

    public ExcelUtil(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * 对list数据源将其里面的数据导入到excel表单
     *
     * @param list 导出数据集合
     * @param sheetName 工作表的名称
     * @return 结果
     */
    public void exportExcel(HttpServletResponse response, List<T> list, String sheetName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(sheetName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        List<Map<Integer, String>> data = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        List<Field> fields = new ArrayList<>();

        // 获取所有带@Excel注解的字段
        for (Field field : clazz.getDeclaredFields()) {
            Excel excel = field.getAnnotation(Excel.class);
            if (excel != null) {
                field.setAccessible(true);
                fields.add(field);
                headers.add(excel.name());
            }
        }

        // 填充数据
        for (T item : list) {
            Map<Integer, String> row = new LinkedHashMap<>();
            int index = 0;
            for (Field field : fields) {
                Excel excel = field.getAnnotation(Excel.class);
                Object value = null;
                try {
                    value = field.get(item);
                } catch (IllegalAccessException e) {
                    log.error("读取字段值失败", e);
                }
                String text = value == null ? excel.defaultValue() : convertValue(value, excel);
                row.put(index++, text);
            }
            data.add(row);
        }

        // 构建表头
        List<List<String>> head = new ArrayList<>();
        for (String h : headers) {
            List<String> item = new ArrayList<>();
            item.add(h);
            head.add(item);
        }

        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).head(head).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();

        List<List<String>> sheetData = new ArrayList<>();
        for (Map<Integer, String> row : data) {
            List<String> rowData = new ArrayList<>();
            for (int i = 0; i < headers.size(); i++) {
                rowData.add(row.get(i));
            }
            sheetData.add(rowData);
        }
        excelWriter.write(sheetData, writeSheet);
        excelWriter.finish();
    }

    /**
     * 转换字段值
     */
    private String convertValue(Object value, Excel excel) {
        if (value instanceof Date) {
            String dateFormat = StringUtils.isNotEmpty(excel.dateFormat()) ? excel.dateFormat() : "yyyy-MM-dd HH:mm:ss";
            return new SimpleDateFormat(dateFormat).format((Date) value);
        }
        String text = value.toString();
        if (StringUtils.isNotEmpty(excel.readConverterExp())) {
            text = convertByExp(text, excel.readConverterExp());
        }
        if (StringUtils.isNotEmpty(excel.suffix())) {
            text = text + excel.suffix();
        }
        return text;
    }

    /**
     * 解析表达式
     */
    private String convertByExp(String propertyValue, String converterExp) {
        try {
            String[] convertSource = converterExp.split(",");
            for (String item : convertSource) {
                String[] itemArray = item.split("=");
                if (itemArray[0].equals(propertyValue)) {
                    return itemArray[1];
                }
            }
        } catch (Exception e) {
            log.error("表达式解析失败：{}，异常：", converterExp, e);
        }
        return propertyValue;
    }
}
