<template>
  <div class="hk-dash-wrapper" v-loading="loading">
    <div class="welcome-section">
      <div class="time-display">
        {{ currentDate }}
        <div class="time">{{ currentTime }}</div>
      </div>
      <div class="greeting">
        <h1>欢迎回来，祝今天一切顺利</h1>
      </div>
    </div>

    <el-alert
      v-if="loaded && !hasProfile"
      title="未找到家政人员档案，请联系管理员维护账号与家政人员绑定后再查看数据。"
      type="warning"
      show-icon
      :closable="false"
      class="hk-dash-alert"
    />

    <div class="stat-overview">
      <el-row :gutter="20" type="flex" justify="center">
        <el-col :xs="24" :sm="12" :md="6" v-for="(item, index) in statCards" :key="index">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" :class="item.class">
                <i :class="item.icon"></i>
              </div>
              <div class="stat-info">
                <div class="stat-label">{{ item.label }}</div>
                <div class="stat-value">{{ formatStat(item) }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="hk-charts">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="chart-card">
            <div slot="header"><span>服务项目分布</span></div>
            <div class="chart-container">
              <div ref="servicePieRef" style="height: 300px"></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <div slot="header"><span>评价星级分布</span></div>
            <div class="chart-container">
              <div ref="ratingBarRef" style="height: 300px"></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 0">
        <el-col :span="12">
          <el-card class="list-card">
            <div slot="header"><span>订单完成趋势（近 6 个月）</span></div>
            <div class="chart-container">
              <div ref="trendLineRef" style="height: 300px"></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="list-card">
            <div slot="header"><span>订单状态分布</span></div>
            <div class="chart-container">
              <div ref="statusRingRef" style="height: 300px"></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { mapGetters } from 'vuex'
import request from '@/utils/request'

export default {
  name: 'HousekeeperDashboard',
  data() {
    return {
      loading: false,
      loaded: false,
      timer: null,
      currentTime: '',
      currentDate: '',
      hasProfile: true,
      stats: {
        todayNewOrders: 0,
        inProgressOrders: 0,
        monthCompleted: 0,
        hasAvgRating: false,
        avgRating: null
      },
      chartPayload: {
        servicePie: [],
        ratingBars: [],
        completionTrendMonths: [],
        completionTrendCounts: [],
        statusRing: []
      },
      servicePieChart: null,
      ratingBarChart: null,
      trendLineChart: null,
      statusRingChart: null
    }
  },
  computed: {
    ...mapGetters(['currentUser']),
    statCards() {
      return [
        {
          label: '今日新订单',
          value: this.stats.todayNewOrders,
          icon: 'el-icon-s-order',
          class: 'residents',
          type: 'number'
        },
        {
          label: '在办订单',
          value: this.stats.inProgressOrders,
          icon: 'el-icon-loading',
          class: 'repairs',
          type: 'number'
        },
        {
          label: '本月完成',
          value: this.stats.monthCompleted,
          icon: 'el-icon-circle-check',
          class: 'fees',
          type: 'number'
        },
        {
          label: '平均评分',
          value: this.stats.hasAvgRating ? this.stats.avgRating : null,
          icon: 'el-icon-star-on',
          class: 'complaints',
          type: 'rating'
        }
      ]
    }
  },
  created() {
    this.updateTime()
    this.timer = setInterval(this.updateTime, 1000)
    this.fetchData()
  },
  mounted() {
    this.$nextTick(() => {
      this.initCharts()
      this.updateCharts()
      window.addEventListener('resize', this.resizeCharts)
    })
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer)
    window.removeEventListener('resize', this.resizeCharts)
    this.disposeCharts()
  },
  methods: {
    updateTime() {
      const now = new Date()
      this.currentTime = now.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
      this.currentDate = now.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
      })
    },
    formatStat(item) {
      if (item.type === 'rating') {
        if (item.value === null || item.value === undefined) return '暂无评价'
        return Number(item.value).toFixed(1) + ' ★'
      }
      return item.value
    },
    fetchData() {
      if (!this.currentUser || this.currentUser.id == null) return
      this.loading = true
      request
        .get('/dashboard/housekeeper-overview', { params: { userId: this.currentUser.id } })
        .then((res) => {
          if (res.code === '0' && res.data) {
            const d = res.data
            this.loaded = true
            this.hasProfile = !!d.hasHousekeeperProfile
            this.stats = {
              todayNewOrders: d.todayNewOrders || 0,
              inProgressOrders: d.inProgressOrders || 0,
              monthCompleted: d.monthCompleted || 0,
              hasAvgRating: !!d.hasAvgRating,
              avgRating: d.avgRating != null ? d.avgRating : null
            }
            this.chartPayload = {
              servicePie: d.servicePie || [],
              ratingBars: d.ratingBars || [],
              completionTrendMonths: d.completionTrendMonths || [],
              completionTrendCounts: d.completionTrendCounts || [],
              statusRing: d.statusRing || []
            }
            this.$nextTick(() => this.updateCharts())
          }
        })
        .finally(() => {
          this.loading = false
        })
    },
    initCharts() {
      const pieRef = this.$refs.servicePieRef
      const barRef = this.$refs.ratingBarRef
      const lineRef = this.$refs.trendLineRef
      const ringRef = this.$refs.statusRingRef
      if (!pieRef || !barRef || !lineRef || !ringRef) return

      this.servicePieChart = echarts.init(pieRef)
      this.servicePieChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { orient: 'vertical', left: 10, data: [] },
        series: [{
          name: '服务',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
          label: { show: false, position: 'center' },
          emphasis: {
            label: { show: true, fontSize: '18', fontWeight: 'bold' }
          },
          data: []
        }]
      })

      this.ratingBarChart = echarts.init(barRef)
      this.ratingBarChart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
        xAxis: { type: 'category', data: ['1星', '2星', '3星', '4星', '5星'] },
        yAxis: { type: 'value', name: '条数', minInterval: 1 },
        series: [{ name: '评价数', type: 'bar', barMaxWidth: 40, data: [] }]
      })

      this.trendLineChart = echarts.init(lineRef)
      this.trendLineChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
        xAxis: { type: 'category', boundaryGap: false, data: [] },
        yAxis: { type: 'value', name: '完成数', minInterval: 1 },
        series: [{
          name: '已完成',
          type: 'line',
          smooth: true,
          areaStyle: { opacity: 0.12 },
          itemStyle: { color: '#6366f1' },
          data: []
        }]
      })

      this.statusRingChart = echarts.init(ringRef)
      this.statusRingChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { orient: 'vertical', left: 10, data: [] },
        series: [{
          name: '状态',
          type: 'pie',
          radius: ['42%', '68%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
          label: { show: true, formatter: '{b}\n{c}' },
          data: []
        }]
      })
    },
    updateCharts() {
      const d = this.chartPayload
      if (this.servicePieChart) {
        const pieData = d.servicePie || []
        this.servicePieChart.setOption({
          legend: { data: pieData.map((i) => i.name) },
          series: [{ data: pieData }]
        })
      }
      if (this.ratingBarChart) {
        const rows = d.ratingBars || []
        const barData = rows.map((r) => {
          const s = r.star
          let color = '#91cc75'
          if (s <= 2) color = '#ee6666'
          else if (s === 3) color = '#fac858'
          return { value: r.value, itemStyle: { color } }
        })
        this.ratingBarChart.setOption({ series: [{ data: barData }] })
      }
      if (this.trendLineChart) {
        this.trendLineChart.setOption({
          xAxis: { data: d.completionTrendMonths || [] },
          series: [{ data: d.completionTrendCounts || [] }]
        })
      }
      if (this.statusRingChart) {
        const ring = d.statusRing || []
        this.statusRingChart.setOption({
          legend: { data: ring.map((i) => i.name) },
          series: [{ data: ring }]
        })
      }
    },
    resizeCharts() {
      if (this.servicePieChart) this.servicePieChart.resize()
      if (this.ratingBarChart) this.ratingBarChart.resize()
      if (this.trendLineChart) this.trendLineChart.resize()
      if (this.statusRingChart) this.statusRingChart.resize()
    },
    disposeCharts() {
      if (this.servicePieChart) this.servicePieChart.dispose()
      if (this.ratingBarChart) this.ratingBarChart.dispose()
      if (this.trendLineChart) this.trendLineChart.dispose()
      if (this.statusRingChart) this.statusRingChart.dispose()
    }
  }
}
</script>

