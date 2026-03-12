<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <nav-header></nav-header>
    
    <!-- 主要内容区 -->
    <div class="main-content">
      <!-- 欢迎横幅 -->
      <div class="welcome-banner" v-if="isLoggedIn">
        <el-card shadow="never" class="banner-card">
          <div class="banner-content">
            <div class="user-info">
              <el-avatar :size="60" :src="userAvatar" class="user-avatar">
                {{ userInitial }}
              </el-avatar>
              <div class="greeting">
                <span class="greeting-text">欢迎回来，{{ userName }}</span>
                <span class="greeting-meta">{{ dateTime }} · {{ weatherText }}{{ weatherTemp !== '' ? ' ' + weatherTemp + '°C' : '' }}</span>
                <p class="greeting-subtitle">今日小区服务状态一切正常，祝您生活愉快！</p>
              </div>
            </div>
            <div class="stats-overview">
              <div class="stat-item">
                <div class="stat-value">{{ pendingTasks }}</div>
                <div class="stat-label">待办事项</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ completedServices }}</div>
                <div class="stat-label">已完成服务</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ communityActivities }}</div>
                <div class="stat-label">小区活动</div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 新闻资讯：轮播样式 -->
      <news-carousel></news-carousel>

      <!-- 小区公告：滚动播放，点击可查看详情 -->
      <scroll-notice></scroll-notice>

      <!-- 数据统计卡片 -->
      <div class="stats-section">
        <h2 class="section-title">小区数据概览</h2>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6" v-for="stat in statistics" :key="stat.id">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <div class="stat-icon" :style="{ color: stat.color }">
                  <i :class="stat.icon"></i>
                </div>
                <div class="stat-info">
                  <div class="stat-number">{{ stat.value }}</div>
                  <div class="stat-text">{{ stat.label }}</div>
                </div>
                <div class="stat-trend" :class="stat.trendClass">
                  <i :class="stat.trendIcon"></i>
                  <span>{{ stat.trend }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 我的待办 -->
      <div class="todo-section" v-if="isLoggedIn">
        <h2 class="section-title">我的待办</h2>
        <el-card shadow="hover" class="todo-card">
          <el-table :data="todoList" style="width: 100%">
            <el-table-column prop="title" label="事项" width="300">
              <template slot-scope="scope">
                <div class="todo-title">
                  <i :class="scope.row.icon" class="todo-icon"></i>
                  {{ scope.row.title }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明"></el-table-column>
            <el-table-column prop="deadline" label="截止时间" width="150"></el-table-column>
            <el-table-column label="操作" width="120">
              <template slot-scope="scope">
                <el-button type="primary" size="small" @click="handleTodoAction(scope.row)">
                  处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <!-- 快捷入口 -->
      <div class="quick-entry-section">
        <h2 class="section-title">快捷入口</h2>
        <quick-entry-buttons></quick-entry-buttons>
      </div>
    </div>

    <!-- 页脚 -->
    <page-footer></page-footer>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Request from '@/utils/request'
import NavHeader from '../../components/front/NavHeader.vue'
import NewsCarousel from '../../components/front/NewsCarousel.vue'
import ScrollNotice from '../../components/front/ScrollNotice.vue'
import TodoTicker from '../../components/front/TodoTicker.vue'
import QuickEntryButtons from '../../components/front/QuickEntryButtons.vue'
import PageFooter from '../../components/front/PageFooter.vue'

export default {
  name: 'Home',
  components: {
    NavHeader,
    NewsCarousel,
    ScrollNotice,
    TodoTicker,
    QuickEntryButtons,
    PageFooter
  },
  data() {
    return {
      dateTime: '',
      weatherText: '--',
      weatherTemp: '',
      timeTimer: null,
      // 待办事项示例数据
      todoList: [
        {
          id: 1,
          title: '物业费缴纳',
          description: '2025年第一季度物业费待缴纳',
          deadline: '2025-03-31',
          icon: 'el-icon-money',
          action: '/property-fee'
        },
        {
          id: 2,
          title: '报修进度',
          description: '水管漏水维修中，请关注进度',
          deadline: '2025-03-20',
          icon: 'el-icon-service',
          action: '/repair'
        },
        {
          id: 3,
          title: '停车费续费',
          description: '车位A-001本月停车费待缴纳',
          deadline: '2025-03-15',
          icon: 'el-icon-location-information',
          action: '/parking'
        }
      ],
      // 统计数据（由接口 getCommunityStats 填充，先占位）
      statistics: [
        { id: 1, value: '--', label: '服务满意度', icon: 'el-icon-star-on', color: '#FFC107', trend: '-', trendIcon: 'el-icon-minus', trendClass: '' },
        { id: 2, value: '--', label: '本月报修单', icon: 'el-icon-service', color: '#2196F3', trend: '-', trendIcon: 'el-icon-minus', trendClass: '' },
        { id: 3, value: '--', label: '社区总人数', icon: 'el-icon-user', color: '#4CAF50', trend: '-', trendIcon: 'el-icon-minus', trendClass: '' },
        { id: 4, value: '--', label: '进行中活动', icon: 'el-icon-date', color: '#9C27B0', trend: '-', trendIcon: 'el-icon-minus', trendClass: '' }
      ],
      // 个人统计数据（欢迎横幅内）
      pendingTasks: 3,
      completedServices: 24,
      communityActivities: 7
    }
  },
  created() {
    this.fetchCommunityStats()
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'currentUser']),
    userName() {
      return this.currentUser?.username || '用户'
    },
    userAvatar() {
      const avatar = this.currentUser?.avatar
      if (!avatar) return ''
      if (avatar.startsWith('http')) return avatar
      return '/api' + (avatar.startsWith('/') ? avatar : '/' + avatar)
    },
    userInitial() {
      return this.userName.charAt(0).toUpperCase()
    }
  },
  mounted() {
    this.updateDateTime()
    this.timeTimer = setInterval(this.updateDateTime, 1000)
    this.fetchWeather()
  },
  beforeDestroy() {
    if (this.timeTimer) clearInterval(this.timeTimer)
  },
  methods: {
    fetchCommunityStats() {
      Request.get('/dashboard/community-stats').then(response => {
        if (response.code === '0' && response.data) {
          const d = response.data
          const trendUp = (s) => (s && String(s).startsWith('-')) ? 'trend-down' : 'trend-up'
          const trendIcon = (s) => (s && String(s).startsWith('-')) ? 'el-icon-bottom' : 'el-icon-top'
          const fmtNum = (n) => Number(n).toLocaleString()
          this.statistics = [
            {
              id: 1,
              value: (d.serviceSatisfaction != null ? d.serviceSatisfaction : 0) + '%',
              label: '服务满意度',
              icon: 'el-icon-star-on',
              color: '#FFC107',
              trend: d.serviceSatisfactionTrend || '+0%',
              trendIcon: trendIcon(d.serviceSatisfactionTrend),
              trendClass: trendUp(d.serviceSatisfactionTrend)
            },
            {
              id: 2,
              value: fmtNum(d.monthlyRepairCount != null ? d.monthlyRepairCount : 0),
              label: '本月报修单',
              icon: 'el-icon-service',
              color: '#2196F3',
              trend: d.monthlyRepairTrend || '+0%',
              trendIcon: trendIcon(d.monthlyRepairTrend),
              trendClass: trendUp(d.monthlyRepairTrend)
            },
            {
              id: 3,
              value: fmtNum(d.totalCommunityPopulation != null ? d.totalCommunityPopulation : 0),
              label: '社区总人数',
              icon: 'el-icon-user',
              color: '#4CAF50',
              trend: d.totalCommunityPopulationTrend || '+0%',
              trendIcon: trendIcon(d.totalCommunityPopulationTrend),
              trendClass: trendUp(d.totalCommunityPopulationTrend)
            },
            {
              id: 4,
              value: String(d.ongoingActivityCount != null ? d.ongoingActivityCount : 0),
              label: '进行中活动',
              icon: 'el-icon-date',
              color: '#9C27B0',
              trend: d.ongoingActivityTrend || '+0',
              trendIcon: trendIcon(d.ongoingActivityTrend),
              trendClass: trendUp(d.ongoingActivityTrend)
            }
          ]
        }
      }).catch(() => {
        // 接口失败时使用默认占位，避免空白
        this.statistics = [
          { id: 1, value: '--', label: '服务满意度', icon: 'el-icon-star-on', color: '#FFC107', trend: '-', trendIcon: 'el-icon-minus', trendClass: '' },
          { id: 2, value: '--', label: '本月报修单', icon: 'el-icon-service', color: '#2196F3', trend: '-', trendIcon: 'el-icon-minus', trendClass: '' },
          { id: 3, value: '--', label: '社区总人数', icon: 'el-icon-user', color: '#4CAF50', trend: '-', trendIcon: 'el-icon-minus', trendClass: '' },
          { id: 4, value: '--', label: '进行中活动', icon: 'el-icon-date', color: '#9C27B0', trend: '-', trendIcon: 'el-icon-minus', trendClass: '' }
        ]
      })
    },
    updateDateTime() {
      const now = new Date()
      const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      const week = weekDays[now.getDay()]
      const h = String(now.getHours()).padStart(2, '0')
      const min = String(now.getMinutes()).padStart(2, '0')
      const sec = String(now.getSeconds()).padStart(2, '0')
      this.dateTime = `${y}年${m}月${d}日 ${week} ${h}:${min}:${sec}`
    },
    fetchWeather() {
      // 默认北京坐标，可后续改为定位或用户设置
      const lat = 39.9042
      const lon = 116.4074
      const url = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current_weather=true`
      fetch(url)
        .then(res => res.json())
        .then(data => {
          if (data.current_weather) {
            const c = data.current_weather
            this.weatherTemp = Math.round(c.temperature)
            this.weatherText = this.weatherCodeToText(c.weathercode)
          }
        })
        .catch(() => {
          this.weatherText = '--'
          this.weatherTemp = ''
        })
    },
    weatherCodeToText(code) {
      const map = {
        0: '晴', 1: '少云', 2: '多云', 3: '阴',
        45: '雾', 48: '冻雾',
        51: '毛毛雨', 53: '毛毛雨', 55: '毛毛雨',
        61: '小雨', 63: '中雨', 65: '大雨',
        71: '小雪', 73: '中雪', 75: '大雪',
        80: '小阵雨', 81: '阵雨', 82: '强阵雨',
        85: '小阵雪', 86: '阵雪',
        95: '雷阵雨', 96: '雷阵雨', 99: '雷阵雨'
      }
      return map[code] || '--'
    },
    handleTodoAction(item) {
      if (item.action) {
        this.$router.push(item.action)
      }
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  width: 100%;
}

/* 欢迎横幅样式 */
.welcome-banner {
  margin-bottom: 30px;
}

.banner-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-avatar {
  background: rgba(255, 255, 255, 0.2);
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.greeting {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 6px 14px;
  align-items: baseline;
}

.greeting-text {
  font-size: 24px;
  font-weight: 600;
  white-space: nowrap;
}

.greeting-meta {
  font-size: 16px;
  font-weight: 500;
  opacity: 0.95;
  white-space: nowrap;
}

.greeting-subtitle {
  grid-column: 2;
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
  line-height: 1.5;
}

.stats-overview {
  display: flex;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

/* 通用区域样式 */
.section-title {
  font-size: 24px;
  color: #333;
  margin: 30px 0 20px;
  padding-left: 10px;
  border-left: 4px solid #409EFF;
  display: flex;
  align-items: center;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 30px 0 20px;
}

/* 数据统计卡片样式 */
.stats-section {
  margin: 30px 0;
}

.stat-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-icon {
  font-size: 32px;
}

.stat-info {
  flex: 1;
  margin-left: 16px;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-text {
  font-size: 14px;
  color: #666;
}

.stat-trend {
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.trend-up {
  color: #52c41a;
}

.trend-down {
  color: #f56c6c;
}

/* 待办事项样式 */
.todo-section {
  margin: 30px 0;
}

.todo-card {
  transition: all 0.3s;
}

.todo-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.todo-icon {
  font-size: 18px;
  color: #409EFF;
}

.quick-entry-section {
  margin: 30px 0 50px;
}

/* 响应式设计 */
@media screen and (max-width: 992px) {
  .banner-content {
    flex-direction: column;
    gap: 24px;
    text-align: center;
  }
  
  .user-info {
    flex-direction: column;
    gap: 16px;
  }
  
  .stats-overview {
    width: 100%;
    justify-content: space-around;
  }
  
  .stat-value {
    font-size: 28px;
  }
}

@media screen and (max-width: 768px) {
  .main-content {
    padding: 15px;
  }
  
  .greeting-text {
    font-size: 20px;
  }
  
  .greeting-meta {
    font-size: 14px;
  }
  
  .greeting-subtitle {
    font-size: 14px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .stat-label {
    font-size: 12px;
  }
  
  .section-title {
    font-size: 20px;
    margin: 20px 0 15px;
  }
}

@media screen and (max-width: 576px) {
  .stats-overview {
    flex-direction: column;
    gap: 20px;
  }
  
  .stat-content {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .stat-info {
    margin-left: 0;
  }
}
</style>