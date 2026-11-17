import Vue from 'vue'
import Router from 'vue-router'
import Layout from '@/layout'

Vue.use(Router)

// 静态路由：本项目后端无动态路由接口（/getRouters），
// 菜单直接在此声明，Sidebar 依据 routes 的 meta 生成。
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index')
      }
    ]
  },
  {
    path: '/',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: 'index',
        component: () => import('@/views/dashboard/index'),
        name: 'Index',
        meta: { title: '首页', icon: 'el-icon-menu', affix: true }
      }
    ]
  },
  {
    path: '/medical',
    component: Layout,
    redirect: '/medical/patient',
    meta: { title: '医疗管理', icon: 'el-icon-s-data' },
    children: [
      {
        path: 'patient',
        name: 'Patient',
        component: () => import('@/views/medical/patient/index'),
        meta: { title: '患者管理', icon: 'el-icon-user' }
      },
      {
        path: 'diagnosis',
        name: 'Diagnosis',
        component: () => import('@/views/medical/diagnosis/index'),
        meta: { title: '诊疗记录', icon: 'el-icon-document' }
      },
      {
        path: 'drug',
        name: 'Drug',
        component: () => import('@/views/medical/drug/index'),
        meta: { title: '药品管理', icon: 'el-icon-goods' }
      },
      {
        path: 'followup',
        name: 'Followup',
        component: () => import('@/views/medical/followup/index'),
        meta: { title: '随访管理', icon: 'el-icon-date' }
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('@/views/medical/message/index'),
        meta: { title: '站内信', icon: 'el-icon-message' }
      },
      {
        path: 'feeReport',
        name: 'FeeReport',
        component: () => import('@/views/medical/feeReport/index'),
        meta: { title: '费用报表', icon: 'el-icon-money' }
      },
      {
        path: 'price',
        name: 'Price',
        component: () => import('@/views/medical/price/index'),
        meta: { title: '费用价目', icon: 'el-icon-price-tag' }
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('@/views/medical/schedule/index'),
        meta: { title: '号源排班', icon: 'el-icon-date' }
      },
      {
        path: 'registration',
        name: 'Registration',
        component: () => import('@/views/medical/registration/index'),
        meta: { title: '挂号管理', icon: 'el-icon-tickets' }
      },
      {
        path: 'prescription',
        name: 'Prescription',
        component: () => import('@/views/medical/prescription/index'),
        meta: { title: '处方发药', icon: 'el-icon-first-aid-kit' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/medical/statistics/index'),
        meta: { title: '统计看板', icon: 'el-icon-pie-chart' }
      },
      {
        path: 'ai-record',
        name: 'AiRecord',
        component: () => import('@/views/medical/ai/record'),
        meta: { title: 'AI病历书写', icon: 'el-icon-edit-outline' }
      },
      {
        path: 'ai-risk',
        name: 'AiRisk',
        component: () => import('@/views/medical/ai/risk'),
        meta: { title: '疾病风险预测', icon: 'el-icon-data-analysis' }
      },
      {
        path: 'ai-xray',
        name: 'AiXray',
        component: () => import('@/views/medical/ai/xray'),
        meta: { title: '胸片AI初筛', icon: 'el-icon-picture-outline' }
      },
      {
        path: 'ai-chat',
        name: 'AiChat',
        component: () => import('@/views/medical/ai/chat'),
        meta: { title: '智能导诊问答', icon: 'el-icon-chat-dot-round' }
      },
      {
        path: 'ai-knowledge',
        name: 'AiKnowledge',
        component: () => import('@/views/medical/ai/knowledge'),
        meta: { title: '导诊知识库', icon: 'el-icon-collection' }
      }
    ]
  },
  {
    path: '/monitor',
    component: Layout,
    redirect: '/monitor/job',
    meta: { title: '系统监控', icon: 'el-icon-monitor' },
    children: [
      {
        path: 'job',
        name: 'Job',
        component: () => import('@/views/monitor/job/index'),
        meta: { title: '定时任务', icon: 'el-icon-time' }
      },
      {
        path: 'job/log',
        name: 'JobLog',
        component: () => import('@/views/monitor/job/log'),
        meta: { title: '任务日志', icon: 'el-icon-document' }
      }
    ]
  },
  { path: '*', redirect: '/404', hidden: true }
]

export default new Router({
  mode: 'history',
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})
