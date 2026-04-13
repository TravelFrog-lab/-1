<template>
  <div class="nav-header">
    <div class="nav-content">
      <router-link to="/home" class="logo">
        <div class="logo-icon">
          <i class="el-icon-house"></i>
        </div>
        <div class="logo-text">
          <span class="logo-title">邻聚里</span>
          <span class="logo-subtitle">社区服务协同系统</span>
        </div>
      </router-link>
      
      <div class="nav-menu-container">
        <el-menu 
          :default-active="activeIndex" 
          mode="horizontal" 
          @select="handleSelect"
          class="nav-menu"
          background-color="#fff"
          text-color="#303133">
          <el-menu-item index="1">首页</el-menu-item>
          <el-submenu index="2">
            <template slot="title">快捷服务</template>
            <el-menu-item-group title="物业服务">
              <el-menu-item index="2-1">
                <i class="el-icon-wallet"></i>
                物业缴费
              </el-menu-item>
              <el-menu-item index="2-2">
                <i class="el-icon-service"></i>
                报修服务
              </el-menu-item>
              <el-menu-item index="2-3">
                <i class="el-icon-chat-line-round"></i>
                投诉建议
              </el-menu-item>
            </el-menu-item-group>
            <el-menu-item-group title="邻里互助">
              <el-menu-item index="2-4">
                <i class="el-icon-user-solid"></i>
                邻里互助
              </el-menu-item>
              <el-menu-item index="2-7">
                <i class="el-icon-location-information"></i>
                家政服务
              </el-menu-item>
            </el-menu-item-group>
          </el-submenu>
          
    
          
          <el-menu-item index="4">
            <i class="el-icon-user"></i>
            个人中心
          </el-menu-item>
        </el-menu>

        <!-- 用户信息区域 -->
        <div class="user-area">
          <!-- 未登录状态 -->
          <template v-if="!isLoggedIn">
            <el-button type="text" @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" size="small" @click="$router.push('/register')">注册</el-button>
          </template>

          <!-- 已登录状态 -->
          <template v-else>
            <!-- 通知图标和弹出框 -->
            <el-popover
              placement="bottom-end"
              width="400"
              trigger="click"
              popper-class="notification-popover"
              @show="handlePopoverShow">
              <notification-list 
                ref="notificationList"
                @update-unread-count="updateUnreadCount">
              </notification-list>
              <div slot="reference" class="notice-container">
                <el-badge :value="unreadCount" :max="99">
                  <i class="el-icon-bell"></i>
                </el-badge>
              </div>
            </el-popover>

            <!-- 用户下拉菜单 -->
            <el-dropdown @command="handleUserCommand" trigger="click">
              <div class="user-info">
                <el-avatar 
                  :size="32" 
                  class="user-avatar"
                  :src="avatarImageUrl"
                  :style="avatarStyle">
                  {{ currentUser.username ? currentUser.username.charAt(0).toUpperCase() : 'U' }}
                </el-avatar>
                <span class="username">{{ currentUser.username }}</span>
                <i class="el-icon-arrow-down"></i>
              </div>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="profile">
                  <i class="el-icon-user"></i> 个人中心
                </el-dropdown-item>
           
                <el-dropdown-item divided command="logout">
                  <i class="el-icon-switch-button"></i> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import NotificationList from './NotificationList.vue'
