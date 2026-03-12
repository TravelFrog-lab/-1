<template>
  <div class="news-carousel-section">
    <el-carousel :height="carouselHeight" :interval="5000" arrow="hover">
      <el-carousel-item v-for="item in displayList" :key="item.id">
        <div
          class="carousel-content"
          :style="{ backgroundImage: item.img ? `url(/api${item.img})` : 'none' }"
          @click="item.id && viewDetail(item.id)"
        >
          <div class="carousel-overlay"></div>
          <div class="carousel-tag">
            <el-tag type="info" effect="dark">新闻资讯</el-tag>
          </div>
          <div class="carousel-info">
            <div class="info-container">
              <h3 class="title">{{ item.title }}</h3>
              <p class="description" v-if="item.content">{{ summaryText(item.content) }}</p>
            </div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script>
import Request from '@/utils/request'

export default {
  name: 'NewsCarousel',
  data() {
    return {
      newsList: [],
      carouselHeight: '33.75vw'
    }
  },
  computed: {
    displayList() {
      if (this.newsList && this.newsList.length > 0) return this.newsList
      return [{ id: 0, title: '暂无新闻资讯', content: '', img: '' }]
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
          pageSize: 10
        }
      }).then(response => {
        if (response.code === '0' && response.data && response.data.records) {
          this.newsList = response.data.records
        } else {
          this.$message.error(response.msg || '获取新闻资讯失败')
        }
      })
    },
    summaryText(content) {
      if (!content) return ''
      const plain = content.replace(/<[^>]+>/g, '')
      return plain.length > 80 ? plain.substring(0, 80) + '...' : plain
    },
    viewDetail(id) {
      this.$router.push(`/news/${id}`)
    }
  }
}
</script>

<style scoped>
.news-carousel-section {
  margin-top: 20px;
  border-radius: 12px;
  overflow: hidden;
  width: 100%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.carousel-content {
  height: 100%;
  background-size: cover;
  background-position: center;
  background-color: #f0f2f5;
  position: relative;
  width: 100%;
  aspect-ratio: 16/9;
  transition: transform 0.3s ease;
  cursor: pointer;
}

.carousel-content:hover {
  transform: scale(1.02);
}

.carousel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.1) 0%,
    rgba(0, 0, 0, 0.3) 50%,
    rgba(0, 0, 0, 0.7) 100%
  );
  z-index: 1;
}

.carousel-tag {
  position: absolute;
  top: 24px;
  left: 24px;
  z-index: 3;
}

.carousel-tag .el-tag {
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.carousel-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 32px;
  color: #fff;
  z-index: 3;
}

.info-container {
  max-width: 800px;
  margin: 0 auto;
}

.title {
  margin: 0;
  font-size: 32px;
  font-weight: 600;
  line-height: 1.4;
  margin-bottom: 12px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.description {
  margin: 0;
  font-size: 16px;
  line-height: 1.6;
  opacity: 0.9;
  max-width: 600px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

:deep(.el-carousel__indicators) {
  bottom: 24px;
}

:deep(.el-carousel__indicator) {
  padding: 12px 4px;
}

:deep(.el-carousel__button) {
  width: 24px;
  height: 3px;
  border-radius: 2px;
  background-color: rgba(255, 255, 255, 0.7);
  transition: all 0.3s ease;
}

:deep(.el-carousel__indicator.is-active .el-carousel__button) {
  background-color: #fff;
  width: 32px;
}

:deep(.el-carousel__arrow) {
  background-color: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  width: 44px;
  height: 44px;
  transition: all 0.3s ease;
}

:deep(.el-carousel__arrow:hover) {
  background-color: rgba(0, 0, 0, 0.6);
}
</style>
