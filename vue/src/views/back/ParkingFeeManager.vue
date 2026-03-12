<template>
  <div class="wrapper">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h2>停车费管理</h2>
        <div class="divider"></div>
        <p class="subtitle">Parking Fee Management</p>
      </div>
    </div>

    <el-card class="menu-card" shadow="never">
      <!-- 操作栏 -->
      <div class="control-btns">
        <div class="left-btns">
          <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="fetchData">刷新</el-button>
        </div>

        <div class="right-btns">
          <el-button type="primary" size="medium" icon="el-icon-download" @click="exportVisible = true" plain>导出数据</el-button>
          <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增记录</el-button>
        </div>
      </div>

      <!-- 查询栏 -->
      <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
        <el-form-item label="车牌号">
          <el-input v-model="listQuery.plateNumber" placeholder="请输入车牌号" size="medium" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="listQuery.status" placeholder="请选择状态" size="medium">
            <el-option label="未缴费" value="UNPAID" />
            <el-option label="已缴费" value="PAID" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="medium" plain icon="el-icon-search" @click="onSubmit">查询</el-button>
          <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格栏 -->
      <el-table ref="multipleTable" v-loading="listLoading" :data="tableData" tooltip-effect="dark" row-key="id" style="width: 100%" size="medium"
        :header-cell-style="{background:'#f8f9fa',color:'#606266'}" :cell-style="{padding:'12px 0'}" border>
        <el-table-column prop="parkingSpace.location" label="车位位置" />
        <el-table-column prop="plateNumber" label="车牌号" />
        <el-table-column prop="ownerName" label="车主姓名" />
        <el-table-column prop="ownerPhone" label="联系电话" />
        <el-table-column prop="entryTime" label="入场时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.entryTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="exitTime" label="出场时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.exitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="feeAmount" label="费用金额">
          <template slot-scope="scope">
            {{ scope.row.feeAmount ? `￥${scope.row.feeAmount}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'PAID' ? 'success' : 'warning'">
              {{ scope.row.status === 'PAID' ? '已缴费' : '未缴费' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="!scope.row.exitTime" plain size="small" type="success" icon="el-icon-check" @click="handleCheckout(scope.row)">出场</el-button>
            <el-button v-if="scope.row.status === 'UNPAID' && scope.row.exitTime" plain size="small" type="primary" icon="el-icon-money"
              @click="handlePay(scope.row)">缴费</el-button>
            <el-button v-if="scope.row.status === 'UNPAID' && !scope.row.exitTime" plain size="small" type="primary" icon="el-icon-edit"
              @click="handleEdit(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.status === 'PAID' && !scope.row.exitTime" plain size="small" type="danger" icon="el-icon-delete"
              @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <Pagination v-show="total>0" :total="total" :page.sync="listQuery.pageNum" :limit.sync="listQuery.pageSize" @pagination="fetchData" />

      <!-- 新增/编辑弹窗 -->
      <el-dialog :title="dialogForm.id ? '编辑停车记录' : '新增停车记录'" :visible.sync="formVisible" width="500px" :close-on-click-modal="false">
        <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
          <el-form-item label="车位" prop="parkingSpaceId">
            <el-select v-model="dialogForm.parkingSpaceId" placeholder="请选择车位" filterable>
              <el-option v-for="item in parkingSpaces" :key="item.id" :label="item.location" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="车牌号" prop="plateNumber">
            <el-input v-model="dialogForm.plateNumber" placeholder="请输入车牌号" />
          </el-form-item>
          <el-form-item label="车主姓名" prop="ownerName">
            <el-input v-model="dialogForm.ownerName" placeholder="请输入车主姓名" />
          </el-form-item>
          <el-form-item label="联系电话" prop="ownerPhone">
            <el-input v-model="dialogForm.ownerPhone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item  label="入场时间" prop="entryTime">
            <el-date-picker :disabled="!!dialogForm.id" v-model="dialogForm.entryTime" type="datetime" placeholder="选择入场时间" value-format="yyyy-MM-dd HH:mm:ss" />
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="formVisible = false">取 消</el-button>
          <el-button type="primary" :loading="isSubmit" @click="handleSave('dialogForm')">确 定</el-button>
        </div>
      </el-dialog>

      <!-- 导出对话框 -->
      <el-dialog title="导出数据" :visible.sync="exportVisible" width="400px">
        <div class="export-data">
          <div class="export-option" @click="exportTable('xlsx')">
            <i class="el-icon-document"></i>
            <span>EXCEL格式</span>
          </div>
          <div class="export-option" @click="exportTable('csv')">
            <i class="el-icon-tickets"></i>
            <span>CSV格式</span>
          </div>
          <div class="export-option" @click="exportTable('txt')">
            <i class="el-icon-document-copy"></i>
            <span>TXT格式</span>
          </div>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import excel from '@/utils/excel.js'
import Pagination from '@/components/Pagination/index.vue'
import Request from '@/utils/request.js'
import moment from 'moment'

export default {
  name: 'ParkingFeeManager',
  components: { Pagination },
  data() {
    return {
      // 数据列表加载动画
      listLoading: true,

      // 查询列表参数对象
      listQuery: {
        plateNumber: undefined,
        status: undefined,
        pageNum: 1,
        pageSize: 10
      },

      // 新增/编辑提交表单对象
      dialogForm: {
        id: undefined,
        parkingSpaceId: undefined,
        plateNumber: '',
        ownerName: '',
        ownerPhone: '',
        entryTime: undefined
      },

      // 表单校验规则
      formRules: {
        parkingSpaceId: [
          { required: true, message: '请选择车位', trigger: 'change' }
        ],
        plateNumber: [
          { required: true, message: '请输入车牌号', trigger: 'blur' },
          { pattern: /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{4,5}[A-Z0-9挂学警港澳]$/, message: '请输入正确的车牌号', trigger: 'blur' }
        ],
        ownerName: [
          { required: true, message: '请输入车主姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        ownerPhone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        entryTime: [
          { required: true, message: '请选择入场时间', trigger: 'change' },
          {
            validator: (rule, value, callback) => {
              if (value && moment(value).isAfter(moment())) {
                callback(new Error('入场时间不能大于当前时间'))
              } else {
                callback()
              }
            },
            trigger: 'change'
          }
        ]
      },

      // 数据总条数
      total: 0,
      // 表格数据数组
      tableData: [],
      // 新增/编辑弹出框显示/隐藏
      formVisible: false,
      // 导出弹出框显示/隐藏
      exportVisible: false,
      // 防止多次连续提交表单
      isSubmit: false,
      // 车位列表
      parkingSpaces: []
    }
  },
  created() {
    this.fetchData()
    this.fetchParkingSpaces()
  },
  methods: {
    formatDateTime(date) {
      return date ? moment(date).format('YYYY-MM-DD HH:mm:ss') : '-'
    },
    // 获取车位列表
    fetchParkingSpaces() {
      Request.get('/parking-space/type/PUBLIC').then(response => {
        if (response.code === '0') {
          this.parkingSpaces = response.data
        }
      })
    },
    // 获取数据列表
    fetchData({ page, limit } = {}) {
      if (page) this.listQuery.pageNum = page
      if (limit) this.listQuery.pageSize = limit

      this.listLoading = true
      Request.get('/parking-fee/page', {
        params: this.listQuery
      }).then(response => {
        if (response.code === '0') {
          const { total, records } = response.data
          this.total = total
          this.tableData = records
        }
        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
      })
    },

    // 新建数据
    handleCreate() {
      this.dialogForm = {
        parkingSpaceId: undefined,
        plateNumber: '',
        ownerName: '',
        ownerPhone: '',
        entryTime: undefined
      }
      this.formVisible = true
    },

    // 编辑数据
    handleEdit(row) {
      this.dialogForm = JSON.parse(JSON.stringify(row))
      this.formVisible = true
    },

    // 删除数据
    handleDelete(row) {
      this.$confirm('此操作将删除该停车记录, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.delete(`/parking-fee/${row.id}`).then(response => {
          if (response.code === '0') {
            this.$message.success('删除成功')
            this.fetchData()
          } else {
            this.$message.error(response.msg)
          }
        })
      })
    },

    // 车辆出场
    handleCheckout(row) {
      this.$confirm('确认车辆出场?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.post(`/parking-fee/${row.id}/checkout`).then(response => {
          if (response.code === '0') {
            this.$message.success(`出场成功，应付金额：￥${response.data}`)
            this.fetchData()
          } else {
            this.$message.error(response.msg)
          }
        })
      })
    },

    // 缴费
    handlePay(row) {
      this.$confirm(`确认收取停车费 ￥${row.feeAmount}?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.post(`/parking-fee/${row.id}/pay`).then(response => {
          if (response.code === '0') {
            this.$message.success('缴费成功')
            this.fetchData()

          } else {
            this.$message.error(response.msg)
          }
        })
      })
    },

    // 保存数据
    handleSave(formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) return

        this.isSubmit = true
        const request = this.dialogForm.id
          ? Request.put(`/parking-fee`, this.dialogForm)
          : Request.post('/parking-fee', this.dialogForm)

        request.then(response => {
          if (response.code === '0') {
            this.$message.success(this.dialogForm.id ? '更新成功' : '添加成功')
            this.formVisible = false
            this.fetchData()
          } else {
            this.$message.error(response.msg)
          }
        }).finally(() => {
          this.isSubmit = false
        })
      })
    },

    // 查询数据
    onSubmit() {
      this.listQuery.pageNum = 1
      this.fetchData()
    },

    // 重置查询
    onReset() {
      this.listQuery = {
        plateNumber: undefined,
        status: undefined,
        pageNum: 1,
        pageSize: 10
      }
      this.fetchData()
    },

    // 导出数据
    exportTable(type) {
      if (this.tableData.length) {
        const params = {
          header: ['车位位置', '车牌号', '车主姓名', '联系电话', '入场时间', '出场时间', '费用金额', '状态'],
          key: ['parkingSpace.location', 'plateNumber', 'ownerName', 'ownerPhone', 'entryTime', 'exitTime', 'feeAmount', 'status'],
          data: this.tableData,
          autoWidth: true,
          fileName: '停车费记录',
          bookType: type
        }
        excel.exportDataToExcel(params)
        this.exportVisible = false
      }
    }
  }
}
</script>

