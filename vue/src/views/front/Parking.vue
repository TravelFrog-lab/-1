<template>
  <div class="parking-page">
    <nav-header></nav-header>
    
    <div class="content-wrapper">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2 class="page-title">停车服务</h2>
        <div class="page-desc">提供便捷的停车查询和缴费服务</div>
      </div>

      <!-- 查询区域 -->
      <el-row :gutter="20">
        <!-- 临时停车查询 -->
        <el-col :span="12">
          <el-card class="search-card" shadow="never">
            <div class="form-header">
              <i class="el-icon-search"></i>
              <span>临时停车查询</span>
            </div>
            <el-form :inline="true" size="small">
              <el-form-item label="车牌号">
                <el-input v-model="plateNumber" placeholder="请输入车牌号"></el-input>
              </el-form-item>
              <el-form-item label="缴费状态">
                <el-select v-model="payStatus" placeholder="请选择" clearable>
                  <el-option label="未缴费" value="UNPAID"></el-option>
                  <el-option label="已缴费" value="PAID"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="searchParkingFee" :loading="loading">
                  查询
                </el-button>
                <el-button @click="resetSearch">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>

        <!-- 车位查询 -->
        <el-col :span="12">
          <el-card class="search-card" shadow="never">
            <div class="form-header">
              <i class="el-icon-location"></i>
              <span>车位查询</span>
            </div>
            <el-form :inline="true" size="small">
              <el-form-item label="车位类型">
                <el-select v-model="spaceType" placeholder="请选择" clearable>
                  <el-option label="公共车位" value="PUBLIC"></el-option>
                  <el-option label="私人车位" value="PRIVATE"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="使用状态">
                <el-select v-model="spaceStatus" placeholder="请选择" clearable>
                  <el-option label="空置" value="0"></el-option>
                  <el-option label="已占用" value="1"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="showSpaceDialog" :loading="spaceLoading">
                  查看车位
                </el-button>
                <el-button @click="resetSpaceSearch">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>

      <!-- 停车费记录 -->
      <el-card v-if="parkingFees && parkingFees.length > 0" class="fee-card" shadow="never">
        <div class="form-header">
          <div class="header-left">
            <i class="el-icon-tickets"></i>
            <span>停车记录</span>
          </div>
          <div class="header-right">
            <span class="total-text">共 {{ total }} 条记录</span>
          </div>
        </div>
        <el-table :data="parkingFees" v-loading="loading">
          <el-table-column prop="plateNumber" label="车牌号">
            <template slot-scope="scope">
              {{ scope.row.plateNumber || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="parkingSpace.location" label="车位位置">
            <template slot-scope="scope">
              {{ scope.row.parkingSpace ? scope.row.parkingSpace.location : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="parkingSpace.code" label="车位编号">
            <template slot-scope="scope">
              {{ scope.row.parkingSpace ? scope.row.parkingSpace.code : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="ownerName" label="车主姓名">
            <template slot-scope="scope">
              {{ scope.row.ownerName || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="ownerPhone" label="车主电话">
            <template slot-scope="scope">
              {{ scope.row.ownerPhone || '-' }}
            </template>
          </el-table-column>
          <!-- 入场时间 -->
          <el-table-column prop="entryTime" label="入场时间" width="160">
            <template slot-scope="scope">
              {{ formatTime(scope.row.entryTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="exitTime" label="出场时间" width="160">
            <template slot-scope="scope">
              {{ formatTime(scope.row.exitTime) }}
            </template>
          </el-table-column>
          <el-table-column label="停车时长" width="100">
            <template slot-scope="scope">
              {{ getParkingDuration(scope.row.entryTime, scope.row.exitTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="feeAmount" label="应付金额" width="100">
            <template slot-scope="scope">
              ￥{{ scope.row.feeAmount }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 'PAID' ? 'success' : 'warning'">
                {{ scope.row.status === 'PAID' ? '已缴费' : '未缴费' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template slot-scope="scope">
              <el-button 
                v-if="scope.row.status === 'UNPAID'"
                type="text" 
                @click="handlePay(scope.row)">
                缴费
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 添加分页 -->
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

      <!-- 私人车位信息 -->
      <el-card class="private-card" shadow="never">
        <div class="form-header">
          <div class="header-left">
            <i class="el-icon-s-order"></i>
            <span>我的车位</span>
          </div>
        </div>
        
        <!-- 未登录提示 -->
        <div v-if="!isLoggedIn" class="not-login-tip">
          <i class="el-icon-warning-outline"></i>
          <span>请登录后查看私人车位信息</span>
        </div>

        <!-- 已登录显示表格 -->
        <el-table 
          v-else
          :data="privateSpaces" 
          v-loading="loading">
          <el-table-column prop="parkingSpace.location" label="车位位置">
          </el-table-column>
          <el-table-column prop="parkingSpace.code" label="车位编号">
          </el-table-column>
          <el-table-column prop="plateInfo" label="车牌号">
            <template slot-scope="scope">
              {{ scope.row.plateInfo || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="登记时间" width="160">
            <template slot-scope="scope">
              {{ formatTime(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" width="160">
            <template slot-scope="scope">
              {{ formatTime(scope.row.updatedAt) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">
                {{ scope.row.status === '0' ? '空置' : '已占用' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 车位列表弹窗 -->
      <el-dialog
        title="车位列表"
        :visible.sync="spaceDialogVisible"
        width="800px"
        :close-on-click-modal="false"
        :before-close="handleSpaceDialogClose">
        <div class="dialog-content">
          <el-table :data="parkingSpaces" v-loading="spaceLoading">
            <el-table-column prop="location" label="车位位置">
            </el-table-column>
              <el-table-column prop="code" label="车位编号">
            </el-table-column>
            <el-table-column prop="type" label="车位类型">
              <template slot-scope="scope">
                {{ scope.row.type === 'PUBLIC' ? '公共车位' : '私人车位' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="使用状态">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">
                  {{ scope.row.status === '0' ? '空置' : '已占用' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              background
              @size-change="handleSpaceSizeChange"
              @current-change="handleSpaceCurrentChange"
              :current-page="spacePageNum"
              :page-sizes="[10, 20, 30, 50]"
              :page-size="spacePageSize"
              layout="total, sizes, prev, pager, next, jumper"
              :total="spaceTotal">
            </el-pagination>
          </div>
        </div>
      </el-dialog>
      
      <!-- 缴费对话框 -->
      <el-dialog title="停车费缴纳确认" :visible.sync="payDialogVisible" width="400px" :close-on-click-modal="false">
        <div class="pay-dialog-content">
          <div class="pay-amount">
            <span class="amount-label">缴费金额</span>
            <span class="amount-value">¥{{ payAmount }}</span>
          </div>
          <div class="pay-info">
            <div class="info-item">
              <i class="el-icon-document"></i>
              <span>订单编号: {{ currentRecord ? currentRecord.orderNo : '' }}</span>
            </div>
            <div class="info-item">
              <i class="el-icon-car"></i>
              <span>车牌号: {{ currentRecord ? currentRecord.plateNumber : '' }}</span>
            </div>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="payDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="payByBalance" :loading="paying">
            余额支付
          </el-button>
        </span>
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
  name: 'Parking',
  components: {
    NavHeader,
    PageFooter
  },
  data() {
    return {
      loading: false,
      plateNumber: '',
      payStatus: '', // 缴费状态查询条件
      parkingFees: [],
      privateSpaces: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      
      // 车位查询相关
      spaceType: '',
      spaceStatus: '', // 改为数字类型
      spaceLoading: false,
      parkingSpaces: [],
      spacePageNum: 1,
      spacePageSize: 10,
      spaceTotal: 0,
      spaceDialogVisible: false, // 车位弹窗显示控制
      
      // 支付相关
      payDialogVisible: false,
      payAmount: 0,
      paying: false,
      currentRecord: null,
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'currentUser', 'ownerInfo']),
  },
  created() {
    // 只在登录状态下获取私人车位
    if (this.isLoggedIn) {
      this.getPrivateSpaces()
    }
  },
  methods: {
    // 查询停车费
    searchParkingFee() {
      if (!this.plateNumber) {
        this.$message.warning('请输入车牌号')
        return
      }
      this.loading = true
      this.parkingFees = [] // 查询前清空数组
      Request.get('/parking-fee/page', {
        params: {
          plateNumber: this.plateNumber,
          status: this.payStatus,
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
      }).then(response => {
        if (response.code === '0' && response.data) {
          this.parkingFees = response.data.records || []
          this.total = response.data.total || 0
          
          // 添加无数据提示
          if (this.parkingFees.length === 0) {
            this.$message.info('暂无停车记录')
          }
        } else {
          this.$message.error(response.msg || '查询失败')
        }
      }).finally(() => {
        this.loading = false
      })
    },

    // 获取私人车位
    getPrivateSpaces() {
      if (!this.isLoggedIn || !this.ownerInfo?.id) return
      
      this.loading = true
      this.privateSpaces = []
      Request.get('/private-parking/ownerId/' + this.ownerInfo.id)
        .then(response => {
          if (response.code === '0' ) {
            this.privateSpaces = response.data || []
            if (this.privateSpaces.length === 0) {
              this.$message.info('暂无私人车位记录')
            }
          } else {
            this.$message.error(response.msg || '查询失败')
          }
        })
        .finally(() => {
          this.loading = false
        })
    },

    // 格式化时间
    formatTime(time) {
      if (!time) return '-'
      // 处理时间戳
      if (typeof time === 'number') {
        return moment(time).format('YYYY-MM-DD HH:mm:ss')
      }
      // 处理字符串时间
      return moment(time).format('YYYY-MM-DD HH:mm:ss')
    },

    // 计算停车时长
    getParkingDuration(entryTime, exitTime) {
      if (!entryTime) return '-'
      
      const entry = moment(entryTime)
      if (!entry.isValid()) return '-'

      if (!exitTime) {
        // 如果还未出场，显示至今的时长
        const duration = moment.duration(moment().diff(entry))
        const hours = Math.ceil(duration.asHours())
        return `${hours}小时(未出场)`
      } else {
        // 已出场，计算实际停车时长
        const exit = moment(exitTime)
        if (!exit.isValid()) return '-'
        const duration = moment.duration(exit.diff(entry))
        const hours = Math.ceil(duration.asHours())
        return `${hours}小时`
      }
    },

    // 缴费处理
    handlePay(row) {
      this.currentRecord = row;
      this.payAmount = row.feeAmount;
      this.payDialogVisible = true;
    },
    
    // 余额支付
    payByBalance() {
      this.paying = true;
      // 调用直接支付接口
      Request.post(`/parking-fee/${this.currentRecord.id}/pay`).then(response => {
        if (response.code === '0') {
          this.$message.success('余额支付成功！');
          this.payDialogVisible = false;
          this.searchParkingFee(); // 刷新缴费记录
        } else {
          this.$message.error(response.msg || '余额支付失败');
        }
      }).finally(() => {
        this.paying = false;
      });
    },

    // 分页方法
    handleSizeChange(val) {
      this.pageSize = val
      this.searchParkingFee()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.searchParkingFee()
    },

    // 重置查询条件
    resetSearch() {
      // 重置查询条件
      this.plateNumber = ''
      this.payStatus = ''
      this.pageNum = 1
      
      // 清空查询结果
      this.parkingFees = []
      this.total = 0
      
      // 清空加载状态
      this.loading = false
      
      // 显示提示信息
      this.$message.info('已重置查询条件')
    },

    // 查询车位
    searchParkingSpaces() {
      this.spaceLoading = true
      this.parkingSpaces = []
      Request.get('/parking-space/page', {
        params: {
          type: this.spaceType,
          status: this.spaceStatus, // 传递数字值
          pageNum: this.spacePageNum,
          pageSize: this.spacePageSize
        }
      }).then(response => {
        if (response.code === '0' && response.data) {
          this.parkingSpaces = response.data.records || []
          this.spaceTotal = response.data.total || 0
          
          if (this.parkingSpaces.length === 0) {
            this.$message.info('暂无车位信息')
          }
        } else {
          this.$message.error(response.msg || '查询失败')
        }
      }).finally(() => {
        this.spaceLoading = false
      })
    },

    // 重置车位查询
    resetSpaceSearch() {
      this.spaceType = ''
      this.spaceStatus = ''
      this.spacePageNum = 1
      this.parkingSpaces = []
      this.spaceTotal = 0
      this.spaceLoading = false
      this.$message.info('已重置查询条件')
    },

    // 车位分页方法
    handleSpaceSizeChange(val) {
      this.spacePageSize = val
      this.searchParkingSpaces()
    },
    handleSpaceCurrentChange(val) {
      this.spacePageNum = val
      this.searchParkingSpaces()
    },

    // 显示车位弹窗
    showSpaceDialog() {
      this.spaceDialogVisible = true
      this.searchParkingSpaces()
    },

    // 关闭车位弹窗
    handleSpaceDialogClose(done) {
      this.parkingSpaces = []
      this.spaceTotal = 0
      this.spacePageNum = 1
      done()
    },
  }
}
</script>

<style scoped>
/* 页面样式 */
.parking-page {
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
.search-card,
.fee-card,
.private-card,
.space-card {
  background: #fff;
  padding: 20px;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.search-card:hover,
.fee-card:hover,
.private-card:hover,
.space-card:hover {
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

/* 查询表单样式 */
.el-form {
  margin-top: 20px;
}

.el-select {
  width: 120px;
}

.el-form-item {
  margin-bottom: 18px;
  margin-right: 18px;
}

.el-form-item:last-child {
  margin-right: 0;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .content-wrapper {
    padding: 15px;
  }
  
  .search-card,
  .fee-card,
  .private-card,
  .space-card {
    padding: 15px;
  }
  
  .form-header {
    padding-bottom: 12px;
    margin-bottom: 12px;
  }
  
  .el-form {
    display: flex;
    flex-direction: column;
  }
  
  .el-form-item {
    margin-right: 0;
    margin-bottom: 12px;
    width: 100%;
  }
  
  .el-select {
    width: 100%;
  }
  
  .el-button {
    width: 100%;
    margin-bottom: 10px;
  }
  
  .el-button:last-child {
    margin-bottom: 0;
  }
}

/* 查询区域布局 */
.el-row {
  margin-bottom: 20px;
}

@media screen and (max-width: 768px) {
  .el-col {
    width: 100% !important;
    margin-bottom: 15px;
  }
  
  .el-col:last-child {
    margin-bottom: 0;
  }
}

/* 弹窗样式 */
.dialog-content {
  padding: 0 20px;
}

.el-dialog__body {
  padding: 20px 0;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .el-dialog {
    width: 95% !important;
    margin: 10px auto !important;
  }
  
  .dialog-content {
    padding: 0 10px;
  }
}

/* 添加未登录提示样式 */
.not-login-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30px 0;
  color: #909399;
}

.not-login-tip i {
  font-size: 20px;
  margin-right: 8px;
}

.not-login-tip span {
  font-size: 14px;
}

/* 支付对话框样式 */
.pay-dialog-content {
  padding: 20px;
}

.pay-amount {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
}

.amount-label {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.amount-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

.pay-info .info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  color: #606266;
}

.pay-info .info-item i {
  margin-right: 8px;
  color: #909399;
}
</style> 