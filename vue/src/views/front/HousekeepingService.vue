<template>
  <div class="housekeeping-service-page">
    <nav-header></nav-header>
    
    <div class="content-wrapper">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-content">
          <div class="title-section">
            <h2 class="page-title">家政服务</h2>
            <div class="page-desc">为您提供专业的家政服务，让生活更轻松舒适</div>
          </div>
          <div class="header-actions" v-if="isLoggedIn && ownerInfo">
            <el-button type="primary" icon="el-icon-s-order" @click="showMyOrders">我的订单</el-button>
          </div>
        </div>
      </div>

      <!-- 服务类别筛选 -->
      <div class="category-filter">
        <el-radio-group v-model="selectedCategory" @change="handleCategoryChange" size="medium">
          <el-radio-button label="">全部服务</el-radio-button>
          <el-radio-button v-for="category in categories" :key="category" :label="category">
            {{ category }}
          </el-radio-button>
        </el-radio-group>
      </div>

      <!-- 服务项目卡片列表 -->
      <el-row :gutter="20" class="service-list" v-loading="loading">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="service in serviceList" :key="service.id" class="service-item-col">
          <el-card class="service-card" shadow="hover">
            <div class="service-icon">
              <i :class="getServiceIcon(service.category)"></i>
            </div>
            <h3 class="service-name">{{ service.name }}</h3>
            <div class="service-category">{{ service.category }}</div>
            <div class="service-price">
              <span class="price">¥{{ service.basePrice }}</span>
              <span class="unit">/ {{ service.unit }}</span>
            </div>
            <div class="service-desc">{{ service.description }}</div>
            <div class="service-action">
              <el-button type="primary" @click="handleBooking(service)" round>立即预约</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[8, 12, 16, 24]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>

      <!-- 我的订单 -->
      <el-card v-if="isLoggedIn" class="my-orders" shadow="hover">
        <div class="card-header">
          <div class="header-left">
            <i class="el-icon-s-order"></i>
            <span>我的家政订单</span>
          </div>
          <div class="header-right">
            <el-button type="text" @click="loadMyOrders">刷新</el-button>
          </div>
        </div>

        <el-table :data="myOrders" v-loading="orderLoading">
          <el-table-column prop="orderNo" label="订单编号" width="180"></el-table-column>
          <el-table-column label="服务项目" width="150">
            <template slot-scope="scope">
              <div v-if="scope.row.service">
                {{ scope.row.service.name }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="家政人员" width="200">
            <template slot-scope="scope">
              <div v-if="scope.row.housekeeper">
                {{ scope.row.housekeeper.name }}
                <el-tag size="mini" style="margin-left: 5px">
                  {{ scope.row.housekeeper.phone }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="appointmentTime" label="预约时间" width="160">
            <template slot-scope="scope">
              {{ formatDate(scope.row.appointmentTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="订单金额" width="100">
            <template slot-scope="scope">
              {{ scope.row.amount }} 元
            </template>
          </el-table-column>
          <el-table-column prop="status" label="订单状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="paymentStatus" label="支付状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
                {{ getPaymentStatusText(scope.row.paymentStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template slot-scope="scope">
              <el-button
                v-if="scope.row.paymentStatus === 'UNPAID' && scope.row.status !== 'CANCELLED'"
                plain size="mini" type="success" icon="el-icon-money"
                @click="handlePay(scope.row)">支付</el-button>
              <el-button
                v-if="scope.row.status !== 'COMPLETED' && scope.row.status !== 'CANCELLED'"
                plain size="mini" type="danger"
                @click="handleCancel(scope.row)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 订单分页 -->
        <div class="pagination-container">
          <el-pagination
            background
            @size-change="handleOrderSizeChange"
            @current-change="handleOrderCurrentChange"
            :current-page="orderPageNum"
            :page-sizes="[5, 10, 20]"
            :page-size="orderPageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="orderTotal">
          </el-pagination>
        </div>
      </el-card>
    </div>

    <!-- 预约对话框 -->
    <el-dialog
      title="服务预约"
      :visible.sync="bookingVisible"
      width="600px"
      :close-on-click-modal="false">
      <div v-if="currentService" class="booking-form">
        <el-steps :active="bookingStep" finish-status="success" simple style="margin-bottom: 20px">
          <el-step title="选择家政人员"></el-step>
          <el-step title="填写预约信息"></el-step>
          <el-step title="确认订单"></el-step>
        </el-steps>

        <!-- 步骤1：选择家政人员 -->
        <div v-if="bookingStep === 1" class="step-content">
          <div class="step-title">请选择家政人员</div>
          <el-table
            :data="housekeeperList"
            highlight-current-row
            @current-change="handleHousekeeperSelect"
            style="width: 100%">
            <el-table-column prop="name" label="姓名" width="100"></el-table-column>
            <el-table-column prop="phone" label="联系电话" width="120"></el-table-column>
            <el-table-column prop="workYears" label="工作年限" width="100">
              <template slot-scope="scope">
                {{ scope.row.workYears }} 年
              </template>
            </el-table-column>
            <el-table-column prop="rating" label="评分" width="120">
              <template slot-scope="scope">
                <el-rate
                  v-model="scope.row.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                  score-template="{value}">
                </el-rate>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'info'">
                  {{ scope.row.status === 'ACTIVE' ? '在岗' : '休息' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="个人描述">
              <template slot-scope="scope">
                <el-tooltip :content="scope.row.description" placement="top" effect="light">
                  <div class="description-cell">{{ scope.row.description }}</div>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 步骤2：填写预约信息 -->
        <div v-if="bookingStep === 2" class="step-content">
          <div class="step-title">填写预约信息</div>
          <el-form ref="bookingForm" :model="bookingForm" :rules="bookingRules" label-width="100px">
            <el-form-item label="预约时间" prop="appointmentTime">
              <el-date-picker
                v-model="bookingForm.appointmentTime"
                type="datetime"
                placeholder="选择预约时间"
                :picker-options="pickerOptions"
                style="width: 100%">
              </el-date-picker>
            </el-form-item>
            <el-form-item label="服务时长" prop="serviceDuration">
              <el-input-number
                v-model="bookingForm.serviceDuration"
                :min="60"
                :step="30"
                :step-strictly="true"
                @change="calculateAmount"
                style="width: 100%">
              </el-input-number>
              <span class="unit">分钟</span>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input
                type="textarea"
                :rows="3"
                placeholder="请输入备注信息"
                v-model="bookingForm.remark">
              </el-input>
            </el-form-item>
          </el-form>
        </div>

        <!-- 步骤3：确认订单 -->
        <div v-if="bookingStep === 3" class="step-content">
          <div class="step-title">确认订单信息</div>
          <div class="order-summary">
            <div class="summary-item">
              <span class="label">服务项目：</span>
              <span class="value">{{ currentService.name }}</span>
            </div>
            <div class="summary-item">
              <span class="label">家政人员：</span>
              <span class="value">{{ selectedHousekeeper.name }}</span>
            </div>
            <div class="summary-item">
              <span class="label">联系电话：</span>
              <span class="value">{{ selectedHousekeeper.phone }}</span>
            </div>
            <div class="summary-item">
              <span class="label">工作年限：</span>
              <span class="value">{{ selectedHousekeeper.workYears }} 年</span>
            </div>
            <div class="summary-item">
              <span class="label">预约时间：</span>
              <span class="value">{{ formatDate(bookingForm.appointmentTime) }}</span>
            </div>
            <div class="summary-item">
              <span class="label">服务时长：</span>
              <span class="value">{{ bookingForm.serviceDuration }} 分钟</span>
            </div>
            <div class="summary-item">
              <span class="label">订单金额：</span>
              <span class="value price">¥{{ calculatedAmount }}</span>
            </div>
            <div class="summary-item">
              <span class="label">备注信息：</span>
              <span class="value">{{ bookingForm.remark || '无' }}</span>
            </div>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="prevStep" v-if="bookingStep > 1">上一步</el-button>
        <el-button @click="bookingVisible = false">取 消</el-button>
        <el-button type="primary" @click="nextStep" v-if="bookingStep < 3">下一步</el-button>
        <el-button type="primary" @click="submitBooking" v-if="bookingStep === 3" :loading="submitting">提交订单</el-button>
      </div>
    </el-dialog>

    <!-- 添加我的订单对话框 -->
    <el-dialog
      title="我的家政订单"
      :visible.sync="myOrdersVisible"
      width="90%"
      :close-on-click-modal="false">
      <div class="my-orders-dialog">
        <!-- 查询栏 -->
        <el-form ref="orderSearchForm" :inline="true" :model="orderQuery" class="search-form">
          <el-form-item label="订单编号">
            <el-input v-model="orderQuery.orderNo" placeholder="请输入订单编号" size="medium" clearable />
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select v-model="orderQuery.status" placeholder="请选择状态" size="medium" clearable>
              <el-option label="待确认" value="PENDING"></el-option>
              <el-option label="已确认" value="CONFIRMED"></el-option>
              <el-option label="进行中" value="IN_PROGRESS"></el-option>
              <el-option label="已完成" value="COMPLETED"></el-option>
              <el-option label="已取消" value="CANCELLED"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="medium" plain icon="el-icon-search" @click="searchOrders">查询</el-button>
            <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="resetOrderSearch">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 订单表格 -->
        <el-table :data="myOrders" v-loading="orderLoading" style="width: 100%">
          <el-table-column prop="orderNo" label="订单编号" width="180"></el-table-column>
          <el-table-column label="服务项目" width="150">
            <template slot-scope="scope">
              <div v-if="scope.row.service">
                {{ scope.row.service.name }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="家政人员" width="200">
            <template slot-scope="scope">
              <div v-if="scope.row.housekeeper">
                {{ scope.row.housekeeper.name }}
                <el-tag size="mini" style="margin-left: 5px">
                  {{ scope.row.housekeeper.phone }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="appointmentTime" label="预约时间" width="160">
            <template slot-scope="scope">
              {{ formatDate(scope.row.appointmentTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="订单金额" width="100">
            <template slot-scope="scope">
              {{ scope.row.amount }} 元
            </template>
          </el-table-column>
          <el-table-column prop="status" label="订单状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="paymentStatus" label="支付状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
                {{ getPaymentStatusText(scope.row.paymentStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="160">
            <template slot-scope="scope">
              {{ formatDate(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template slot-scope="scope">
              <el-button
                plain size="mini" type="primary" icon="el-icon-view"
                @click="viewOrderDetail(scope.row)">查看</el-button>
              <el-button
                v-if="scope.row.paymentStatus === 'UNPAID' && scope.row.status !== 'CANCELLED'"
                plain size="mini" type="success" icon="el-icon-money"
                @click="handlePay(scope.row)">支付</el-button>
              <el-button
                v-if="scope.row.status !== 'COMPLETED' && scope.row.status !== 'CANCELLED'"
                plain size="mini" type="danger" icon="el-icon-close"
                @click="handleCancel(scope.row)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            background
            @size-change="handleOrderSizeChange"
            @current-change="handleOrderCurrentChange"
            :current-page="orderPageNum"
            :page-sizes="[5, 10, 20]"
            :page-size="orderPageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="orderTotal">
          </el-pagination>
        </div>
      </div>
    </el-dialog>
    
    <!-- 订单详情对话框 -->
    <el-dialog
      title="订单详情"
      :visible.sync="orderDetailVisible"
      width="600px"
      :close-on-click-modal="false"
      append-to-body>
      <div v-if="currentOrder" class="order-detail">
        <div class="detail-header">
          <div class="order-no">订单编号：{{ currentOrder.orderNo }}</div>
          <el-tag :type="getStatusType(currentOrder.status)">
            {{ getStatusText(currentOrder.status) }}
          </el-tag>
        </div>
        
        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="detail-item">
            <span class="label">创建时间：</span>
            <span class="value">{{ formatDate(currentOrder.createdAt) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">预约时间：</span>
            <span class="value">{{ formatDate(currentOrder.appointmentTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">服务时长：</span>
            <span class="value">{{ currentOrder.serviceDuration }} 分钟</span>
          </div>
          <div class="detail-item">
            <span class="label">订单金额：</span>
            <span class="value">{{ currentOrder.amount }} 元</span>
          </div>
          <div class="detail-item">
            <span class="label">支付状态：</span>
            <el-tag size="small" :type="getPaymentStatusType(currentOrder.paymentStatus)">
              {{ getPaymentStatusText(currentOrder.paymentStatus) }}
            </el-tag>
          </div>
        </div>
        
        <div class="detail-section">
          <h4>服务信息</h4>
          <div class="detail-item" v-if="currentOrder.service">
            <span class="label">服务项目：</span>
            <span class="value">{{ currentOrder.service.name }}</span>
          </div>
          <div class="detail-item" v-if="currentOrder.service">
            <span class="label">服务类别：</span>
            <span class="value">{{ currentOrder.service.category }}</span>
          </div>
        </div>
        
        <div class="detail-section">
          <h4>家政人员信息</h4>
          <div class="detail-item" v-if="currentOrder.housekeeper">
            <span class="label">姓名：</span>
            <span class="value">{{ currentOrder.housekeeper.name }}</span>
          </div>
          <div class="detail-item" v-if="currentOrder.housekeeper">
            <span class="label">联系电话：</span>
            <span class="value">{{ currentOrder.housekeeper.phone }}</span>
          </div>
          <div class="detail-item" v-if="currentOrder.housekeeper">
            <span class="label">工作年限：</span>
            <span class="value">{{ currentOrder.housekeeper.workYears }} 年</span>
          </div>
        </div>
        
        <div class="detail-section" v-if="currentOrder.remark">
          <h4>备注信息</h4>
          <div class="detail-item">
            <div class="value">{{ currentOrder.remark }}</div>
          </div>
        </div>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="orderDetailVisible = false">关闭</el-button>
        <el-button 
          v-if="currentOrder && currentOrder.paymentStatus === 'UNPAID' && currentOrder.status !== 'CANCELLED'"
          type="success" 
          @click="handlePay(currentOrder)">支付订单</el-button>
        <el-button 
          v-if="currentOrder && currentOrder.status !== 'COMPLETED' && currentOrder.status !== 'CANCELLED'"
          type="danger" 
          @click="handleCancel(currentOrder)">取消订单</el-button>
      </div>
    </el-dialog>

    <!-- 支付对话框 -->
    <el-dialog 
      title="家政服务订单支付" 
      :visible.sync="payDialogVisible" 
      width="400px" 
      :close-on-click-modal="false"
      append-to-body>
      <div class="pay-dialog-content">
        <div class="pay-amount">
          <span class="amount-label">支付金额</span>
          <span class="amount-value">¥{{ currentOrder ? currentOrder.amount : '0.00' }}</span>
        </div>
        <div class="pay-info">
          <div class="info-item">
            <i class="el-icon-document"></i>
            <span>订单编号: {{ currentOrder ? currentOrder.orderNo : '' }}</span>
          </div>
          <div class="info-item" v-if="currentOrder && currentOrder.service">
            <i class="el-icon-s-cooperation"></i>
            <span>服务项目: {{ currentOrder.service.name }}</span>
          </div>
          <div class="info-item">
            <i class="el-icon-date"></i>
            <span>预约时间: {{ currentOrder ? formatDate(currentOrder.appointmentTime) : '' }}</span>
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

    <page-footer></page-footer>
  </div>
</template>

<style lang="less" scoped>
.housekeeping-service-page {
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

/* 类别筛选 */
.category-filter {
  margin-bottom: 24px;
  text-align: center;
}

/* 服务卡片列表 */
.service-list {
  margin-bottom: 24px;
}

.service-item-col {
  margin-bottom: 20px;
}

.service-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
  padding: 20px;
}

.service-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.service-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 16px;
  text-align: center;
}

.service-name {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 8px;
  text-align: center;
}

.service-category {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
  text-align: center;
}

.service-price {
  margin-bottom: 16px;
  text-align: center;
}

.price {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

.unit {
  font-size: 14px;
  color: #909399;
}

.service-desc {
  flex: 1;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.service-action {
  text-align: center;
}

/* 分页容器 */
.pagination-container {
  margin: 24px 0;
  text-align: center;
}

/* 我的订单 */
.my-orders {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
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

/* 预约表单 */
.step-content {
  padding: 20px 0;
}

.step-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 20px;
}

.description-cell {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

/* 订单确认 */
.order-summary {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 4px;
}

.summary-item {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.summary-item .label {
  width: 100px;
  color: #909399;
}

.summary-item .value {
  flex: 1;
  color: #303133;
}

.summary-item .value.price {
  color: #f56c6c;
  font-weight: bold;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .content-wrapper {
    padding: 15px;
  }

  .service-card {
    margin-bottom: 15px;
  }

  .el-dialog {
    width: 95% !important;
    margin: 10px auto !important;
  }

  .step-content {
    padding: 15px 0;
  }

  .order-summary {
    padding: 15px;
  }
}

/* 添加新样式 */
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title-section {
  flex: 1;
}

.my-orders-dialog {
  .search-form {
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid #ebeef5;
  }
}

.order-detail {
  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #ebeef5;
    
    .order-no {
      font-size: 16px;
      font-weight: bold;
      color: #303133;
    }
  }
  
  .detail-section {
    margin-bottom: 20px;
    
    h4 {
      margin: 0 0 10px 0;
      font-size: 15px;
      color: #303133;
      padding-left: 8px;
      border-left: 3px solid #409EFF;
    }
    
    .detail-item {
      margin-bottom: 8px;
      display: flex;
      align-items: center;
      
      .label {
        width: 100px;
        color: #909399;
      }
      
      .value {
        flex: 1;
        color: #303133;
      }
    }
  }
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
  
  i {
    margin-right: 8px;
    color: #909399;
  }
}
</style>

<script>
import NavHeader from '@/components/front/NavHeader.vue'
import PageFooter from '@/components/front/PageFooter.vue'
import Request from '@/utils/request'
import moment from 'moment'
import { mapGetters } from 'vuex'

export default {
  name: 'HousekeepingService',
  components: {
    NavHeader,
    PageFooter
  },
  data() {
    return {
      // 服务列表相关
      loading: false,
      pageNum: 1,
      pageSize: 8,
      total: 0,
      serviceList: [],
      selectedCategory: '',
      categories: ['清洁服务', '照护服务', '烹饪服务', '维修服务', '其他服务'],

      // 预约相关
      bookingVisible: false,
      bookingStep: 1,
      currentService: null,
      selectedHousekeeper: null,
      housekeeperList: [],
      bookingForm: {
        housekeeperId: null,
        serviceId: null,
        appointmentTime: null,
        serviceDuration: 60,
        remark: ''
      },
      bookingRules: {
        appointmentTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }],
        serviceDuration: [{ required: true, message: '请输入服务时长', trigger: 'blur' }]
      },

      // 我的订单相关
      orderLoading: false,
      orderPageNum: 1,
      orderPageSize: 5,
      orderTotal: 0,
      myOrders: [],

      // 其他数据
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8.64e7; // 禁用过去的日期
        }
      },

      // 添加我的订单对话框相关数据
      myOrdersVisible: false,
      orderDetailVisible: false,
      currentOrder: null,
      orderQuery: {
        orderNo: '',
        status: ''
      },

      // 支付相关
      payDialogVisible: false,
      paying: false
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'currentUser', 'ownerInfo']),
    calculatedAmount() {
      if (!this.currentService || !this.bookingForm.serviceDuration) return 0;
      const duration = this.bookingForm.serviceDuration;
      const price = this.selectedHousekeeper?.price || this.currentService.basePrice;
      if (this.currentService.unit === '小时') {
        return Math.ceil(duration / 60) * price;
      } else if (this.currentService.unit === '次') {
        return price;
      }
      return 0;
    }
  },
  created() {
    this.fetchServices();
    if (this.isLoggedIn && this.ownerInfo) {
      this.loadMyOrders();
    }
  },
  methods: {
    // 获取服务列表
    async fetchServices() {
      try {
        this.loading = true;
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          category: this.selectedCategory
        };
        const response = await Request.get('/housekeeping-service/page', { params });
        if (response.code === '0') {
          this.serviceList = response.data.records;
          this.total = response.data.total;
        }
      } catch (error) {
        this.$message.error('获取服务列表失败');
      } finally {
        this.loading = false;
      }
    },

    // 获取家政人员列表
    async loadHousekeepers(serviceId) {
      try {
        const response = await Request.get(`/housekeeper-service/service/${serviceId}`);
        if (response.code === '0') {
          // 处理返回的数据，提取家政人员信息和价格
          this.housekeeperList = response.data.map(item => {
            // 将家政人员信息与价格合并
            return {
              ...item.housekeeper,
              price: item.price,
              relationId: item.id // 保存关联ID，可能在后续操作中需要
            };
          });
        }
      } catch (error) {
        this.$message.error('获取家政人员列表失败');
      }
    },

    // 处理预约
    handleBooking(service) {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录');
        this.$router.push('/login');
        return;
      }
      this.currentService = service;
      this.bookingVisible = true;
      this.bookingStep = 1;
      this.loadHousekeepers(service.id);
      this.resetBookingForm();
    },

    // 处理家政人员选择
    handleHousekeeperSelect(housekeeper) {
      this.selectedHousekeeper = housekeeper;
    },

    // 下一步
    async nextStep() {
      if (this.bookingStep === 1 && !this.selectedHousekeeper) {
        this.$message.warning('请选择家政人员');
        return;
      }
      if (this.bookingStep === 2) {
        try {
          await this.$refs.bookingForm.validate();
        } catch (error) {
          return;
        }
      }
      this.bookingStep++;
    },

    // 提交预约
    async submitBooking() {
      try {
        // 检查是否有业主信息
        if (!this.ownerInfo || !this.ownerInfo.id) {
          this.$message.error('未找到业主信息，请先完善业主资料');
          return;
        }

        // 格式化日期时间，移除时区信息
        const formattedDate = this.bookingForm.appointmentTime ? 
          moment(this.bookingForm.appointmentTime).format('YYYY-MM-DDTHH:mm:ss') : null;
        
        const order = {
          ownerId: this.ownerInfo.id, // 添加业主ID
          housekeeperId: this.selectedHousekeeper.id,
          serviceId: this.currentService.id,
          appointmentTime: formattedDate,
          serviceDuration: this.bookingForm.serviceDuration,
          remark: this.bookingForm.remark,
          amount: this.calculatedAmount
        };
        
        const response = await Request.post('/housekeeping-order', order);
        if (response.code === '0') {
          this.$message.success('预约成功');
          this.bookingVisible = false;
          this.loadMyOrders();
        } else {
          this.$message.error(response.msg || '预约失败');
        }
      } catch (error) {
        console.error('预约失败:', error);
        this.$message.error('预约失败');
      }
    },

    // 加载我的订单
    async loadMyOrders() {
      try {
        // 检查是否有业主信息
        if (!this.ownerInfo || !this.ownerInfo.id) {
          console.warn('未找到业主信息，无法加载订单');
          return;
        }

        this.orderLoading = true;
        const params = {
          pageNum: this.orderPageNum,
          pageSize: this.orderPageSize,
          ownerId: this.ownerInfo.id,
          orderNo: this.orderQuery.orderNo || undefined,
          status: this.orderQuery.status || undefined
        };
        const response = await Request.get('/housekeeping-order/page', { params });
        if (response.code === '0') {
          this.myOrders = response.data.records;
          this.orderTotal = response.data.total;
        }
      } catch (error) {
        this.$message.error('获取订单列表失败');
      } finally {
        this.orderLoading = false;
      }
    },

    // 其他辅助方法
    getServiceIcon(category) {
      const iconMap = {
        '清洁服务': 'el-icon-brush',
        '照护服务': 'el-icon-user',
        '烹饪服务': 'el-icon-dish',
        '维修服务': 'el-icon-tools',
        '其他服务': 'el-icon-more'
      };
      return iconMap[category] || 'el-icon-service';
    },

    formatDate(date) {
      return moment(date).format('YYYY-MM-DD HH:mm');
    },

    handleCategoryChange() {
      this.pageNum = 1;
      this.fetchServices();
    },

    handleSizeChange(val) {
      this.pageSize = val;
      this.fetchServices();
    },

    handleCurrentChange(val) {
      this.pageNum = val;
      this.fetchServices();
    },

    resetBookingForm() {
      this.bookingForm = {
        housekeeperId: null,
        serviceId: this.currentService?.id,
        appointmentTime: null,
        serviceDuration: 60,
        remark: ''
      };
      this.selectedHousekeeper = null;
    },

    // 添加订单分页相关的方法
    handleOrderSizeChange(val) {
      this.orderPageSize = val;
      this.loadMyOrders();
    },

    handleOrderCurrentChange(val) {
      this.orderPageNum = val;
      this.loadMyOrders();
    },

    // 添加状态相关的方法
    getStatusType(status) {
      const statusMap = {
        'PENDING': 'warning',
        'CONFIRMED': 'primary',
        'IN_PROGRESS': 'success',
        'COMPLETED': 'success',
        'CANCELLED': 'info'
      };
      return statusMap[status] || 'info';
    },

    getStatusText(status) {
      const statusMap = {
        'PENDING': '待确认',
        'CONFIRMED': '已确认',
        'IN_PROGRESS': '进行中',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      };
      return statusMap[status] || '未知状态';
    },

    getPaymentStatusType(status) {
      const statusMap = {
        'UNPAID': 'warning',
        'PAID': 'success',
        'REFUNDED': 'info'
      };
      return statusMap[status] || 'info';
    },

    getPaymentStatusText(status) {
      const statusMap = {
        'UNPAID': '未支付',
        'PAID': '已支付',
        'REFUNDED': '已退款'
      };
      return statusMap[status] || '未知状态';
    },

    // 显示支付对话框
    handlePay(order) {
      this.currentOrder = order;
      this.payDialogVisible = true;
    },
    
    // 余额支付
    payByBalance() {
      this.paying = true;
      Request.put(`/housekeeping-order/${this.currentOrder.id}/pay`).then(response => {
        if (response.code === '0') {
          this.$message.success('支付成功');
          this.payDialogVisible = false;
          this.loadMyOrders();
        } else {
          this.$message.error(response.msg || '支付失败');
        }
      }).finally(() => {
        this.paying = false;
      });
    },
    
    // 显示我的订单对话框
    showMyOrders() {
      this.myOrdersVisible = true;
      this.loadMyOrders();
    },
    
    // 查看订单详情
    viewOrderDetail(order) {
      this.currentOrder = order;
      this.orderDetailVisible = true;
    },
    
    // 搜索订单
    searchOrders() {
      this.orderPageNum = 1;
      this.loadMyOrders();
    },
    
    // 重置订单搜索
    resetOrderSearch() {
      this.orderQuery = {
        orderNo: '',
        status: ''
      };
      this.orderPageNum = 1;
      this.loadMyOrders();
    },

    // 取消订单
    async handleCancel(order) {
      try {
        await this.$confirm('确认取消该订单吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });

        const response = await Request.put(`/housekeeping-order/${order.id}/cancel`);
        if (response.code === '0') {
          this.$message.success('订单已取消');
          this.loadMyOrders();
        } else {
          this.$message.error(response.msg || '取消失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('取消失败');
        }
      }
    }
  }
}
</script> 