<style lang="less" scoped>
.wrapper {
  padding: 20px;
  min-height: 100vh;
  background-color: #ffffff;
  border: 1px solid #dcdfe6;

  .page-header {
    margin-bottom: 32px;

    .header-content {
      display: flex;
      align-items: center;

      h2 {
        margin: 0;
        font-size: 28px;
        font-weight: 500;
        color: #1a1a1a;
        letter-spacing: 1px;
      }

      .divider {
        width: 2px;
        height: 24px;
        background-color: #dcdfe6;
        margin: 0 16px;
      }

      .subtitle {
        margin: 0;
        color: #909399;
        font-size: 16px;
        letter-spacing: 0.5px;
        text-transform: uppercase;
      }
    }
  }

  .control-btns {
    display: flex;
    justify-content: space-between;
    margin-bottom: 24px;
    align-items: center;

    .left-btns {
      display: flex;
      align-items: center;
      gap: 8px;

      .el-button {
        min-width: 88px;
      }
    }

    .right-btns {
      display: flex;
      gap: 8px;

      .el-button {
        min-width: 100px;
      }
    }
  }

  .search-form {
    padding: 24px 0;
    margin-bottom: 24px;
    display: flex;
    align-items: center;
    justify-content: flex-start;

    .el-form-item {
      margin-bottom: 0;
      margin-right: 16px;

      &:last-child {
        margin-right: 0;
      }
    }
  }

  .export-data {
    padding: 16px;

    .export-option {
      display: flex;
      align-items: center;
      padding: 16px;
      cursor: pointer;
      transition: all 0.3s;
      border-radius: 4px;

      &:hover {
        background-color: #f5f7fa;
      }

      i {
        font-size: 24px;
        margin-right: 12px;
        color: #409eff;
      }

      span {
        font-size: 16px;
        color: #606266;
      }
    }
  }
}

// 表格内容样式优化
.el-table {
  :deep(th) {
    background-color: #f8f9fa !important;
    color: #606266;
    font-weight: 500;
    padding: 12px 0;
  }

  :deep(td) {
    padding: 12px 0;
  }
}

// 弹窗样式优化
.el-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 24px;
    border-bottom: 1px solid #ebeef5;
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }

  :deep(.el-dialog__footer) {
    padding: 16px 24px;
    border-top: 1px solid #ebeef5;
  }
}

// 表单项间距
.el-form {
  .el-form-item {
    margin-bottom: 24px;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

// 按钮组样式
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

// 状态标签样式优化
.el-tag {
  padding: 0 12px;
  height: 28px;
  line-height: 26px;
  border-radius: 4px;
  font-weight: 500;
}
</style> 