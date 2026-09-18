import axios from 'axios'
import { Message } from 'element-ui'
import { getToken } from '@/utils/auth'

// 创建 axios 实例
const service = axios.create({
  // 留空：通过 vue.config.js 的 devServer.proxy 按路径转发到后端 8080
  baseURL: process.env.VUE_APP_BASE_API || '',
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    if (getToken()) {
      config.headers['Authorization'] = 'Bearer ' + getToken()
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  res => {
    // 若返回的是文件流（Blob），直接放行
    if (res.request && res.request.responseType === 'blob') {
      return res
    }
    const code = res.data && res.data.code !== undefined ? res.data.code : 200
    if (code === 200) {
      return res.data
    } else if (code === 401) {
      Message.error('认证失败，请重新登录')
      removeToken()
      // 跳转登录页（整页刷新，重置路由守卫与状态）
      if (location.href.indexOf('/login') === -1) {
        location.href = '/login'
      }
      return Promise.reject('认证失败')
    } else {
      Message.error(res.data.msg || '请求失败')
      return Promise.reject(res.data.msg || 'error')
    }
  },
  error => {
    Message.error(error.message || '网络异常，请稍后重试')
    return Promise.reject(error)
  }
)

export default service

/**
 * 通用下载封装（若依标准）。
 * 业务页通过 this.download(url, params, filename) 调用。
 * 以 POST + 表单方式提交参数，后端 @RequestMapping(method={GET,POST}) 均可接收。
 */
export function download(url, params, filename) {
  return service
    .post(url, params, {
      transformRequest: [
        params => {
          const formData = new URLSearchParams()
          if (params) {
            Object.keys(params).forEach(key => {
              if (params[key] !== undefined && params[key] !== null) {
                formData.append(key, params[key])
              }
            })
          }
          return formData
        }
      ],
      responseType: 'blob'
    })
    .then(res => {
      const blob = new Blob([res.data])
      let name = filename
      if (!name) {
        const disposition = res.headers['content-disposition']
        if (disposition) {
          const m = disposition.match(/filename\*?=(?:UTF-8'')?([^;]+)/i)
          if (m) {
            name = decodeURIComponent(m[1].replace(/^["']|["']$/g, ''))
          }
        }
      }
      if (!name) {
        name = 'download'
      }
      if ('download' in document.createElement('a')) {
        const elink = document.createElement('a')
        elink.download = name
        elink.style.display = 'none'
        elink.href = URL.createObjectURL(blob)
        document.body.appendChild(elink)
        elink.click()
        URL.revokeObjectURL(elink.href)
        document.body.removeChild(elink)
      } else {
        navigator.msSaveBlob(blob, name)
      }
    })
    .catch(r => {
      console.error(r)
    })
}
