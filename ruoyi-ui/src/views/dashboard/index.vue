<template>
  <div class="dashboard">
    <!-- 欢迎区 -->
    <div class="welcome">
      <div class="welcome-text">
        <h2>{{ greeting }}，{{ name }}</h2>
        <p>欢迎使用 {{ title }}，请从左侧菜单进入业务办理</p>
      </div>
      <div class="welcome-date">
        <span class="date">{{ today }}</span>
        <span class="week">{{ week }}</span>
      </div>
    </div>

    <!-- 核心指标 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="5" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card" :class="card.warn && card.value > 0 ? 'is-warn' : ''">
          <div class="stat-inner">
            <div class="stat-icon" :style="{ background: card.color }">
              <i :class="card.icon"></i>
            </div>
            <div class="stat-meta">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-card shadow="never" class="entry-card">
      <div slot="header" class="entry-header">
        <span>快捷入口</span>
      </div>
      <el-row :gutter="16">
        <el-col :span="6" v-for="entry in entries" :key="entry.path">
          <div class="entry-item" @click="go(entry.path)">
            <div class="entry-icon" :style="{ color: entry.color, background: entry.bg }">
              <i :class="entry.icon"></i>
            </div>
            <div class="entry-info">
              <div class="entry-title">{{ entry.title }}</div>
              <div class="entry-desc">{{ entry.desc }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import settings from '@/settings'
import { getDashboard } from '@/api/medical/statistics'

export default {
  name: 'Dashboard',
  data() {
    return {
      dashboard: {
        patientTotal: 0,
        registrationTotal: 0,
        prescriptionTotal: 0,
        dispensingTotalQty: 0,
        lowStockCount: 0
      },
      entries: [
        { path: '/medical/patient', title: '患者管理', desc: '患者档案维护', icon: 'el-icon-user', color: '#409EFF', bg: '#ecf5ff' },
        { path: '/medical/registration', title: '挂号管理', desc: '挂号 / 接诊串联', icon: 'el-icon-tickets', color: '#67C23A', bg: '#f0f9eb' },
        { path: '/medical/prescription', title: '处方发药', desc: '开方 / 发药 / 退药', icon: 'el-icon-first-aid-kit', color: '#E6A23C', bg: '#fdf6ec' },
        { path: '/medical/drug', title: '药品管理', desc: '库存与效期', icon: 'el-icon-goods', color: '#F56C6C', bg: '#fef0f0' },
        { path: '/medical/statistics', title: '统计看板', desc: '业务数据汇总', icon: 'el-icon-pie-chart', color: '#409EFF', bg: '#ecf5ff' },
        { path: '/medical/ai-record', title: 'AI病历书写', desc: '大模型辅助生成', icon: 'el-icon-edit-outline', color: '#67C23A', bg: '#f0f9eb' },
        { path: '/medical/ai-chat', title: '智能导诊问答', desc: '知识库 RAG 问答', icon: 'el-icon-chat-dot-round', color: '#E6A23C', bg: '#fdf6ec' },
        { path: '/medical/message', title: '站内信', desc: '预警与通知', icon: 'el-icon-message', color: '#909399', bg: '#f4f4f5' }
      ]
    }
  },
  computed: {
    ...mapGetters(['name']),
    title() {
      return settings.title
    },
    greeting() {
      const h = new Date().getHours()
      if (h < 6) return '夜深了'
      if (h < 9) return '早上好'
      if (h < 12) return '上午好'
      if (h < 14) return '中午好'
      if (h < 18) return '下午好'
      return '晚上好'
    },
    today() {
      const d = new Date()
      return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
    },
    week() {
      return ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][new Date().getDay()]
    },
    statCards() {
      return [
        { label: '患者总数', value: this.dashboard.patientTotal || 0, icon: 'el-icon-user', color: '#409EFF' },
        { label: '挂号总量', value: this.dashboard.registrationTotal || 0, icon: 'el-icon-tickets', color: '#67C23A' },
        { label: '处方总量', value: this.dashboard.prescriptionTotal || 0, icon: 'el-icon-document', color: '#E6A23C' },
        { label: '发药总量', value: this.dashboard.dispensingTotalQty || 0, icon: 'el-icon-first-aid-kit', color: '#909399' },
        { label: '库存预警', value: this.dashboard.lowStockCount || 0, icon: 'el-icon-warning-outline', color: '#F56C6C', warn: true }
      ]
    }
  },
  created() {
    getDashboard().then(res => {
      this.dashboard = res.data || {}
    }).catch(() => {})
  },
  methods: {
    go(path) {
      if (this.$route.path !== path) {
        this.$router.push(path)
      }
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 0;
}
.welcome {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22px 24px;
  margin-bottom: 16px;
  background: linear-gradient(120deg, #409eff 0%, #2f7fd8 100%);
  border-radius: 6px;
  color: #fff;
}
.welcome-text h2 {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 600;
}
.welcome-text p {
  margin: 0;
  font-size: 13px;
  opacity: .85;
}
.welcome-date {
  text-align: right;
  line-height: 1.6;
}
.welcome-date .date {
  display: block;
  font-size: 14px;
  font-weight: 600;
}
.welcome-date .week {
  display: block;
  font-size: 12px;
  opacity: .85;
}

.stat-row {
  margin-bottom: 16px;
}
.stat-card {
  cursor: default;
}
.stat-card.is-warn >>> .el-card__body {
  background: #fef0f0;
}
.stat-inner {
  display: flex;
  align-items: center;
}
.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
}
.stat-icon i {
  font-size: 22px;
  color: #fff;
}
.stat-meta {
  margin-left: 14px;
  min-width: 0;
}
.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.entry-header {
  font-weight: 600;
}
.entry-item {
  display: flex;
  align-items: center;
  padding: 14px 12px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all .2s;
}
.entry-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 10px rgba(64, 158, 255, .15);
  transform: translateY(-2px);
}
.entry-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
}
.entry-icon i {
  font-size: 20px;
}
.entry-info {
  margin-left: 12px;
  min-width: 0;
}
.entry-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.entry-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
