<template>
  <div class="home-wrapper">
    <div class="welcome-section">
      <div class="time-display">
        {{ currentDate }}
        <div class="time">{{ currentTime }}</div>
      </div>
      
      <div class="greeting">
        <h1>{{ welcomeTitle }}</h1>
      </div>
    </div>

    <!-- 数据统计卡片 -->
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
                <div class="stat-value">{{ formatValue(item.value, item.type) }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 管理员图表 -->
    <template v-if="!isMaintenanceRole">
      <div class="chart-section">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="chart-card">
              <div slot="header" class="chart-header">
                <span>物业费收缴统计</span>
              </div>
              <div class="chart-container">
                <div ref="feeChart" style="height: 300px"></div>
              </div>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card class="chart-card">
              <div slot="header">
                <span>房屋类型分布</span>
              </div>
              <div class="chart-container">
                <div ref="houseTypeChart" style="height: 300px"></div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <div class="activity-section">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="list-card">
              <div slot="header">
                <span>报修类型分布</span>
              </div>
              <div class="chart-container">
                <div ref="repairTypeBarChart" style="height: 300px"></div>
              </div>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card class="list-card">
              <div slot="header">
                <span>投诉类型分布</span>
              </div>
              <div class="chart-container">
                <div ref="complaintTypePieChart" style="height: 300px"></div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </template>

    <!-- 维修工个人看板图表 -->
    <div v-else class="maintainer-charts">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="chart-card">
            <div slot="header">
              <span>维修类型分布</span>
            </div>
            <div class="chart-container">
              <div ref="mRepairPieChart" style="height: 300px"></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <div slot="header">
              <span>评价星级分布</span>
            </div>
            <div class="chart-container">
              <div ref="mRatingBarChart" style="height: 300px"></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 0">
        <el-col :span="12">
          <el-card class="list-card">
            <div slot="header">
              <span>工单完成趋势（近 6 个月）</span>
            </div>
            <div class="chart-container">
              <div ref="mTrendLineChart" style="height: 300px"></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="list-card">
            <div slot="header">
              <span>工单状态分布</span>
            </div>
            <div class="chart-container">
              <div ref="mStatusRingChart" style="height: 300px"></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 说明：本页面主要展示图表与概览；“最新物业费收缴”列表已移除 -->
  </div>
</template>

<script>
import { Card, Row, Col, Table, TableColumn, Tag, RadioGroup, RadioButton } from 'element-ui'
import * as echarts from 'echarts'
import moment from 'moment'
import { mapGetters } from 'vuex'
import request from '@/utils/request'

export default {
  name: 'HomeView',
  components: {
    [Card.name]: Card,
    [Row.name]: Row,
    [Col.name]: Col,
    [Table.name]: Table,
    [TableColumn.name]: TableColumn,
    [Tag.name]: Tag,
    [RadioGroup.name]: RadioGroup,
    [RadioButton.name]: RadioButton
  },
  data() {
    return {
      currentTime: '',
      currentDate: '',
      username: 'Admin', // 这里可以从用户状态获取实际用户名
      timer: null,
      
      // 统计数据
      statistics: {
        totalOwners: 0,
        propertyFeeRate: 0,
        repairCompletionRate: 0,
        complaintResolutionRate: 0
      },
      
      // 图表数据
      chartData: {
        months: [],
        expectedFees: [],
        actualFees: [],
        houseTypes: [],
        repairTypeDistribution: [],
        complaintTypeDistribution: []
      },
      
      // 图表实例
      feeChart: null,
      houseTypeChart: null,
      repairTypeBarChart: null,
      complaintTypePieChart: null,
      mRepairPieChart: null,
      mRatingBarChart: null,
      mTrendLineChart: null,
      mStatusRingChart: null,

      maintainerStats: {
        todayNewAssigned: 0,
        inWorkOrders: 0,
        monthCompleted: 0,
        hasAvgRating: false,
        avgRating: null
      },
      maintainerChartData: {
        repairTypePie: [],
        ratingBars: [],
        completionTrendMonths: [],
        completionTrendCounts: [],
        statusRing: []
      }
    }
  },
  computed: {
    ...mapGetters(['currentUser']),
    isMaintenanceRole() {
      return !!(this.currentUser && this.currentUser.role === 'MAINTENANCE')
    },
    welcomeTitle() {
      if (this.isMaintenanceRole) return '欢迎回来，祝今天一切顺利'
      return '欢迎回来, ' + (this.username || '用户')
    },
    statCards() {
      if (this.isMaintenanceRole) {
        return [
          {
            label: '今日新派单',
            value: this.maintainerStats.todayNewAssigned,
            icon: 'el-icon-s-order',
            class: 'residents',
            type: 'number'
          },
          {
            label: '在办工单（待接单+处理中）',
            value: this.maintainerStats.inWorkOrders,
            icon: 'el-icon-loading',
            class: 'repairs',
            type: 'number'
          },
          {
            label: '本月完成',
            value: this.maintainerStats.monthCompleted,
            icon: 'el-icon-circle-check',
            class: 'fees',
            type: 'number'
          },
          {
            label: '平均评分',
            value: this.maintainerStats.hasAvgRating ? this.maintainerStats.avgRating : null,
            icon: 'el-icon-star-on',
            class: 'complaints',
            type: 'rating'
          }
        ]
      }
      return [
        {
          label: '总业主数',
          value: this.statistics.totalOwners,
          icon: 'el-icon-user',
          class: 'residents',
          type: 'number'
        },
        {
          label: '物业费收缴率',
          value: this.statistics.propertyFeeRate,
          icon: 'el-icon-money',
          class: 'fees',
          type: 'percent'
        },
        {
          label: '维修完成率',
          value: this.statistics.repairCompletionRate,
          icon: 'el-icon-service',
          class: 'repairs',
          type: 'percent'
        },
        {
          label: '投诉处理率',
          value: this.statistics.complaintResolutionRate,
          icon: 'el-icon-warning',
          class: 'complaints',
          type: 'percent'
        }
      ]
    }
  },
  created() {
    this.updateTime()
    this.timer = setInterval(this.updateTime, 1000)
    if (this.currentUser) {
      this.username = this.currentUser.name || this.currentUser.username || '用户'
    }
    this.fetchDashboardData()
  },
  mounted() {
    this.$nextTick(() => {
      if (this.isMaintenanceRole) {
        this.initMaintainerCharts()
        this.updateMaintainerCharts()
      } else {
        this.initCharts()
      }
      window.addEventListener('resize', this.resizeCharts)
    })
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }

    if (this.feeChart) {
      this.feeChart.dispose()
    }
    if (this.houseTypeChart) {
      this.houseTypeChart.dispose()
    }
    if (this.repairTypeBarChart) {
      this.repairTypeBarChart.dispose()
    }
    if (this.complaintTypePieChart) {
      this.complaintTypePieChart.dispose()
    }
    if (this.mRepairPieChart) {
      this.mRepairPieChart.dispose()
    }
    if (this.mRatingBarChart) {
      this.mRatingBarChart.dispose()
    }
    if (this.mTrendLineChart) {
      this.mTrendLineChart.dispose()
    }
    if (this.mStatusRingChart) {
      this.mStatusRingChart.dispose()
    }

    window.removeEventListener('resize', this.resizeCharts)
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
    
    fetchDashboardData() {
      if (this.isMaintenanceRole) {
        this.fetchMaintainerDashboard()
        return
      }
      request.get('/dashboard/overview').then(res => {
        if (res.code === '0') {
          this.statistics = res.data
        }
      }).catch(err => {
        console.error('获取总览数据失败', err)
      })

      this.fetchPropertyFeeStats()

      request.get('/dashboard/house-type/stats').then(res => {
        if (res.code === '0') {
          this.chartData.houseTypes = res.data.houseTypes
          this.updateHouseTypeChart()
        }
      }).catch(err => {
        console.error('获取房屋类型统计失败', err)
      })

      request.get('/dashboard/community-stats').then(res => {
        if (res.code === '0' && res.data) {
          this.chartData.repairTypeDistribution = res.data.repairTypeDistribution || []
          this.chartData.complaintTypeDistribution = res.data.complaintTypeDistribution || []
          this.updateRepairTypeBarChart()
          this.updateComplaintTypePieChart()
        }
      }).catch(err => {
        console.error('获取类型分布失败', err)
      })
    },

    applyMaintainerData(d) {
      this.maintainerStats = {
        todayNewAssigned: d.todayNewAssigned || 0,
        inWorkOrders: d.inWorkOrders || 0,
        monthCompleted: d.monthCompleted || 0,
        hasAvgRating: !!d.hasAvgRating,
        avgRating: d.avgRating != null ? d.avgRating : null
      }
      this.maintainerChartData = {
        repairTypePie: d.repairTypePie || [],
        ratingBars: d.ratingBars || [],
        completionTrendMonths: d.completionTrendMonths || [],
        completionTrendCounts: d.completionTrendCounts || [],
        statusRing: d.statusRing || []
      }
    },

    fetchMaintainerDashboard() {
      if (!this.currentUser || this.currentUser.id == null) {
        return
      }
      request.get('/dashboard/maintainer-overview', {
        params: { userId: this.currentUser.id }
      }).then(res => {
        if (res.code === '0' && res.data) {
          this.applyMaintainerData(res.data)
          this.$nextTick(() => {
            this.updateMaintainerCharts()
          })
        }
      }).catch(err => {
        console.error('获取维修工看板失败', err)
      })
    },
    
    fetchPropertyFeeStats() {
      request.get('/dashboard/property-fee/stats', {
        params: { years: 1 }
      }).then(res => {
        if (res.code === '0') {
          const months = res.data.months || []
          const expectedFees = res.data.expectedFees || []
          const actualFees = res.data.actualFees || []
          this.chartData.months = months.slice(-6)
          this.chartData.expectedFees = expectedFees.slice(-6)
          this.chartData.actualFees = actualFees.slice(-6)
          this.updateFeeChart()
        }
      }).catch(err => {
        console.error('获取物业费统计失败', err)
      })
    },
    
    initCharts() {
      // 初始化物业费图表
      this.feeChart = echarts.init(this.$refs.feeChart)
      this.feeChart.setOption({
        title: {
          text: '物业费收缴情况',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function(params) {
            let tooltip = params[0].name + '<br/>'
            params.forEach(param => {
              tooltip += param.seriesName + ': ¥' + param.value.toFixed(2) + '<br/>'
            })
            return tooltip
          }
        },
        legend: {
          data: ['应收金额', '实收金额'],
          bottom: 0
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '10%',
          top: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: this.chartData.months
        },
        yAxis: {
          type: 'value',
          name: '金额 (元)',
          axisLabel: {
            formatter: '¥{value}'
          }
        },
        series: [
          {
            name: '应收金额',
            type: 'bar',
            stack: 'total',
            itemStyle: {
              color: '#91cc75'
            },
            data: this.chartData.expectedFees
          },
          {
            name: '实收金额',
            type: 'bar',
            stack: 'total',
            itemStyle: {
              color: '#5470c6'
            },
            data: this.chartData.actualFees
          }
        ]
      })
      
      // 初始化房屋类型图表
      this.houseTypeChart = echarts.init(this.$refs.houseTypeChart)
      this.houseTypeChart.setOption({
        title: {
          text: '房屋类型分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 10,
          data: this.chartData.houseTypes.map(item => item.name)
        },
        series: [
          {
            name: '房屋类型',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '18',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: this.chartData.houseTypes
          }
        ]
      })

      // 报修类型柱状图
      this.repairTypeBarChart = echarts.init(this.$refs.repairTypeBarChart)
      this.repairTypeBarChart.setOption({
        title: {
          text: '报修类型分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: []
        },
        yAxis: {
          type: 'value',
          name: '数量'
        },
        series: [
          {
            name: '报修数量',
            type: 'bar',
            itemStyle: { color: '#5470c6' },
            data: []
          }
        ]
      })

      // 投诉类型饼图
      this.complaintTypePieChart = echarts.init(this.$refs.complaintTypePieChart)
      this.complaintTypePieChart.setOption({
        title: {
          text: '投诉类型分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 10,
          data: []
        },
        series: [
          {
            name: '投诉数量',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: { show: false, position: 'center' },
            emphasis: {
              label: { show: true, fontSize: 18, fontWeight: 'bold' }
            },
            labelLine: { show: false },
            data: []
          }
        ]
      })
    },

    initMaintainerCharts() {
      const pieRef = this.$refs.mRepairPieChart
      const barRef = this.$refs.mRatingBarChart
      const lineRef = this.$refs.mTrendLineChart
      const ringRef = this.$refs.mStatusRingChart
      if (!pieRef || !barRef || !lineRef || !ringRef) return

      this.mRepairPieChart = echarts.init(pieRef)
      this.mRepairPieChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { orient: 'vertical', left: 10, data: [] },
        series: [{
          name: '工单数',
          type: 'pie',
          radius: ['38%', '68%'],
          itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
          data: []
        }]
      })

      this.mRatingBarChart = echarts.init(barRef)
      this.mRatingBarChart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
        xAxis: { type: 'category', data: ['1星', '2星', '3星', '4星', '5星'] },
        yAxis: { type: 'value', name: '条数' },
        series: [{
          name: '评价数',
          type: 'bar',
          barMaxWidth: 40,
          data: []
        }]
      })

      this.mTrendLineChart = echarts.init(lineRef)
      this.mTrendLineChart.setOption({
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

      this.mStatusRingChart = echarts.init(ringRef)
      this.mStatusRingChart.setOption({
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

    updateMaintainerCharts() {
      if (!this.mRepairPieChart || !this.mRatingBarChart || !this.mTrendLineChart || !this.mStatusRingChart) {
        return
      }
      const d = this.maintainerChartData
      const pieData = d.repairTypePie || []
      this.mRepairPieChart.setOption({
        legend: { data: pieData.map(i => i.name) },
        series: [{ data: pieData }]
      })

      const rows = d.ratingBars || []
      const barData = rows.map(r => {
        const s = r.star
        let color = '#91cc75'
        if (s <= 2) color = '#ee6666'
        else if (s === 3) color = '#fac858'
        return { value: r.value, itemStyle: { color } }
      })
      this.mRatingBarChart.setOption({
        series: [{ data: barData }]
      })

      this.mTrendLineChart.setOption({
        xAxis: { data: d.completionTrendMonths || [] },
        series: [{ data: d.completionTrendCounts || [] }]
      })

      const ring = d.statusRing || []
      this.mStatusRingChart.setOption({
        legend: { data: ring.map(i => i.name) },
        series: [{ data: ring }]
      })
    },

    resizeCharts() {
      if (this.feeChart) this.feeChart.resize()
      if (this.houseTypeChart) this.houseTypeChart.resize()
      if (this.repairTypeBarChart) this.repairTypeBarChart.resize()
      if (this.complaintTypePieChart) this.complaintTypePieChart.resize()
      if (this.mRepairPieChart) this.mRepairPieChart.resize()
      if (this.mRatingBarChart) this.mRatingBarChart.resize()
      if (this.mTrendLineChart) this.mTrendLineChart.resize()
      if (this.mStatusRingChart) this.mStatusRingChart.resize()
    },
    
    // 更新物业费图表数据
    updateFeeChart() {
      if (this.feeChart) {
        this.feeChart.setOption({
          xAxis: { data: this.chartData.months },
          series: [
            { data: this.chartData.expectedFees },
            { data: this.chartData.actualFees }
          ]
        })
      }
    },
    
    // 更新房屋类型图表数据
    updateHouseTypeChart() {
      if (this.houseTypeChart) {
        this.houseTypeChart.setOption({
          legend: {
            data: this.chartData.houseTypes.map(item => item.name)
          },
          series: [{ data: this.chartData.houseTypes }]
        })
      }
    },

    updateRepairTypeBarChart() {
      if (!this.repairTypeBarChart) return
      const rows = this.chartData.repairTypeDistribution || []
      this.repairTypeBarChart.setOption({
        xAxis: {
          data: rows.map(item => item.name)
        },
        series: [
          {
            data: rows.map(item => item.value)
          }
        ]
      })
    },

    updateComplaintTypePieChart() {
      if (!this.complaintTypePieChart) return
      const rows = this.chartData.complaintTypeDistribution || []
      this.complaintTypePieChart.setOption({
        legend: {
          data: rows.map(item => item.name)
        },
        series: [
          {
            data: rows
          }
        ]
      })
    },
    
    formatValue(value, type) {
      if (type === 'rating') {
        if (value === null || value === undefined) return '暂无评价'
        return Number(value).toFixed(1) + ' ★'
      }
      if (type === 'percent') {
        return (value * 100).toFixed(2) + '%'
      } else if (type === 'money') {
        return '¥' + value.toFixed(2)
      }
      return value
    },
    
    // 格式化日期
    formatDate(date) {
      return moment(date).format('MM-DD HH:mm')
    },
    
    // 获取维修状态类型
    getStatusType(status) {
      const types = {
        'PENDING': 'info',
        'IN_PROGRESS': 'warning',
        'COMPLETED': 'success',
        'CANCELLED': 'danger'
      }
      return types[status] || 'info'
    },
    
    // 获取维修状态文本
    getStatusText(status) {
      const texts = {
        'PENDING': '待处理',
        'IN_PROGRESS': '处理中',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return texts[status] || '未知'
    },
    
    // 获取投诉状态类型
    getComplaintStatusType(status) {
      const types = {
        'PENDING': 'info',
        'PROCESSING': 'warning',
        'RESOLVED': 'success'
      }
      return types[status] || 'info'
    },
    
    // 获取投诉状态文本
    getComplaintStatusText(status) {
      const texts = {
        'PENDING': '待处理',
        'PROCESSING': '处理中',
        'RESOLVED': '已解决'
      }
      return texts[status] || '未知'
    },
    
    // 格式化费用月份
    formatFeeDate(feeDate) {
      if (!feeDate) return '';
      return moment(feeDate).format('YYYY-MM');
    }
  }
}
</script>

<style lang="less" scoped>
.home-wrapper {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
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

.greeting {
  h1 {
    font-size: 2rem;
    margin: 0;
    font-weight: 600;
  }

  .subtitle {
    font-size: 1.1rem;
    opacity: 0.9;
    margin-top: 10px;
  }
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

.stat-info {
  width: 100%;
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

.residents { background: linear-gradient(135deg, #1976d2, #64b5f6); }
.fees { background: linear-gradient(135deg, #2e7d32, #81c784); }
.repairs { background: linear-gradient(135deg, #ff9800, #ffd54f); }
.complaints { background: linear-gradient(135deg, #f44336, #ff8a80); }

.chart-section, .activity-section {
  margin-bottom: 10px;
}

.chart-card, .list-card {
  margin-bottom: 20px;
}

.chart-container {
  padding: 10px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>