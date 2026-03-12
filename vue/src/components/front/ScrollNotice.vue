<template>
  <div class="scroll-notice">
    <div class="notice-icon">
      <i class="el-icon-bell"></i>
      <span class="notice-label">社区公告：</span>
    </div>
    <div class="notice-content" ref="noticeContent" @mouseenter="stopScroll" @mouseleave="startScroll">
      <transition-group name="scroll-notice" tag="div">
        <p 
          v-for="item in currentNotices" 
          :key="item.id"
          class="notice-item"
          @click="showNoticeDialog(item)">
          <span class="notice-title">
            <el-tag size="small" :type="getTagType(item.type)">{{ item.type }}</el-tag>
            {{ item.title }}
          </span>
          <span class="notice-time">{{ item.createdAt | formatDate }}</span>
        </p>
      </transition-group>
      <p v-if="notices.length === 0" class="notice-item notice-empty">暂无社区公告</p>
    </div>

    <!-- 通知详情对话框 -->
    <el-dialog
      :title="currentNotice.title"
      :visible.sync="dialogVisible"
      width="50%"
      :before-close="handleClose">
      <div class="notice-dialog-content">
        <div class="notice-meta">
          <el-tag :type="getTagType(currentNotice.type)">{{ currentNotice.type }}</el-tag>
          <span class="notice-date">{{ currentNotice.createdAt | formatDateTime }}</span>
        </div>
        <div class="notice-body" v-html="currentNotice.content"></div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import Request from '@/utils/request'
import moment from 'moment'

export default {
  name: 'ScrollNotice',
  data() {
    return {
      notices: [],
      currentIndex: 0,
      timer: null,
      dialogVisible: false,
      currentNotice: {
        title: '',
        type: '',
        content: '',
        createdAt: ''
      }
    }
  },
  computed: {
    currentNotices() {
      return [this.notices[this.currentIndex]].filter(Boolean)
    }
  },
  filters: {
    formatDate(date) {
      return moment(date).format('MM-DD')
    },
    formatDateTime(date) {
      return moment(date).format('YYYY-MM-DD HH:mm:ss')
    }
  },
  created() {
    this.getNotices()
  },
  mounted() {
    this.startScroll()
  },
  beforeDestroy() {
    this.stopScroll()
  },
  methods: {
    getNotices() {
      Request.get('/announcements/page', {
        params: {
          pageNum: 1,
          pageSize: 5
        }
      }).then(response => {
        if (response.code === '0') {
          this.notices = (response.data.records || []).map(item => ({
            ...item,
            type: this.getNoticeType(item)
          }))
        }
      })
    },
    getNoticeType(item) {
      if (item.type !== undefined && item.type !== null) {
        const map = { REPAIR: '维修通知', PAY: '缴费通知', ACTIVITY: '活动通知' }
        return map[item.type] || '社区公告'
      }
      if (item.title && item.title.includes('维修')) return '维修通知'
      if (item.title && item.title.includes('缴费')) return '缴费通知'
      if (item.title && item.title.includes('活动')) return '活动通知'
      return '社区公告'
    },
    getTagType(type) {
      const typeMap = {
        '维修通知': 'warning',
        '缴费通知': 'danger',
        '活动通知': 'success',
        '社区公告': 'info'
      }
      return typeMap[type] || 'info'
    },
    startScroll() {
      if (!this.timer && this.notices.length > 1) {
        this.timer = setInterval(() => {
          this.currentIndex = (this.currentIndex + 1) % this.notices.length
        }, 3000)
      }
    },
    stopScroll() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    },
    showNoticeDialog(notice) {
      this.currentNotice = { ...notice }
      this.dialogVisible = true
      this.stopScroll()
    },
    handleClose(done) {
      this.startScroll()
      done()
    }
  }
}
</script>

<style scoped>
.scroll-notice {
  background: #fff;
  padding: 12px 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  margin: 20px 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 1px solid #ebeef5;
}

.notice-icon {
  display: flex;
  align-items: center;
  margin-right: 20px;
  color: #409EFF;
}

.notice-icon i {
  font-size: 20px;
  margin-right: 8px;
}

.notice-label {
  font-weight: bold;
  color: #303133;
}

.notice-content {
  flex: 1;
  overflow: hidden;
  height: 24px;
  position: relative;
}

.notice-item {
  margin: 0;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: absolute;
  width: 100%;
}

.notice-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: flex;
  align-items: center;
}

.notice-title .el-tag {
  margin-right: 8px;
}

.notice-time {
  color: #909399;
  margin-left: 20px;
  font-size: 14px;
}

.notice-empty {
  color: #909399;
  cursor: default;
}

/* 滚动动画 */
.scroll-notice-enter-active,
.scroll-notice-leave-active {
  transition: all 0.5s;
}

.scroll-notice-enter {
  transform: translateY(100%);
  opacity: 0;
}

.scroll-notice-leave-to {
  transform: translateY(-100%);
  opacity: 0;
}

/* 对话框样式 */
.notice-dialog-content {
  padding: 20px;
}

.notice-meta {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.notice-date {
  color: #909399;
  font-size: 14px;
}

.notice-body {
  line-height: 1.8;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 0;
}

.notice-content:hover {
  color: #409EFF;
}

/* 调整tag在对话框中的样式 */
.notice-meta .el-tag {
  font-size: 14px;
  padding: 5px 10px;
}
</style> 