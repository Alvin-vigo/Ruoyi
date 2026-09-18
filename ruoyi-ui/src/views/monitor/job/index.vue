<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="任务名称" prop="jobName">
        <el-input
          v-model="queryParams.jobName"
          placeholder="请输入任务名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="任务组名" prop="jobGroup">
        <el-select v-model="queryParams.jobGroup" placeholder="请选择任务组名" clearable>
          <el-option label="默认" value="DEFAULT" />
          <el-option label="系统" value="SYSTEM" />
        </el-select>
      </el-form-item>
      <el-form-item label="任务状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择任务状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="暂停" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
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
          v-hasPermi="['monitor:job:add']"
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
          v-hasPermi="['monitor:job:edit']"
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
          v-hasPermi="['monitor:job:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['monitor:job:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="jobList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="任务编号" align="center" prop="jobId" width="100" />
      <el-table-column label="任务名称" align="center" prop="jobName" :show-overflow-tooltip="true" />
      <el-table-column label="任务组名" align="center" prop="jobGroup" width="100" />
      <el-table-column label="调用目标" align="center" prop="invokeTarget" :show-overflow-tooltip="true" />
      <el-table-column label="cron表达式" align="center" prop="cronExpression" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'warning'">
            {{ scope.row.status === '0' ? '正常' : '暂停' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['monitor:job:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['monitor:job:remove']"
          >删除</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-caret-right"
            @click="handleRun(scope.row)"
            v-hasPermi="['monitor:job:run']"
          >执行一次</el-button>
          <el-button
            size="mini"
            type="text"
            :icon="scope.row.status === '0' ? 'el-icon-video-pause' : 'el-icon-video-play'"
            @click="handleStatus(scope.row)"
            v-hasPermi="['monitor:job:changeStatus']"
          >{{ scope.row.status === '0' ? '暂停' : '恢复' }}</el-button>
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

    <!-- 添加或修改定时任务对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="任务名称" prop="jobName">
          <el-input v-model="form.jobName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务组名" prop="jobGroup">
          <el-select v-model="form.jobGroup" placeholder="请选择任务组名">
            <el-option label="默认" value="DEFAULT" />
            <el-option label="系统" value="SYSTEM" />
          </el-select>
        </el-form-item>
        <el-form-item label="调用目标" prop="invokeTarget">
          <el-input v-model="form.invokeTarget" placeholder="例如：ryTask.ryParams('ruoyi')" />
          <div class="help">示例：ryTask.ryNoParams()、ryTask.ryParams('参数')、medicalRemindTask.scanLowStock(10)</div>
        </el-form-item>
        <el-form-item label="cron表达式" prop="cronExpression">
          <el-input v-model="form.cronExpression" placeholder="例如：0/10 * * * * ?" @blur="checkCron">
            <el-button slot="append" @click="checkCron">校验</el-button>
          </el-input>
          <div class="help" v-if="cronMsg">{{ cronMsg }}</div>
        </el-form-item>
        <el-form-item label="执行策略" prop="misfirePolicy">
          <el-radio-group v-model="form.misfirePolicy">
            <el-radio label="0">立即执行</el-radio>
            <el-radio label="1">停止执行</el-radio>
            <el-radio label="2">放弃执行</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="并发执行" prop="concurrent">
          <el-radio-group v-model="form.concurrent">
            <el-radio label="0">允许</el-radio>
            <el-radio label="1">禁止</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">暂停</el-radio>
          </el-radio-group>
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
import { listJob, getJob, delJob, addJob, updateJob, changeJobStatus, runJob, checkCronExpression } from "@/api/monitor/job";

export default {
  name: "MonitorJob",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      jobList: [],
      title: "",
      open: false,
      cronMsg: "",
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        jobName: null,
        jobGroup: null,
        status: null
      },
      form: {},
      rules: {
        jobName: [
          { required: true, message: "任务名称不能为空", trigger: "blur" }
        ],
        invokeTarget: [
          { required: true, message: "调用目标不能为空", trigger: "blur" }
        ],
        cronExpression: [
          { required: true, message: "cron表达式不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listJob(this.queryParams).then(res => {
        this.jobList = res.rows;
        this.total = res.total;
        this.loading = false;
      });
    },
    reset() {
      this.cronMsg = "";
      this.form = {
        jobId: null,
        jobName: null,
        jobGroup: "DEFAULT",
        invokeTarget: null,
        cronExpression: null,
        misfirePolicy: "0",
        concurrent: "1",
        status: "0",
        remark: null
      };
      this.resetForm("form");
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.jobId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加定时任务";
    },
    handleUpdate(row) {
      this.reset();
      const jobId = row.jobId || this.ids;
      getJob(jobId).then(res => {
        this.form = res.data;
        this.open = true;
        this.title = "修改定时任务";
      });
    },
    checkCron() {
      if (!this.form.cronExpression) {
        this.cronMsg = "请输入cron表达式";
        return;
      }
      checkCronExpression(this.form.cronExpression).then(res => {
        this.cronMsg = res.data ? "表达式有效" : "表达式无效";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.jobId != null) {
            updateJob(this.form).then(() => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addJob(this.form).then(() => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const jobIds = row.jobId || this.ids;
      this.$modal.confirm('是否确认删除定时任务编号为"' + jobIds + '"的数据项？').then(() => {
        return delJob(jobIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleRun(row) {
      runJob(row).then(() => {
        this.$modal.msgSuccess("执行成功");
      });
    },
    handleStatus(row) {
      const text = row.status === "0" ? "暂停" : "恢复";
      const newStatus = row.status === "0" ? "1" : "0";
      this.$modal.confirm('确认要"' + text + '"任务「' + row.jobName + '」吗？').then(() => {
        return changeJobStatus({ jobId: row.jobId, status: newStatus });
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(text + "成功");
      }).catch(() => {});
    },
    handleExport() {
      this.download('/monitor/job/export', {
        ...this.queryParams
      }, `job_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>

<style scoped>
.help {
  color: #909399;
  font-size: 12px;
  line-height: 18px;
}
</style>
