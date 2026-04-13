<template>
  <div class="front-user-layout">
    <!-- 左侧垂直导航 -->
    <aside class="front-sidebar">
      <div class="sidebar-brand">
        <i class="el-icon-house"></i>
        <span>邻聚里</span>
      </div>

      <div class="sidebar-user">
        <el-avatar :size="48" :src="avatarUrl" class="sidebar-avatar">
          {{ userInitial }}
        </el-avatar>
        <div class="sidebar-nickname">{{ displayName }}</div>
      </div>

      <el-menu
        :default-active="activeMenuPath"
        class="sidebar-menu"
        background-color="#1e293b"
        text-color="#94a3b8"
        active-text-color="#ffffff"
        router
        @select="onMenuSelect"
      >
        <el-menu-item index="/home">
          <i class="el-icon-s-home"></i>
          <span slot="title">首页</span>
        </el-menu-item>
        <el-menu-item index="/property-fee">
          <i class="el-icon-wallet"></i>
          <span slot="title">物业缴费</span>
        </el-menu-item>
        <el-menu-item index="/repair">
          <i class="el-icon-service"></i>
          <span slot="title">报修服务</span>
        </el-menu-item>
        <el-menu-item index="/complaint">
          <i class="el-icon-chat-line-round"></i>
          <span slot="title">投诉建议</span>
        </el-menu-item>
        <el-menu-item index="/community-activity">
          <i class="el-icon-coordinate"></i>
          <span slot="title">小区活动</span>
        </el-menu-item>
        <el-menu-item index="/housekeeping">
          <i class="el-icon-brush"></i>
          <span slot="title">家政服务</span>
        </el-menu-item>
        <el-menu-item index="/vote">
          <i class="el-icon-s-data"></i>
          <span slot="title">投票表决</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <i class="el-icon-user"></i>
          <span slot="title">个人中心</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 右侧主区域 -->
    <div class="front-main">
      <header class="front-main__topbar">
        <div class="topbar-actions">
          <el-button
            type="text"
            class="topbar-bell-btn"
            title="消息与待办"
            aria-label="消息与待办"
            @click="$router.push('/notification')"
          >
            <i class="el-icon-bell"></i>
          </el-button>
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
      </header>

      <main class="front-main__content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'FrontUserLayout',
  computed: {
    ...mapGetters(['currentUser']),
    displayName() {
      return this.currentUser?.name || this.currentUser?.username || '业主'
    },
    userInitial() {
      const n = this.displayName
      return n ? n.charAt(0).toUpperCase() : 'U'
    },
    avatarUrl() {
      const a = this.currentUser?.avatar
      if (!a) return ''
      if (a.startsWith('http')) return a
      return '/api' + (a.startsWith('/') ? a : '/' + a)
    },
    activeMenuPath() {
      const sidebarPaths = [
        '/home',
        '/property-fee',
        '/repair',
        '/complaint',
        '/community-activity',
        '/housekeeping',
        '/vote',
        '/profile'
      ]
      const p = this.$route.path
      if (p.startsWith('/vote/')) return '/vote'
      if (p.startsWith('/news/')) return '/home'
      return sidebarPaths.includes(p) ? p : ''
    }
  },
  methods: {
    onMenuSelect() {},
    handleLogout() {
      this.$store.dispatch('logout')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.front-user-layout {
  display: flex;
  height: 100vh;
  min-height: 0;
  overflow: hidden;
  background: #f0f2f5;
  position: relative;
}

/* 左侧栏固定，不随页面/主区域滚动 */
.front-sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 220px;
  z-index: 100;
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 24px rgba(15, 23, 42, 0.12);
  overflow: hidden;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 16px;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar-brand i {
  font-size: 24px;
  color: #38bdf8;
}

.sidebar-user {
  padding: 20px 16px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar-avatar {
  border: 2px solid rgba(56, 189, 248, 0.5);
}

.sidebar-nickname {
  margin-top: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #f1f5f9;
  word-break: break-all;
}

.sidebar-menu {
  flex: 1;
  min-height: 0;
  border-right: none !important;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-menu >>> .el-menu-item {
  height: 48px;
  line-height: 48px;
  margin: 2px 8px;
  border-radius: 8px;
}

.sidebar-menu >>> .el-menu-item i {
  color: inherit;
}

.sidebar-menu >>> .el-menu-item.is-active {
  background: linear-gradient(90deg, #0ea5e9 0%, #0284c7 100%) !important;
}

.front-main {
  flex: 1;
  min-width: 0;
  min-height: 0;
  margin-left: 220px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 顶栏固定，仅下方内容区滚动 */
.front-main__topbar {
  position: sticky;
  top: 0;
  height: 56px;
  flex-shrink: 0;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  z-index: 50;
}

.topbar-actions {
  display: flex;
  align-items: center;
}

.topbar-bell-btn {
  font-size: 22px;
  color: #606266 !important;
  padding: 8px !important;
}

.topbar-bell-btn:hover {
  color: #409eff !important;
}

.logout-btn {
  font-size: 22px;
  color: #606266 !important;
  padding: 8px !important;
}

.logout-btn:hover {
  color: #f56c6c !important;
}

.front-main__content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 0;
  -webkit-overflow-scrolling: touch;
}

@media screen and (max-width: 768px) {
  .front-sidebar {
    width: 64px;
  }

  .front-main {
    margin-left: 64px;
  }

  .sidebar-brand span,
  .sidebar-nickname,
  .sidebar-menu >>> .el-menu-item span {
    display: none;
  }

  .sidebar-brand {
    justify-content: center;
    padding: 16px 8px;
  }

  .sidebar-user {
    padding: 12px 8px;
  }

  .sidebar-menu >>> .el-menu-item {
    padding: 0 !important;
    text-align: center;
  }

  .sidebar-menu >>> .el-menu-item i {
    margin-right: 0 !important;
  }
}
</style>
