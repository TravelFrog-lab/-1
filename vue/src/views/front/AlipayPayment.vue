<template>
  <div class="alipay-payment-page">
    <nav-header></nav-header>

    <div class="content-wrapper">
      <div class="payment-container">
        <!-- 页面标题 -->
        <div class="page-header">
          <h2 class="page-title">支付宝支付</h2>
          <div class="page-desc">安全、便捷的在线支付服务</div>
          <div class="page-tips">
            <el-alert
              title="支付提示"
              type="info"
              description="本系统使用支付宝沙盒环境进行演示，支付金额不会实际扣除，请放心体验。"
              show-icon
              :closable="false">
            </el-alert>
          </div>
        </div>

        <!-- 支付进度条 -->
        <el-steps :active="step" finish-status="success" simple class="payment-steps">
          <el-step title="订单确认"></el-step>
          <el-step title="支付中"></el-step>
          <el-step title="完成"></el-step>
        </el-steps>

        <!-- 步骤1: 订单确认 -->
        <transition name="fade-slide" mode="out-in">
          <div v-if="step === 1" class="step-container step-order-confirm">
            <el-card class="order-info-card" shadow="hover">
              <div class="card-header">
                <i class="el-icon-document"></i>
                <span>订单信息</span>
              </div>
              <div class="order-details">
                <el-row :gutter="20">
                  <el-col :xs="24" :sm="12">
                    <div class="detail-item">
                      <span class="label">订单标题:</span>
                      <span class="value">{{ orderInfo.subject }}</span>
                    </div>
                    <div class="detail-item">
                      <span class="label">业务类型:</span>
                      <span class="value">{{ orderInfo.businessType }}</span>
                    </div>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <div class="detail-item">
                      <span class="label">金额:</span>
                      <span class="value amount">¥{{ orderInfo.amount }}</span>
                    </div>
                    <div class="detail-item">
                      <span class="label">业务订单号:</span>
                      <span class="value">{{ orderInfo.businessOrderNo || '系统生成' }}</span>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </el-card>

            <el-card class="payment-method-card" shadow="hover">
              <div class="card-header">
                <i class="el-icon-wallet"></i>
                <span>支付方式</span>
              </div>
              <div class="payment-options">
                <el-radio-group v-model="paymentMethod" class="payment-radio-group">
                  <el-radio label="alipay" class="payment-option">
                    <div class="option-content">
                      <span class="payment-logo payment-logo-svg" aria-label="支付宝">
                        <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
                          <rect width="200" height="200" rx="12" fill="#1677FF"/>
                          <text x="100" y="128" text-anchor="middle" fill="#fff" font-size="100" font-weight="bold" font-family="sans-serif">支</text>
                        </svg>
                      </span>
                      <div class="option-info">
                        <div class="option-title">支付宝</div>
                        <div class="option-desc">扫码支付或跳转支付</div>
                      </div>
                    </div>
                  </el-radio>
                </el-radio-group>
              </div>
            </el-card>

            <div class="payment-guide">
              <el-card shadow="never">
                <div class="guide-header">
                  <i class="el-icon-info"></i>
                  <span>支付流程说明</span>
                </div>
                <div class="guide-steps">
                  <div class="guide-step">
                    <div class="step-number">1</div>
                    <div class="step-content">
                      <div class="step-title">确认订单</div>
                      <div class="step-desc">核对订单信息，选择支付方式</div>
                    </div>
                  </div>
                  <div class="guide-step">
                    <div class="step-number">2</div>
                    <div class="step-content">
                      <div class="step-title">跳转支付</div>
                      <div class="step-desc">系统将跳转到支付宝沙盒页面</div>
                    </div>
                  </div>
                  <div class="guide-step">
                    <div class="step-number">3</div>
                    <div class="step-content">
                      <div class="step-title">完成支付</div>
                      <div class="step-desc">在支付宝页面完成支付操作</div>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>

            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="large" 
                :loading="creatingOrder" 
                @click="createPaymentOrder"
                class="confirm-button">
                <i class="el-icon-check"></i>
                确认支付
              </el-button>
              <el-button 
                size="large" 
                @click="goBack"
                class="back-button">
                <i class="el-icon-arrow-left"></i>
                返回
              </el-button>
            </div>
          </div>
        </transition>

        <!-- 步骤2: 支付中 -->
        <transition name="fade-slide" mode="out-in">
          <div v-if="step === 2" class="step-container step-paying">
            <el-card class="paying-card" shadow="hover">
              <div class="paying-content">
                <div class="spinner-container">
                  <i class="el-icon-loading"></i>
                </div>
                <h3 class="paying-title">等待支付完成</h3>
                <p class="paying-desc">请在新打开的支付宝沙盒页面完成支付</p>
                
                <div class="order-no">
                  <span class="label">支付订单号:</span>
                  <span class="value">{{ paymentOrderNo }}</span>
                </div>
                
                <div class="paying-actions">
                  <el-button 
                    type="success" 
                    @click="openPayUrl" 
                    :disabled="!payUrl"
                    class="pay-action-button">
                    <i class="el-icon-link"></i> 重新打开支付页面
                  </el-button>
                  <el-button 
                    @click="checkPaymentStatus"
                    class="check-status-button">
                    <i class="el-icon-refresh"></i> 手动检查支付状态
                  </el-button>
                </div>
              </div>
            </el-card>

            <div class="payment-status-card">
              <el-card shadow="hover">
                <div class="status-content">
                  <h4>支付状态</h4>
                  <el-tag :type="statusTagType" class="status-tag">
                    {{ paymentStatusText }}
                  </el-tag>
                  <p class="status-desc">{{ statusDescription }}</p>
                </div>
              </el-card>
            </div>

            <div class="action-buttons">
              <el-button 
                type="primary" 
                :loading="checkingStatus" 
                @click="checkPaymentStatus"
                class="refresh-button">
                <i class="el-icon-refresh"></i> 刷新状态
              </el-button>
              <el-button 
                @click="cancelPayment"
                class="cancel-button">
                <i class="el-icon-close"></i> 取消支付
              </el-button>
            </div>
          </div>
        </transition>

        <!-- 步骤3: 支付完成 -->
        <transition name="fade-slide" mode="out-in">
          <div v-if="step === 3" class="step-container step-result">
            <el-card class="result-card" shadow="hover" :class="resultClass">
              <div class="result-content">
                <div class="result-icon">
                  <i :class="resultIcon"></i>
                </div>
                <h3 class="result-title">{{ resultTitle }}</h3>
                <p class="result-desc">{{ resultDesc }}</p>
                
                <div class="result-details" v-if="paymentResult">
                  <div class="detail-row">
                    <span class="label">支付订单号:</span>
                    <span class="value">{{ paymentResult.orderNo }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">支付宝交易号:</span>
                    <span class="value">{{ paymentResult.tradeNo || '等待返回' }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">支付金额:</span>
                    <span class="value">¥{{ paymentResult.amount }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">支付时间:</span>
                    <span class="value">{{ paymentResult.payTime || '未支付' }}</span>
                  </div>
                </div>
              </div>
            </el-card>

            <div class="action-buttons">
              <el-button 
                type="primary" 
                @click="goToHome"
                class="home-button">
                <i class="el-icon-house"></i> 返回首页
              </el-button>
              <el-button 
                @click="goToOrders"
                class="orders-button">
                <i class="el-icon-document"></i> 查看订单
              </el-button>
              <el-button 
                v-if="resultType === 'success'" 
                @click="printReceipt"
                class="print-button">
                <i class="el-icon-printer"></i> 打印凭证
              </el-button>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'
import { MessageBox } from 'element-ui'
import NavHeader from '@/components/front/NavHeader.vue'

export default {
  name: 'AlipayPayment',
  components: { NavHeader },
  data() {
    return {
      step: 1, // 当前步骤 1:订单确认, 2:支付中, 3:完成
      
      // 订单信息 (可以从路由参数或父组件传入)
      orderInfo: {
        userId: null,
        amount: '0.00',
        subject: '物业管理系统支付',
        businessType: 'UNKNOWN',
        businessOrderNo: '',
        returnUrl: `${window.location.origin}/api/alipay/return`
      },
      
      // 支付信息
      paymentMethod: 'alipay',
      creatingOrder: false,
      payUrl: '',
      paymentOrderNo: '',
      paymentStatus: '', // UNPAID, PAID, FAILED, CLOSED
      paymentResult: null,
      
      // 轮询相关
      pollingInterval: null,
      pollingCount: 0,
      maxPollingCount: 60, // 最多轮询60次 (约3分钟)
      checkingStatus: false,
      
      // 结果信息
      resultType: '', // success, error, warning
      resultTitle: '',
      resultDesc: ''
    }
  },
  computed: {
    statusTagType() {
      switch (this.paymentStatus) {
        case 'PAID': return 'success'
        case 'FAILED': return 'danger'
        case 'CLOSED': return 'info'
        default: return 'warning'
      }
    },
    paymentStatusText() {
      const statusMap = {
        'UNPAID': '待支付',
        'PAID': '支付成功',
        'FAILED': '支付失败',
        'CLOSED': '交易关闭'
      }
      return statusMap[this.paymentStatus] || this.paymentStatus
    },
    statusDescription() {
      switch (this.paymentStatus) {
        case 'UNPAID': return '订单已创建，等待您完成支付'
        case 'PAID': return '支付已成功，系统正在处理您的订单'
        case 'FAILED': return '支付失败，请重新尝试支付'
        case 'CLOSED': return '支付超时或已取消，交易已关闭'
        default: return '支付状态未知'
      }
    },
    resultClass() {
      return `result-${this.resultType}`
    },
    resultIcon() {
      switch (this.resultType) {
        case 'success': return 'el-icon-success'
        case 'error': return 'el-icon-error'
        case 'warning': return 'el-icon-warning'
        default: return 'el-icon-info'
      }
    }
  },
  created() {
    // 从路由参数获取订单信息
    this.initOrderInfo()
  },
  beforeDestroy() {
    // 清理轮询
    this.stopPolling()
  },
  methods: {
    initOrderInfo() {
      const query = this.$route.query
      
      // 设置用户ID (管理端 backUser / 用户端 currentUser)
      const backUser = sessionStorage.getItem('backUser')
      const currentUser = sessionStorage.getItem('currentUser')
      const user = backUser ? JSON.parse(backUser) : (currentUser ? JSON.parse(currentUser) : {})
      this.orderInfo.userId = user.id || user.userId || null
      if (this.orderInfo.userId == null) this.orderInfo.userId = 1
      
      // 从查询参数获取金额和业务信息
      if (query.amount) {
        this.orderInfo.amount = parseFloat(query.amount).toFixed(2)
      }
      if (query.subject) {
        this.orderInfo.subject = decodeURIComponent(query.subject)
      }
      if (query.businessType) {
        this.orderInfo.businessType = decodeURIComponent(query.businessType)
      }
      if (query.businessOrderNo) {
        this.orderInfo.businessOrderNo = decodeURIComponent(query.businessOrderNo)
      }
      
      // 设置默认业务类型和标题
      if (!this.orderInfo.businessType || this.orderInfo.businessType === 'UNKNOWN') {
        // 根据当前路由路径猜测业务类型
        const path = this.$route.path
        if (path.includes('property-fee')) {
          this.orderInfo.businessType = 'PROPERTY_FEE'
          this.orderInfo.subject = '物业费缴纳'
        } else if (path.includes('repair')) {
          this.orderInfo.businessType = 'REPAIR'
          this.orderInfo.subject = '报修服务支付'
        } else if (path.includes('housekeeping')) {
          this.orderInfo.businessType = 'HOUSEKEEPING'
          this.orderInfo.subject = '家政服务支付'
        } else if (path.includes('parking')) {
          this.orderInfo.businessType = 'PARKING'
          this.orderInfo.subject = '停车费支付'
        } else {
          this.orderInfo.businessType = 'OTHER'
        }
      }
    },
    
    // 创建支付订单（支持多种后端路径与返回格式，确保能跳转支付宝沙箱）
    async createPaymentOrder() {
      if (this.creatingOrder) return

      this.creatingOrder = true
      const payload = this.buildPaymentPayload()
      // 你的后端是 POST /api/payment/create，优先请求该路径
      const apiPaths = ['/payment/create', '/alipay/pay', '/alipay/create']

      for (const apiPath of apiPaths) {
        try {
          const response = await request.post(apiPath, payload, { skipGlobalError: true })

          const payUrl = response.payUrl || (response.data && response.data.payUrl)
          const orderNo = response.orderNo || (response.data && response.data.orderNo)
          let formHtml = response.form || (response.data && (response.data.form || (typeof response.data === 'string' ? response.data : null)))
          if (!formHtml && response.data && typeof response.data === 'string') formHtml = response.data

          const ok = response.code === '0' || response.code === 0 || response.success === true

          if (ok) {
            this.paymentOrderNo = orderNo || this.orderInfo.businessOrderNo || 'PAY-' + Date.now()

            if (payUrl && typeof payUrl === 'string') {
              const isUrl = payUrl.trim().startsWith('http://') || payUrl.trim().startsWith('https://')
              if (isUrl) {
                this.payUrl = payUrl
                this.paymentStatus = 'UNPAID'
                this.step = 2
                this.startPolling()
                window.location.href = payUrl
                return
              }
            }

            if (formHtml && typeof formHtml === 'string' && formHtml.trim().startsWith('<')) {
              this.paymentStatus = 'UNPAID'
              this.step = 2
              this.startPolling()
              this.submitAlipayForm(formHtml)
              return
            }

            if (orderNo) {
              this.$message.warning('后端未返回支付链接(payUrl)或表单(form)，请检查后端接口返回')
              this.paymentOrderNo = orderNo
              this.paymentStatus = 'UNPAID'
              this.step = 2
              this.startPolling()
              return
            }
          }
        } catch (err) {
          console.warn(`请求 ${apiPath} 失败:`, err.message || err)
          continue
        }
      }

      this.$message.error('无法创建支付订单。请确认：1) 后端服务已启动（端口 1234）；2) 已配置支付宝沙箱应用私钥与支付宝公钥；3) 接口为 POST /api/payment/create')
      this.creatingOrder = false
    },

    // 构建请求体（兼容常见 Java 后端字段名）
    buildPaymentPayload() {
      const origin = window.location.origin
      const amount = parseFloat(this.orderInfo.amount) || 0.01
      return {
        userId: this.orderInfo.userId || 1,
        amount: amount,
        subject: this.orderInfo.subject || '物业费缴纳',
        businessType: this.orderInfo.businessType || 'PROPERTY_FEE',
        businessOrderNo: this.orderInfo.businessOrderNo || 'ORD' + Date.now(),
        returnUrl: this.orderInfo.returnUrl || `${origin}/#/alipay-payment`
      }
    },

    // 后端返回支付宝 form HTML 时，写入页面并自动提交以跳转沙箱
    submitAlipayForm(html) {
      const div = document.createElement('div')
      div.innerHTML = html
      document.body.appendChild(div)
      const form = div.querySelector('form')
      if (form) {
        form.submit()
      } else {
        this.$message.warning('未解析到支付表单，请检查后端返回')
      }
      setTimeout(() => document.body.removeChild(div), 500)
    },
    
    // 打开支付页面
    openPayUrl() {
      if (this.payUrl) {
        window.open(this.payUrl, '_blank')
      } else {
        this.$message.warning('支付链接未生成')
      }
    },
    
    // 开始轮询支付状态
    startPolling() {
      // 先立即查询一次
      this.checkPaymentStatus()
      
      // 设置轮询间隔 (每3秒一次)
      this.pollingInterval = setInterval(() => {
        if (this.pollingCount >= this.maxPollingCount) {
          this.stopPolling()
          this.handlePollingTimeout()
          return
        }
        
        this.checkPaymentStatus()
        this.pollingCount++
      }, 3000)
    },
    
    // 停止轮询
    stopPolling() {
      if (this.pollingInterval) {
        clearInterval(this.pollingInterval)
        this.pollingInterval = null
      }
    },
    
    // 轮询超时处理
    handlePollingTimeout() {
      this.$message.warning('支付超时，请检查支付状态或重新支付')
      this.paymentStatus = 'CLOSED'
    },
    
    // 检查支付状态
    async checkPaymentStatus() {
      if (this.checkingStatus || !this.paymentOrderNo) return

      // 演示订单：直接模拟支付成功，不再请求后端
      if (this.paymentOrderNo.startsWith('DEMO-')) {
        this.stopPolling()
        this.showPaymentSuccess({
          orderNo: this.paymentOrderNo,
          status: 'PAID',
          amount: this.orderInfo.amount,
          tradeNo: 'DEMO' + Date.now(),
          payTime: new Date().toLocaleString('zh-CN')
        })
        return
      }

      this.checkingStatus = true
      try {
        const response = await request.get('/alipay/query', {
          params: { orderNo: this.paymentOrderNo },
          skipGlobalError: true
        })

        if (response.code === '200' && response.data) {
          const order = response.data
          this.paymentStatus = order.status
          this.paymentResult = order

          if (order.status === 'PAID') {
            this.stopPolling()
            this.showPaymentSuccess(order)
          } else if (order.status === 'FAILED' || order.status === 'CLOSED') {
            this.stopPolling()
            this.showPaymentFailed(order)
          }
        }
      } catch (error) {
        console.error('查询支付状态异常:', error)
      } finally {
        this.checkingStatus = false
      }
    },
    
    // 显示支付成功
    showPaymentSuccess(order) {
      this.resultType = 'success'
      this.resultTitle = '支付成功'
      this.resultDesc = '您的支付已成功完成，相关服务将尽快为您安排'
      this.paymentResult = order
      this.step = 3
      
      this.$message.success('支付成功！')
    },
    
    // 显示支付失败
    showPaymentFailed(order) {
      this.resultType = 'error'
      this.resultTitle = '支付失败'
      this.resultDesc = order.status === 'FAILED' 
        ? '支付过程中出现错误，请重新尝试支付' 
        : '支付已超时或取消，交易已关闭'
      this.paymentResult = order
      this.step = 3
      
      this.$message.error(this.resultDesc)
    },
    
    // 取消支付
    cancelPayment() {
      MessageBox.confirm('确定要取消支付吗？', '取消支付', {
        confirmButtonText: '确定',
        cancelButtonText: '继续支付',
        type: 'warning'
      }).then(() => {
        this.stopPolling()
        this.step = 1
        this.$message.info('已取消支付')
      }).catch(() => {
        // 用户点击了继续支付
      })
    },
    
    // 返回上一页
    goBack() {
      this.$router.go(-1)
    },
    
    // 返回首页
    goToHome() {
      this.$router.push('/home')
    },
    
    // 查看订单
    goToOrders() {
      this.$router.push('/profile')
    },
    
    // 打印凭证
    printReceipt() {
      window.print()
    }
  }
}
</script>

<style scoped>
.alipay-payment-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.content-wrapper {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.payment-container {
  margin-top: 20px;
}

/* 页面标题样式 */
.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  color: #3f51b5;
  margin-bottom: 10px;
  font-weight: 600;
}

.page-desc {
  font-size: 16px;
  color: #666;
  margin-bottom: 20px;
}

.page-tips {
  max-width: 800px;
  margin: 0 auto;
}

/* 支付步骤样式 */
.payment-steps {
  margin: 30px 0;
}

:deep(.el-step.is-simple .el-step__head) {
  color: #3f51b5;
}

:deep(.el-step.is-simple .el-step__title) {
  color: #3f51b5;
  font-weight: 500;
}

:deep(.el-step.is-simple.is-process .el-step__title) {
  color: #3f51b5;
  font-weight: 600;
}

/* 步骤容器动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s ease;
}

.fade-slide-enter {
  opacity: 0;
  transform: translateY(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.step-container {
  animation: fadeIn 0.5s;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 卡片通用样式 */
.order-info-card,
.payment-method-card,
.paying-card,
.result-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.order-info-card:hover,
.payment-method-card:hover,
.paying-card:hover,
.result-card:hover {
  box-shadow: 0 8px 20px rgba(63, 81, 181, 0.15);
  border-color: #3f51b5;
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header i {
  font-size: 20px;
  color: #3f51b5;
  margin-right: 10px;
}

.card-header span {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.order-details {
  padding: 10px 0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 8px 0;
  border-bottom: 1px dashed #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item .label {
  color: #666;
  font-weight: 500;
}

.detail-item .value {
  color: #333;
  font-weight: 500;
}

.detail-item .amount {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

/* 支付方式选项 */
.payment-options {
  padding: 10px 0;
}

.payment-radio-group {
  width: 100%;
}

.payment-option {
  width: 100%;
  padding: 20px;
  border-radius: 8px;
  border: 2px solid #e4e7ed;
  margin-bottom: 10px;
  transition: all 0.3s;
}

.payment-option.is-checked {
  border-color: #3f51b5;
  background-color: rgba(63, 81, 181, 0.05);
  box-shadow: 0 4px 12px rgba(63, 81, 181, 0.1);
}

.option-content {
  display: flex;
  align-items: center;
}

.payment-logo {
  width: 40px;
  height: 40px;
  margin-right: 15px;
}

.payment-logo-svg {
  display: inline-block;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 8px;
}

.payment-logo-svg svg {
  width: 100%;
  height: 100%;
  display: block;
}

.option-info {
  flex: 1;
}

.option-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.option-desc {
  font-size: 14px;
  color: #666;
}

/* 支付流程说明 */
.payment-guide {
  margin: 30px 0;
}

.guide-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  color: #3f51b5;
}

.guide-header i {
  font-size: 20px;
  margin-right: 10px;
}

.guide-header span {
  font-size: 18px;
  font-weight: 600;
}

.guide-steps {
  display: flex;
  justify-content: space-between;
  gap: 20px;
}

.guide-step {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15px;
}

.step-number {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #3f51b5, #7986cb);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 18px;
}

.step-content {
  text-align: left;
}

.step-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.step-desc {
  font-size: 14px;
  color: #666;
}

/* 支付中状态样式 */
.paying-content {
  text-align: center;
  padding: 40px 20px;
}

.spinner-container {
  margin-bottom: 20px;
}

.spinner-container i {
  font-size: 50px;
  color: #3f51b5;
  animation: spin 1.5s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.paying-title {
  font-size: 22px;
  color: #333;
  margin-bottom: 10px;
  font-weight: 600;
}

.paying-desc {
  font-size: 16px;
  color: #666;
  margin-bottom: 20px;
}

.order-no {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  background-color: #f5f7fa;
  border-radius: 8px;
  color: #333;
  font-family: monospace;
  margin-bottom: 30px;
  border: 1px solid #e4e7ed;
}

.order-no .label {
  color: #666;
  font-weight: 500;
}

.order-no .value {
  color: #3f51b5;
  font-weight: 600;
}

.paying-actions {
  margin-top: 20px;
}

/* 支付状态卡片 */
.payment-status-card {
  margin: 30px 0;
}

.status-content {
  text-align: center;
  padding: 20px;
}

.status-content h4 {
  font-size: 18px;
  color: #333;
  margin-bottom: 15px;
  font-weight: 600;
}

.status-tag {
  font-size: 16px;
  padding: 8px 16px;
  margin-bottom: 15px;
  border-radius: 20px;
}

.status-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

/* 支付结果样式 */
.result-content {
  text-align: center;
  padding: 40px 20px;
}

.result-icon {
  margin-bottom: 20px;
}

.result-icon i {
  font-size: 60px;
}

.result-success .result-icon i {
  color: #52c41a;
}

.result-error .result-icon i {
  color: #f56c6c;
}

.result-warning .result-icon i {
  color: #faad14;
}

.result-title {
  font-size: 24px;
  color: #333;
  margin-bottom: 15px;
  font-weight: 600;
}

.result-desc {
  font-size: 16px;
  color: #666;
  margin-bottom: 30px;
  line-height: 1.6;
}

.result-details {
  max-width: 500px;
  margin: 30px auto 0;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 12px;
  text-align: left;
  border: 1px solid #e4e7ed;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.detail-row:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.detail-row .label {
  color: #666;
  font-weight: 500;
}

.detail-row .value {
  color: #333;
  font-weight: 500;
  font-family: monospace;
}

/* 按钮样式 */
.action-buttons {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.action-buttons .el-button {
  margin: 0 10px;
  min-width: 120px;
  border-radius: 8px;
  transition: all 0.3s;
  padding: 12px 24px;
  font-weight: 500;
}

.confirm-button {
  background: linear-gradient(135deg, #3f51b5, #7986cb);
  border: none;
  box-shadow: 0 4px 12px rgba(63, 81, 181, 0.3);
}

.confirm-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(63, 81, 181, 0.4);
}

.back-button:hover {
  border-color: #3f51b5;
  color: #3f51b5;
}

.pay-action-button:hover {
  background-color: #67c23a;
  border-color: #67c23a;
}

.check-status-button:hover {
  border-color: #3f51b5;
  color: #3f51b5;
}

.refresh-button {
  background-color: #3f51b5;
  border-color: #3f51b5;
  color: white;
}

.refresh-button:hover {
  background-color: #7986cb;
  border-color: #7986cb;
}

.cancel-button:hover {
  border-color: #f56c6c;
  color: #f56c6c;
}

.home-button {
  background-color: #3f51b5;
  border-color: #3f51b5;
  color: white;
}

.home-button:hover {
  background-color: #7986cb;
  border-color: #7986cb;
}

.orders-button:hover {
  border-color: #3f51b5;
  color: #3f51b5;
}

.print-button:hover {
  border-color: #409EFF;
  color: #409EFF;
}

/* 响应式设计 */
@media screen and (max-width: 992px) {
  .content-wrapper {
    padding: 15px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .guide-steps {
    flex-direction: column;
    gap: 15px;
  }
  
  .guide-step {
    justify-content: flex-start;
  }
}

@media screen and (max-width: 768px) {
  .payment-steps {
    margin: 20px 0;
  }
  
  .card-header {
    font-size: 16px;
  }
  
  .detail-item {
    flex-direction: column;
    gap: 4px;
  }
  
  .detail-item .label,
  .detail-item .value {
    width: 100%;
    text-align: left;
  }
  
  .action-buttons .el-button {
    margin: 5px;
    width: calc(50% - 10px);
    min-width: auto;
  }
  
  .order-no {
    flex-direction: column;
    gap: 5px;
    text-align: center;
  }
}

@media screen and (max-width: 576px) {
  .main-content {
    padding: 10px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .page-desc {
    font-size: 14px;
  }
  
  .paying-title {
    font-size: 18px;
  }
  
  .paying-desc {
    font-size: 14px;
  }
  
  .action-buttons .el-button {
    width: 100%;
    margin: 5px 0;
  }
}
</style>