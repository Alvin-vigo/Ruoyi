<template>
  <div class="app-wrapper">
    <sidebar class="sidebar-container" />
    <div class="main-container">
      <navbar />
      <app-main />
    </div>
  </div>
</template>

<script>
import { Sidebar, Navbar, AppMain } from './components'

export default {
  name: 'Layout',
  components: { Sidebar, Navbar, AppMain }
}
</script>

<style lang="scss" scoped>
.app-wrapper {
  position: relative;
  height: 100%;
  width: 100%;
  display: flex;
}
.main-container {
  flex: 1;
  height: 100%;
  overflow: hidden;
}
</style>
