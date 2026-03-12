<template>
  <div class="property-fee-page">
    <nav-header></nav-header>

    <div class="content-wrapper">
      <div class="fee-container">
        <div class="page-header">
          <h2 class="page-title">物业费缴纳</h2>
          <div class="page-desc">在线查询和缴纳物业费，支持实时支付</div>
        </div>

        <!-- 房屋信息卡片 -->
        <el-card class="house-info" v-if="houseInfo" shadow="hover">
          <div class="house-info-header">
            <i class="el-icon-office-building"></i>
            <span>我的房屋</span>
          </div>
          <div class="house-info-content">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="info-item">
                  <span class="label">房屋地址</span>
                  <span class="value">{{ houseInfo.address }}</span>
                </div>
                <div class="info-item">
                  <span class="label">房屋类型</span>
                  <span class="value">{{ houseInfo.typeName }}</span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-item">
                  <span class="label">建筑面积</span>
                  <span class="value">{{ houseInfo.area }} ㎡</span>
                </div>
                <div class="info-item">
                  <span class="label">物业单价</span>
                  <span class="value">{{ houseInfo.unitPrice }} 元/㎡/月</span>
                </div>
              </el-col>
            </el-row>
            <div class="fee-calc">
              <div class="calc-title">物业费计算方式：</div>
              <div class="calc-formula">
                {{ houseInfo.area }} ㎡ × {{ houseInfo.unitPrice }} 元/㎡/月 = {{ currentMonthFee }} 元/月
              </div>
              <div class="calc-note">注：物业费按月计算，每月账单由管理员生成</div>
            </div>
          </div>
        </el-card>

        <!-- 费用概览卡片 -->
        <el-row :gutter="20" class="fee-stats">
          <el-col :span="8">
            <el-card shadow="hover" class="fee-stat-card">
              <div class="stat-icon current-month">
                <i class="el-icon-money"></i>
              </div>
              <div class="stat-info">
                <div class="stat-label">本月应缴</div>
                <div class="stat-value">¥{{ currentMonthFee || '0.00' }}</div>
                <div class="stat-note">以实际账单为准</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="fee-stat-card">
              <div class="stat-icon unpaid">
                <i class="el-icon-warning"></i>
              </div>
              <div class="stat-info">
                <div class="stat-label">未缴总额</div>
                <div class="stat-value warning">¥{{ unpaidTotal || '0.00' }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="fee-stat-card">
              <div class="stat-icon paid">
                <i class="el-icon-circle-check"></i>
              </div>
              <div class="stat-info">
                <div class="stat-label">已缴总额</div>
                <div class="stat-value success">¥{{ paidTotal || '0.00' }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 缴费记录表格 -->
        <el-card class="fee-records" shadow="hover">
          <div slot="header" class="card-header">
            <div class="header-left">
              <i class="el-icon-document"></i>
              <span>缴费记录</span>
            </div>
          </div>

          <el-table :data="feeRecords" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNo" label="订单编号" width="180"></el-table-column>
            <el-table-column prop="feeDate" label="费用月份" width="120">
              <template slot-scope="scope">
                {{ scope.row.feeDate | formatDate }}
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="费用金额" width="120">
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
            <el-table-column prop="paymentTime" label="缴费时间" width="180">
              <template slot-scope="scope">
                {{ scope.row.paymentTime ? formatDateTime(scope.row.paymentTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注"></el-table-column>
            <el-table-column label="操作" width="180" align="center">
              <template slot-scope="scope">
                <el-button v-if="scope.row.status === 'UNPAID'" type="text" @click="handlePaySingle(scope.row)">
                  缴费
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination background @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="pageNum"
              :page-sizes="[10, 20, 30, 50]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper" :total="total">
            </el-pagination>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 缴费对话框 -->
    <el-dialog title="支付方式选择" :visible.sync="payDialogVisible" width="400px" :close-on-click-modal="false">
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
            <i class="el-icon-house"></i>
            <span>{{ houseInfo ? houseInfo.address : '' }}</span>
          </div>
          <div class="info-item">
            <i class="el-icon-date"></i>
            <span>{{ payMonth }}</span>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="payDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="payByBalance" :loading="paying">
          支付宝支付
        </el-button>
      </span>
    </el-dialog>

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
  name: 'PropertyFee',
  components: {
    NavHeader,
    PageFooter
  },
  data() {
    return {
      loading: false,

      houseInfo: null,
      currentMonthFee: 0,
      unpaidTotal: 0,
      paidTotal: 0,
      feeRecords: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      payDialogVisible: false,
      payAmount: 0,
      payMonth: '',
      paying: false,
      currentRecord: null
    }
  },
  filters: {
    formatDate(date) {
      return moment(date).format('YYYY-MM')
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'currentUser', 'ownerInfo']),
  },
  created() {
    this.getHouseInfo()
    this.getFeeRecords()
  },
  methods: {
    // 格式化缴费时间显示
    formatDateTime(dateStr) {
      if (!dateStr) return '-'
      return moment(dateStr).format('YYYY-MM-DD HH:mm')
    },
    // 获取业主和房屋信息
    getHouseInfo() {
      Request.get('/owner/' + this.ownerInfo.id).then(response => {
        if (response.code === '0') {
          const owner = response.data
          const house = owner.house || {}
          const houseType = house.houseType || {}
          const area = house.area != null ? Number(house.area) : 0
          const unitPrice = houseType.propertyFee != null ? Number(houseType.propertyFee) : 0
          this.houseInfo = {
            address: house.address || '暂无',
            area,
            typeName: houseType.name || '暂无信息',
            unitPrice
          }
          // 计算本月应缴费用
          this.currentMonthFee = area * unitPrice
        }
      })
    },
    // 获取缴费记录
    getFeeRecords() {
      this.loading = true
      Request.get('/property-fees/page', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          ownerId: this.ownerInfo.id
        }
      }).then(response => {
        if (response.code === '0') {
          this.feeRecords = response.data.records
          this.total = response.data.total
          this.calculateTotals()
        }
      }).finally(() => {
        this.loading = false
      })
    },
    // 计算总额
    calculateTotals() {
      if (!this.feeRecords) {
        this.paidTotal = 0;
        this.unpaidTotal = 0;
        return;
      }

      this.paidTotal = this.feeRecords
        .filter(item => item.status === 'PAID')
        .reduce((sum, item) => sum + item.amount, 0)

      this.unpaidTotal = this.feeRecords
        .filter(item => item.status === 'UNPAID')
        .reduce((sum, item) => sum + item.amount, 0)
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getFeeRecords()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getFeeRecords()
    },
    handlePaySingle(record) {
      this.currentRecord = record
      this.payAmount = record.amount
      this.payMonth = moment(record.feeDate).format('YYYY-MM')
      this.payDialogVisible = true
    },
    // 跳转到支付宝支付页面
    payByBalance() {
      // 关闭对话框
      this.payDialogVisible = false;
      // 有订单号用订单号，没有则用 ID:xx 供后端按 id 更新（物业费 order_no 可能为空）
      const businessOrderNo = this.currentRecord.orderNo
        ? this.currentRecord.orderNo
        : 'ID:' + this.currentRecord.id;
      this.$router.push({
        path: '/alipay-payment',
        query: {
          amount: this.currentRecord.amount,
          subject: `物业费缴纳 - ${this.payMonth}`,
          businessType: 'PROPERTY_FEE',
          businessOrderNo
        }
      });
    }
  }
}
</script>

<style scoped>
.property-fee-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.content-wrapper {
  flex: 1;
  padding: 20px;
}

.fee-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px;
}

