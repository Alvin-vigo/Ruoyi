const getters = {
  sidebar: state => state.app.sidebar,
  device: state => state.app.device,
  token: state => state.user.token,
  name: state => state.user.name,
  avatar: state => state.user.avatar,
  roles: state => state.user.roles,
  permissions: state => state.user.permissions,
  visitedViews: state => state.tagsView.visitedViews,
  cachedViews: state => state.tagsView.cachedViews,
  sidebarLogo: state => state.settings.sidebarLogo,
  fixedHeader: state => state.settings.fixedHeader,
  showSettings: state => state.settings.showSettings,
  messageUnreadCount: state => state.message.unreadCount,
  messageLatest: state => state.message.latestList
}
export default getters
