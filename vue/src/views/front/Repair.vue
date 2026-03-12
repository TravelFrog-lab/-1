<template>
  <div class="repair-page">
    <nav-header></nav-header>

    <div class="content-wrapper">
      <div class="repair-container">
        <div class="page-header">
          <h2 class="page-title">报修服务</h2>
          <div class="page-desc">提供快捷的在线报修服务，专业维修人员及时响应</div>
        </div>

        <!-- 报修表单卡片 -->
        <el-card class="repair-form" shadow="never">
          <div class="form-header">
            <i class="el-icon-edit"></i>
            <span>提交报修</span>
          </div>
          <el-form ref="repairForm" :model="repairForm" :rules="rules" label-width="100px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="报修类型" prop="type">
                  <el-select v-model="repairForm.type" placeholder="请选择报修类型" style="width: 100%">
                    <el-option v-for="item in repairTypes" :key="item.value" :label="item.label" :value="item.value">
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="期望时间" prop="expectTime">
                  <el-date-picker v-model="repairForm.expectTime" type="datetime" placeholder="选择期望上门时间" style="width: 100%" :picker-options="pickerOptions">
                  </el-date-picker>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="报修描述" prop="description">
              <el-input type="textarea" :rows="4" placeholder="请详细描述问题，以便维修人员更好地了解情况" v-model="repairForm.description">
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitRepair" :loading="submitting">
                提交报修
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 报修记录表格 -->
        <el-card class="repair-records" shadow="never">
          <div slot="header" class="card-header">
            <div class="header-left">
              <i class="el-icon-document"></i>
              <span>报修记录</span>
            </div>
          </div>

          <el-table :data="repairRecords" style="width: 100%" v-loading="loading">
            <el-table-column prop="repairType.name" label="报修类型" width="120">
              <template slot-scope="scope">
                <el-tag>{{ scope.row.repairType.name }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="报修描述" show-overflow-tooltip>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStatusTag(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="expectedTime" label="期望时间" width="160">
              <template slot-scope="scope">
                {{ scope.row.expectedTime | formatDateTime }}
              </template>
            </el-table-column>
            <el-table-column prop="actualTime" label="维修时间" width="160">
              <template slot-scope="scope">
                {{ scope.row.actualTime | formatDateTime }}
              </template>
            </el-table-column>
            <el-table-column prop="feeAmount" label="维修费用" width="120">
              <template slot-scope="scope">
                <span v-if="scope.row.feeAmount != null">¥{{ scope.row.feeAmount }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="payStatus" label="支付状态" width="100">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.payStatus === 'PAID'" type="success">已支付</el-tag>
                <el-tag v-else-if="scope.row.feeAmount != null" type="warning">待支付</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <!-- 维修人员 -->
            <el-table-column prop="maintainer.user.name" label="维修人员" width="150"/>

            <el-table-column prop="resultDescription" label="维修结果" show-overflow-tooltip>
              <template slot-scope="scope">
                {{ scope.row.resultDescription ? scope.row.resultDescription : '-' }}
              </template>
            </el-table-column>

            <!-- evaluation -->
            <el-table-column prop="evaluation" label="评价" width="150" align="center" show-overflow-tooltip>
              <template slot-scope="scope">
                {{ scope.row.evaluation ? scope.row.evaluation : '-' }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="120" align="center" fixed="right">

              <template slot-scope="scope">
                <el-button v-if="scope.row.status === 'PENDING'" type="text" @click="handleEdit(scope.row)">
                  编辑
                </el-button>
                <el-button v-if="scope.row.status === 'COMPLETED' && !scope.row.evaluation" type="text" @click="handleEvaluate(scope.row)">
                  评价
                </el-button>
                <span v-else-if="scope.row.evaluation">已评价</span>
                <el-button
                  v-if="scope.row.status === 'COMPLETED' && scope.row.feeAmount != null && scope.row.payStatus !== 'PAID'"
                  type="text"
                  @click="handlePay(scope.row)"
                >
                  去支付
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

    <!-- 评价对话框 -->
    <el-dialog title="维修评价" :visible.sync="evaluateVisible" width="500px" :close-on-click-modal="false">
      <el-form :model="evaluateForm" ref="evaluateForm" :rules="evaluateRules" label-width="80px">
        <el-form-item label="评价内容" prop="evaluation ">
          <el-input type="textarea" :rows="4" placeholder="请输入您对本次维修服务的评价" v-model="evaluateForm.evaluation ">
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="evaluateVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitEvaluate" :loading="evaluating">
          提交评价
        </el-button>
      </span>
    </el-dialog>

    <!-- 编辑报修对话框 -->
    <el-dialog title="编辑报修" :visible.sync="editVisible" width="500px" :close-on-click-modal="false">
      <el-form :model="editForm" ref="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="报修类型" prop="repairTypeId">
          <el-select v-model="editForm.repairTypeId" placeholder="请选择报修类型" style="width: 100%">
            <el-option v-for="item in repairTypes" :key="item.value" :label="item.label" :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="报修描述" prop="description">
          <el-input type="textarea" :rows="4" placeholder="请详细描述问题，以便维修人员更好地了解情况" v-model="editForm.description">
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editing">
          保存修改
        </el-button>
      </span>
    </el-dialog>

    <!-- 支付维修费对话框（演示） -->
    <el-dialog title="维修费用支付" :visible.sync="payVisible" width="400px" :close-on-click-modal="false">
      <div v-if="payForm">
        <p>本次维修费用：<strong>¥{{ payForm.amount }}</strong></p>
        <p class="pay-tip">当前为演示支付：点击“确认支付”即视为已支付，实际项目中可接入支付宝等线上支付。</p>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="payVisible = false">取 消</el-button>
        <el-button type="primary" :loading="paying" @click="submitPay">确认支付</el-button>
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
  name: 'Repair',
  components: {
    NavHeader,
    PageFooter
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'currentUser', 'ownerInfo']),
  },
  data() {
    return {
     
      loading: false,
      submitting: false,
      evaluating: false,
      editing: false,
      // 报修表单
      repairForm: {
        type: '',
        expectTime: '',
        description: ''
      },
      // 表单校验规则
      rules: {
        type: [
          { required: true, message: '请选择报修类型', trigger: 'change' }
        ],
        expectTime: [
          { required: true, message: '请选择期望时间', trigger: 'change' }
        ],
        description: [
          { required: true, message: '请输入报修描述', trigger: 'blur' },
          { min: 10, message: '描述至少10个字符', trigger: 'blur' }
        ]
      },
      // 报修类型选项
      repairTypes: [],
      // 时间选择器配置
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8.64e7
        }
      },
      // 报修记录
      repairRecords: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      
      // 评价相关
      evaluateVisible: false,
      currentRepair: null,
      evaluateForm: {
        evaluation: ''
      },
      evaluateRules: {
        evaluation: [
          { required: true, message: '请输入评价内容', trigger: 'blur' },
          { min: 5, message: '评价内容至少5个字符', trigger: 'blur' }
        ]
      },

      // 编辑相关
      editVisible: false,
      editForm: {
        id: '',
        repairTypeId: '',
        description: ''
      },
      editRules: {
        repairTypeId: [
          { required: true, message: '请选择报修类型', trigger: 'change' }
        ],
        description: [
          { required: true, message: '请输入报修描述', trigger: 'blur' },
          { min: 10, message: '描述至少10个字符', trigger: 'blur' }
        ]
      },

      // 支付相关（跳转支付宝沙箱）
    }
  },
  filters: {
    formatDateTime(date) {
      if (!date) return '-'
      const now = moment()
      const target = moment(date)
      const diff = now.diff(target, 'days')
      
      if (diff === 0) {
        return target.format('今天 HH:mm')
      } else if (diff === 1) {
        return target.format('昨天 HH:mm')
      } else if (diff < 7) {
        return target.format('dddd HH:mm')
      } else if (target.year() === now.year()) {
        return target.format('MM-DD HH:mm')
      } else {
        return target.format('YYYY-MM-DD HH:mm')
      }
    }
  },
  created() {
    this.getRepairTypes()
    this.getRepairRecords()
  },
  methods: {
    // 获取维修类型
    getRepairTypes() {
      Request.get('/repair-types/all').then(response => {
        if (response.code === '0') {
          this.repairTypes = response.data.map(item => ({
            value: item.id, // 改为使用id作为value
            label: item.name
          }))
        } else {
          this.$message({
            message: response.msg,
            type: 'error'
          })
        }

      })
    },


    // 获取报修记录
    getRepairRecords() {
      this.loading = true
      Request.get('/repair-records/page', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          applicantId: this.ownerInfo.id // 只查看自己的报修记录
        }
      }).then(response => {
        if (response.code === '0') {
          this.repairRecords = response.data.records
          this.total = response.data.total
        } else {
          this.$message({
            message: response.msg,
            type: 'error'
          })
        }
      }).finally(() => {
        this.loading = false
      })

    },

    // 提交报修
    submitRepair() {
      this.$refs.repairForm.validate(valid => {
        if (valid) {
          this.submitting = true
          const data = {
            applicantId: this.ownerInfo.id, // 添加申请人ID
            houseId: this.ownerInfo.houseId, // 添加房屋ID
            repairTypeId: this.repairForm.type,
            description: this.repairForm.description,
            expectedTime: moment(this.repairForm.expectTime).format('YYYY-MM-DD HH:mm:ss')
          }
          Request.post('/repair-records', data).then(response => {
            if (response.code === '0') {
              this.$message.success('报修提交成功')
              this.$refs.repairForm.resetFields()
              this.getRepairRecords()
            } else {
              this.$message({
                message: response.msg,
                type: 'error'
              })
            }
          }).finally(() => {
            this.submitting = false
          })
        }
      })
    },

    // 编辑报修
    handleEdit(row) {
      this.currentRepair = row
      this.editForm.id = row.id
      this.editForm.repairTypeId = row.repairTypeId
      this.editForm.description = row.description
      this.editVisible = true
    },

    // 提交编辑
    submitEdit() {
      this.$refs.editForm.validate(valid => {
        if (valid) {
          this.editing = true
          // 只更新允许的字段
          const data = {
            id: this.editForm.id,
            repairTypeId: this.editForm.repairTypeId,
            description: this.editForm.description,
            status: 'PENDING' // 确保状态仍为待处理
          }
          
          Request.put('/repair-records/edit-pending', data).then(response => {
            if (response.code === '0') {
              this.$message.success('报修修改成功')
              this.editVisible = false
              this.getRepairRecords()
              // 重置编辑表单
              this.$refs.editForm.resetFields()
              this.currentRepair = null
            } else {
              this.$message({
                message: response.msg,
                type: 'error'
              })
            }
          }).finally(() => {
            this.editing = false
          })
        }
      })
    },

    // 提交评价
    handleEvaluate(row) {
      this.currentRepair = row
      this.evaluateVisible = true
    },
    submitEvaluate() {
      this.$refs.evaluateForm.validate(valid => {
        if (valid) {
          this.evaluating = true
          // 只更新id和evaluation字段
          const data = {
            ...this.currentRepair,
            evaluation: this.evaluateForm.evaluation
          }
          Request.put('/repair-records', data)

            .then(response => {
              if (response.code === '0') {
                this.$message.success('评价提交成功')
                this.evaluateVisible = false
                this.getRepairRecords()
                // 重置评价表单
                this.$refs.evaluateForm.resetFields()
                this.currentRepair = null
              } else {
                this.$message({
                  message: response.msg,
                  type: 'error'
                })
              }
            }).finally(() => {
              this.evaluating = false
            })

        }
      })
    },
    // 支付维修费用：跳转到支付宝沙箱统一支付页
    handlePay(row) {
      if (!row.feeAmount || row.feeAmount <= 0) {
        this.$message.warning('该维修记录未设置费用')
        return
      }
      // 跳转到通用支付宝支付页面，业务类型为 REPAIR，业务订单号为报修记录ID
      const subject = encodeURIComponent(`报修服务费用-${row.repairType?.name || ''}`)
      this.$router.push({
        path: '/alipay-payment',
        query: {
          amount: row.feeAmount,
          businessType: 'REPAIR',
          businessOrderNo: row.id,
          subject
        }
      })
    },
    // 分页方法
    handleSizeChange(val) {
      this.pageSize = val
      this.getRepairRecords()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getRepairRecords()
    },
    // 状态显示方法
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待处理',
        'IN_PROGRESS': '处理中',
        'COMPLETED': '已完成'
      }
      return statusMap[status] || '未知'
    },
    getStatusTag(status) {
      const statusMap = {
        'PENDING': 'info',
        'IN_PROGRESS': 'warning',
        'COMPLETED': 'success'
      }
      return statusMap[status] || 'info'
    }
  }
}
</script>

