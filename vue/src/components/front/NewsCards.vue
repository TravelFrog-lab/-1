<template>
  <div class="news-cards">
    <el-row :gutter="20">
      <el-col :span="8" v-for="news in newsList" :key="news.id">
        <el-card class="news-card" :body-style="{ padding: '0px', height: '100%' }" shadow="never">
          <div class="card-content">
            <img :src="`/api${news.img}`" class="news-image">
            <div class="news-info">
              <div class="news-main">
                <h3>{{ news.title }}</h3>
                <p>{{ news.content | summary }}</p>
              </div>
              <div class="news-footer">
                <span>{{ news.createdAt | formatDate }}</span>
                <el-button type="text" @click="viewDetail(news.id)">查看详情</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import Request from '@/utils/request'
import moment from 'moment'

export default {
  name: 'NewsCards',
  data() {
    return {
      newsList: []
    }
  },
  filters: {
    summary(content) {
      const plainText = content.replace(/<[^>]+>/g, '')
      return plainText.length > 100 ? plainText.substring(0, 100) + '...' : plainText
    },
    formatDate(date) {
      return moment(date).format('YYYY-MM-DD')
    }
  },
  created() {
    this.getNewsList()
  },
  methods: {
    getNewsList() {
      Request.get('/news/page', {
        params: {
          pageNum: 1,
          pageSize: 3
        }
      }).then(response => {
        if (response.code === '0') {
          this.newsList = response.data.records
        } else {
          this.$message.error(response.msg)
        }
      })
    },
    viewDetail(id) {
      this.$router.push(`/news/${id}`)
    }
  }
}
</script>

<style scoped>
.news-cards {
  margin-top: 20px;
}

.news-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.news-card:hover {
  transform: translateY(-5px);
}

.card-content {
  height: 400px;
  display: flex;
  flex-direction: column;
}

.news-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.news-info {
  padding: 14px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.news-main {
  flex: 1;
}

.news-info h3 {
  margin: 0;
  font-size: 18px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-info p {
  font-size: 14px;
  color: #666;
  margin: 10px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #999;
  font-size: 13px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
</style> 