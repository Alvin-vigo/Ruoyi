package com.ruoyi.generator.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.generator.domain.GenTable;
import com.ruoyi.generator.service.IGenTableService;
import com.ruoyi.generator.util.GenUtils;
import com.ruoyi.generator.util.VelocityUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 代码生成 操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/tool/gen")
public class GenTableController {
    @Autowired
    private IGenTableService genTableService;

    /**
     * 查询代码生成列表
     */
    @GetMapping("/list")
    public TableDataInfo genList(GenTable genTable,
                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<GenTable> list = genTableService.selectGenTableList(genTable);
        PageInfo<GenTable> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    /**
     * 查询数据库列表
     */
    @GetMapping("/db/list")
    public TableDataInfo dataList(GenTable genTable,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<GenTable> list = genTableService.selectDbTableList(genTable);
        PageInfo<GenTable> pageInfo = new PageInfo<>(list);
        return TableSupport.getDataTable(list, pageInfo.getTotal());
    }

    /**
     * 查询表详细信息
     */
    @GetMapping(value = "/{tableId}")
    public AjaxResult getInfo(@PathVariable Long tableId) {
        GenTable table = genTableService.selectGenTableById(tableId);
        return AjaxResult.success(table);
    }

    /**
     * 查询当前选中数据库的所有表
     */
    @GetMapping("/db/list/all")
    public AjaxResult dataListAll() {
        List<GenTable> list = genTableService.selectGenTableAll();
        return AjaxResult.success(list);
    }

    /**
     * 修改代码生成业务
     */
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody GenTable genTable) {
        genTableService.updateGenTable(genTable);
        return AjaxResult.success();
    }

    /**
     * 删除代码生成
     */
    @DeleteMapping("/{tableIds}")
    public AjaxResult remove(@PathVariable Long[] tableIds) {
        genTableService.deleteGenTableByIds(tableIds);
        return AjaxResult.success();
    }

    /**
     * 导入表结构（保存）
     */
    @PostMapping("/importTable")
    public AjaxResult importTableSave(String tables) {
        String[] tableNames = GenUtils.toStrArray(tables);
        genTableService.importGenTable(tableNames);
        return AjaxResult.success();
    }

    /**
     * 预览代码
     */
    @GetMapping("/preview/{tableId}")
    public AjaxResult preview(@PathVariable("tableId") Long tableId) {
        Map<String, String> dataMap = genTableService.previewCode(tableId);
        return AjaxResult.success(dataMap);
    }

    /**
     * 生成代码（自定义路径）
     */
    @GetMapping("/genCode/{tableName}")
    public AjaxResult genCode(@PathVariable("tableName") String tableName) {
        genTableService.generatorCode(tableName);
        return AjaxResult.success("生成成功");
    }

    /**
     * 批量生成代码
     */
    @GetMapping("/batchGenCode")
    public void batchGenCode(HttpServletResponse response, String tables) throws IOException {
        String[] tableNames = GenUtils.toStrArray(tables);
        byte[] data = genTableService.downloadCode(tableNames);
        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"ruoyi.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IOUtils.write(data, response.getOutputStream());
    }

    /**
     * 同步数据库
     */
    @GetMapping("/synchDb/{tableName}")
    public AjaxResult synchDb(@PathVariable("tableName") String tableName) {
        genTableService.synchDb(tableName);
        return AjaxResult.success("同步成功");
    }
}
