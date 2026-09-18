<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">患者总数</div>
          <div class="stat-value" style="color:#409EFF">{{ dashboard.patientTotal }}</div>
          <div class="stat-sub">人</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">诊疗记录总数</div>
          <div class="stat-value" style="color:#67C23A">{{ dashboard.diagnosisTotal }}</div>
          <div class="stat-sub">条</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">药品总数</div>
          <div class="stat-value" style="color:#E6A23C">{{ dashboard.drugTotal }}</div>
          <div class="stat-sub">种</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">诊疗总费用</div>
          <div class="stat-value" style="color:#F56C6C">{{ fmtMoney(costTotal) }}</div>
          <div class="stat-sub">元</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">挂号量</div>
          <div class="stat-value" style="color:#409EFF">{{ dashboard.registrationTotal }}</div>
          <div class="stat-sub">人次</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">处方量</div>
          <div class="stat-value" style="color:#67C23A">{{ dashboard.prescriptionTotal }}</div>
          <div class="stat-sub">张</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">发药总量</div>
          <div class="stat-value" style="color:#E6A23C">{{ dashboard.dispensingTotalQty }}</div>
          <div class="stat-sub">盒/片/支</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">库存预警</div>
          <div class="stat-value" style="color:#F56C6C">{{ dashboard.lowStockCount }}</div>
          <div class="stat-sub">种（≤50）</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card shadow="never">
          <div slot="header" class="clearfix"><span>科室诊疗分布</span></div>
          <el-table :data="dashboard.byDepartment" v-loading="loading" border height="260">
            <el-table-column label="科室" align="center" prop="department" :show-overflow-tooltip="true" />
            <el-table-column label="诊疗数量" align="center" prop="count" width="100" />
            <el-table-column label="占比" align="center">
              <template slot-scope="scope">
                <el-progress :percentage="calcPercent(scope.row.count, dashboard.diagnosisTotal)" :stroke-width="14" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <div slot="header" class="clearfix"><span>药品分类分布</span></div>
          <el-table :data="byCategoryView" v-loading="loading" border height="260">
            <el-table-column label="分类" align="center" prop="label" width="120" />
            <el-table-column label="药品数量" align="center" prop="count" width="100" />
            <el-table-column label="占比" align="center">
              <template slot-scope="scope">
                <el-progress :percentage="calcPercent(scope.row.count, dashboard.drugTotal)" :stroke-width="14" :color="scope.row.color" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card shadow="never">
          <div slot="header" class="clearfix"><span>科室费用汇总</span></div>
          <el-table :data="dashboard.costByDepartment" v-loading="loading" border height="260">
            <el-table-column label="科室" align="center" prop="department" :show-overflow-tooltip="true" />
            <el-table-column label="诊疗数量" align="center" prop="count" width="100" />
            <el-table-column label="费用(元)" align="center" width="120">
              <template slot-scope="scope"><span>{{ fmtMoney(scope.row.totalCost) }}</span></template>
            </el-table-column>
            <el-table-column label="占比" align="center">
              <template slot-scope="scope">
                <el-progress :percentage="calcPercent(Number(scope.row.totalCost), costTotal)" :stroke-width="14" :color="'#F56C6C'" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <div slot="header" class="clearfix"><span>月度费用趋势</span></div>
          <el-table :data="dashboard.costTrend" v-loading="loading" border height="260">
            <el-table-column label="月份" align="center" prop="month" width="110" />
            <el-table-column label="诊疗数量" align="center" prop="count" width="100" />
            <el-table-column label="费用(元)" align="center">
              <template slot-scope="scope"><span>{{ fmtMoney(scope.row.totalCost) }}</span></template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card shadow="never">
          <div slot="header" class="clearfix"><span>患者性别分布</span></div>
          <el-table :data="genderView" v-loading="loading" border height="220">
            <el-table-column label="性别" align="center" prop="label" width="120" />
            <el-table-column label="人数" align="center" prop="count" width="100" />
            <el-table-column label="占比" align="center">
              <template slot-scope="scope">
                <el-progress :percentage="calcPercent(scope.row.count, dashboard.patientTotal)" :stroke-width="14" :color="scope.row.color" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <div slot="header" class="clearfix"><span>患者年龄段分布</span></div>
          <el-table :data="dashboard.ageStats" v-loading="loading" border height="220">
            <el-table-column label="年龄段" align="center" prop="ageGroup" width="140" />
            <el-table-column label="人数" align="center" prop="count" width="100" />
            <el-table-column label="占比" align="center">
              <template slot-scope="scope">
                <el-progress :percentage="calcPercent(scope.row.count, dashboard.patientTotal)" :stroke-width="14" :color="'#909399'" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getDashboard } from "@/api/medical/statistics";

export default {
  name: "MedicalStatistics",
  data() {
    return {
      loading: true,
      dashboard: {
        patientTotal: 0,
        diagnosisTotal: 0,
        drugTotal: 0,
        registrationTotal: 0,
        prescriptionTotal: 0,
        dispensingTotalQty: 0,
        lowStockCount: 0,
        byDepartment: [],
        byCategory: [],
        costByDepartment: [],
        costTrend: [],
        genderStats: [],
        ageStats: []
      },
      categoryMap: {
        '0': { label: '西药', color: '#409EFF' },
        '1': { label: '中药', color: '#67C23A' },
        '2': { label: '中成药', color: '#E6A23C' },
        '3': { label: '生物制品', color: '#F56C6C' }
      },
      genderMap: {
        '0': { label: '男', color: '#409EFF' },
        '1': { label: '女', color: '#F56C6C' },
        '2': { label: '未知', color: '#909399' }
      }
    };
  },
  computed: {
    byCategoryView() {
      return (this.dashboard.byCategory || []).map(item => ({
        label: (this.categoryMap[item.category] || {}).label || item.category,
        count: item.count,
        color: (this.categoryMap[item.category] || {}).color || '#909399'
      }));
    },
    genderView() {
      return (this.dashboard.genderStats || []).map(item => ({
        label: (this.genderMap[item.gender] || {}).label || '未知',
        count: item.count,
        color: (this.genderMap[item.gender] || {}).color || '#909399'
      }));
    },
    costTotal() {
      return (this.dashboard.costByDepartment || []).reduce((sum, item) => sum + Number(item.totalCost || 0), 0);
    }
  },
  created() {
    this.getDashboard();
  },
  methods: {
    getDashboard() {
      this.loading = true;
      getDashboard().then(response => {
        this.dashboard = response.data || {};
        this.loading = false;
      });
    },
    calcPercent(count, total) {
      if (!total || total === 0) return 0;
      return Math.round((count / total) * 100);
    },
    fmtMoney(value) {
      const n = Number(value || 0);
      return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }
  }
};
</script>

<style scoped>
.stat-card { text-align: center; }
.stat-title { font-size: 14px; color: #909399; }
.stat-value { font-size: 36px; font-weight: bold; margin: 10px 0 4px; }
.stat-sub { font-size: 12px; color: #C0C4CC; }
</style>
