<template>
  <div class="app-container ai-page">
    <el-alert
      v-if="chestReady === false"
      title="胸片 CNN 模型尚未训练或未加载。本模块需 PyTorch + 数据集，在 RTX4050 上执行 python scripts/train_chest.py 生成模型。"
      type="warning"
      :closable="false"
      show-icon
      class="mb8"
    />

    <el-row :gutter="16">
      <el-col :span="10">
        <el-card shadow="never">
          <div slot="header"><span>① 上传胸片图像</span></div>
          <el-form label-width="90px" size="small">
            <el-form-item label="关联患者ID">
              <el-input-number v-model="patientId" :min="1" :controls="false" placeholder="可选" style="width:100%" />
            </el-form-item>
            <el-form-item label="选择图片">
              <el-upload
                ref="upload"
                action="#"
                :auto-upload="false"
                :limit="1"
                accept="image/*"
                list-type="picture-card"
                :on-change="handleFileChange"
                :on-remove="handleFileRemove"
              >
                <i class="el-icon-plus" />
              </el-upload>
              <div class="el-upload__tip">支持 png / jpg，单张不超过 20MB</div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                icon="el-icon-upload"
                :loading="detecting"
                :disabled="!file"
                @click="handleDetect"
              >开始 AI 检测</el-button>
              <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="never">
          <div slot="header"><span>② 检测结果</span></div>
          <el-row v-if="result" :gutter="16">
            <el-col :span="10">
              <img v-if="previewUrl" :src="previewUrl" class="preview-img" alt="胸片预览" />
              <img v-else-if="result.img_url" :src="result.img_url" class="preview-img" alt="胸片预览" />
            </el-col>
            <el-col :span="14">
              <el-result
                :icon="resultIcon(result.result)"
                :title="result.result"
                :sub-title="resultTip(result.result) + '（置信度 ' + ((result.confidence || 0) * 100).toFixed(1) + '%）'"
              />
              <div class="prob-block">
                <div class="prob-row" v-for="(p, name) in (result.probs || {})" :key="name">
                  <span class="plabel">{{ name }}</span>
                  <el-progress
                    :percentage="Math.round((p || 0) * 100)"
                    :color="name === 'NORMAL' ? '#67C23A' : '#F56C6C'"
                    :stroke-width="16"
                    style="flex: 1"
                  />
                </div>
                <div class="prob-row">
                  <span class="plabel">置信度</span>
                  <el-tag size="mini">{{ ((result.confidence || 0) * 100).toFixed(1) }}%</el-tag>
                </div>
                <div class="prob-row" v-if="result.device">
                  <span class="plabel">推理设备</span>
                  <el-tag size="mini" :type="result.device === 'cuda' ? 'success' : 'info'">
                    {{ result.device === 'cuda' ? 'GPU (CUDA)' : 'CPU' }}
                  </el-tag>
                </div>
                <div class="prob-row" v-if="result.elapsed_ms !== undefined">
                  <span class="plabel">耗时</span>
                  <span>{{ result.elapsed_ms }} ms</span>
                </div>
              </div>
              <el-alert
                class="mt10"
                type="warning"
                :closable="false"
                description="本结果为 AI 辅助初筛提示，不能替代放射科医师诊断。"
                show-icon
              />
            </el-col>
          </el-row>
          <el-empty v-else description="上传胸片后点击「开始 AI 检测」" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top:16px">
      <div slot="header"><span>③ 历史检测记录</span></div>
      <el-table v-loading="listLoading" :data="list" size="small">
        <el-table-column label="编号" align="center" prop="xrayId" width="70" />
        <el-table-column label="结果" align="center" prop="predictResult" width="140" />
        <el-table-column label="置信度" align="center" width="100">
          <template slot-scope="scope">{{ ((scope.row.confidence || 0) * 100).toFixed(1) }}%</template>
        </el-table-column>
        <el-table-column label="设备" align="center" prop="device" width="90" />
        <el-table-column label="耗时" align="center" prop="elapsedMs" width="90">
          <template slot-scope="scope">{{ scope.row.elapsedMs }} ms</template>
        </el-table-column>
        <el-table-column label="图片路径" prop="imgUrl" min-width="160" show-overflow-tooltip />
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
import { chestXray, listChestXray, delChestXray, aiHealth, unwrapAi } from '@/api/medical/ai'

