<template>
  <div class="volunteer-page">
    <nav-header></nav-header>
    
    <div class="content-wrapper">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2 class="page-title">志愿活动</h2>
        <div class="page-desc">参与社区志愿服务，共建美好家园</div>
      </div>

      <!-- 活动列表 -->
      <el-card class="activity-card" shadow="never">
        <div class="form-header">
          <div class="header-left">
            <i class="el-icon-s-flag"></i>
            <span>活动列表</span>
          </div>
          <div class="header-right">
            <span class="total-text">共 {{ total }} 个活动</span>
          </div>
        </div>

        <el-table :data="activities" v-loading="loading">
          <el-table-column prop="name" label="活动名称"></el-table-column>
          <el-table-column prop="location" label="活动地点"></el-table-column>
          <el-table-column prop="contactPerson" label="联系人"></el-table-column>
          <el-table-column prop="contactPhone" label="联系电话"></el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="160">
            <template slot-scope="scope">
              {{ formatTime(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="160">
            <template slot-scope="scope">
              {{ formatTime(scope.row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column label="报名情况" width="120">
            <template slot-scope="scope">
              {{ scope.row.currentParticipants }}/{{ scope.row.maxParticipants }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template slot-scope="scope">
              <el-button 
                type="text" 
                @click="handleRegister(scope.row)"
                :disabled="isActivityFull(scope.row) || isActivityStarted(scope.row)">
                {{ getActionText(scope.row) }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[10, 20, 30, 50]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total">
          </el-pagination>
        </div>
      </el-card>

      <!-- 我的报名记录 -->
      <el-card v-if="isLogin" class="registration-card" shadow="never">
        <div class="form-header">
          <div class="header-left">
            <i class="el-icon-s-order"></i>
            <span>我的报名</span>
          </div>
        </div>

        <!-- 搜索区域 -->
        <div class="search-area">
          <el-form :inline="true" size="small">
            <el-form-item label="活动名称">
              <el-input
                v-model="searchName"
                placeholder="请输入活动名称"
                clearable
                @clear="getRegistrations"
                @keyup.enter.native="getRegistrations">
              </el-input>
            </el-form-item>
            <el-form-item label="报名状态">
              <el-select v-model="searchStatus" placeholder="请选择" clearable @change="getRegistrations">
                <el-option label="已报名" value="REGISTERED"></el-option>
                <el-option label="已签到" value="CHECKED_IN"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="getRegistrations" :loading="registrationLoading">
                查询
              </el-button>
              <el-button @click="resetRegistrationSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 报名记录表格 -->
        <el-table :data="registrations" v-loading="registrationLoading">
          <el-table-column prop="activity.name" label="活动名称"></el-table-column>
          <el-table-column prop="activity.location" label="活动地点"></el-table-column>
          <el-table-column prop="activity.contactPerson" label="联系人"></el-table-column>
          <!-- 活动开始时间 -->
          <el-table-column prop="activity.startTime" label="活动开始时间" width="160">
            <template slot-scope="scope">
              {{ formatTime(scope.row.activity.startTime) }}
            </template>
          </el-table-column>
          <!-- 活动结束时间 --> 
          <el-table-column prop="activity.endTime" label="活动结束时间" width="160">
            <template slot-scope="scope">
              {{ formatTime(scope.row.activity.endTime) }}
            </template>
          </el-table-column>
          

          <el-table-column prop="registerTime" label="报名时间" width="160">
            <template slot-scope="scope">
              {{ formatTime(scope.row.registerTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template slot-scope="scope">
              <el-button 
                v-if="scope.row.status === 'REGISTERED' && !isActivityStarted(scope.row.activity)"
                type="text" 
                @click="handleCancel(scope.row)">
                取消报名
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 报名记录分页 -->
        <div class="pagination-container">
          <el-pagination
            background
            @size-change="handleRegistrationSizeChange"
            @current-change="handleRegistrationCurrentChange"
            :current-page="registrationPageNum"
            :page-sizes="[10, 20, 30, 50]"
            :page-size="registrationPageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="registrationTotal">
          </el-pagination>
        </div>
      </el-card>

      <!-- 活动详情弹窗 -->
      <el-dialog
        title="活动详情"
        :visible.sync="dialogVisible"
        width="600px"
        :close-on-click-modal="false">
        <div v-if="currentActivity" class="activity-detail">
          <div class="detail-item">
            <label>活动名称：</label>
            <span>{{ currentActivity.name }}</span>
          </div>
          <div class="detail-item">
            <label>活动地点：</label>
            <span>{{ currentActivity.location }}</span>
          </div>
          <div class="detail-item">
            <label>联系人：</label>
            <span>{{ currentActivity.contactPerson }}</span>
          </div>
          <div class="detail-item">
            <label>联系电话：</label>
            <span>{{ currentActivity.contactPhone }}</span>
          </div>
          <div class="detail-item">
            <label>活动时间：</label>
            <span>{{ formatTime(currentActivity.startTime) }} 至 {{ formatTime(currentActivity.endTime) }}</span>
          </div>
          <div class="detail-item">
            <label>报名情况：</label>
            <span>{{ currentActivity.currentParticipants }}/{{ currentActivity.maxParticipants }}</span>
          </div>
          <div class="detail-item">
            <label>活动描述：</label>
            <div class="description">{{ currentActivity.description }}</div>
          </div>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">关 闭</el-button>
          <el-button 
            type="primary" 
            @click="confirmRegister"
            :loading="submitting"
            :disabled="isActivityFull(currentActivity) || isActivityStarted(currentActivity)">
            确认报名
          </el-button>
        </div>
      </el-dialog>
    </div>

    <page-footer></page-footer>
  </div>
</template>

<script>
import NavHeader from '@/components/front/NavHeader.vue'
import PageFooter from '@/components/front/PageFooter.vue'
import Request from '@/utils/request'
import moment from 'moment'
import { mapGetters } from 'vuex'

export default {
  name: 'Volunteer',
  components: {
    NavHeader,
    PageFooter
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'currentUser', 'ownerInfo']),
  },
  data() {
    return {
      isLogin: true, // 暂时固定为已登录

      loading: false,
      registrationLoading: false,
      activities: [],
      registrations: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      
      // 弹窗相关
      dialogVisible: false,
      submitting: false,
      currentActivity: null,
      
      // 报名记录查询条件
      searchName: '',
      searchStatus: '',
      registrationPageNum: 1,
      registrationPageSize: 10,
      registrationTotal: 0
    }
  },
  created() {
    this.getActivities()
    if (this.isLogin) {
      this.getRegistrations()
    }
  },
  methods: {
    // 获取活动列表
    getActivities() {
      this.loading = true
      Request.get('/volunteer-activity/page', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
      }).then(response => {
        if (response.code === '0' && response.data) {
          this.activities = response.data.records || []
          this.total = response.data.total || 0
        } else {
          this.$message.error(response.msg || '获取活动列表失败')
        }
      }).finally(() => {
        this.loading = false
      })
    },
    
    // 获取报名记录
    getRegistrations() {
      this.registrationLoading = true
      Request.get('/volunteer-registration/page', {
        params: {
          volunteerId: this.ownerInfo.id,
          name: this.searchName,
          status: this.searchStatus,
          pageNum: this.registrationPageNum,
          pageSize: this.registrationPageSize

        }
      }).then(response => {
        if (response.code === '0' && response.data) {
          this.registrations = response.data.records || []
          this.registrationTotal = response.data.total || 0
        } else {
          this.$message.error(response.msg || '获取报名记录失败')
        }
      }).finally(() => {
        this.registrationLoading = false
      })
    },

    // 重置报名记录搜索
    resetRegistrationSearch() {
      this.searchName = ''
      this.searchStatus = ''
      this.registrationPageNum = 1
      this.getRegistrations()
    },

    // 报名活动
    handleRegister(activity) {
      this.currentActivity = activity
      this.dialogVisible = true
    },

    // 确认报名
    confirmRegister() {
      if (!this.currentActivity) return
      
      this.submitting = true
      Request.post(`/volunteer-activity/${this.currentActivity.id}/register?volunteerId=${this.ownerInfo.id}`).then(response => {
        if (response.code === '0') {
          this.$message.success('报名成功')
          this.dialogVisible = false
          this.getActivities()
          this.getRegistrations()
        } else {
          this.$message.error(response.msg || '报名失败')
        }
      }).finally(() => {
        this.submitting = false
      })
    },

    // 取消报名
    handleCancel(registration) {
      this.$confirm('确认取消报名该活动?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.delete(`/volunteer-registration/${registration.id}`).then(response => {
          if (response.code === '0') {
            this.$message.success('已取消报名')
            this.getActivities()
            this.getRegistrations()
          } else {
            this.$message.error(response.msg || '取消失败')
          }
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消操作'
        })
      })
    },

    // 判断活动是否已满
    isActivityFull(activity) {
      return activity && activity.currentParticipants >= activity.maxParticipants
    },

    // 判断活动是否已开始
    isActivityStarted(activity) {
      return activity && moment(activity.startTime).isBefore(moment())
    },

    // 获取操作按钮文字
    getActionText(activity) {
      if (this.isActivityStarted(activity)) return '已开始'
      if (this.isActivityFull(activity)) return '已满员'
      return '报名'
    },

    // 获取状态样式
    getStatusType(status) {
      const map = {
        'REGISTERED': 'warning',
        'CHECKED_IN': 'success'
      }
      return map[status] || 'info'
    },
    
    // 获取状态文本
    getStatusText(status) {
      const map = {
        'REGISTERED': '已报名',
        'CHECKED_IN': '已签到'
      }
      return map[status] || status
    },
    
    // 分页方法
    handleSizeChange(val) {
      this.pageSize = val
      this.getActivities()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getActivities()
    },
    
    // 格式化时间
    formatTime(time) {
      return moment(time).format('YYYY-MM-DD HH:mm')
    },

    // 报名记录分页方法
    handleRegistrationSizeChange(val) {
      this.registrationPageSize = val
      this.getRegistrations()
    },
    handleRegistrationCurrentChange(val) {
      this.registrationPageNum = val
      this.getRegistrations()
    }
  }
}
</script>

<style scoped>
/* 页面样式 */
.volunteer-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.content-wrapper {
  flex: 1;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

/* 页面标题 */
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px;
  font-weight: bold;
}

.page-desc {
  font-size: 14px;
  color: #909399;
}

/* 卡片样式 */
.activity-card,
.registration-card {
  background: #fff;
  padding: 20px;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.activity-card:hover,
.registration-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

/* 表单头部 */
.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
  margin-bottom: 15px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-left i {
  font-size: 18px;
  margin-right: 8px;
  color: #409EFF;
}

.header-left span {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.header-right .total-text {
  color: #909399;
  font-size: 14px;
}

/* 表格样式 */
.el-table {
  margin: 15px 0;
}

.el-table ::v-deep th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 500;
}

.el-table ::v-deep .el-button--text {
  padding: 0;
}

/* 分页容器 */
.pagination-container {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  text-align: right;
}

/* 活动详情样式 */
.activity-detail {
  padding: 0 20px;
}

.detail-item {
  margin-bottom: 15px;
}

.detail-item label {
  font-weight: bold;
  color: #606266;
  margin-right: 10px;
}

.detail-item .description {
  margin-top: 10px;
  color: #606266;
  line-height: 1.6;
}

/* 搜索区域样式 */
.search-area {
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 15px;
}

.search-area .el-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.search-area .el-input {
  width: 220px;
}

.search-area .el-select {
  width: 120px;
}

.search-area .el-form-item {
  margin-bottom: 0;
  margin-right: 10px;
}

.search-area .el-form-item:last-child {
  margin-right: 0;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .content-wrapper {
    padding: 15px;
  }
  
  .activity-card,
  .registration-card {
    padding: 15px;
  }
  
  .form-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-right {
    margin-top: 10px;
  }
  
  .el-dialog {
    width: 95% !important;
    margin: 10px auto !important;
  }
  
  .activity-detail {
    padding: 0 10px;
  }
  
  .search-area .el-form {
    flex-direction: column;
  }
  
  .search-area .el-form-item {
    width: 100%;
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .search-area .el-input,
  .search-area .el-select {
    width: 100%;
  }
  
  .search-area .el-form-item:last-child {
    margin-bottom: 0;
  }
}
</style> 