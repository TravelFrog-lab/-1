<template>
  <div class="complaint-page">
    <nav-header></nav-header>

    <div class="content-wrapper">
      <div class="complaint-container">
        <div class="page-header">
          <h2 class="page-title">投诉建议</h2>
          <div class="page-desc">提供便捷的投诉建议渠道，我们将及时处理您的反馈</div>
        </div>

        <!-- 投诉表单卡片 -->
        <el-card class="complaint-form" shadow="never">
          <div class="form-header">
            <i class="el-icon-edit-outline"></i>
            <span>提交投诉</span>
          </div>
          <el-form ref="complaintForm" :model="complaintForm" :rules="rules" label-width="100px">
   
            <el-form-item label="投诉内容" prop="content">
              <el-input 
                type="textarea" 
                :rows="4" 
                placeholder="请详细描述您的投诉内容，以便我们更好地解决问题" 
                v-model="complaintForm.content">
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitComplaint" :loading="submitting">
                提交投诉
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 投诉记录表格 -->
        <el-card class="complaint-records" shadow="never">
          <div slot="header" class="card-header">
            <div class="header-left">
              <i class="el-icon-document"></i>
              <span>投诉记录</span>
            </div>
          </div>

          <el-table :data="complaintRecords" style="width: 100%" v-loading="loading">
        
            <el-table-column prop="content" label="投诉内容" show-overflow-tooltip>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStatusTag(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="handler.name" label="处理人员" width="120">
              <template slot-scope="scope">
                {{ scope.row.handler ? scope.row.handler.name : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="result" label="处理结果" show-overflow-tooltip>

              <template slot-scope="scope">
                {{ scope.row.result || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="evaluation" label="评价内容" show-overflow-tooltip>
              <template slot-scope="scope">
                {{ scope.row.evaluation || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="提交时间" width="160">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column prop="handleTime" label="处理时间" width="160">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.handleTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">

              <template slot-scope="scope">
                <el-button 
                  v-if="scope.row.status === 'PENDING'" 
                  type="text" 
                  @click="handleEdit(scope.row)">
                  编辑
                </el-button>
                <el-button 
                  v-if="scope.row.status === 'RESOLVED' && !scope.row.evaluation" 
                  type="text" 
                  @click="handleEvaluate(scope.row)">
                  评价
                </el-button>
                <span v-else-if="scope.row.evaluation">已评价</span>
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

        <!-- 添加评价对话框 -->
        <el-dialog title="投诉评价" :visible.sync="evaluateVisible" width="500px" :close-on-click-modal="false">
          <el-form :model="evaluateForm" ref="evaluateForm" :rules="evaluateRules" label-width="80px">
            <el-form-item label="评价内容" prop="evaluation">
              <el-input
                type="textarea"
                :rows="4"
                placeholder="请输入您对本次投诉处理的评价"
                v-model="evaluateForm.evaluation">
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

        <!-- 添加编辑对话框 -->
        <el-dialog title="编辑投诉" :visible.sync="editVisible" width="500px" :close-on-click-modal="false">
          <el-form :model="editForm" ref="editForm" :rules="editRules" label-width="100px">
            <el-form-item label="投诉内容" prop="content">
              <el-input
                type="textarea"
                :rows="4"
                placeholder="请详细描述您的投诉内容，以便我们更好地解决问题"
                v-model="editForm.content">
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
      </div>
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
  name: 'Complaint',
  components: {
    NavHeader,
    PageFooter
  },
  data() {
    return {
      ownerId: 1, // 暂时固定业主id
      loading: false,
      submitting: false,
      evaluating: false,
      editing: false,
      // 投诉表单
      complaintForm: {
        content: ''
      },
      // 表单校验规则
      rules: {
        content: [
          { required: true, message: '请输入投诉内容', trigger: 'blur' },
          { min: 10, message: '内容至少10个字符', trigger: 'blur' }
        ]
      },
      // 投诉记录
      complaintRecords: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      // 评价相关
      evaluateVisible: false,
      currentComplaint: null,
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
        content: ''
      },
      editRules: {
        content: [
          { required: true, message: '请输入投诉内容', trigger: 'blur' },
          { min: 10, message: '内容至少10个字符', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getComplaintRecords()
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'currentUser', 'ownerInfo']),
  },
  methods: {
    // 获取投诉记录
    getComplaintRecords() {
      this.loading = true
      Request.get('/complaints/page', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          complainantId: this.ownerInfo.id
        }
      }).then(response => {
        if (response.code === '0') {
          this.complaintRecords = response.data.records
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

    // 提交投诉
    submitComplaint() {
      this.$refs.complaintForm.validate(valid => {
        if (valid) {
          this.submitting = true
          const data = {
            complainantId: this.ownerInfo.id,
            content: this.complaintForm.content
          }
          Request.post('/complaints', data).then(response => {
            if (response.code === '0') {
              this.$message.success('投诉提交成功')
              this.$refs.complaintForm.resetFields()
              this.getComplaintRecords()
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

    // 编辑投诉
    handleEdit(row) {
      this.currentComplaint = row
      this.editForm.id = row.id
      this.editForm.content = row.content
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
            content: this.editForm.content,
            status: 'PENDING' // 确保状态仍为待处理
          }
          
          Request.put('/complaints/edit-pending', data).then(response => {
            if (response.code === '0') {
              this.$message.success('投诉修改成功')
              this.editVisible = false
              this.getComplaintRecords()
              // 重置编辑表单
              this.$refs.editForm.resetFields()
              this.currentComplaint = null
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

    // 分页方法
    handleSizeChange(val) {
      this.pageSize = val
      this.getComplaintRecords()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getComplaintRecords()
    },

    // 状态显示方法
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待处理',
        'PROCESSING': '处理中',
        'RESOLVED': '已完成'
      }
      return statusMap[status] || '未知'
    },
    getStatusTag(status) {
      const statusMap = {
        'PENDING': 'info',
        'PROCESSING': 'warning',
        'RESOLVED': 'success'
      }
      return statusMap[status] || 'info'
    },

    // 打开评价对话框
    handleEvaluate(row) {
      this.currentComplaint = row
      this.evaluateVisible = true
    },

    // 提交评价
    submitEvaluate() {
      this.$refs.evaluateForm.validate(valid => {
        if (valid) {
          this.evaluating = true
          const data = {
            ...this.currentComplaint,
            evaluation: this.evaluateForm.evaluation
          }
          Request.put('/complaints', data)
            .then(response => {
              if (response.code === '0') {
                this.$message.success('评价提交成功')
                this.evaluateVisible = false
                this.getComplaintRecords()
                // 重置评价表单
                this.$refs.evaluateForm.resetFields()
                this.currentComplaint = null
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

    // 添加 formatDateTime 方法
    formatDateTime(date) {
      if (!date) return '-'
      return moment(date).format('YYYY-MM-DD HH:mm')
    }
  }
}
</script>

<style scoped>
/* 整体页面布局 */
.complaint-page {
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

/* 投诉表单卡片 */
.complaint-form {
  background: #fff;
  padding: 30px;
  margin-bottom: 24px;
  transition: all 0.3s;
}

.complaint-form:hover {
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
.complaint-records {
  background: #fff;
  padding: 20px;
  transition: all 0.3s;
}

.complaint-records:hover {
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

/* 分页 */
.pagination-container {
  margin-top: 20px;
  text-align: right;
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
  
  .complaint-form,
  .complaint-records {
    padding: 15px;
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