<template>
  <div class="unauthorized">
    <h1>403</h1>
    <h2>抱歉，您没有访问该页面的权限</h2>
    <el-button type="primary" @click="goBack">返回</el-button>
  </div>
</template>

<script>
export default {
  name: '403',
  methods: {
    goBack() {
      this.$router.go(-1)
    }
  }
}
</script>

<style scoped>
.unauthorized {
  text-align: center;
  padding: 50px;
}
h1 {
  font-size: 72px;
  color: #409EFF;
}
h2 {
  color: #606266;
  margin: 20px 0;
}
</style> 