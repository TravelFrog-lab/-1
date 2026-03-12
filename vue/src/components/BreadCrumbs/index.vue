<template>
  <div style="display: flex;flex-direction: row; align-items: center;">


    <el-breadcrumb separator-class="el-icon-arrow-right" >
      <!-- <el-breadcrumb-item :to="{ path: '/showView' }">首页</el-breadcrumb-item> -->
      <el-breadcrumb-item v-for="item in BreadcrumbItemList" :key="item.path" style="font-size: medium;">{{ item.meta.title }}</el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script>

export default {
  name: 'BreadCrumbs',
  data() {
    return {

      hasClick: false
    }
  },
  computed: {
    BreadcrumbItemList() {
      return this.$route.matched
    },
  },
  created() {
    console.log("------------------------------")
    console.log(this.$route)
    console.log("------------------------------")
    this.isHome(this.$route.path)
  },
  methods: {
    isHome(path) {
      if (path === '/showView') {
        this.hasClick = false
      } else {
        this.hasClick = true
      }
    },
    goHome() {
      this.$router.push('/showView')
    },
  }
}
</script>

<style lang="less">
.el-breadcrumb__inner{
  font-size: medium;
  
  font-weight:600
}
.home-link{
  font-size: medium;

  font-weight:600
}
</style>