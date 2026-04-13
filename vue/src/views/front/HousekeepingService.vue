<template>
  <div class="housekeeping-service-page">
    <div class="content-wrapper">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-content">
          <div class="title-section">
            <h2 class="page-title">家政服务</h2>
            <div class="page-desc">为您提供专业的家政服务，让生活更轻松舒适</div>
          </div>
        </div>
      </div>

      <!-- 服务项目卡片：大屏一行 5 张均分；中屏 3+2（第二行两张居中）；小屏自适应 -->
      <div class="service-grid-wrap" v-loading="loading">
        <div class="service-grid">
          <el-card
            v-for="service in serviceList"
            :key="service.id"
            class="service-card"
            shadow="hover">
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
        </div>
      </div>

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

      <!-- 我的家政订单 -->
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

        <el-tabs v-model="housekeepingOrderTab" class="housekeeping-order-tabs">
          <el-tab-pane label="待处理" name="pending" />
          <el-tab-pane label="处理中" name="progress" />
          <el-tab-pane label="待评价" name="to_evaluate" />
          <el-tab-pane label="已完成" name="done" />
        </el-tabs>

        <el-table :data="orderRowsForTab" v-loading="orderLoading" :empty-text="housekeepingOrderListEmptyText">
          <!-- 预约时间 / 家政人员 / 状态 / 金额 / 支付等均在「查看详情」弹窗中展示 -->
          <el-table-column label="服务项目" min-width="180" show-overflow-tooltip>
            <template slot-scope="scope">
              <span v-if="scope.row.service">{{ scope.row.service.name }}</span>
              <span v-else>—</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="220" align="center">
            <template slot-scope="scope">
              <el-button type="primary" size="small" plain @click="viewOrderDetail(scope.row)">
                查看详情
              </el-button>
              <el-button
                v-if="canPayHousekeepingOrder(scope.row) && housekeepingOrderTab === 'to_evaluate'"
                plain size="mini" type="success" icon="el-icon-money"
                @click="handlePay(scope.row)">支付</el-button>
              <el-button
                v-if="canCancelHousekeepingOrder(scope.row)"
                plain size="mini" type="danger"
                @click="handleCancel(scope.row)">取消</el-button>
              <span v-if="showRefundHint(scope.row)" class="refund-hint">如需退款请联系物业</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 订单分页 -->
        <div class="pagination-container orders-pagination">
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
            <el-table-column prop="rating" label="评分" width="100" align="center">
              <template slot-scope="scope">
                <span
                  v-if="housekeeperDisplayRating(scope.row) != null"
                  class="rating-num">{{ housekeeperDisplayRating(scope.row) }} 分</span>
                <span v-else class="rating-empty">暂无评分</span>
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
          <el-form ref="bookingForm" :model="bookingForm" :rules="bookingFormRules" label-width="100px">
            <el-form-item label="预约时间" prop="appointmentTime">
              <el-date-picker
                v-model="bookingForm.appointmentTime"
                type="datetime"
                placeholder="选择预约时间"
                :picker-options="pickerOptions"
                style="width: 100%">
              </el-date-picker>
            </el-form-item>
            <!-- 按小时：填写分钟数；按次/其它：与后端一致不按时长计价，不展示分钟 -->
            <el-form-item v-if="isHourlyService" label="服务时长" prop="serviceDuration">
              <el-input-number
                v-model="bookingForm.serviceDuration"
                :min="60"
                :step="30"
                :step-strictly="true"
                style="width: 100%">
              </el-input-number>
              <span class="unit">分钟</span>
            </el-form-item>
            <el-form-item v-else label="计费说明">
              <span class="pricing-hint">
                本服务按「<strong>{{ currentService.unit }}</strong>」计费，金额为单次价格，无需填写服务时长。
              </span>
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
            <div v-if="isHourlyService" class="summary-item">
              <span class="label">服务时长：</span>
              <span class="value">{{ bookingForm.serviceDuration }} 分钟</span>
            </div>
            <div v-else class="summary-item">
              <span class="label">计费方式：</span>
              <span class="value">按{{ currentService.unit }}计费（单次）</span>
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

    <!-- 订单详情（金额、支付、创建时间、家政电话等见弹窗，与报修「查看详情」一致） -->
    <el-dialog
      title="订单详情"
      :visible.sync="orderDetailVisible"
      width="640px"
      class="housekeeping-order-detail-dialog"
      :close-on-click-modal="false"
      append-to-body
    >
      <div v-if="currentOrder" class="order-detail-body">
        <el-descriptions :column="2" border size="small" class="hk-order-detail-desc">
          <el-descriptions-item label="订单编号" :span="2">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getOrderStatusTagType(currentOrder)">{{ getOrderStatusLabel(currentOrder) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag size="small" :type="getPaymentStatusType(currentOrder.paymentStatus)">
              {{ getPaymentStatusText(currentOrder.paymentStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(currentOrder.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="预约时间">{{ formatDate(currentOrder.appointmentTime) }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">{{ currentOrder.amount }} 元</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.service && currentOrder.service.unit === '小时'" label="服务时长">
            {{ currentOrder.serviceDuration }} 分钟
          </el-descriptions-item>
          <el-descriptions-item v-else-if="currentOrder.service" label="计费方式">
            按{{ currentOrder.service.unit }}计费（单次）
          </el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.service" label="服务项目">{{ currentOrder.service.name }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.service" label="服务类别">{{ currentOrder.service.category }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.housekeeper" label="家政人员">{{ currentOrder.housekeeper.name }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.housekeeper" label="联系电话">{{ currentOrder.housekeeper.phone }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.housekeeper" label="工作年限" :span="2">
            {{ currentOrder.housekeeper.workYears }} 年
          </el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.remark" label="备注" :span="2">{{ currentOrder.remark }}</el-descriptions-item>
        </el-descriptions>

        <!-- 完工后评价（与报修服务一致：星级 + 文字，仅一次） -->
        <div v-if="currentOrder.status === 'COMPLETED'" class="hk-detail-eval-block">
          <el-divider content-position="left">服务评价</el-divider>
          <p v-if="!currentOrder.evaluationRating" class="eval-flow-hint">
            服务已完工、尚未评价时，该单会出现在「待评价」列表。提交评价后，订单将进入「已完成」。
          </p>
          <div v-if="currentOrder.evaluationRating" class="eval-readonly">
            <div class="eval-stars-row">
              <span class="eval-label">星级</span>
              <el-rate :value="currentOrder.evaluationRating" disabled show-score text-color="#ff9900" score-template="{value} 分" />
            </div>
            <p class="eval-text">{{ currentOrder.evaluation || '—' }}</p>
          </div>
          <el-form v-else ref="evaluateForm" :model="evaluateForm" :rules="evaluateRules" label-width="80px">
            <el-form-item label="星级" prop="evaluationRating">
              <el-rate
                v-model="evaluateForm.evaluationRating"
                :max="5"
                show-text
                :texts="['很差', '较差', '一般', '满意', '非常满意']"
              />
            </el-form-item>
            <el-form-item label="评价" prop="evaluation">
              <el-input
                v-model="evaluateForm.evaluation"
                type="textarea"
                :rows="4"
                maxlength="500"
                show-word-limit
                placeholder="请分享本次家政服务体验"
              />
            </el-form-item>
          </el-form>
        </div>

        <el-steps
          v-if="currentOrder && currentOrder.status !== 'CANCELLED'"
          :active="housekeepingDetailStepActive"
          finish-status="success"
          align-center
          class="hk-order-progress-steps"
        >
          <el-step title="待接单" />
          <el-step title="处理中" description="家政已接单，待付款" />
          <el-step title="服务中" description="已付款，上门服务" />
          <el-step title="已完成" />
        </el-steps>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button
          v-if="currentOrder && currentOrder.status === 'COMPLETED' && !currentOrder.evaluationRating"
          type="primary"
          :loading="evaluating"
          @click="submitEvaluateFromDetail"
        >提交评价</el-button>
        <el-button
          v-if="currentOrder && canPayHousekeepingOrder(currentOrder)"
          type="success"
          @click="handlePay(currentOrder)"
        >支付订单</el-button>
        <el-button
          v-if="currentOrder && canCancelHousekeepingOrder(currentOrder)"
          type="danger"
          plain
          @click="handleCancel(currentOrder)"
        >取消订单</el-button>
        <el-button @click="orderDetailVisible = false">关 闭</el-button>
      </span>
    </el-dialog>

    <!-- 支付对话框（与物业费一致：支付宝沙箱 + 可选演示余额支付） -->
    <el-dialog 
      title="支付方式选择" 
      :visible.sync="payDialogVisible" 
      width="440px" 
      :close-on-click-modal="false"
      append-to-body>
      <div class="pay-dialog-content">
        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="支付宝沙箱"
          description="选择「支付宝支付」将跳转沙箱环境模拟付款，与物业费缴纳流程相同。"
          style="margin-bottom: 16px" />
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
        <el-button @click="payByBalance" :loading="paying">余额支付(演示)</el-button>
        <el-button type="primary" @click="goAlipaySandboxPay">支付宝支付</el-button>
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
  max-width: 1280px;
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

/* 服务卡片：auto-fit 网格，列数随屏宽变化，避免原 lg=6 时 5 张变成「4+1」孤卡 */
.service-grid-wrap {
  margin-bottom: 0;
  padding-bottom: 28px; /* 给 hover 阴影、上浮留出绘制空间，避免压住下方分页 */
  min-height: 120px;
  position: relative;
  z-index: 0;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  align-items: stretch;
}

.service-card {
  height: 100%;
  min-height: 340px;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.3s, transform 0.3s;
  padding: 20px;
  border-radius: 12px;
  position: relative;
  z-index: 0;
}

.service-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
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

/* 分页容器：与上方卡片区拉开距离，并叠在卡片 hover 层之上，避免被阴影/位移盖住 */
.pagination-container {
  clear: both;
  margin: 0 0 24px;
  padding: 16px 8px 8px;
  text-align: center;
  position: relative;
  z-index: 2;
}

/* 我的订单 */
.my-orders {
  margin-bottom: 24px;
}

.refund-hint {
  display: inline-block;
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
  vertical-align: middle;
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

.pricing-hint {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.description-cell {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

.rating-num {
  font-size: 14px;
  font-weight: 600;
  color: #e6a23c;
}

.rating-empty {
  font-size: 13px;
  color: #909399;
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
  margin-bottom: 20px;
}

.housekeeping-order-tabs {
  margin-bottom: 16px;
}

.housekeeping-order-tabs ::v-deep .el-tabs__header {
  margin-bottom: 0;
}

.orders-pagination.pagination-container {
  margin-top: 8px;
  text-align: left;
}

/* 订单分页：整体靠左，与表格内容对齐，避免贴最右 */
.orders-pagination ::v-deep .el-pagination {
  display: flex;
  justify-content: flex-start;
  flex-wrap: wrap;
  text-align: left;
  padding-left: 0;
}

.housekeeping-order-detail-dialog ::v-deep .el-dialog__body {
  padding-top: 12px;
}

.hk-order-detail-desc {
  margin-bottom: 0;
}

.hk-order-progress-steps {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.hk-detail-eval-block {
  margin-top: 16px;
}

.hk-detail-eval-block .eval-readonly .eval-stars-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.hk-detail-eval-block .eval-readonly .eval-label {
  color: #606266;
  font-size: 14px;
}

.hk-detail-eval-block .eval-readonly .eval-text {
  margin: 0;
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
}

.hk-detail-eval-block .eval-flow-hint {
  margin: 0 0 12px;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
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
import PageFooter from '@/components/front/PageFooter.vue'
import Request from '@/utils/request'
import moment from 'moment'
import { mapGetters } from 'vuex'

export default {
  name: 'HousekeepingService',
  components: {
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

      // 我的订单相关
      orderLoading: false,
      orderPageNum: 1,
      orderPageSize: 5,
      orderTotal: 0,
      myOrders: [],
      /** 与报修一致：待处理 / 处理中 / 待评价 / 已完成 */
      housekeepingOrderTab: 'pending',
      orderLoadSeq: 0,

      // 其他数据
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8.64e7; // 禁用过去的日期
        }
      },

      orderDetailVisible: false,
      currentOrder: null,

      evaluating: false,
      evaluateForm: {
        evaluationRating: 0,
        evaluation: ''
      },
      evaluateRules: {
        evaluationRating: [
          {
            validator: (rule, value, callback) => {
              if (!value || value < 1) {
                callback(new Error('请选择1-5星评分'))
              } else {
                callback()
              }
            },
            trigger: 'change'
          }
        ],
        evaluation: [
          { required: true, message: '请填写评价内容', trigger: 'blur' },
          { min: 5, message: '评价内容至少5个字符', trigger: 'blur' },
          { max: 500, message: '评价内容最多500个字符', trigger: 'blur' }
        ]
      },

      // 支付相关
      payDialogVisible: false,
      paying: false,
      submitting: false
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'currentUser', 'ownerInfo']),
    /** 是否按小时计价（需填分钟） */
    isHourlyService() {
      return this.currentService && this.currentService.unit === '小时';
    },
    /** 预约第二步校验：仅按小时时要求填写时长 */
    bookingFormRules() {
      const rules = {
        appointmentTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }]
      };
      if (this.isHourlyService) {
        rules.serviceDuration = [
          { required: true, message: '请输入服务时长', trigger: 'blur' },
          {
            type: 'number',
            min: 60,
            message: '服务时长不少于 60 分钟',
            trigger: 'blur'
          }
        ];
      }
      return rules;
    },
    calculatedAmount() {
      if (!this.currentService) return 0;
      const price = Number(this.selectedHousekeeper?.price || this.currentService.basePrice);
      const unit = this.currentService.unit;
      if (unit === '小时') {
        if (this.bookingForm.serviceDuration == null) return 0;
        const duration = this.bookingForm.serviceDuration;
        return Math.ceil(duration / 60) * price;
      }
      if (unit === '次') {
        return price;
      }
      // 平米等：与后端一致按单次定价展示
      return price;
    },
    housekeepingOrderListEmptyText() {
      const m = {
        pending: '暂无待处理订单',
        progress: '暂无处理中的订单',
        to_evaluate: '暂无待评价订单（服务完工后请在此评价）',
        done: '暂无已完成订单'
      }
      return m[this.housekeepingOrderTab] || '暂无数据'
    },
    /**
     * 与当前 Tab 一致（防止后端未按 ownerTab 筛选时混入其它状态）
     */
    orderRowsForTab() {
      const rows = this.myOrders || []
      const tab = this.normalizeHousekeepingOrderTab(this.housekeepingOrderTab)
      return rows.filter(r => this.rowMatchesHousekeepingOwnerTab(r, tab))
    },
    /**
     * 详情弹窗进度条（与报修一致：结束时 active 需大于最后一步下标，四步全部 finish）
     */
    housekeepingDetailStepActive() {
      const o = this.currentOrder
      if (!o || !o.status) return 0
      if (o.status === 'CANCELLED') return 0
      if (o.status === 'COMPLETED') return 4
      if (o.status === 'PENDING') return 0
      if (o.status === 'CONFIRMED') return 1
      if (o.status === 'IN_PROGRESS') return 2
      return 0
    }
  },
  watch: {
    ownerInfo: {
      handler(newVal) {
        if (newVal && newVal.id != null) {
          this.orderPageNum = 1
          this.loadMyOrders()
        }
      }
    },
    /**
     * 须在 v-model(housekeepingOrderTab) 更新后再拉列表（与报修页一致）
     */
    housekeepingOrderTab(newVal, oldVal) {
      if (newVal === oldVal) return
      this.orderPageNum = 1
      this.loadMyOrders()
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
          pageSize: this.pageSize
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

    // 上一步
    prevStep() {
      if (this.bookingStep > 1) {
        this.bookingStep--;
      }
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
          // 按次等非小时：传 1 表示单次，与计价逻辑一致
          serviceDuration: this.isHourlyService ? this.bookingForm.serviceDuration : 1,
          remark: this.bookingForm.remark,
          amount: this.calculatedAmount
        };
        
        this.submitting = true;
        const response = await Request.post('/housekeeping-order', order);
        if (response.code === '0') {
          this.$message.success('预约成功，请等待服务人员接单并开始服务后再支付');
          this.bookingVisible = false;
          this.bookingStep = 1;
          this.housekeepingOrderTab = 'pending';
          this.orderPageNum = 1;
          this.loadMyOrders();
        } else {
          this.$message.error(response.msg || '预约失败');
        }
      } catch (error) {
        console.error('预约失败:', error);
        this.$message.error('预约失败');
      } finally {
        this.submitting = false;
      }
    },

    /** 与后端 ownerTab 一致：pending / progress / to_evaluate / done */
    normalizeHousekeepingOrderTab(t) {
      const s = t == null ? '' : String(t)
      if (s === 'pending' || s === 'progress' || s === 'to_evaluate' || s === 'done') return s
      return 'pending'
    },
    rowMatchesHousekeepingOwnerTab(row, tab) {
      if (!row || !row.status) return false
      const s = row.status
      if (tab === 'pending') {
        return s === 'PENDING' || s === 'CANCELLED'
      }
      if (tab === 'progress') {
        return s === 'IN_PROGRESS' || s === 'CONFIRMED'
      }
      if (tab === 'to_evaluate') {
        return s === 'COMPLETED' && row.evaluationRating == null
      }
      if (tab === 'done') {
        return s === 'COMPLETED' && row.evaluationRating != null
      }
      return true
    },

    // 加载我的订单
    async loadMyOrders() {
      if (!this.ownerInfo || !this.ownerInfo.id) {
        return
      }
      const seq = ++this.orderLoadSeq
      try {
        this.orderLoading = true
        const tab = this.normalizeHousekeepingOrderTab(this.housekeepingOrderTab)
        const params = {
          pageNum: this.orderPageNum,
          pageSize: this.orderPageSize,
          ownerId: this.ownerInfo.id,
          ownerTab: tab
        }
        const response = await Request.get('/housekeeping-order/page', { params })
        if (seq !== this.orderLoadSeq) return
        if (response.code === '0') {
          this.myOrders = response.data.records
          this.orderTotal = response.data.total
        }
      } catch (error) {
        this.$message.error('获取订单列表失败')
      } finally {
        if (seq !== this.orderLoadSeq) return
        this.orderLoading = false
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

    /** 后端按已完成订单评价均分（1 位小数）；无评价时为 null */
    housekeeperDisplayRating(row) {
      if (!row || row.rating == null || row.rating === '') return null;
      const n = Number(row.rating);
      if (!Number.isFinite(n)) return null;
      return Math.round(n * 10) / 10;
    },
    /**
     * 业务态文案：待接单 → 处理中(已接单待付) → 待支付/服务中 → 已完成
     */
    getOrderStatusLabel(row) {
      if (!row) return '';
      if (row.status === 'CANCELLED') return '已取消';
      if (row.status === 'COMPLETED' && !row.evaluationRating && row.paymentStatus === 'UNPAID') return '待支付';
      if (row.status === 'COMPLETED' && !row.evaluationRating) return '待评价';
      if (row.status === 'COMPLETED' && row.evaluationRating) return '已完成';
      if (row.status === 'PENDING' && row.paymentStatus === 'UNPAID') return '待接单';
      if (row.status === 'CONFIRMED' && row.paymentStatus === 'UNPAID') return '处理中';
      if (row.status === 'CONFIRMED' && row.paymentStatus === 'PAID') return '待服务';
      if (row.status === 'IN_PROGRESS' && row.paymentStatus === 'UNPAID') return '待支付';
      if (row.status === 'IN_PROGRESS' && row.paymentStatus === 'PAID') return '服务中';
      return row.status || '未知';
    },
    getOrderStatusTagType(row) {
      const t = {
        待接单: 'warning',
        处理中: 'primary',
        待支付: 'warning',
        待服务: 'success',
        服务中: 'success',
        待评价: 'primary',
        已完成: 'success',
        已取消: 'info'
      };
      return t[this.getOrderStatusLabel(row)] || 'info';
    },
    /** 待接单且未支付才可取消 */
    canCancelHousekeepingOrder(order) {
      if (!order) return false;
      return order.status === 'PENDING' && order.paymentStatus === 'UNPAID';
    },
    /** 未支付可付款：在「待评价」Tab 展示（含已完工未付补付） */
    canPayHousekeepingOrder(order) {
      if (!order || order.paymentStatus !== 'UNPAID') return false;
      if (order.status === 'COMPLETED' && order.evaluationRating == null) return true;
      return order.status === 'CONFIRMED' || order.status === 'IN_PROGRESS';
    },
    /** 已支付且未结束：提示走线下退款 */
    showRefundHint(row) {
      if (!row) return false;
      return row.paymentStatus === 'PAID' && row.status !== 'CANCELLED' && row.status !== 'COMPLETED';
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
      const hourly = this.currentService && this.currentService.unit === '小时';
      this.bookingForm = {
        housekeeperId: null,
        serviceId: this.currentService?.id,
        appointmentTime: null,
        // 按次/其它：后端仍存整数，用 1 表示单次；按小时为分钟数
        serviceDuration: hourly ? 60 : 1,
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
        'PENDING': '待接单',
        'CONFIRMED': '已接单',
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
      this.orderDetailVisible = false;
      this.payDialogVisible = true;
    },
    
    // 演示：直接调后端标记已支付（不走支付宝）
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

    /** 跳转统一支付宝沙箱页（businessType=HOUSEKEEPING，回调后更新家政订单） */
    goAlipaySandboxPay() {
      if (!this.currentOrder || !this.currentOrder.orderNo) {
        this.$message.warning('订单信息不完整');
        return;
      }
      const serviceName = this.currentOrder.service ? this.currentOrder.service.name : '家政服务';
      const origin = window.location.origin;
      this.payDialogVisible = false;
      this.$router.push({
        path: '/alipay-payment',
        query: {
          amount: String(this.currentOrder.amount),
          subject: `家政服务 - ${serviceName}`,
          businessType: 'HOUSEKEEPING',
          businessOrderNo: this.currentOrder.orderNo,
          returnUrl: encodeURIComponent(`${origin}/#/housekeeping`)
        }
      });
    },
    
    // 查看订单详情
    viewOrderDetail(order) {
      this.currentOrder = order
      this.evaluateForm = {
        evaluationRating: 0,
        evaluation: ''
      }
      this.orderDetailVisible = true
      this.$nextTick(() => {
        if (this.$refs.evaluateForm) this.$refs.evaluateForm.clearValidate()
      })
    },

    submitEvaluateFromDetail() {
      if (!this.$refs.evaluateForm) return
      this.$refs.evaluateForm.validate(valid => {
        if (!valid) return
        if (!this.ownerInfo || this.ownerInfo.id == null) return
        this.evaluating = true
        Request.put(
          `/housekeeping-order/${this.currentOrder.id}/evaluation?ownerId=${this.ownerInfo.id}`,
          {
            evaluationRating: this.evaluateForm.evaluationRating,
            evaluation: this.evaluateForm.evaluation
          }
        )
          .then(response => {
            if (response.code === '0') {
              this.$message.success('评价提交成功')
              this.orderPageNum = 1
              this.housekeepingOrderTab = 'done'
              this.loadMyOrders()
              this.syncOrderDetailAfterEval(this.currentOrder.id)
            } else {
              this.$message.error(response.msg || '提交失败')
            }
          })
          .finally(() => {
            this.evaluating = false
          })
      })
    },

    syncOrderDetailAfterEval(id) {
      if (!id) return
      Request.get(`/housekeeping-order/${id}`).then(res => {
        if (res.code === '0' && res.data) {
          this.currentOrder = res.data
        }
      })
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
          this.orderDetailVisible = false;
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