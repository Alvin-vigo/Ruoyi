<template>
  <div class="app-container ai-page">
    <!-- 顶部：AI 服务状态条 -->
    <el-alert
      v-if="!healthChecked"
      title="正在检测 AI 微服务状态…"
      type="info"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-alert
      v-else-if="llmReady"
      title="AI 服务在线：病历由本地大模型（Ollama）真实生成。"
      type="success"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-alert
      v-else
      :title="'AI 服务已连通，但大模型暂不可用（' + llmReason + '）。当前为【模板降级】模式，仍可生成结构化病历草稿。'"
      type="warning"
      :closable="false"
      show-icon
      class="mb8"
    />

    <el-row :gutter="16">
      <!-- 左：输入区 -->
      <el-col :span="10">
        <el-card shadow="never">
          <div slot="header"><span>① 填写患者与主诉</span></div>
          <el-form ref="form" :model="form" :rules="rules" label-width="80px" size="small">
            <el-form-item label="患者姓名" prop="patientName">
              <el-input v-model="form.patientName" placeholder="请输入患者姓名" />
            </el-form-item>
            <el-form-item label="患者ID" prop="patientId">
              <el-input-number v-model="form.patientId" :min="1" :controls="false" placeholder="可选，关联档案" style="width:100%" />
            </el-form-item>
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="0" :max="120" :controls="false" style="width:100%" />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio label="男">男</el-radio>
                <el-radio label="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="主诉" prop="chiefComplaint">
              <el-input
                v-model="form.chiefComplaint"
                type="textarea"
                :rows="5"
                placeholder="例：反复头晕、乏力3天，伴食欲减退；既往血压偏高。"
              />
            </el-form-item>
            <el-form-item label="过敏史" prop="allergyHistory">
              <el-input v-model="form.allergyHistory" placeholder="关联档案后自动带入，可修改" />
            </el-form-item>
            <el-form-item label="既往史" prop="pastHistory">
              <el-input v-model="form.pastHistory" placeholder="关联档案后自动带入，可修改" />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                icon="el-icon-magic-stick"
                :loading="generating"
                @click="handleGenerate"
              >AI 生成病历</el-button>
              <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右：结果区 -->
      <el-col :span="14">
        <el-card shadow="never">
          <div slot="header">
            <span>② AI 生成结果（可编辑后保存）</span>
            <span v-if="result.content" class="fr">
              <el-tag size="mini" :type="result.degraded ? 'warning' : 'success'">
                {{ result.engine === 'ollama' ? '大模型生成' : '模板降级' }}
              </el-tag>
              <el-tag size="mini" type="info" style="margin-left:6px">{{ result.elapsed_ms }} ms</el-tag>
            </span>
          </div>
          <el-input
            v-model="result.content"
            type="textarea"
            :rows="16"
            placeholder="点击左侧「AI 生成病历」后，结果将显示在此处"
          />
          <div style="margin-top:10px">
            <el-button
              type="success"
              icon="el-icon-check"
              size="small"
              :disabled="!result || !result.content"
              :loading="saving"
              @click="handleSave"
            >保存修改后的病历</el-button>
            <el-button
              size="small"
              icon="el-icon-document-copy"
              :disabled="!result || !result.content"
              @click="copyContent"
            >复制</el-button>
          </div>
          <el-collapse v-if="result.aiContent" style="margin-top:12px" accordion>
            <el-collapse-item title="AI 生成原文（医生修改前，供对比留痕）" name="1">
              <pre class="ai-origin">{{ result.aiContent }}</pre>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </el-col>
    </el-row>

    <!-- 历史记录 -->
    <el-card shadow="never" style="margin-top:16px">
      <div slot="header"><span>③ 历次 AI 生成记录</span></div>
      <el-table v-loading="listLoading" :data="recordList" size="small" empty-text="暂无生成记录">
        <el-table-column label="编号" align="center" prop="recordId" width="70" />
        <el-table-column label="患者" align="center" prop="patientName" width="100" />
        <el-table-column label="主诉" prop="chiefComplaint" min-width="180" show-overflow-tooltip />
        <el-table-column label="引擎" align="center" width="110">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.degraded === '1' ? 'warning' : 'success'">
              {{ scope.row.degraded === '1' ? '模板降级' : '大模型' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" align="center" prop="elapsedMs" width="90">
          <template slot-scope="scope">{{ scope.row.elapsedMs }} ms</template>
        </el-table-column>
        <el-table-column label="生成时间" align="center" prop="createTime" width="160" />
        <el-table-column label="操作" align="center" width="140">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-view" @click="viewRecord(scope.row)">查看</el-button>
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
import { aiHealth, generateRecord, generateRecordStream, saveAiRecord, listAiRecord, updateAiRecord, delAiRecord, unwrapAi } from '@/api/medical/ai'
import { getPatient } from '@/api/medical/patient'

export default {
  name: 'MedicalAiRecord',
  data() {
    return {
      form: {
        patientName: '',
        patientId: undefined,
        age: undefined,
        gender: '男',
        chiefComplaint: '',
        allergyHistory: '',
        pastHistory: ''
      },
      rules: {
        patientName: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }],
        chiefComplaint: [{ required: true, message: '请输入主诉', trigger: 'blur' }]
      },
      result: { content: '' },
      generating: false,
      saving: false,
      listLoading: false,
      recordList: [],
      total: 0,
      queryParams: { pageNum: 1, pageSize: 10 },
      healthChecked: false,
      llmReady: false,
      llmReason: ''
    }
  },
  created() {
    this.checkHealth()
    this.getList()
  },
  watch: {
    'form.patientId'(val) {
      if (val) {
        getPatient(val).then(raw => {
          const res = unwrapAi(raw)
          if (res) {
            this.form.patientName = res.patientName || this.form.patientName
            this.form.allergyHistory = res.allergyHistory || ''
            this.form.pastHistory = res.pastHistory || ''
          }
        }).catch(() => {})
      }
    }
  },
  methods: {
    /** 页面一进来就探一次 AI 服务状态，让用户先知道当前是真实大模型还是降级模式 */
    checkHealth() {
      aiHealth().then(raw => {
        const res = unwrapAi(raw)
        this.healthChecked = true
        if (res && res.llm) {
          this.llmReady = !!res.llm.ready
          this.llmReason = res.llm.reason || ''
        }
      }).catch(() => {
        this.healthChecked = true
        this.llmReady = false
        this.llmReason = 'AI 微服务未启动（请先运行 ruoyi-ai/main.py）'
      })
    },
    handleGenerate() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.generating = true
        this.result = { content: '' }
        const payload = { ...this.form }
        // 优先走流式（打字机效果）；失败则回退到普通一次性返回
        generateRecordStream(
          payload,
          (full) => { this.result = { ...this.result, content: full } },
          (meta) => {
            this.result = {
              ...this.result,
              content: meta.content,
              aiContent: meta.content,
              engine: meta.engine,
              degraded: meta.degraded,
              elapsed_ms: meta.elapsed_ms
            }
            if (meta.degraded) {
              this.$notify.warning({
                title: '已降级生成',
                message: '大模型不可用，当前输出为结构化模板草稿',
                duration: 3000
              })
            }
            // 流式结束：把完整文本落库，保证历史记录可查（不再二次调用模型）
            const record = {
              patientName: this.form.patientName,
              patientId: this.form.patientId,
              age: this.form.age,
              gender: this.form.gender,
              chiefComplaint: this.form.chiefComplaint,
              aiContent: meta.content,
              engine: meta.engine,
              modelName: meta.model,
              degraded: meta.degraded ? '1' : '0',
              elapsedMs: meta.elapsed_ms,
              status: '0'
            }
            saveAiRecord(record).then(res => {
              const r = unwrapAi(res)
              const rid = (r && typeof r === 'object') ? r.recordId : r
              this.result.recordId = rid
              this.getList()
            }).catch(() => {
              this.$message.warning('文本已生成，但落库失败，历史列表可能未更新')
            }).finally(() => { this.generating = false })
          }
        ).catch(() => {
          // 流式通道异常（如 AI 服务未启动）：回退普通模式
          generateRecord(this.form).then(res => {
            const r = unwrapAi(res) || {}
            this.result = {
              ...r,
              aiContent: r.content,
              recordId: r.recordId
            }
            if (r.degraded) {
              this.$notify.warning({
                title: '已降级生成',
                message: '大模型不可用，当前输出为结构化模板草稿',
                duration: 3000
              })
            }
          }).catch(() => {
            this.$message.error('生成失败，请确认 AI 微服务已启动')
          }).finally(() => { this.generating = false })
        })
      })
    },
    handleSave() {
      if (!this.result || !this.result.recordId) {
        this.$message.warning('该记录尚无编号，请重新生成后再保存')
        return
      }
      this.saving = true
      // 留痕设计：AI 原文存 aiContent，医生定稿存 doctorContent，便于论文对比"初稿 vs 终稿"
      updateAiRecord({
        recordId: this.result.recordId,
        aiContent: this.result.aiContent || this.result.content,
        doctorContent: this.result.content,
        status: '1'
      }).then(() => {
        this.saving = false
        this.$message.success('病历已保存（AI 原文已留痕）')
        this.getList()
      }).catch(() => { this.saving = false })
    },
    viewRecord(row) {
      this.result = {
        recordId: row.recordId,
        aiContent: row.aiContent,
        content: row.doctorContent || row.aiContent,
        engine: row.engine,
        degraded: row.degraded === '1',
        elapsed_ms: row.elapsedMs
      }
      this.form.patientName = row.patientName
      this.form.age = row.age
      this.form.gender = row.gender
      this.form.chiefComplaint = row.chiefComplaint
    },
    handleDelete(row) {
      this.$confirm('确认删除该条 AI 生成记录？', '提示', { type: 'warning' }).then(() => {
        return delAiRecord(row.recordId)
      }).then(() => {
        this.getList()
        this.$message.success('删除成功')
      }).catch(() => {})
    },
    getList() {
      this.listLoading = true
      listAiRecord(this.queryParams).then(res => {
        this.recordList = res.rows
        this.total = res.total
        this.listLoading = false
      }).catch(() => { this.listLoading = false })
    },
    // 注意：方法名不能叫 resetForm，否则会遮蔽全局 this.resetForm 导致递归
    handleReset() {
      this.resetForm('form')
      this.result = { content: '' }
    },
    /** 本项目未引入 clipboard 指令，用原生 API 实现复制 */
    copyContent() {
      const text = this.result && this.result.content
      if (!text) return
      if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(text).then(() => {
          this.$message.success('已复制到剪贴板')
        }).catch(() => this.$message.warning('浏览器拒绝了剪贴板权限，请手动选中复制'))
      } else {
        this.$alert(text, '病历内容（请手动复制）', { confirmButtonText: '关闭' })
      }
    }
  }
}
</script>

<style scoped>
.fr { float: right; }
.mb8 { margin-bottom: 8px; }
.ai-origin {
  white-space: pre-wrap;
  font-size: 13px;
  line-height: 1.6;
  background: #fbfcfe;
  padding: 10px;
  border-radius: 4px;
  max-height: 240px;
  overflow: auto;
  color: #606266;
  margin: 0;
}
</style>