<style scoped>
/* 整体页面布局 */
.repair-page {
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

/* 报修表单卡片 */
.repair-form {
  background: #fff;
  padding: 30px;
  margin-bottom: 24px;
  transition: all 0.3s;
}

.repair-form:hover {
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.form-header {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 20px;
  margin-bottom: 20px;
}

.form-header i {
  font-size: 20px;
  margin-right: 8px;
  color: #409EFF;
}

.form-header span {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

/* 表格卡片 */
.repair-records {
  background: #fff;
  padding: 20px;
  transition: all 0.3s;
}

.repair-records:hover {
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.card-header {
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

/* 表格样式 */
.el-table {
  margin: 15px 0;
}

.el-table ::v-deep .el-table__header-wrapper th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 500;
}

/* 标签样式 */
.el-tag {
  border-radius: 4px;
  padding: 0 8px;
  height: 24px;
  line-height: 22px;
}

/* 状态标签 */
.el-tag.el-tag--info {
  background-color: #f4f4f5;
  border-color: #e9e9eb;
  color: #909399;
}

.el-tag.el-tag--warning {
  background-color: #fdf6ec;
  border-color: #faecd8;
  color: #e6a23c;
}

.el-tag.el-tag--success {
  background-color: #f0f9eb;
  border-color: #e1f3d8;
  color: #67c23a;
}

/* 按钮样式 */
.el-button--primary {
  padding: 10px 20px;
}

/* 评价对话框 */
.el-dialog {
  border-radius: 0;
}

.el-dialog ::v-deep .el-dialog__header {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.el-dialog ::v-deep .el-dialog__body {
  padding: 30px 20px;
}

.el-dialog ::v-deep .el-dialog__footer {
  padding: 15px 20px;
  border-top: 1px solid #ebeef5;
}

/* 分页 */
.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.pay-tip {
  font-size: 13px;
  color: #909399;
  margin-top: 8px;
}

/* 表单项 */
.el-form-item ::v-deep .el-form-item__label {
  color: #606266;
}

.el-input ::v-deep .el-input__inner,
.el-textarea ::v-deep .el-textarea__inner {
  border-color: #dcdfe6;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .content-wrapper {
    padding: 15px;
  }
  
  .repair-form,
  .repair-records {
    padding: 15px;
  }
  
  .el-col {
    width: 100% !important;
  }
  
  .form-header,
  .card-header {
    padding-bottom: 15px;
    margin-bottom: 15px;
  }
  
  .page-title {
    font-size: 20px;
  }
}
</style> 