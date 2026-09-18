<template>
  <div class="app-container ai-page">
    <el-alert
      v-if="riskReady === false"
      title="风险预测模型尚未训练。请在 ruoyi-ai 目录执行 python scripts/train_risk.py 生成模型后重启 AI 服务。"
      type="warning"
      :closable="false"
      show-icon
      class="mb8"
    />

    <el-row :gutter="16">
      <el-col :span="10">
        <el-card shadow="never">
          <div slot="header"><span>① 输入体检指标</span></div>
          <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
            <el-form-item label="关联患者ID" prop="patientId">
              <el-input-number v-model="form.patientId" :min="1" :controls="false" placeholder="可选" style="width:100%" />
            </el-form-item>
            <el-form-item label="预测病种" prop="diseaseName">
              <el-select v-model="form.diseaseName" style="width:100%">
                <el-option label="糖尿病" value="糖尿病" />
                <el-option label="高血压" value="高血压" />
              </el-select>
            </el-form-item>
            <el-divider content-position="left">体检数值</el-divider>
            <!-- 特征项由后端 models/risk_features.json 驱动，避免前后端顺序错位 -->
            <template v-if="featureNames.length">
              <el-form-item
                v-for="name in featureNames"
                :key="name"
                :label="featureLabel(name)"
                :prop="'values.' + name"
              >
                <el-input-number
                  v-model="form.values[name]"
                  :controls="false"
                  :precision="featurePrecision(name)"
                  :step="featurePrecision(name) ? 0.1 : 1"
                  :min="0"
                  style="width:100%"
                  :placeholder="featurePlaceholder(name)"
                />
              </el-form-item>
            </template>
            <el-alert
              v-else
              title="未能获取模型特征定义，请确认 AI 服务已启动且风险模型已训练。"
              type="info"
              :closable="false"
              show-icon
            />
            <el-form-item>
              <el-button type="primary" icon="el-icon-data-analysis" :loading="predicting" @click="handlePredict">开始预测</el-button>
              <el-button icon="el-icon-refresh" @click="handleReset">清空</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="never">
          <div slot="header"><span>② 预测结果</span></div>
          <div v-if="result" class="result-box">
            <el-row :gutter="20">
              <el-col :span="10">
                <el-progress
                  type="dashboard"
                  :percentage="probPercent"
                  :color="riskColor"
                  :width="150"
                >
                  <template slot="default">
                    <div class="progress-inner">
                      <strong :style="{ color: riskColor }">{{ (result.prob * 100).toFixed(1) }}%</strong>
                      <span>患病概率</span>
                    </div>
                  </template>
                </el-progress>
              </el-col>
              <el-col :span="14">
                <div class="result-line">
                  <span class="label">风险等级</span>
                  <el-tag :type="result.result_label === 1 ? 'danger' : 'success'" size="medium">
                    {{ result.result_label === 1 ? '高风险' : '低风险' }}
                  </el-tag>
                </div>
                <div class="result-line">
                  <span class="label">判定阈值</span>
                  <span>{{ result.threshold }}</span>
                </div>
                <div class="result-line" v-if="result.elapsed_ms !== undefined">
                  <span class="label">耗时</span>
                  <span>{{ result.elapsed_ms }} ms</span>
                </div>
                <div class="result-line" v-if="result.advice">
                  <span class="label">健康建议</span>
                  <div class="advice">{{ result.advice }}</div>
                </div>
              </el-col>
            </el-row>
            <el-collapse v-if="result.model_metrics" class="mt10">
              <el-collapse-item name="1" title="模型评估指标（论文可用）">
                <el-descriptions :column="2" size="small" border>
                  <el-descriptions-item v-for="(v, k) in result.model_metrics" :key="k" :label="String(k)">
                    {{ v }}
                  </el-descriptions-item>
                </el-descriptions>
              </el-collapse-item>
            </el-collapse>
          </div>
          <el-empty v-else description="填写左侧指标并点击「开始预测」" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top:16px">
      <div slot="header"><span>③ 历史预测记录</span></div>
      <el-table v-loading="listLoading" :data="list" size="small">
        <el-table-column label="编号" align="center" prop="predictId" width="70" />
        <el-table-column label="病种" align="center" prop="diseaseName" width="100" />
        <el-table-column label="概率" align="center" width="100">
          <template slot-scope="scope">{{ (scope.row.riskProb * 100).toFixed(1) }}%</template>
        </el-table-column>
        <el-table-column label="等级" align="center" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.resultLabel === '1' ? 'danger' : 'success'">
              {{ scope.row.resultLabel === '1' ? '高风险' : '低风险' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="输入指标" prop="featureJson" min-width="180" show-overflow-tooltip />
        <el-table-column label="时间" align="center" prop="createTime" width="160" />
        <el-table-column label="操作" align="center" width="90">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>
  </div>
</template>

<script>
import { riskFeatures, riskPredict, listRiskPredict, delRiskPredict, unwrapAi } from '@/api/medical/ai'

// 与 models/risk_features.json（模型训练真实特征，Pima 糖尿病数据集）保持一致，
// 键名必须与后端 RISK_FEATURE_NAMES 一一对应。
const FEATURE_META = {
  Pregnancies: { label: '怀孕次数', unit: '次', placeholder: '如 0', precision: 0 },
  Glucose: { label: '血糖', unit: 'mmol/L', placeholder: '如 6.8', precision: 1 },
  BloodPressure: { label: '舒张压', unit: 'mmHg', placeholder: '如 72', precision: 0 },
  SkinThickness: { label: '皮褶厚度', unit: 'mm', placeholder: '如 20', precision: 0 },
  Insulin: { label: '胰岛素', unit: 'mu U/ml', placeholder: '如 80', precision: 0 },
  BMI: { label: 'BMI 体质指数', unit: '', placeholder: '如 26.5', precision: 1 },
  DiabetesPedigreeFunction: { label: '糖尿病家族史指数', unit: '', placeholder: '如 0.5', precision: 2 },
  Age: { label: '年龄', unit: '岁', placeholder: '如 45', precision: 0 }
}

export default {
  name: 'MedicalAiRisk',
  data() {
    return {
      form: {
        patientId: undefined,
        diseaseName: '糖尿病',
        values: {}
      },
      rules: {},
      featureNames: [],
      result: null,
      predicting: false,
      listLoading: false,
      list: [],
      total: 0,
      queryParams: { pageNum: 1, pageSize: 10 },
      riskReady: null,
      prefillAge: undefined
    }
  },
  computed: {
    probPercent() {
      if (!this.result) return 0
      return Math.round(this.result.prob * 100)
    },
    riskColor() {
      if (!this.result) return '#409EFF'
      return this.result.result_label === 1 ? '#F56C6C' : '#67C23A'
    }
  },
  created() {
    this.loadFeatures()
    // 优化①联动点：从患者档案页跳转而来时，预填关联患者ID与年龄
    const q = this.$route.query || {}
    if (q.patientId) {
      this.form.patientId = Number(q.patientId) || undefined
    }
    if (q.age) {
      this.prefillAge = Number(q.age) || undefined
    }
    this.getList()
  },
  methods: {
    loadFeatures() {
      riskFeatures().then(res => {
        const unwrapped = unwrapAi(res)
        const names = Array.isArray(unwrapped) ? unwrapped : []
        this.featureNames = names
        this.riskReady = names.length > 0
        const values = {}
        names.forEach(n => { values[n] = undefined })
        // 联动点：若从患者档案带入年龄，自动填到 age 特征
        if (this.prefillAge !== undefined && names.indexOf('age') !== -1) {
          values.age = this.prefillAge
        }
        this.form.values = values
      }).catch(() => { this.riskReady = false })
    },
    featureLabel(name) {
      const meta = FEATURE_META[name]
      if (!meta) return name
      return meta.unit ? `${meta.label}(${meta.unit})` : meta.label
    },
    featurePlaceholder(name) {
      return (FEATURE_META[name] && FEATURE_META[name].placeholder) || ''
    },
    featurePrecision(name) {
      return (FEATURE_META[name] && FEATURE_META[name].precision) || 0
    },
    handlePredict() {
      const features = this.featureNames.map(n => this.form.values[n])
      if (features.some(v => v === undefined || v === null || v === '')) {
        this.$message.warning('请完整填写所有体检指标')
        return
      }
      this.predicting = true
      riskPredict({
        patientId: this.form.patientId,
        diseaseName: this.form.diseaseName,
        features: features
      }).then(res => {
        this.result = unwrapAi(res)
        this.predicting = false
        this.getList()
      }).catch(() => { this.predicting = false })
    },
    handleDelete(row) {
      this.$confirm('确认删除该条预测记录？', '提示', { type: 'warning' }).then(() => {
        return delRiskPredict(row.predictId)
      }).then(() => {
        this.getList()
        this.$message.success('删除成功')
      }).catch(() => {})
    },
    getList() {
      this.listLoading = true
      listRiskPredict(this.queryParams).then(res => {
        this.list = res.rows
        this.total = res.total
        this.listLoading = false
      }).catch(() => { this.listLoading = false })
    },
    handleReset() {
      const values = {}
      this.featureNames.forEach(n => { values[n] = undefined })
      this.form.values = values
      this.result = null
    }
  }
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt10 { margin-top: 10px; }
.result-box { padding: 6px 0; }
.progress-inner { display: flex; flex-direction: column; align-items: center; line-height: 1.4; }
.progress-inner strong { font-size: 22px; }
.progress-inner span { font-size: 12px; color: #909399; }
.result-line { display: flex; align-items: flex-start; margin-bottom: 10px; font-size: 14px; }
.result-line .label { width: 80px; color: #909399; flex-shrink: 0; }
.advice { flex: 1; color: #606266; line-height: 1.6; }
</style>
