import Vue from 'vue'
import Vuex from 'vuex'
import app from './modules/app'
import user from './modules/user'
import settings from './modules/settings'
import tagsView from './modules/tagsView'
import message from './modules/message'
import getters from './getters'

Vue.use(Vuex)

const store = new Vuex.Store({
  modules: {
    app,
    user,
    settings,
    tagsView,
    message
  },
  getters
})

export default store
