<template>
  <div class="menu-item-wrapper">
    <i v-if="icon" :class="icon" />
    <span v-if="title">{{ title }}</span>
  </div>
</template>

<script>
export default {
  name: 'MenuItem',
  props: {
    icon: { type: String, default: '' },
    title: { type: String, default: '' }
  }
}
</script>

<style scoped>
.menu-item-wrapper {
  display: inline-flex;
  align-items: center;
}
.menu-item-wrapper i {
  margin-right: 8px;
}
</style>
