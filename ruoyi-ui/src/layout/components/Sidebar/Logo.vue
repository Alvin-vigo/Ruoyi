<template>
  <div class="sidebar-logo-container" :class="{ collapse: isCollapse }">
    <a class="sidebar-logo-link">
      <span class="sidebar-title">{{ title }}</span>
    </a>
  </div>
</template>

<script>
import settings from '@/settings'

export default {
  name: 'SidebarLogo',
  props: {
    collapse: { type: Boolean, required: true }
  },
  data() {
    return {
      title: settings.title
    }
  }
}
</script>

<style lang="scss" scoped>
.sidebar-logo-container {
  position: relative;
  width: 100%;
  height: 50px;
  line-height: 50px;
  background: #2b3648;
  text-align: center;
  overflow: hidden;
  .sidebar-logo-link {
    height: 100%;
    width: 100%;
    display: block;
    .sidebar-title {
      color: #fff;
      font-weight: 600;
      font-size: 15px;
      white-space: nowrap;
    }
  }
}
</style>
