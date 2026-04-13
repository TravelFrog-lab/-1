<template>
  <div class="home-container">
    <div class="main-content">
      <!-- 首页小区动态轮播（管理端「轮播图管理」carousel） -->
      <banner-news-carousel />

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
            <div class="stats-overview stats-overview--two">
              <div class="stat-item">
                <div class="stat-value">{{ pendingTodoCount }}</div>
                <div class="stat-label">待办事项</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ bannerCommunityActivityCount }}</div>
                <div class="stat-label">小区活动</div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 小区数据概览：2×2 -->
      <div class="stats-section" v-loading="statsLoading">
        <h2 class="section-title">小区数据概览</h2>
        <el-row :gutter="20" class="overview-row">
          <!-- 第一行左：本月服务满意度 -->
          <el-col :xs="24" :sm="12">
            <el-card shadow="hover" class="overview-card overview-card--ring">
              <div class="overview-card__title">本月服务满意度</div>
              <p class="overview-card__hint">基于本月新建报修、投诉的完成/处理情况加权计算</p>
              <div class="overview-ring">
                <el-progress
                  type="circle"
                  :percentage="safeSatisfaction"
                  :width="140"
                  :stroke-width="12"
                  :color="progressRingColors"
                />
              </div>
              <div class="overview-card__footer">
                <span class="overview-trend" :class="communityStats.satisfactionTrendClass">
                  <i :class="communityStats.satisfactionTrendIcon"></i>
                  较上月 {{ communityStats.satisfactionTrend }}
                </span>
              </div>
            </el-card>
          </el-col>
          <!-- 第一行右：近6个月报修完成率（柱状图） -->
          <el-col :xs="24" :sm="12">
            <el-card shadow="hover" class="overview-card overview-card--chart overview-card--repair-bar">
              <div class="overview-card__title">近6个月报修完成率</div>
              <p class="overview-card__hint">各月新建报修单中，状态为「已完成」的占比</p>
              <div ref="repairCompletionBarChart" class="overview-chart overview-chart--bar"></div>
            </el-card>
          </el-col>
        </el-row>
        <el-row :gutter="20" class="overview-row">
          <!-- 第二行左：近6个月投票参与人次（折线图） -->
          <el-col :xs="24" :sm="12">
            <el-card shadow="hover" class="overview-card overview-card--chart">
              <div class="overview-card__title">本期投票参与情况</div>
              <p class="overview-card__hint">按投票项目统计：已参与人数、应参与人数与参与率</p>
              <div ref="voteTrendChart" class="overview-chart overview-chart--line"></div>
            </el-card>
          </el-col>
          <!-- 第二行右：我本月报修类型统计（柱状图） -->
          <el-col :xs="24" :sm="12">
            <el-card shadow="hover" class="overview-card overview-card--chart">
              <div class="overview-card__title">我本月报修统计</div>
              <p class="overview-card__hint">按报修类型统计：本月报修数与已完成数</p>
              <div ref="ownerRepairTypeBarChart" class="overview-chart overview-chart--bar"></div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 我的待办 -->
      <div class="todo-section" v-if="isLoggedIn">
        <div class="section-title-row">
          <h2 class="section-title">我的待办</h2>
          <router-link class="todo-more-link" to="/notification?tab=todo">查看全部待办</router-link>
        </div>
        <el-card shadow="hover" class="todo-card">
          <el-table :data="todoList" style="width: 100%" empty-text="暂无待办事项">
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
                  {{ todoActionButtonLabel(scope.row) }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

    </div>

    <!-- 页脚 -->
    <page-footer></page-footer>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import * as echarts from 'echarts'
import Request from '@/utils/request'
import { todoActionButtonLabel } from '@/utils/todoActionLabel'
import { navigateTodoItem } from '@/utils/todoNavigate'
import BannerNewsCarousel from '../../components/front/BannerNewsCarousel.vue'
import PageFooter from '../../components/front/PageFooter.vue'

export default {
  name: 'Home',
  components: {
    BannerNewsCarousel,
    PageFooter
  },
  data() {
    return {
      dateTime: '',
      weatherText: '--',
      weatherTemp: '',
      timeTimer: null,
      /** 首页「我的待办」仅展示最近 3 条（接口按时间排序，总数见 pendingTodoCount） */
      todoList: [],
      pendingTodoCount: 0,
      statsLoading: false,
      // 首页概览（/dashboard/community-stats）
      communityStats: {
        satisfaction: 0,
        satisfactionTrend: '+0%',
        satisfactionTrendIcon: 'el-icon-top',
        satisfactionTrendClass: 'trend-up',
        /** 近6个月报修完成率柱状图 */
        repairMonthLabels: [],
        repairCompletionMonthlyRates: [],
        repairMonthlyTrend: [],
        repairMonthlyCompletedCounts: [],
        /** 房屋表总数 */
        houseCount: 0,
        /** 图表数据 */
        voteProjectLabels: [],
        voteProjectParticipantCounts: [],
        voteProjectEligibleCounts: [],
        voteProjectRates: [],
        ownerRepairTypeLabels: [],
        ownerRepairTypeCreatedCounts: [],
        ownerRepairTypeCompletedCounts: [],
        complaintTypeDistribution: [],
        /** 小区活动表中的活动总数（横幅「小区活动」） */
        communityActivityTotal: 0
      },
      overviewBarChart: null,
      overviewLineChart: null,
      overviewPieChart: null,
      _overviewChartResize: null,
      progressRingColors: [
        { color: '#f56c6c', percentage: 40 },
        { color: '#e6a23c', percentage: 70 },
        { color: '#67c23a', percentage: 100 }
      ],
    }
  },
  created() {
    this.fetchCommunityStats()
    this.fetchOwnerPendingTodos()
    this.fetchOwnerRepairTypeMonthly()
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
    },
    safeSatisfaction() {
      const s = Number(this.communityStats.satisfaction) || 0
      return Math.min(100, Math.max(0, Math.round(s)))
    },
    /** 小区活动表中已发布活动总数（与 community-stats 一致） */
    bannerCommunityActivityCount() {
      const n = Number(this.communityStats.communityActivityTotal)
      return Number.isFinite(n) ? n : 0
    }
  },
  watch: {
    currentUser: {
      handler() {
        this.fetchOwnerPendingTodos()
        this.fetchOwnerRepairTypeMonthly()
      },
      deep: true
    }
  },
  mounted() {
    this.updateDateTime()
    this.timeTimer = setInterval(this.updateDateTime, 1000)
    this.fetchWeather()
    this._overviewChartResize = () => {
      if (this.overviewBarChart) this.overviewBarChart.resize()
      if (this.overviewLineChart) this.overviewLineChart.resize()
      if (this.overviewPieChart) this.overviewPieChart.resize()
    }
    window.addEventListener('resize', this._overviewChartResize)
  },
  beforeDestroy() {
    if (this.timeTimer) clearInterval(this.timeTimer)
    if (this._overviewChartResize) {
      window.removeEventListener('resize', this._overviewChartResize)
    }
    if (this.overviewBarChart) {
      this.overviewBarChart.dispose()
      this.overviewBarChart = null
    }
    if (this.overviewLineChart) {
      this.overviewLineChart.dispose()
      this.overviewLineChart = null
    }
    if (this.overviewPieChart) {
      this.overviewPieChart.dispose()
      this.overviewPieChart = null
    }
  },
  methods: {
    /** 与后端 Result.code 兼容（字符串/数字） */
    isApiSuccess(response) {
      if (!response || response.code === undefined || response.code === null) return false
      return String(response.code) === '0'
    },
    /** 与后端「近6个月」规则一致：从当前月往前共6个自然月 */
    last6MonthLabels() {
      const labels = []
      for (let i = 5; i >= 0; i--) {
        const d = new Date()
        d.setDate(1)
        d.setMonth(d.getMonth() - i)
        labels.push(`${d.getMonth() + 1}月`)
      }
      return labels
    },
    scheduleRenderOverviewCharts() {
      this.$nextTick(() => {
        this.renderOverviewCharts()
        // 卡片/loading 布局完成后补一次尺寸，避免 ECharts 宽高为 0 导致「空白」
        requestAnimationFrame(() => {
          setTimeout(() => {
            if (this._overviewChartResize) this._overviewChartResize()
          }, 80)
        })
      })
    },
    formatInt(n) {
      if (n == null || n === '') return '0'
      return Number(n).toLocaleString()
    },
    fetchOwnerPendingTodos() {
      const uid = this.currentUser && this.currentUser.id
      if (!uid) {
        this.todoList = []
        this.pendingTodoCount = 0
        return
      }
      Request.get('/dashboard/owner-pending-todos', { params: { userId: uid } })
        .then((response) => {
          if (this.isApiSuccess(response) && response.data != null) {
            const list = response.data.todos
            const raw = Array.isArray(list) ? list : []
            const total = response.data.pendingTodoCount
            this.pendingTodoCount =
              typeof total === 'number' ? total : (raw.length || 0)
            this.todoList = raw.slice(0, 3)
          } else {
            this.todoList = []
            this.pendingTodoCount = 0
          }
        })
        .catch(() => {
          this.todoList = []
          this.pendingTodoCount = 0
        })
    },
    fetchCommunityStats() {
      this.statsLoading = true
      Request.get('/dashboard/community-stats')
        .then(response => {
          if (this.isApiSuccess(response) && response.data) {
            const d = response.data
            const trendClass = (s) => (s && String(s).startsWith('-')) ? 'trend-down' : 'trend-up'
            const trendIconFn = (s) => (s && String(s).startsWith('-')) ? 'el-icon-bottom' : 'el-icon-top'
            const satTrend = d.serviceSatisfactionTrend || '+0%'
            this.communityStats = {
              satisfaction: d.serviceSatisfaction != null ? d.serviceSatisfaction : 0,
              satisfactionTrend: satTrend,
              satisfactionTrendIcon: trendIconFn(satTrend),
              satisfactionTrendClass: trendClass(satTrend),
              repairMonthLabels: Array.isArray(d.repairMonthLabels) ? d.repairMonthLabels : [],
              repairCompletionMonthlyRates: Array.isArray(d.repairCompletionMonthlyRates) ? d.repairCompletionMonthlyRates : [],
              repairMonthlyTrend: Array.isArray(d.repairMonthlyTrend) ? d.repairMonthlyTrend : [],
              repairMonthlyCompletedCounts: Array.isArray(d.repairMonthlyCompletedCounts) ? d.repairMonthlyCompletedCounts : [],
              houseCount: d.totalHouseCount != null ? d.totalHouseCount : 0,
              voteProjectLabels: Array.isArray(d.voteProjectLabels) ? d.voteProjectLabels : [],
              voteProjectParticipantCounts: Array.isArray(d.voteProjectParticipantCounts) ? d.voteProjectParticipantCounts : [],
              voteProjectEligibleCounts: Array.isArray(d.voteProjectEligibleCounts) ? d.voteProjectEligibleCounts : [],
              voteProjectRates: Array.isArray(d.voteProjectRates) ? d.voteProjectRates : [],
              ownerRepairTypeLabels: this.communityStats.ownerRepairTypeLabels || [],
              ownerRepairTypeCreatedCounts: this.communityStats.ownerRepairTypeCreatedCounts || [],
              ownerRepairTypeCompletedCounts: this.communityStats.ownerRepairTypeCompletedCounts || [],
              complaintTypeDistribution: Array.isArray(d.complaintTypeDistribution) ? d.complaintTypeDistribution : [],
              communityActivityTotal: d.communityActivityTotalCount != null ? d.communityActivityTotalCount : 0
            }
            this.scheduleRenderOverviewCharts()
          } else {
            this.resetCommunityStats()
            this.scheduleRenderOverviewCharts()
          }
        })
        .catch(() => {
          this.resetCommunityStats()
          this.scheduleRenderOverviewCharts()
        })
        .finally(() => {
          this.statsLoading = false
        })
    },
    resetCommunityStats() {
      this.communityStats = {
        satisfaction: 0,
        satisfactionTrend: '-',
        satisfactionTrendIcon: 'el-icon-minus',
        satisfactionTrendClass: '',
        repairMonthLabels: [],
        repairCompletionMonthlyRates: [],
        repairMonthlyTrend: [],
        repairMonthlyCompletedCounts: [],
        houseCount: 0,
        voteProjectLabels: [],
        voteProjectParticipantCounts: [],
        voteProjectEligibleCounts: [],
        voteProjectRates: [],
        ownerRepairTypeLabels: [],
        ownerRepairTypeCreatedCounts: [],
        ownerRepairTypeCompletedCounts: [],
        complaintTypeDistribution: [],
        communityActivityTotal: 0
      }
    },
    fetchOwnerRepairTypeMonthly() {
      const uid = this.currentUser && this.currentUser.id
      if (!uid) {
        this.communityStats.ownerRepairTypeLabels = []
        this.communityStats.ownerRepairTypeCreatedCounts = []
        this.communityStats.ownerRepairTypeCompletedCounts = []
        this.$nextTick(() => this.renderOverviewCharts())
        return
      }
      Request.get('/dashboard/owner-repair-type-monthly', { params: { userId: uid } })
        .then((res) => {
          if (this.isApiSuccess(res) && res.data) {
            this.communityStats.ownerRepairTypeLabels = Array.isArray(res.data.labels) ? res.data.labels : []
            this.communityStats.ownerRepairTypeCreatedCounts = Array.isArray(res.data.createdCounts) ? res.data.createdCounts : []
            this.communityStats.ownerRepairTypeCompletedCounts = Array.isArray(res.data.completedCounts) ? res.data.completedCounts : []
          } else {
            this.communityStats.ownerRepairTypeLabels = []
            this.communityStats.ownerRepairTypeCreatedCounts = []
            this.communityStats.ownerRepairTypeCompletedCounts = []
          }
          this.$nextTick(() => this.renderOverviewCharts())
        })
        .catch(() => {
          this.communityStats.ownerRepairTypeLabels = []
          this.communityStats.ownerRepairTypeCreatedCounts = []
          this.communityStats.ownerRepairTypeCompletedCounts = []
          this.$nextTick(() => this.renderOverviewCharts())
        })
    },
    renderRepairCompletionBarChart() {
      const barEl = this.$refs.repairCompletionBarChart
      if (!barEl) return

      const labels =
        this.communityStats.repairMonthLabels && this.communityStats.repairMonthLabels.length >= 6
          ? [...this.communityStats.repairMonthLabels]
          : this.last6MonthLabels()
      let rates =
        this.communityStats.repairCompletionMonthlyRates &&
        this.communityStats.repairCompletionMonthlyRates.length
          ? [...this.communityStats.repairCompletionMonthlyRates]
          : [0, 0, 0, 0, 0, 0]
      while (rates.length < 6) rates.push(0)
      rates = rates.slice(0, 6)

      const totals = this.communityStats.repairMonthlyTrend || []
      const dones = this.communityStats.repairMonthlyCompletedCounts || []
      const totalRepairs = totals.reduce((sum, v) => sum + (Number(v) || 0), 0)
      const barEmpty = totalRepairs === 0

      if (!this.overviewBarChart) {
        this.overviewBarChart = echarts.init(barEl)
      }
      this.overviewBarChart.setOption(
        {
          color: ['#409EFF'],
          graphic: barEmpty
            ? [
                {
                  type: 'text',
                  left: 'center',
                  top: 'middle',
                  style: {
                    text: '近6个月暂无报修单',
                    fill: '#909399',
                    fontSize: 13,
                    fontWeight: 'normal'
                  }
                }
              ]
            : [],
          grid: { left: '3%', right: '4%', top: '14%', bottom: '3%', containLabel: true },
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            formatter(params) {
              const p = params[0]
              const i = p.dataIndex
              const total = totals[i] != null ? totals[i] : 0
              const done = dones[i] != null ? dones[i] : 0
              if (Number(total) === 0) {
                return `${p.name}<br/>该月暂无报修单`
              }
              return `${p.name}<br/>完成率：<b>${p.value}%</b><br/>已完成 ${done} / 共 ${total} 单`
            }
          },
          xAxis: {
            type: 'category',
            data: labels,
            axisLine: { lineStyle: { color: '#dcdfe6' } },
            axisLabel: { fontSize: 11, color: '#909399' }
          },
          yAxis: {
            type: 'value',
            min: 0,
            max: 100,
            axisLabel: { fontSize: 11, color: '#909399', formatter: '{value}%' },
            splitLine: { lineStyle: { type: 'dashed', color: '#ebeef5' } }
          },
          series: [
            {
              name: '完成率',
              type: 'bar',
              barMaxWidth: 36,
              itemStyle: { borderRadius: [4, 4, 0, 0] },
              data: rates
            }
          ]
        },
        true
      )
      this.overviewBarChart.resize()
    },
    renderOverviewCharts() {
      this.$nextTick(() => {
        this.renderRepairCompletionBarChart()

        const lineEl = this.$refs.voteTrendChart
        const ownerRepairBarEl = this.$refs.ownerRepairTypeBarChart
        if (!lineEl || !ownerRepairBarEl) return

        const labels = (this.communityStats.voteProjectLabels || []).slice()
        const participants = (this.communityStats.voteProjectParticipantCounts || []).map(v => Number(v) || 0)
        const eligibles = (this.communityStats.voteProjectEligibleCounts || []).map(v => Number(v) || 0)
        const rates = (this.communityStats.voteProjectRates || []).map(v => Number(v) || 0)
        const voteEmpty = labels.length === 0

        if (!this.overviewLineChart) {
          this.overviewLineChart = echarts.init(lineEl)
        }
        this.overviewLineChart.setOption(
          {
            color: ['#409EFF', '#67C23A', '#E6A23C'],
            graphic: voteEmpty
              ? [
                  {
                    type: 'text',
                    left: 'center',
                    top: 'middle',
                    style: {
                      text: '暂无投票项目',
                      fill: '#909399',
                      fontSize: 13,
                      fontWeight: 'normal'
                    }
                  }
                ]
              : [],
            grid: { left: '3%', right: '4%', top: '14%', bottom: '3%', containLabel: true },
            tooltip: {
              trigger: 'axis',
              axisPointer: { type: 'shadow' },
              formatter: (params) => {
                const idx = params && params.length ? params[0].dataIndex : 0
                const p = participants[idx] != null ? participants[idx] : 0
                const e = eligibles[idx] != null ? eligibles[idx] : 0
                const r = rates[idx] != null ? rates[idx] : 0
                return `${labels[idx] || ''}<br/>已参与人数：<b>${p}</b><br/>应参与人数：<b>${e}</b><br/>参与率：<b>${r}%</b>`
              }
            },
            xAxis: {
              type: 'category',
              data: labels,
              axisLine: { lineStyle: { color: '#dcdfe6' } },
              axisLabel: {
                fontSize: 11,
                color: '#909399',
                interval: 0,
                formatter: (value) => {
                  if (!value) return ''
                  return value.length > 8 ? value.slice(0, 8) + '…' : value
                }
              }
            },
            yAxis: [
              {
                type: 'value',
                minInterval: 1,
                splitLine: { lineStyle: { type: 'dashed', color: '#ebeef5' } },
                axisLabel: { fontSize: 11, color: '#909399' },
                name: '人数'
              },
              {
                type: 'value',
                min: 0,
                max: 100,
                axisLabel: { fontSize: 11, color: '#909399', formatter: '{value}%' },
                splitLine: { show: false },
                name: '参与率'
              }
            ],
            series: [
              {
                name: '已参与人数',
                type: 'bar',
                barMaxWidth: 28,
                data: participants
              },
              {
                name: '应参与人数',
                type: 'bar',
                barMaxWidth: 28,
                data: eligibles
              },
              {
                name: '参与率',
                type: 'line',
                yAxisIndex: 1,
                smooth: true,
                symbol: 'circle',
                symbolSize: 6,
                lineStyle: { width: 2 },
                data: rates
              },
            ]
          },
          true
        )
        this.overviewLineChart.resize()

        const rrLabels = (this.communityStats.ownerRepairTypeLabels || []).slice()
        const rrCreated = (this.communityStats.ownerRepairTypeCreatedCounts || []).map(v => Number(v) || 0)
        const rrCompleted = (this.communityStats.ownerRepairTypeCompletedCounts || []).map(v => Number(v) || 0)
        const rrEmpty = rrLabels.length === 0 || rrCreated.reduce((s, v) => s + v, 0) === 0
        if (!this.overviewPieChart) {
          this.overviewPieChart = echarts.init(ownerRepairBarEl)
        }
        this.overviewPieChart.setOption(
          {
            color: ['#409EFF', '#67C23A'],
            graphic: rrEmpty
              ? [{
                  type: 'text',
                  left: 'center',
                  top: 'middle',
                  style: { text: '本月暂无报修记录', fill: '#909399', fontSize: 13 }
                }]
              : [],
            grid: { left: '3%', right: '4%', top: '14%', bottom: '3%', containLabel: true },
            tooltip: {
              trigger: 'axis',
              axisPointer: { type: 'shadow' }
            },
            legend: rrEmpty ? { show: false } : { data: ['本月报修数', '本月已完成数'] },
            xAxis: {
              type: 'category',
              data: rrLabels,
              axisLine: { lineStyle: { color: '#dcdfe6' } },
              axisLabel: { fontSize: 11, color: '#909399' }
            },
            yAxis: {
              type: 'value',
              minInterval: 1,
              splitLine: { lineStyle: { type: 'dashed', color: '#ebeef5' } },
              axisLabel: { fontSize: 11, color: '#909399' }
            },
            series: rrEmpty
              ? []
              : [
                  { name: '本月报修数', type: 'bar', barMaxWidth: 30, data: rrCreated },
                  { name: '本月已完成数', type: 'bar', barMaxWidth: 30, data: rrCompleted }
                ]
          },
          true
        )
        this.overviewPieChart.resize()
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
      // 默认为北京坐标
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
    todoActionButtonLabel,
    handleTodoAction(item) {
      navigateTodoItem(this.$router, item)
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

/* 欢迎横幅：圆角、阴影与新闻资讯框一致 */
.welcome-banner {
  margin-top: 0;
  margin-bottom: 36px;
}

.banner-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none !important;
  color: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.banner-card >>> .el-card__body {
  padding: 0;
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 28px;
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

.stats-overview--two {
  gap: 56px;
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

.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin: 30px 0 20px;
}

.section-title-row .section-title {
  margin: 0;
}

.todo-more-link {
  font-size: 14px;
  color: #409eff;
  text-decoration: none;
  white-space: nowrap;
}

.todo-more-link:hover {
  text-decoration: underline;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 30px 0 20px;
}

/* 小区数据概览 2×2（与上方欢迎区拉开间距） */
.stats-section {
  margin: 0 0 32px;
}

.stats-section > .section-title:first-child {
  margin-top: 0;
  margin-bottom: 20px;
}

.overview-row {
  margin-bottom: 20px;
}

.overview-row:last-child {
  margin-bottom: 0;
}

.overview-card {
  min-height: 280px;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
}

.overview-card >>> .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 18px 20px 14px;
}

.overview-card__title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.overview-card__hint {
  font-size: 12px;
  color: #909399;
  margin: 0 0 8px;
  line-height: 1.4;
}

.overview-card__footer {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.overview-trend {
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.trend-up {
  color: #52c41a;
}

.trend-down {
  color: #f56c6c;
}

/* 环形满意度 */
.overview-card--ring .overview-ring {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 0 4px;
}

.repair-rate-detail {
  margin: 0 0 10px;
  font-size: 13px;
  color: #606266;
  text-align: center;
  line-height: 1.5;
}

.repair-rate-detail strong {
  color: #303133;
}

/* 带 ECharts 的概览卡片（含第一行报修完成率柱状图） */
.overview-card--chart {
  min-height: 320px;
}

/* 第一行与右侧柱状图卡片等高 */
.stats-section .overview-row:first-child .overview-card--ring {
  min-height: 320px;
}

.overview-chart {
  width: 100%;
  height: 240px;
  flex: 1;
  min-height: 220px;
}

/* 房屋 / 车位统计 */
.overview-card--population .population-block {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 24px 0;
}

.population-block__icon {
  font-size: 56px;
  opacity: 0.9;
}

.overview-card--house-stat .population-block__icon {
  color: #409eff;
}

.population-block__num {
  font-size: 48px;
  font-weight: 700;
  color: #303133;
  letter-spacing: 0.02em;
  line-height: 1;
}

/* 进行中活动 */
.overview-card--activity .activity-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 8px;
  text-align: center;
}

.activity-status-tag {
  margin-bottom: 12px;
}

.activity-count {
  font-size: 42px;
  font-weight: 700;
  color: #67c23a;
  line-height: 1;
  margin-bottom: 8px;
}

.activity-desc {
  margin: 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}

.activity-body--empty {
  padding: 28px 12px;
}

.activity-empty-tip {
  margin: 12px 0 0;
  font-size: 14px;
  color: #c0c4cc;
}

.overview-card--activity.is-activity-empty {
  background: #fafafa;
  border: 1px solid #ebeef5;
}

.overview-card--activity.is-activity-empty .overview-card__title {
  color: #909399;
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

  .population-block__num {
    font-size: 36px;
  }

  .population-block__icon {
    font-size: 44px;
  }

  .activity-count {
    font-size: 32px;
  }
}
</style>