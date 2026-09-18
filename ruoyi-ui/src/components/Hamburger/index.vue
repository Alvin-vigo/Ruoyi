<template>
  <div class="hamburger" @click="toggleClick">
    <svg
      :class="{ 'is-active': isActive }"
      viewBox="0 0 1024 1024"
      width="18"
      height="18"
    >
      <path
        d="M896 320H128c-17.7 0-32 14.3-32 32s14.3 32 32 32h768c17.7 0 32-14.3 32-32s-14.3-32-32-32zM896 544H128c-17.7 0-32 14.3-32 32s14.3 32 32 32h768c17.7 0 32-14.3 32-32s-14.3-32-32-32zM896 768H128c-17.7 0-32 14.3-32 32s14.3 32 32 32h768c17.7 0 32-14.3 32-32s-14.3-32-32-32z"
        fill="currentColor"
      />
    </svg>
  </div>
</template>

<script>
export default {
  name: 'Hamburger',
  props: {
    isActive: { type: Boolean, default: false }
  },
  methods: {
    toggleClick() {
      this.$emit('toggleClick')
    }
  }
}
</script>

<style scoped>
.hamburger {
  display: inline-block;
  vertical-align: middle;
  cursor: pointer;
  color: #5a5e66;
}
.hamburger svg.is-active {
  transform: rotate(180deg);
  transition: 0.38s;
}
</style>