<style lang="less" scoped>
.hk-dash-wrapper {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100%;
}

.hk-dash-alert {
  margin-bottom: 16px;
}

.welcome-section {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 15px;
  padding: 30px;
  color: white;
  margin-bottom: 30px;
}

.time-display {
  font-size: 1.2rem;
  margin-bottom: 20px;
  .time {
    font-size: 2.5rem;
    font-weight: 700;
    margin-top: 5px;
  }
}

.greeting h1 {
  font-size: 2rem;
  margin: 0;
  font-weight: 600;
}

.stat-overview {
  margin-bottom: 10px;
  width: 100%;
}

.stat-card {
  height: auto;
  padding: 5px;
  transition: transform 0.3s;
  margin-bottom: 10px;
  &:hover {
    transform: translateY(-5px);
  }
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  i {
    font-size: 32px;
    color: white;
  }
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.residents {
  background: linear-gradient(135deg, #1976d2, #64b5f6);
}
.repairs {
  background: linear-gradient(135deg, #ff9800, #ffd54f);
}
.fees {
  background: linear-gradient(135deg, #2e7d32, #81c784);
}
.complaints {
  background: linear-gradient(135deg, #f44336, #ff8a80);
}

.chart-card,
.list-card {
  margin-bottom: 20px;
}

.chart-container {
  padding: 10px;
}
</style>