.page-desc {
  font-size: 14px;
  color: #909399;
}

.house-info {
  margin-bottom: 24px;
  background-color: #fafafa;
}

.house-info-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.house-info-header i {
  font-size: 20px;
  margin-right: 8px;
  color: #409eff;
}

.house-info-header span {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.house-info-content {
  padding: 0 10px;
}

.info-item {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
}

.info-item .label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.info-item .value {
  font-size: 15px;
  color: #303133;
  font-weight: 500;
}

.fee-calc {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e0e0e0;
}

.calc-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.calc-formula {
  font-size: 15px;
  color: #409eff;
  font-weight: 500;
  padding: 8px 12px;
  background-color: #ecf5ff;
  border-radius: 4px;
  display: inline-block;
}

.calc-note {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.fee-stats {
  margin-bottom: 24px;
}

.fee-stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  height: 120px; /* 设置固定高度 */
}

.fee-stat-card ::v-deep .el-card__body {
  padding: 0 100px !important;
  width: 100% !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon i {
  font-size: 24px;
  color: #fff;
}

.stat-icon.current-month {
  background-color: #409eff;
}

.stat-icon.unpaid {
  background-color: #e6a23c;
}

.stat-icon.paid {
  background-color: #67c23a;
}

.stat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center; /* 居中对齐内容 */
}

.stat-label {
  font-size: 14px;
  text-align: center;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.stat-note {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.card-header {
  padding: 0;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-left i {
  margin-right: 8px;
  color: #409eff;
}

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

.el-table {
  margin: 16px 0;
}

.pagination-container {
  padding: 16px 0;
  text-align: right;
}

.el-card.is-hover-shadow:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
  transition: all 0.3s;
}

/* 适配移动端 */
@media screen and (max-width: 768px) {
  .el-col {
    width: 100% !important;
  }

  .info-item {
    margin-bottom: 12px;
  }

  .fee-calc {
    margin-top: 12px;
  }

  .calc-formula {
    font-size: 14px;
    padding: 6px 10px;
  }
}
</style> 