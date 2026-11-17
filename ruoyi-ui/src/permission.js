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
            // 获取信息失败（token 失效等）：清除本地状态并显式放行到登录页。
            //
            // 这里必须调用 next()，否则路由跳转会永远停在 pending 状态，
            // 用户表现为「点了没反应」。
            // 不依赖 request.js 的 401 整页跳转兜底：那条链路只覆盖「接口请求被拦截」，
            // 覆盖不到 GetInfo 自身失败的情况。
            removeToken()
            store.commit('RESET')
            next({ path: '/login', query: { redirect: to.fullPath }, replace: true })
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
