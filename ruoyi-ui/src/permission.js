import router from './router'
import store from './store'
import { getToken, removeToken } from '@/utils/auth'

// 路由守卫：未登录跳转登录页；已登录则拉取用户信息（含角色/权限）后再放行
router.beforeEach((to, from, next) => {
  const hasToken = getToken()

  if (hasToken) {
    if (to.path === '/login') {
      // 已登录还访问登录页 -> 直接进首页
      next({ path: '/' })
    } else {
      // 已登录但角色信息为空 -> 拉取用户信息
      if (store.getters.roles.length === 0) {
        store
          .dispatch('GetInfo')
          .then(() => {
            next({ ...to, replace: true })
          })
          .catch(() => {
            // 获取信息失败（token 失效等）：清除本地状态。
            // 401 的整页跳转由 request.js 统一处理（location.href='/login'），此处不必再 next。
            removeToken()
            store.commit('RESET')
          })
      } else {
        next()
      }
    }
  } else {
    if (to.path === '/login') {
      next()
    } else {
      // 未登录访问受保护页 -> 跳转登录页并携带回调地址
      next(`/login?redirect=${to.path}`)
    }
  }
})
