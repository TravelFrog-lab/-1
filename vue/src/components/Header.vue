<template>
  <div class="header-bar">
    <div class="header-left">
      <BreadCrumbs />
    </div>
    <div class="header-right">
      <el-button
        type="text"
        class="logout-btn"
        title="退出登录"
        aria-label="退出登录"
        @click="handleLogout"
      >
        <i class="el-icon-switch-button"></i>
      </el-button>
    </div>
  </div>
</template>

<script>
import BreadCrumbs from '../components/BreadCrumbs/index.vue'
import { mapActions } from 'vuex'

export default {
  name: 'HeaderBar',
  components: {
    BreadCrumbs
  },
  methods: {
    ...mapActions(['logout']),
    handleLogout() {
      this.$confirm('确定注销并退出系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.logout()
        this.$router.push({ path: '/login' })
      }).catch(() => {})
    }
  }
}
</script>

<style lang="less" scoped>
.header-bar {
  width: 100%;
  height: 100%;
  min-height: 60px;
  box-sizing: border-box;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  padding: 0 8px 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 1000;

  .header-left {
    display: flex;
    align-items: center;
    min-width: 0;
    flex: 1;
  }

  .header-right {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    height: 100%;
    flex-shrink: 0;
    margin-left: auto;
  }
}

.logout-btn {
  font-size: 20px;
  color: #606266;
  padding: 8px;
  min-width: auto;
  margin: 0;

  &:hover {
    color: #1a237e;
  }

  i {
    margin: 0;
  }
}
</style>
