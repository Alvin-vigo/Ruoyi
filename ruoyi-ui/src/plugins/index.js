import Vue from 'vue'
import Pagination from '@/components/Pagination'
import { download } from '@/utils/request'
import { resetForm, addDateRange, parseTime } from '@/utils/index'
import { hasPermi, hasRole } from '@/directive/permission'
import Modal from './modal'
import Tab from './tab'

// 全局注册通用分页组件（各列表页直接写 <pagination>，无需逐个 import）
Vue.component('Pagination', Pagination)

// 注册权限指令。本项目后端无鉴权体系，指令为「全部放行」实现，
// 既避免 8 个业务页刷 "Failed to resolve directive: hasPermi" 警告，
// 又保留 v-hasPermi 语义，将来接入鉴权时只改 directive/permission.js 内部即可。
Vue.directive('hasPermi', hasPermi)
Vue.directive('hasRole', hasRole)

// 全局挂载若依常用对象与方法（业务页通过 this.$modal / this.download 等调用）
Vue.prototype.$modal = Modal
Vue.prototype.$tab = Tab
Vue.prototype.download = download
Vue.prototype.resetForm = resetForm
Vue.prototype.addDateRange = addDateRange
Vue.prototype.parseTime = parseTime
