<template>
  <div class="wscn-http404">
    <div class="bullshit">
      <div class="bullshit__oops">404</div>
      <div class="bullshit__headline">页面不存在</div>
      <el-button type="primary" @click="backHome">返回首页</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotFound',
  methods: {
    backHome() {
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.wscn-http404 {
  text-align: center;
  padding-top: 120px;
}
.bullshit__oops {
  font-size: 80px;
  color: #409eff;
  font-weight: bold;
}
.bullshit__headline {
  font-size: 20px;
  margin: 20px 0 30px;
  color: #303133;
}
</style>
