<template>
  <div class="sidebar-container" :style="{ width: sidebar.opened ? '210px' : '64px' }">
    <logo :collapse="!sidebar.opened" />
    <el-menu
      :default-active="activeMenu"
      :collapse="!sidebar.opened"
      :unique-opened="true"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      mode="vertical"
      @select="handleSelect"
    >
      <sidebar-item
        v-for="route in sidebarRoutes"
        :key="route.path"
        :item="route"
        :base-path="route.path"
      />
    </el-menu>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Logo from './Logo'
import SidebarItem from './SidebarItem'

export default {
  name: 'Sidebar',
  components: { Logo, SidebarItem },
  computed: {
    ...mapGetters(['sidebar']),
    sidebarRoutes() {
      return this.$router.options.routes.filter(r => !r.hidden)
    },
    activeMenu() {
      return this.$route.path
    }
  },
  methods: {
    handleSelect(index) {
      if (index && !/^(https?:|mailto:|tel:)/.test(index)) {
        this.$router.push(index)
      }
    }
  }
}
</script>

<style lang="scss">
.sidebar-container {
  transition: width 0.28s;
  height: 100%;
  background-color: #304156;
  overflow: hidden;
  .el-menu {
    border: none;
    height: 100%;
    width: 100% !important;
  }
}
</style>
