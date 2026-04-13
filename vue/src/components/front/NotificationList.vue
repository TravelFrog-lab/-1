<template>
  <div class="notification-list" :class="{ 'notification-list--page': fullPage }">
    <!-- 操作栏 -->
    <div class="operation-bar">
      <div class="left">
        <el-radio-group v-model="readStatus" size="small" @change="handleFilterChange">
          <el-radio-button label="0">未读</el-radio-button>
          <el-radio-button label="1">已读</el-radio-button>
        </el-radio-group>
      </div>
      <div class="right">
        <el-button type="text" @click="handleReadAll" :disabled="!hasUnread">
          全部已读
        </el-button>
        <el-button type="text" @click="handleClearAll" :disabled="!notifications.length">
          清空消息
        </el-button>
      </div>
    </div>

    <!-- 通知列表 -->
    <el-card v-loading="loading" class="notification-card">
      <div v-if="notifications.length" class="notification-items">
        <div v-for="item in notifications" :key="item.id" 
          class="notification-item" 
          :class="{'unread': !item.isRead}"
          @click="handleRead(item)">
          <div class="item-header">
            <span class="title">{{ item.title }}</span>
            <span class="time">{{ formatTime(item.createdAt) }}</span>
          </div>
          <div class="content">{{ item.content }}</div>
        </div>
      </div>
      <div v-else class="empty-text">
        {{ readStatus === '0' ? '暂无未读通知' : '暂无已读通知' }}
      </div>
    </el-card>
  </div>
</template>

<script>
import Request from '@/utils/request'
import moment from 'moment'
import { mapGetters } from 'vuex'
export default {
  name: 'NotificationList',
  props: {
    /** 独立页面模式：占满内容区高度，进入时自动拉取列表 */
    fullPage: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      loading: false,
      notifications: [],
      readStatus: '0' // 默认显示未读通知
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'currentUser']),
    hasUnread() {
      return this.notifications.some(item => !item.isRead)
    }
  },
  methods: {
    // 获取通知列表
    getNotifications() {
      this.loading = true
      Request.get(`/notification/user/${this.currentUser.id}`).then(response => {
        if (response.code === '0' && response.data) {
          console.log("response.data",response.data)
          // 根据已读状态过滤
          this.notifications = response.data.filter(item => 
            item.isRead === parseInt(this.readStatus)
          )
        }
      }).finally(() => {
        this.loading = false
      })
    },
    
    // 标记已读
    handleRead(item) {
      if (!item.isRead) {
        const updatedNotification = {
          ...item,
          isRead: 1
        }
        Request.put('/notification', updatedNotification).then(response => {
          if (response.code === '0') {
            item.isRead = 1
            // 发射事件通知父组件更新未读数量
            const newUnreadCount = this.notifications.filter(n => !n.isRead).length
            this.$emit('update-unread-count', newUnreadCount)
            
            // 如果当前是未读列表，则刷新列表
            if (this.readStatus === '0') {
              this.getNotifications()
            }
          }
        })
      }
    },
    
    // 全部已读
    handleReadAll() {
      const unreadNotifications = this.notifications.filter(item => !item.isRead)
      if (unreadNotifications.length) {
        const updatePromises = unreadNotifications.map(item => {
          return Request.put('/notification', {
            ...item,
            isRead: 1
          })
        })
        
        Promise.all(updatePromises).then(() => {
          this.$message.success('已全部标记为已读')
          // 发射事件通知父组件更新未读数量
          this.$emit('update-unread-count', 0)
          
          // 如果当前是未读列表，则刷新列表
          if (this.readStatus === '0') {
            this.getNotifications()
          }
        })
      }
    },
    
    // 清空通知
    handleClearAll() {
      this.$confirm('确定要清空所有通知吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const ids = this.notifications.map(item => item.id)
        Request.delete('/notification/batch', {
          data: ids
        }).then(response => {
          if (response.code === '0') {
            this.$message.success('清空成功')
            this.notifications = []
            // 发射事件通知父组件更新未读数量
            this.$emit('update-unread-count', 0)
          }
        })
      })
    },
    
    // 筛选变化
    handleFilterChange() {
      this.getNotifications()
    },
    
    // 格式化时间
    formatTime(time) {
      return moment(time).format('YYYY-MM-DD HH:mm:ss')
    }
  },
  mounted() {
    if (this.fullPage && this.currentUser && this.currentUser.id) {
      this.getNotifications()
    }
  }
}
</script>

<style scoped>
.notification-list {
  padding: 0 10px;
  max-height: 400px;
  overflow-y: auto;
}

.notification-list--page {
  max-height: none;
  min-height: 280px;
  padding: 0;
}

.operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.notification-card {
  box-shadow: none !important;
  border: none;
}

.notification-items {
  padding: 0;
}

.notification-item {
  padding: 12px 15px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.3s;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #ecf5ff;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.title {
  font-weight: bold;
  color: #303133;
}

.time {
  font-size: 12px;
  color: #909399;
}

.content {
  color: #606266;
  margin-bottom: 8px;
  line-height: 1.5;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 30px 0;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .operation-bar {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .right {
    margin-top: 10px;
  }
  
  .notification-list {
    max-height: 400px;
  }
}
</style> 