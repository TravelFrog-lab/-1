<template>
  <div class="announcement-list">
    <el-timeline>
      <el-timeline-item
        v-for="item in announcements"
        :key="item.id"
        :timestamp="item.createdAt | formatDate"
        placement="top">
        <el-card>
          <h4>{{ item.title }}</h4>
          <p>{{ item.content | summary }}</p>
        </el-card>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script>
import Request from '@/utils/request'
import moment from 'moment'

export default {
  name: 'AnnouncementList',
  data() {
    return {
      announcements: []
    }
  },
  filters: {
    summary(content) {
      return content.length > 100 ? content.substring(0, 100) + '...' : content
    },
    formatDate(date) {
      return moment(date).format('YYYY-MM-DD HH:mm')
    }
  },
  created() {
    this.getAnnouncements()
  },
  methods: {
    getAnnouncements() {
      Request.get('/announcements/page', {
        params: {
          pageNum: 1,
          pageSize: 5
        }
      }).then(response => {
        if (response.code === '0') {
          this.announcements = response.data.records
        } else {
          this.$message.error(response.msg)
        }
      })
    }
  }
}
</script>

<style scoped>
.announcement-list {
  margin-top: 20px;
}

.el-card {
  margin-bottom: 10px;
}

h4 {
  margin: 0;
  color: #303133;
}

p {
  margin: 10px 0 0;
  color: #606266;
  font-size: 14px;
}
</style> 