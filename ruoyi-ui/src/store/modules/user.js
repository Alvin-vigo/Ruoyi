import { login, getInfo, logout } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'

const user = {
  state: {
    token: getToken(),
    name: '',
    avatar: '',
    roles: [],
    permissions: []
  },

  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token
    },
    SET_NAME: (state, name) => {
      state.name = name
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_ROLES: (state, roles) => {
      state.roles = roles
    },
    SET_PERMISSIONS: (state, permissions) => {
      state.permissions = permissions
    },
    RESET: (state) => {
      state.token = ''
      state.name = ''
      state.avatar = ''
      state.roles = []
      state.permissions = []
    }
  },

  actions: {
    // 登录
    Login({ commit }, userInfo) {
      const username = userInfo.username.trim()
      const password = userInfo.password
      return new Promise((resolve, reject) => {
        login(username, password).then(res => {
          setToken(res.token)
          commit('SET_TOKEN', res.token)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 获取用户信息
    GetInfo({ commit }) {
      return new Promise((resolve, reject) => {
        getInfo().then(res => {
          const user = res.user || {}
          commit('SET_NAME', user.userName || user.nickName || '')
          commit('SET_AVATAR', user.avatar || '')
          commit('SET_ROLES', res.roles || [])
          commit('SET_PERMISSIONS', res.permissions || [])
          resolve(res)
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 退出登录
    Logout({ commit }) {
      return new Promise((resolve, reject) => {
        logout().then(() => {
          removeToken()
          commit('RESET')
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    }
  }
}

export default user
