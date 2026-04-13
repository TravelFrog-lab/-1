<template>
  <div class="news-detail-page">
    <div class="content-wrapper">
      <div class="news-detail">
        <div class="news-header">
          <div class="header-content">
            <div class="title-section">
              <h2 class="news-title">{{ news.title }}</h2>
              <div class="news-meta">
                <span class="publisher" v-if="news.publisher">
                  <i class="el-icon-user"></i>
                  {{ news.publisher }}
                </span>
                <span class="news-time">
                  <i class="el-icon-time"></i>
                  {{ news.createdAt | formatDateTime }}
                </span>
              </div>
            </div>
            <div class="news-cover" v-if="news.img">
              <img :src="`/api${news.img}`" :alt="news.title">
            </div>
          </div>
        </div>

        <div class="news-content" v-if="news.content" v-html="news.content"></div>
        <div class="news-empty" v-else>
          <el-empty description="暂无内容"></el-empty>
        </div>
      </div>
    </div>

    <page-footer></page-footer>
  </div>
</template>

<script>
import PageFooter from '@/components/front/PageFooter.vue'
import Request from '@/utils/request'
import moment from 'moment'

export default {
  name: 'NewsDetail',
  components: {
    PageFooter
  },
  data() {
    return {
      news: {
        id: '',
        title: '',
        content: '',
        createdAt: '',
        img: ''
      }
    }
  },
  filters: {
    formatDateTime(date) {
      return moment(date).format('YYYY-MM-DD HH:mm:ss')
    }
  },
  created() {
    this.getNewsDetail()
  },
  methods: {
    getNewsDetail() {
      const id = this.$route.params.id
      Request.get(`/news/${id}`).then(response => {
        if (response.code === '0') {
          this.news = response.data
        } else {
          this.$message.error('获取新闻详情失败')
        }
      })
    }
  }
}
</script>

<style scoped>
.news-detail-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.content-wrapper {
  flex: 1;
  padding: 40px 20px;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
}

.news-detail {
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.news-header {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 24px;
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.title-section {
  flex: 1;
  min-width: 0; /* 防止标题溢出 */
}

.news-title {
  font-size: 32px;
  color: #303133;
  margin: 0 0 20px;
  font-weight: 600;
  line-height: 1.4;
}

.news-meta {
  color: #909399;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.news-meta i {
  margin-right: 6px;
}

.news-meta span {
  display: flex;
  align-items: center;
}

.news-cover {
  margin: 0;
  border-radius: 8px;
  overflow: hidden;
  width: 280px;
  flex-shrink: 0;
}

.news-cover img {
  width: 100%;
  height: 180px;
  display: block;
  object-fit: cover;
}

.news-content {
  line-height: 1.8;
  color: #303133;
  font-size: 16px;
}

.news-content :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 24px 0;
  border-radius: 8px;
}

.news-content :deep(p) {
  margin: 20px 0;
  color: #606266;
}

.news-content :deep(h1),
.news-content :deep(h2),
.news-content :deep(h3),
.news-content :deep(h4) {
  margin: 32px 0 16px;
  font-weight: 600;
  color: #303133;
}

.news-content :deep(a) {
  color: #409EFF;
  text-decoration: none;
  transition: color 0.3s;
}

.news-content :deep(a:hover) {
  color: #66b1ff;
}

.news-empty {
  padding: 60px 0;
  text-align: center;
  color: #909399;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 24px;
  }

  .news-cover {
    width: 100%;
    max-width: 400px;
    margin: 0 auto;
  }

  .news-cover img {
    height: 240px;
  }

  .news-title {
    font-size: 24px;
    margin-bottom: 16px;
  }

  .news-meta {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
}
</style> 