import Request from '@/utils/request'
import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'NavHeader',
  components: {
    NotificationList
  },
  data() {
    return {
      activeIndex: '1',
      unreadCount: 0,
      notificationVisible: false,
      notificationTimer: null
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'currentUser']),
    
    // 添加头像样式计算属性
    avatarStyle() {
      const hasAvatar = this.currentUser?.avatar
      return {
        backgroundColor: hasAvatar ? 'transparent' : this.getAvatarColor(this.currentUser?.username)
      }
    },
    avatarImageUrl() {
      const avatar = this.currentUser?.avatar
      if (!avatar) return ''
      if (avatar.startsWith('http')) return avatar
      return '/api' + (avatar.startsWith('/') ? avatar : '/' + avatar)
    }
  },
  methods: {
    ...mapActions(['logout']),
    handleSelect(key) {
      this.activeIndex = key
      switch(key) {
        case '1':
          this.$router.push('/home')
          break
        case '2-1':
          this.$router.push('/property-fee')
          break
        case '2-2':
          this.$router.push('/repair')
          break
        case '2-3':
          this.$router.push('/complaint')
          break
        case '2-4':
          this.$router.push('/community-activity')
          break
        case '2-7':
          this.$router.push('/housekeeping')
          break
        case '3':
          // 点击通知菜单项时触发通知图标的点击
          const noticeBtn = document.querySelector('.notice-btn')
          noticeBtn?.click()
          break
        case '4':
          this.$router.push('/profile')
          break
      }
    },
    getUnreadCount() {
      if (this.isLoggedIn && this.currentUser?.id) {
        Request.get(`/notification/user/${this.currentUser.id}`).then(response => {
          if (response.code === '0' && response.data) {
            this.unreadCount = response.data.filter(item => !item.isRead).length
          }
        })
      }
    },
    updateUnreadCount(count) {
      this.unreadCount = count
    },
    handlePopoverShow() {
      this.$nextTick(() => {
        this.$refs.notificationList?.getNotifications()
      })
    },
    handleUserCommand(command) {
      switch(command) {
        case 'profile':
          this.$router.push('/profile')
          break
        case 'logout':
          this.logout()
          this.$message.success('已退出登录')
          this.$router.push('/login')
          break
      }
    },
    // 根据用户名生成随机颜色
    getAvatarColor(username) {
      const colors = [
        '#409EFF', // 蓝色
        '#67C23A', // 绿色
        '#E6A23C', // 黄色
        '#F56C6C', // 红色
        '#909399', // 灰色
        '#9B59B6', // 紫色
        '#3498DB', // 浅蓝
        '#1ABC9C'  // 青色
      ];
      
      if (!username) return colors[0];
      
      // 使用用户名生成索引
      const index = username.split('')
        .reduce((acc, char) => acc + char.charCodeAt(0), 0) % colors.length;
      
      return colors[index];
    }
  },
  watch: {
    $route(to) {
      const pathMap = {
        '/home': '1',
        '/property-fee': '2-1',
        '/repair': '2-2',
        '/complaint': '2-3',
        '/community-activity': '2-4',
        '/housekeeping': '2-7',
        '/notification': '3',
        '/profile': '4'
      }
      this.activeIndex = pathMap[to.path] || '1'
    },
    isLoggedIn: {
      immediate: true,
      handler(val) {
        if (val) {
          this.getUnreadCount()
          this.notificationTimer = setInterval(() => {
            this.getUnreadCount()
          }, 30000)
        } else {
          if (this.notificationTimer) {
            clearInterval(this.notificationTimer)
          }
        }
      }
    }
  },
  created() {
    const pathMap = {
      '/home': '1',
      '/property-fee': '2-1',
      '/repair': '2-2',
      '/complaint': '2-3',
        '/community-activity': '2-4',
        '/housekeeping': '2-7',
      '/notification': '3',
      '/profile': '4'
    }
    this.activeIndex = pathMap[this.$route.path] || '1'
  },
  beforeDestroy() {
    if (this.notificationTimer) {
      clearInterval(this.notificationTimer)
    }
  }
}
</script>

<style scoped>
.nav-header {
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.logo {
  text-decoration: none;
  display: flex;
  align-items: center;
  padding: 10px 0;
  margin-right: 40px;
  cursor: pointer;
  transition: all 0.3s;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #3f51b5, #7986cb);
  border-radius: 10px;
  margin-right: 12px;
}

.logo-icon i {
  font-size: 24px;
  color: #ffffff;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-title {
  font-size: 20px;
  font-weight: bold;
  color: #3f51b5;
  line-height: 1.2;
}

.logo-subtitle {
  font-size: 12px;
  color: #666666;
  letter-spacing: 1px;
}

/* 添加logo点击效果 */
.logo:hover .logo-icon {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(63, 81, 181, 0.2);
}

.logo:hover .logo-title {
  color: #7986cb;
}

.nav-menu-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.nav-menu {
  border: none;
  margin-right: 20px;
}

.notice-container {
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
}

.notice-container:hover {
  background-color: #f5f7fa;
}

.notice-container i {
  font-size: 20px;
  color: #606266;
}

/* 移除之前的通知相关样式 */
.notice-badge,
.notice-btn {
  display: none;
}

/* 调整badge样式 */
:deep(.el-badge__content) {
  height: 16px;
  line-height: 16px;
  padding: 0 4px;
  border: none;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .nav-menu-container {
    justify-content: space-between;
  }
  
  .nav-menu {
    margin-right: 0;
  }
  
  .notice-container {
    padding: 0 10px;
  }
}

.user-area {
  display: flex;
  align-items: center;
  margin-left: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 12px;
  height: 60px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  margin: 0 8px;
  font-size: 14px;
  color: #606266;
}

.el-dropdown-menu {
  padding: 5px 0;
}

.el-dropdown-menu i {
  margin-right: 8px;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .username {
    display: none;
  }
  
  .user-area {
    margin-left: 10px;
  }
  
  .user-info {
    padding: 0 8px;
  }
}

.user-avatar {
  color: #fff;
  font-weight: bold;
  background: linear-gradient(135deg, #3f51b5, #7986cb);
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(63, 81, 181, 0.2);
}

/* 更新菜单hover样式 */
:deep(.el-menu-item:hover),
:deep(.el-submenu__title:hover) {
  background-color: rgba(63, 81, 181, 0.1) !important;
}

:deep(.el-menu-item.is-active) {
  color: #3f51b5 !important;
  border-bottom-color: #3f51b5 !important;
}
</style> 