import store from '@/store'
import router from '@/router'

/**
 * 页签操作封装（若依标准，最小实现）
 */
const Tab = {
  openPage(url) {
    router.push(url)
  },
  refreshPage(obj) {
    const route = obj || router.currentRoute
    store.dispatch('delCachedView', route).then(() => {
      const { fullPath } = route
      nextTick(() => {
        router.replace({ path: '/redirect' + fullPath })
      })
    })
  },
  closeOpenPage(obj) {
    if (obj) {
      store.dispatch('delView', obj)
    }
  },
  closePage(obj) {
    if (obj) {
      store.dispatch('delView', obj).then(() => {
        const latestView = store.state.tagsView.visitedViews.slice(-1)[0]
        if (latestView) {
          router.push(latestView.fullPath || latestView.path)
        } else {
          router.push('/index')
        }
      })
    }
  }
}

function nextTick(cb) {
  Promise.resolve().then(cb)
}

export default Tab
