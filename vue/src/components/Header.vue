<template>
  <div class="header-bar">
    <div class="header-left">
      <BreadCrumbs />
    </div>
    <div class="header-right">
      <div class="header-actions">
        <el-popover
          placement="bottom"
          width="300"
          trigger="click"
          v-model="showNotifications"
        >
          <div class="notification-popover">
            <div class="notification-header">
              <span>通知</span>
              <el-button type="text" @click="markAllRead" size="small">全部已读</el-button>
            </div>
            <el-divider style="margin: 8px 0" />
            <div class="notification-list" v-loading="loading">
              <div v-if="notifications.length === 0" class="empty-text">
                暂无通知
              </div>
              <template v-else>
                <div v-for="notice in notifications" :key="notice.id" 
                  class="notification-item" :class="{ 'unread': !notice.isRead }"
                  @click="handleNotificationClick(notice)">
                  <div class="notice-title">{{ notice.title }}</div>
                  <div class="notice-content">{{ notice.content }}</div>
                  <div class="notice-time">{{ formatTime(notice.createdAt) }}</div>
                </div>
              </template>
            </div>
          </div>
          <el-badge 
            :value="unreadCount" 
            :hidden="unreadCount === 0"
            slot="reference" 
            class="notice-badge"
          >
            <i class="el-icon-bell"></i>
          </el-badge>
        </el-popover>
        <el-divider direction="vertical" />
        <div class="user-container">
          <UserAvatar />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import BreadCrumbs from '../components/BreadCrumbs/index.vue'
import UserAvatar from '../components/UserAvatar/index.vue'
import Request from '@/utils/request.js'

export default {
  name: 'HeaderBar',
  components: {
    BreadCrumbs,
    UserAvatar
  },
  computed: {
    ...mapGetters(['currentUser']),
    userId() {
      return this.currentUser?.id
    }
  },
  data() {
    return {
      showNotifications: false,
      notifications: [],
      unreadCount: 0,
      loading: false
    }
  },
  created() {
    this.fetchNotifications()
  },
  methods: {
    async fetchNotifications() {
      if (!this.userId) return
      
      this.loading = true
      try {
        const res = await Request.get(`/notification/user/${this.userId}`)
        if (res.code === '0' && res.data) {
          this.notifications = res.data.slice(0, 5) // 只显示最新5条
          this.unreadCount = res.data.filter(n => !n.isRead).length
        } else {
          this.notifications = []
          this.unreadCount = 0
        }
      } catch (error) {
        console.error('获取通知失败:', error)
        this.$message.error('获取通知失败')
        this.notifications = []
        this.unreadCount = 0
      }
      this.loading = false
    },
    
    async handleNotificationClick(notice) {
      if (!notice.isRead) {
        try {
          await Request.put('/notification', {
            id: notice.id,
            isRead: 1
          })
          notice.isRead = 1
          this.unreadCount--
        } catch (error) {
          console.error('标记已读失败:', error)
        }
      }
      // 可以添加查看通知详情的逻辑
    },
    
    async markAllRead() {
      if (this.unreadCount === 0) return
      
      try {
        const unreadNotices = this.notifications.filter(n => !n.isRead)
        await Promise.all(unreadNotices.map(notice => 
          Request.put('/notification', {
            id: notice.id,
            isRead: 1
          })
        ))
        
        this.notifications.forEach(n => n.isRead = 1)
        this.unreadCount = 0
        this.$message.success('已全部标记为已读')
      } catch (error) {
        console.error('标记全部已读失败:', error)
        this.$message.error('操作失败')
      }
    },
    
    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return date.toLocaleDateString()
    }
  }
}
</script>

<style lang="less" scoped>
.header-bar {
  width: 100%;
  height: 60px;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 1000;

  .header-left {
    display: flex;
    align-items: center;
  }

  .header-right {
    display: flex;
    align-items: center;
    height: 100%;

    .header-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      height: 100%;

      .user-container {
        height: 100%;
        display: flex;
        align-items: center;
      }

      .notice-badge {
        height: 40px;
        display: flex;
        align-items: center;
        cursor: pointer;
        padding: 0 8px;
        border-radius: 6px;
        transition: all 0.3s ease;

        &:hover {
          background-color: rgba(0, 0, 0, 0.04);
        }

        .el-icon-bell {
          font-size: 20px;
          color: #666;
        }
      }

      .el-divider--vertical {
        height: 20px;
        margin: 0 8px;
      }
    }
  }
}

.notification-popover {
  .notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 8px;
    
    span {
      font-weight: 500;
      font-size: 16px;
    }
  }
  
  .notification-list {
    max-height: 400px;
    overflow-y: auto;
    
    .empty-text {
      text-align: center;
      color: #999;
      padding: 20px 0;
    }
    
    .notification-item {
      padding: 12px;
      cursor: pointer;
      border-radius: 4px;
      transition: all 0.3s ease;
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      &.unread {
        background-color: #e6f1fc;
        
        &:hover {
          background-color: #d9e9f9;
        }
        
        .notice-title {
          font-weight: 500;
        }
      }
      
      .notice-title {
        font-size: 14px;
        color: #333;
        margin-bottom: 4px;
      }
      
      .notice-content {
        font-size: 12px;
        color: #666;
        margin-bottom: 4px;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .notice-time {
        font-size: 12px;
        color: #999;
      }
    }
  }
}

:deep(.el-popover) {
  z-index: 2002 !important;
}
</style>