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
      <el-form-item label="科室" prop="department">
        <el-input
          v-model="queryParams.department"
          placeholder="请输入科室"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="诊疗状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择诊疗状态" clearable>
          <el-option label="待就诊" value="0" />
          <el-option label="就诊中" value="1" />
          <el-option label="已完成" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="就诊日期">
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
    </el-form>

    <!-- 费用汇总卡片 -->
    <el-row :gutter="16" class="mb8">
      <el-col :span="8">
        <el-card shadow="hover" class="fee-card">
          <div slot="header" class="fee-card-header">诊疗费用总额（元）</div>
          <div class="fee-card-value money">{{ formatMoney(summary.totalCost) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="fee-card">
          <div slot="header" class="fee-card-header">诊疗记录数（条）</div>
          <div class="fee-card-value">{{ summary.totalCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="fee-card">
          <div slot="header" class="fee-card-header">平均诊疗费用（元）</div>
          <div class="fee-card-value money">{{ formatMoney(summary.avgCost) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['medical:fee:export']"
        >导出费用明细</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="diagnosisList" border>
      <el-table-column label="患者姓名" align="center" prop="patientName" />
      <el-table-column label="就诊日期" align="center" prop="visitDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.visitDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="科室" align="center" prop="department" />
      <el-table-column label="主治医生" align="center" prop="doctorName" />
      <el-table-column label="诊断结果" align="center" prop="diagnosisResult" :show-overflow-tooltip="true" />
      <el-table-column label="诊疗费用（元）" align="center" prop="cost" width="130">
        <template slot-scope="scope">
          <span class="money">{{ formatMoney(scope.row.cost) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="诊疗状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '2' ? 'success' : (scope.row.status === '1' ? 'warning' : 'info')">
            {{ { '0':'待就诊', '1':'就诊中', '2':'已完成' }[scope.row.status] || scope.row.status }}
          </el-tag>
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
  </div>
</template>

<script>
import { listDiagnosis, getFeeSummary } from "@/api/medical/diagnosis";

export default {
  name: "MedicalFeeReport",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      diagnosisList: [],
      dateRange: [],
      summary: {
        totalCount: 0,
        totalCost: 0,
        avgCost: 0
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        patientName: null,
        department: null,
        status: null
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    formatMoney(val) {
      if (val === null || val === undefined || val === '') return '0.00';
      const n = Number(val);
      return isNaN(n) ? '0.00' : n.toFixed(2);
    },
    getList() {
      this.loading = true;
      const params = this.addDateRange(this.queryParams, this.dateRange, 'visitDate');
      listDiagnosis(params).then(response => {
        this.diagnosisList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
      getFeeSummary(params).then(response => {
        this.summary = response.data || { totalCount: 0, totalCost: 0, avgCost: 0 };
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleExport() {
      const params = this.addDateRange(this.queryParams, this.dateRange, 'visitDate');
      this.download('medical/diagnosis/export', {
        ...params
      }, `诊疗费用明细_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>

<style scoped>
.fee-card-header {
  font-size: 13px;
  color: #909399;
}
.fee-card-value {
  font-size: 26px;
  font-weight: 600;
  text-align: center;
  color: #303133;
}
.fee-card-value.money {
  color: #f56c6c;
}
.money {
  color: #f56c6c;
  font-weight: 600;
}
</style>