export default {
  name: 'MedicalAiXray',
  data() {
    return {
      file: null,
      previewUrl: '',
      patientId: undefined,
      result: null,
      detecting: false,
      listLoading: false,
      list: [],
      total: 0,
      queryParams: { pageNum: 1, pageSize: 10 },
      chestReady: null
    }
  },
  created() {
    this.checkHealth()
    this.getList()
  },
  beforeDestroy() {
    if (this.previewUrl) URL.revokeObjectURL(this.previewUrl)
  },
  methods: {
    checkHealth() {
      aiHealth().then(res => {
        const h = unwrapAi(res)
        if (h && h.chest) {
          this.chestReady = !!h.chest.ready
        }
      }).catch(() => { this.chestReady = false })
    },
    handleFileChange(file) {
      const ok = (file.raw && file.raw.type || '').indexOf('image/') === 0
      if (!ok) {
        this.$message.error('请选择图片文件')
        return
      }
      const maxBytes = 20 * 1024 * 1024
      if (file.size > maxBytes) {
        this.$message.error('图片大小不能超过 20MB')
        return
      }
      this.file = file.raw
      if (this.previewUrl) URL.revokeObjectURL(this.previewUrl)
      this.previewUrl = URL.createObjectURL(file.raw)
    },
    handleFileRemove() {
      this.file = null
      if (this.previewUrl) {
        URL.revokeObjectURL(this.previewUrl)
        this.previewUrl = ''
      }
    },
    handleDetect() {
      if (!this.file) {
        this.$message.warning('请先选择胸片图片')
        return
      }
      const formData = new FormData()
      formData.append('file', this.file)
      this.detecting = true
      chestXray(formData, this.patientId).then(raw => {
        this.result = unwrapAi(raw)
        this.detecting = false
        this.getList()
      }).catch(() => { this.detecting = false })
    },
    handleDelete(row) {
      this.$confirm('确认删除该条检测记录？', '提示', { type: 'warning' }).then(() => {
        return delChestXray(row.xrayId)
      }).then(() => {
        this.getList()
        this.$message.success('删除成功')
      }).catch(() => {})
    },
    getList() {
      this.listLoading = true
      listChestXray(this.queryParams).then(res => {
        this.list = res.rows
        this.total = res.total
        this.listLoading = false
      }).catch(() => { this.listLoading = false })
    },
    handleReset() {
      this.file = null
      this.result = null
      if (this.previewUrl) {
        URL.revokeObjectURL(this.previewUrl)
        this.previewUrl = ''
      }
      if (this.$refs.upload) this.$refs.upload.clearFiles()
    },
    // 三分类结果对应 el-result 图标：COVID-19 最严重(error)，病毒性肺炎(warning)，正常(success)
    resultIcon(label) {
      if (label === 'COVID-19') return 'error'
      if (label === 'NORMAL') return 'success'
      return 'warning'
    },
    resultTip(label) {
      if (label === 'COVID-19') return '疑似 COVID-19，请立即复核并隔离排查'
      if (label === 'NORMAL') return '未见明显异常'
      return '疑似病毒性肺炎，建议进一步检查'
    }
  }
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt10 { margin-top: 10px; }
.preview-img { width: 100%; border-radius: 4px; border: 1px solid #DCDFE6; }
.prob-block { padding: 0 4px; }
.prob-row { display: flex; align-items: center; margin-bottom: 12px; font-size: 13px; }
.plabel { width: 78px; color: #909399; flex-shrink: 0; }
</style>
