<template>
  <div class="pet-sitting-page">
    <nav-header></nav-header>
    
    <div class="content-wrapper">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2 class="page-title">宠物代喂服务</h2>
        <div class="page-desc">为您的宠物提供专业的代喂照护服务</div>
      </div>

      <!-- 代喂人员列表 -->
      <el-card class="sitter-card" shadow="never">
        <div class="form-header">
          <div class="header-left">
            <i class="el-icon-user"></i>
            <span>代喂人员</span>
          </div>
        </div>

        <!-- 搜索区域 -->
        <div class="search-area">
          <el-form :inline="true" size="small">
            <el-form-item label="姓名">
              <el-input
                v-model="searchName"
                placeholder="请输入姓名"
                clearable
                @clear="getSitters"
                @keyup.enter.native="getSitters">
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="getSitters" :loading="loading">
                查询
              </el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table :data="sitters" v-loading="loading">
          <el-table-column prop="name" label="姓名"></el-table-column>
          <el-table-column prop="phone" label="联系电话"></el-table-column>
          <el-table-column prop="description" label="服务描述"></el-table-column>
          <el-table-column prop="servicePrice" label="服务价格" width="100">
            <template slot-scope="scope">
              ￥{{ scope.row.servicePrice }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'info'">
                {{ scope.row.status === 'ACTIVE' ? '正常' : '已禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template slot-scope="scope">
              <el-button 
                type="text" 
                @click="handleRequest(scope.row)"
                :disabled="scope.row.status !== 'ACTIVE'">
                申请服务
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

      <!-- 我的请求记录 -->
      <el-card v-if="isLogin" class="request-card" shadow="never">
        <div class="form-header">
          <div class="header-left">
            <i class="el-icon-tickets"></i>
            <span>我的请求</span>
          </div>
          <div class="header-right">
            <span class="total-text">共 {{ requestTotal }} 条记录</span>
          </div>
        </div>

        <el-table :data="requests" v-loading="requestLoading">
          <el-table-column prop="sitter.name" label="代喂人员"></el-table-column>
          <el-table-column prop="sitter.phone" label="联系电话"></el-table-column>
          <el-table-column prop="description" label="服务要求"></el-table-column>
          <el-table-column prop="expectedTime" label="期待服务时间">
            <template slot-scope="scope">
              {{ formatTime(scope.row.expectedTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template slot-scope="scope">
              <!-- 待联系状态 -->
              <template v-if="scope.row.status === 'PENDING'">
                <el-button 
                  type="text" 
                  @click="handleContact(scope.row)">
                  已联系
                </el-button>
                <el-button 
                  type="text" 
                  @click="handleCancel(scope.row)">
                  取消申请
                </el-button>
              </template>
              
              <!-- 已联系状态 -->
              <template v-if="scope.row.status === 'CONTACTED'">
                <el-button 
                  type="text" 
                  @click="handleComplete(scope.row)">
                  确认完成
                </el-button>
                <el-button 
                  type="text" 
                  @click="handleCancel(scope.row)">
                  取消申请
                </el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>

        <!-- 请求记录分页 -->
        <div class="pagination-container">
          <el-pagination
            background
            @size-change="handleRequestSizeChange"
            @current-change="handleRequestCurrentChange"
            :current-page="requestPageNum"
            :page-sizes="[10, 20, 30, 50]"
            :page-size="requestPageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="requestTotal">
          </el-pagination>
        </div>
      </el-card>

      <!-- 申请服务弹窗 -->
      <el-dialog
        title="申请代喂服务"
        :visible.sync="dialogVisible"
        width="500px"
        :close-on-click-modal="false">
        <el-form :model="requestForm" :rules="rules" ref="requestForm" label-width="100px">
          <el-form-item label="服务时间" prop="expectedTime">
            <el-date-picker
              v-model="requestForm.expectedTime"
              type="datetime"
              placeholder="选择期待服务时间"
              :picker-options="pickerOptions">
            </el-date-picker>
          </el-form-item>
          <el-form-item label="服务要求" prop="description">
            <el-input
              type="textarea"
              v-model="requestForm.description"
              :rows="3"
              placeholder="请输入具体的服务要求">
            </el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitRequest" :loading="submitting">
            提 交
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
import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'PetSitting',
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
      requestLoading: false,
      searchName: '',
      sitters: [],
      requests: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      
      // 弹窗相关
      dialogVisible: false,
      submitting: false,
      currentSitter: null,
      requestForm: {
        expectedTime: '',
        description: ''
      },
      
      // 表单校验规则
      rules: {
        expectedTime: [
          { required: true, message: '请选择期待服务时间', trigger: 'change' }
        ],
        description: [
          { required: true, message: '请输入服务要求', trigger: 'blur' }
        ]
      },
      
      // 日期选择器配置
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8.64e7
        }
      },
      
      // 请求记录分页
      requestPageNum: 1,
      requestPageSize: 10,
      requestTotal: 0,
    }
  },
  created() {

    this.getSitters()
    console.log(this.ownerInfo)
    if (this.isLogin) {
      this.getRequests()
    }
  },
  methods: {
    // 获取代喂人员列表
    getSitters() {
      this.loading = true
      Request.get('/pet-sitter/page', {
        params: {
          name: this.searchName,
          status: 'ACTIVE',
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
      }).then(response => {
        if (response.code === '0' && response.data) {
          this.sitters = response.data.records || []
          this.total = response.data.total || 0
          
          if (this.sitters.length === 0) {
            this.$message.info('暂无符合条件的代喂人员')
          }
        } else {
          this.$message.error(response.msg || '获取代喂人员失败')
        }
      }).finally(() => {
        this.loading = false
      })
    },
    
    // 获取请求记录
    getRequests() {
      this.requestLoading = true
      Request.get('/pet-sitting-request/page', {
        params: {
          ownerId: this.ownerInfo.id,
          pageNum: this.requestPageNum,
          pageSize: this.requestPageSize
        }
      }).then(response => {
        if (response.code === '0' && response.data) {
          this.requests = response.data.records || []
          this.requestTotal = response.data.total || 0
          
          if (this.requests.length === 0) {
            this.$message.info('暂无代喂请求记录')
          }
        } else {
          this.$message.error(response.msg || '获取请求记录失败')
        }
      }).finally(() => {
        this.requestLoading = false
      })
    },
    
    // 处理申请服务
    handleRequest(sitter) {
      this.currentSitter = sitter
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.requestForm?.resetFields()
      })
    },
    
    // 提交请求
    submitRequest() {
      this.$refs.requestForm.validate(valid => {
        if (valid) {
          this.submitting = true
          Request.post('/pet-sitting-request', {
            ownerId: this.ownerInfo.id,
            sitterId: this.currentSitter.id,
            description: this.requestForm.description,
            expectedTime: moment(this.requestForm.expectedTime).format('YYYY-MM-DD HH:mm:ss')
          }).then(response => {
            if (response.code === '0') {
              this.$message.success('申请提交成功')
              this.dialogVisible = false
              this.getRequests()
            } else {
              this.$message.error(response.msg || '申请提交失败')
            }
          }).finally(() => {
            this.submitting = false
          })
        }
      })
    },
    
    // 获取状态样式
    getStatusType(status) {
      const map = {
        'PENDING': 'warning',
        'CONTACTED': 'success',
        'COMPLETED': 'info',
        'CANCELLED': 'danger'
      }
      return map[status] || 'info'
    },
    
    // 获取状态文本
    getStatusText(status) {
      const map = {
        'PENDING': '待联系',
        'CONTACTED': '已联系',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return map[status] || status
    },
    
    // 分页方法
    handleSizeChange(val) {
      this.pageSize = val
      this.getSitters()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getSitters()
    },
    formatTime(time) {
      return moment(time).format('YYYY-MM-DD HH:mm:ss')
    },
    
    // 请求记录分页方法
    handleRequestSizeChange(val) {
      this.requestPageSize = val
      this.getRequests()
    },
    handleRequestCurrentChange(val) {
      this.requestPageNum = val
      this.getRequests()
    },

    // 更新请求状态
    updateRequestStatus(row, status, message) {
      this.$confirm(`确认${message}?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.put(`/pet-sitting-request/${row.id}/status?status=${status}`).then(response => {
          if (response.code === '0') {
            this.$message.success('操作成功')
            this.requestPageNum = 1 // 重置页码
            this.getRequests()
          } else {
            this.$message.error(response.msg || '操作失败')
          }
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消操作'
        })
      })
    },

    // 标记已联系
    handleContact(row) {
      this.updateRequestStatus(row, 'CONTACTED', '标记为已联系')
    },

    // 确认完成
    handleComplete(row) {
      this.updateRequestStatus(row, 'COMPLETED', '标记为已完成')
    },

    // 取消申请
    handleCancel(row) {
      this.updateRequestStatus(row, 'CANCELLED', '取消该代喂申请')
    },

    // 重置搜索
    resetSearch() {
      this.searchName = ''
      this.pageNum = 1
      this.getSitters()
    }
  }
}
</script>

<style scoped>
/* 页面样式 */
.pet-sitting-page {
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
.sitter-card,
.request-card {
  background: #fff;
  padding: 20px;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.sitter-card:hover,
.request-card:hover {
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

.search-area .el-form-item {
  margin-bottom: 0;
  margin-right: 10px;
}

.search-area .el-form-item:last-child {
  margin-right: 0;
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

/* 弹窗表单 */
.el-date-picker {
  width: 100%;
}

/* 操作按钮间距 */
.el-button + .el-button {
  margin-left: 10px;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .content-wrapper {
    padding: 15px;
  }
  
  .sitter-card,
  .request-card {
    padding: 15px;
  }
  
  .form-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .search-area .el-form {
    flex-direction: column;
  }
  
  .search-area .el-form-item {
    width: 100%;
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .search-area .el-input {
    width: 100%;
  }
  
  .search-area .el-form-item:last-child {
    margin-bottom: 0;
  }
  
  .search-area .el-form-item:last-child .el-form-item__content {
    display: flex;
    justify-content: flex-end;
  }
  
  .search-area .el-button {
    width: 80px;
  }
  
  .el-dialog {
    width: 95% !important;
    margin: 10px auto !important;
  }
  
  .el-form-item {
    margin-bottom: 15px;
  }
  
  .el-date-picker {
    width: 100%;
  }
  
  .el-button + .el-button {
    margin-left: 5px;
  }
}
</style> 