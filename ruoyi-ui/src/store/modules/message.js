import { unreadCount, getLatestMessages } from '@/api/medical/message'

// 站内信红点：未读数 + 最近消息，Navbar 轮询拉取
const state = {
  unreadCount: 0,
  latestList: []
}

const mutations = {
  SET_UNREAD: (state, count) => {
    state.unreadCount = count
  },
  SET_LATEST: (state, list) => {
    state.latestList = list
  }
}

const actions = {
  // 一次性拉取未读数和最近消息，供顶部小红点展示
  // 注意：request.js 拦截器返回的是整个响应体 {msg, code, data}，
  // 此处必须取 .data 解包，否则对象会被 el-badge 当文本渲染成红色 JSON 块
  fetchMessageOverview({ commit }, receiver) {
    const unwrap = res => (res && typeof res === 'object' && 'data' in res) ? res.data : res
    return unreadCount(receiver).then(res => {
      commit('SET_UNREAD', unwrap(res) || 0)
      return getLatestMessages(receiver, 5)
    }).then(res => {
      commit('SET_LATEST', unwrap(res) || [])
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
