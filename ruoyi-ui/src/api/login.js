import request from '@/utils/request'

// 登录
export function login(username, password) {
  const data = { username, password }
  return request({
    url: '/login',
    method: 'post',
    data: data
  })
}

// 获取用户信息
export function getInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}

// 退出登录（由 Spring Security 登出过滤器处理，返回成功 JSON）
export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}
