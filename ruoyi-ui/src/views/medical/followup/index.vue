<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="患者姓名" prop="patientName">
        <el-input
          v-model="queryParams.patientName"
          placeholder="请输入患者姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="随访方式" prop="followupType">
        <el-select v-model="queryParams.followupType" placeholder="请选择随访方式" clearable>
          <el-option label="电话" value="0" />
          <el-option label="微信" value="1" />
          <el-option label="上门" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="随访状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择随访状态" clearable>
          <el-option label="待随访" value="0" />
          <el-option label="已完成" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="随访日期">
        <el-date-picker
          v-model="dateRange"
          size="small"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
      <el-form-item label="快捷筛选" prop="quickFilter">
        <el-radio-group v-model="queryParams.quickFilter" size="small" @change="handleQuery">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="overdue">逾期</el-radio-button>
          <el-radio-button label="today">今日待随访</el-radio-button>
          <el-radio-button label="week">7天内</el-radio-button>
          <el-radio-button label="done">已完成</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['medical:followup:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['medical:followup:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['medical:followup:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['medical:followup:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="followupList" :row-class-name="tableRowClassName" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="随访ID" align="center" prop="followupId" />
      <el-table-column label="患者姓名" align="center" prop="patientName" />
      <el-table-column label="随访日期" align="center" prop="followupDate" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.followupDate, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="随访方式" align="center" prop="followupType">
        <template slot-scope="scope">
          <span>{{ formatType(scope.row.followupType) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="随访内容" align="center" prop="content" :show-overflow-tooltip="true" />
      <el-table-column label="下次随访" align="center" prop="nextFollowupDate" width="110">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.nextFollowupDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="随访人" align="center" prop="followupPerson" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '1' ? 'success' : 'warning'">
            {{ scope.row.status === '1' ? '已完成' : '待随访' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="逾期天数" align="center" prop="overdueDays" width="100">
        <template slot-scope="scope">
          <span v-if="scope.row.overdueDays != null" style="color:#f56c6c;font-weight:600">
            {{ scope.row.overdueDays }} 天
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['medical:followup:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['medical:followup:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改随访记录对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="患者ID" prop="patientId">
          <el-input v-model.number="form.patientId" placeholder="请输入患者ID" />
        </el-form-item>
        <el-form-item label="随访日期" prop="followupDate">
          <el-date-picker clearable
            v-model="form.followupDate"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="请选择随访日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="随访方式" prop="followupType">
          <el-select v-model="form.followupType" placeholder="请选择随访方式">
            <el-option label="电话" value="0" />
            <el-option label="微信" value="1" />
            <el-option label="上门" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="随访内容" prop="content">
          <el-input v-model="form.content" type="textarea" placeholder="请输入随访内容" />
        </el-form-item>
        <el-form-item label="下次随访" prop="nextFollowupDate">
          <el-date-picker clearable
            v-model="form.nextFollowupDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择下次随访日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="随访状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择随访状态">
            <el-option label="待随访" value="0" />
            <el-option label="已完成" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="随访人" prop="followupPerson">
          <el-input v-model="form.followupPerson" placeholder="请输入随访人" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listFollowup, getFollowup, delFollowup, addFollowup, updateFollowup } from "@/api/medical/followup";

export default {
  name: "MedicalFollowup",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      followupList: [],
      title: "",
      open: false,
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        patientName: null,
        followupType: null,
        status: null,
        quickFilter: 'all'
      },
      form: {},
      rules: {
        patientId: [
          { required: true, message: "患者ID不能为空", trigger: "blur" }
        ],
        followupDate: [
          { required: true, message: "随访日期不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    formatType(type) {
      const map = { '0': '电话', '1': '微信', '2': '上门' };
      return map[type] || type;
    },
    getList() {
      this.loading = true;
      const params = this.addDateRange(this.queryParams, this.dateRange, 'followupDate');
      listFollowup(params).then(response => {
        this.followupList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    tableRowClassName({ row }) {
      if (row.status === '0' && row.overdueDays != null) {
        return 'overdue-row';
      }
      return '';
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        followupId: null,
        patientId: null,
        followupDate: null,
        followupType: '0',
        content: null,
        nextFollowupDate: null,
        status: '0',
        followupPerson: null,
        remark: null
      };
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.dateRange = [];
      this.queryParams.quickFilter = 'all';
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.followupId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加随访记录";
    },
    handleUpdate(row) {
      this.reset();
      const followupId = row.followupId || this.ids;
      getFollowup(followupId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改随访记录";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.followupId != null) {
            updateFollowup(this.form).then(() => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addFollowup(this.form).then(() => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const followupIds = row.followupId || this.ids;
      this.$modal.confirm('是否确认删除随访记录编号为"' + followupIds + '"的数据项？').then(() => {
        return delFollowup(followupIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExport() {
      const params = this.addDateRange(this.queryParams, this.dateRange, 'followupDate');
      this.download('medical/followup/export', {
        ...params
      }, `followup_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>

<style lang="scss" scoped>
::v-deep .el-table .overdue-row td {
  background-color: #fef0f0 !important;
}
::v-deep .el-table .overdue-row:hover td {
  background-color: #fbe2e2 !important;
}
</style>
