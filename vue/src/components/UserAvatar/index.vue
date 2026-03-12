<template>
  <el-dropdown class="user-avatar-wrapper" @command="handleCommand" trigger="click">
    <div class="avatar-box">
      <div class="user-info">
        <span class="username">{{currentUser.username}}</span>
        <span class="role">{{currentUser.role || '管理员'}}</span>
      </div>
      <i class="el-icon-caret-bottom" />
    </div>

    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item command="userCenter">
        <i class="el-icon-user-solid"></i>
        个人中心
      </el-dropdown-item>
 
      <el-divider style="margin: 5px 0" />
      <el-dropdown-item command="loginOut">
        <i class="el-icon-switch-button"></i>
        退出登录
      </el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'UserAvatar',
  computed: {
    ...mapGetters(['currentUser'])
  },
  methods: {
    ...mapActions(['logout']),
    handleCommand(command) {
      if (command === 'userCenter') {
        this.$router.push({ path: '/personInfo' })
      }
      if (command === 'loginOut') {
        this.loginOut()
      }
    },
    loginOut() {
      this.$confirm('确定注销并退出系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.logout()  // 使用 Vuex action
        this.$router.push({ path: '/login' })
      })
    }
  }
}
</script>

<style lang="less" scoped>
.user-avatar-wrapper {
  height: 40px;
  min-width: 120px;
  padding: 0 12px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;

  &:hover {
    background-color: rgba(0, 0, 0, 0.04);
  }

  .avatar-box {
    display: flex;
    align-items: center;
    gap: 8px;
    width: 100%;

    .user-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
      min-width: 0;

      .username {
        font-size: 14px;
        color: #333;
        font-weight: 500;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .role {
        font-size: 12px;
        color: #999;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    .el-icon-caret-bottom {
      font-size: 12px;
      color: #999;
      margin-left: 4px;
    }
  }
}

:deep(.el-dropdown-menu) {
  z-index: 2002 !important;
  min-width: 120px;
}

.el-dropdown-menu {
  .el-dropdown-item {
    display: flex;
    align-items: center;
    padding: 8px 16px;
    
    i {
      margin-right: 8px;
      font-size: 16px;
    }
  }
}
</style>