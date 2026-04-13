<template>
  <div class="banner-news-carousel">
    <el-carousel :height="carouselHeight" :interval="5000" arrow="hover">
      <el-carousel-item v-for="item in displayList" :key="item._key">
        <div
          class="carousel-content"
          :style="{ backgroundImage: item._bg ? `url(${item._bg})` : 'none' }"
          :class="{ 'is-clickable': item._kind === 'news' }"
          @click="onSlideClick(item)"
        >
          <div class="carousel-overlay"></div>
          <div class="carousel-tag">
            <el-tag type="info" effect="dark">{{ item._tagLabel }}</el-tag>
          </div>
          <div class="carousel-info">
            <div class="info-container">
              <h3 class="title">{{ item.title || '—' }}</h3>
              <p class="description" v-if="item._summary">{{ item._summary }}</p>
            </div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script>
import Request from '@/utils/request'

/** 首页顶部小区动态轮播：数据来自管理端「轮播图管理」carousel 表 */
export default {
  name: 'BannerNewsCarousel',
  data () {
    return {
      slides: [],
      carouselHeight: '33.75vw'
    }
  },
  computed: {
    displayList () {
      if (this.slides.length) return this.slides
      return [
        {
          _key: 'empty',
          _kind: 'empty',
          title: '暂无小区动态',
          _summary: '请管理员在后台「小区动态」中发布内容',
          _bg: '',
          _tagLabel: '提示'
        }
      ]
    }
  },
  created () {
    this.loadNews()
  },
  methods: {
    toBgUrl (path) {
      if (!path) return ''
      if (String(path).startsWith('http')) return path
      const clean = String(path).replace(/^\/+/, '').replace(/^images\//, '')
      return `/api/${clean}`
    },
    async loadNews () {
      try {
        const res = await Request.get('/carousels/all')
        const list = []
        if (res.code === '0' && res.data && Array.isArray(res.data)) {
          res.data.forEach((row) => {
            const desc = row.description ? String(row.description) : ''
            list.push({
              _key: `carousel-${row.id}`,
              _kind: 'carousel',
              id: row.id,
              title: row.title || '—',
              _summary: desc.length > 160 ? desc.substring(0, 160) + '…' : desc,
              _bg: row.imageUrl ? this.toBgUrl(row.imageUrl) : '',
              _tagLabel: (row.tag && String(row.tag).trim()) || '小区动态',
              raw: row
            })
          })
        }
        this.slides = list
      } catch (e) {
        console.error(e)
        this.slides = []
      }
    },
    onSlideClick (item) {
      // 轮播图由后台配置，无独立资讯详情页；如需跳转可后续在 carousel 表增加 link 字段
      if (item._kind === 'news' && item.id) {
        this.$router.push(`/news/${item.id}`)
      }
    }
  }
}
</script>

<style scoped>
.banner-news-carousel {
  margin-top: 0;
  margin-bottom: 32px;
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
}

.carousel-content.is-clickable {
  cursor: pointer;
}

.carousel-content.is-clickable:hover {
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

::v-deep .el-carousel__indicators {
  bottom: 24px;
}

::v-deep .el-carousel__indicator {
  padding: 12px 4px;
}

::v-deep .el-carousel__button {
  width: 24px;
  height: 3px;
  border-radius: 2px;
  background-color: rgba(255, 255, 255, 0.7);
  transition: all 0.3s ease;
}

::v-deep .el-carousel__indicator.is-active .el-carousel__button {
  background-color: #fff;
  width: 32px;
}

::v-deep .el-carousel__arrow {
  background-color: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  width: 44px;
  height: 44px;
  transition: all 0.3s ease;
}

::v-deep .el-carousel__arrow:hover {
  background-color: rgba(0, 0, 0, 0.6);
}
</style>
