<template>
  <div class="home-wrapper">
    <div class="welcome-section">
      <div class="time-display">
        {{ currentDate }}
        <div class="time">{{ currentTime }}</div>
      </div>
      
      <div class="greeting">
        <h1>欢迎回来, {{ username }}</h1>
        <p class="subtitle">今天是美好的一天，让我们开始工作吧！</p>
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

    <!-- 图表区域 -->
    <div class="chart-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="chart-card">
            <div slot="header" class="chart-header">
              <span>物业费收缴统计</span>
              <el-radio-group v-model="yearRange" size="small" @change="fetchPropertyFeeStats">
                <el-radio-button :label="1">近1年</el-radio-button>
                <el-radio-button :label="2">近2年</el-radio-button>
                <el-radio-button :label="3">近3年</el-radio-button>
              </el-radio-group>
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

    <!-- 活动统计行 -->
    <div class="activity-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="list-card">
            <div slot="header">
              <span>最新维修</span>
            </div>
            <el-table :data="latestRepairs" style="width: 100%">
              <el-table-column prop="applicant.user.name" label="申请人" width="100"></el-table-column>
              <el-table-column prop="description" label="维修内容" show-overflow-tooltip></el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template slot-scope="scope">
                  <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="申请时间" width="150">
                <template slot-scope="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="list-card">
            <div slot="header">
              <span>最新投诉</span>
            </div>
            <el-table :data="latestComplaints" style="width: 100%">
              <el-table-column prop="complainant.user.name" label="投诉人" width="100"></el-table-column>
              <el-table-column prop="content" label="投诉内容" show-overflow-tooltip></el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template slot-scope="scope">
                  <el-tag :type="getComplaintStatusType(scope.row.status)">{{ getComplaintStatusText(scope.row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="投诉时间" width="150">
                <template slot-scope="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 添加物业费收缴情况表格 -->
    <el-card class="list-card" style="margin-top: 20px;">
      <div slot="header">
        <span>最新物业费收缴</span>
      </div>
      <el-table :data="latestPropertyFees" style="width: 100%">
        <el-table-column prop="orderNo" label="订单编号" width="180"></el-table-column>
        <el-table-column prop="owner.user.name" label="业主" width="100"></el-table-column>
        <el-table-column prop="amount" label="金额" width="100">
          <template slot-scope="scope">
            ¥{{ scope.row.amount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'PAID' ? 'success' : 'warning'">
              {{ scope.row.status === 'PAID' ? '已缴费' : '未缴费' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="feeDate" label="费用月份" width="120">
          <template slot-scope="scope">
            {{ formatFeeDate(scope.row.feeDate) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { Card, Row, Col, Table, TableColumn, Tag, RadioGroup, RadioButton } from 'element-ui'
import * as echarts from 'echarts'
import moment from 'moment'
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
      yearRange: 1, // 默认显示近1年的数据
      
      // 统计数据
      statistics: {
        totalOwners: 0,
        propertyFeeRate: 0,
        repairCompletionRate: 0,
        complaintResolutionRate: 0,
        parkingOccupancyRate: 0
      },
      
      // 图表数据
      chartData: {
        months: [],
        expectedFees: [],
        actualFees: [],
        houseTypes: []
      },
      
      // 最新记录
      latestRepairs: [],
      latestComplaints: [],
      latestPropertyFees: [],
      
      // 图表实例
      feeChart: null,
      houseTypeChart: null
    }
  },
  computed: {
    statCards() {
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
    this.fetchDashboardData()
  },
  mounted() {
    this.initCharts()
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
    
    // 销毁图表实例
    if (this.feeChart) {
      this.feeChart.dispose()
    }
    if (this.houseTypeChart) {
      this.houseTypeChart.dispose()
    }
    
    // 移除窗口大小变化监听
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
      // 获取总览数据
      request.get('/dashboard/overview').then(res => {
        if (res.code === '0') {
          this.statistics = res.data
        }
      }).catch(err => {
        console.error('获取总览数据失败', err)
      })
      
      // 获取物业费统计
      this.fetchPropertyFeeStats()
      
      // 获取房屋类型统计
      request.get('/dashboard/house-type/stats').then(res => {
        if (res.code === '0') {
          this.chartData.houseTypes = res.data.houseTypes
          this.updateHouseTypeChart()
        }
      }).catch(err => {
        console.error('获取房屋类型统计失败', err)
      })
      
      // 获取最新维修记录
      request.get('/repair-records/page', {
        params: {
          pageNum: 1,
          pageSize: 5
        }
      }).then(res => {
        if (res.code === '0') {
          this.latestRepairs = res.data.records
        }
      }).catch(err => {
        console.error('获取最新维修记录失败', err)
      })
      
      // 获取最新投诉记录
      request.get('/complaints/page', {
        params: {
          pageNum: 1,
          pageSize: 5
        }
      }).then(res => {
        if (res.code === '0') {
          this.latestComplaints = res.data.records
        }
      }).catch(err => {
        console.error('获取最新投诉记录失败', err)
      })
      
      // 获取最新物业费记录
      request.get('/property-fees/page', {
        params: {
          pageNum: 1,
          pageSize: 5
        }
      }).then(res => {
        if (res.code === '0') {
          this.latestPropertyFees = res.data.records
        }
      }).catch(err => {
        console.error('获取最新物业费记录失败', err)
      })
    },
    
    fetchPropertyFeeStats() {
      request.get('/dashboard/property-fee/stats', {
        params: { years: this.yearRange }
      }).then(res => {
        if (res.code === '0') {
          this.chartData.months = res.data.months
          this.chartData.expectedFees = res.data.expectedFees
          this.chartData.actualFees = res.data.actualFees
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
      
      // 添加窗口大小变化监听
      window.addEventListener('resize', this.resizeCharts)
    },
    
    resizeCharts() {
      if (this.feeChart) this.feeChart.resize()
      if (this.houseTypeChart) this.houseTypeChart.resize()
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
    
    // 格式化数值
    formatValue(value, type) {
      if (type === 'percent') {
        return (value * 100).toFixed(2) + '%'
      } else if (type === 'money') {
        return '¥' + value.toFixed(2)
      } else {
        return value
      }
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