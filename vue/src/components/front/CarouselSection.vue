<template>
  <div class="carousel-section">
    <el-carousel :height="carouselHeight" :interval="5000" arrow="hover">
      <el-carousel-item v-for="item in carouselItems" :key="item.id">
        <div class="carousel-content" :style="{ backgroundImage: `url(/api${item.imageUrl})` }">
          <div class="carousel-overlay"></div>
          <div class="carousel-tag" v-if="item.tag">
            <el-tag type="warning" effect="dark">{{ item.tag }}</el-tag>
          </div>
          <div class="carousel-info">
            <div class="info-container">
              <h3 class="title">{{ item.title }}</h3>
              <!-- <p class="description">{{ item.description }}</p> -->
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
  name: 'CarouselSection',
  data() {
    return {
      carouselItems: [],
      carouselHeight: '33.75vw' // 16:9 比例（100/16*9）
    }
  },
  created() {
    this.getCarouselData()
  },
  methods: {
    getCarouselData() {
      Request.get('/carousels/all').then(response => {
        if (response.code === '0') {
          this.carouselItems = response.data
        } else {
          this.$message.error(response.msg)
        }
      })
    }
  }
}
</script>

<style scoped>
.carousel-section {
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
  position: relative;
  width: 100%;
  aspect-ratio: 16/9;
  transition: transform 0.3s ease;
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
}

/* 轮播指示器样式 */
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

/* 轮播箭头样式 */
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