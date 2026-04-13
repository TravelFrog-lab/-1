<template>
    <div class="wrapper">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-content">
          <h2>代喂请求管理</h2>
          <div class="divider"></div>
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
          </div>
        </div>
  
        <!-- 查询栏 -->
        <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
          <el-form-item label="业主姓名">
            <el-input v-model="listQuery.ownerName" placeholder="请输入业主姓名" size="medium" />
          </el-form-item>
          <el-form-item label="代喂人员">
            <el-input v-model="listQuery.sitterName" placeholder="请输入代喂人员姓名" size="medium" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="listQuery.status" placeholder="请选择状态" clearable>
              <el-option label="待联系" value="PENDING" />
              <el-option label="已联系" value="CONTACTED" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="medium" plain icon="el-icon-search" @click="onSubmit">查询</el-button>
            <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="onReset">重置</el-button>
          </el-form-item>
        </el-form>
  
        <!-- 表格栏 -->
        <el-table 
          v-loading="listLoading" 
          :data="tableData" 
          style="width: 100%" 
          size="medium"
          :header-cell-style="{background:'#f8f9fa',color:'#606266'}"
          :cell-style="{padding:'12px 0'}"
          border>
          <template slot="empty">
            <div class="empty-block">
              <i class="el-icon-folder"></i>
              <div class="empty-tips">
                <p class="empty-text">暂无数据</p>
                <p class="empty-sub-text">可以尝试刷新或修改查询条件</p>
              </div>
            </div>
          </template>
          
          <el-table-column prop="id" label="ID" width="60" fixed></el-table-column>
          <el-table-column label="业主信息" width="180">
            <template slot-scope="scope">
              <div class="info-cell">
                <span class="main-info">{{ scope.row.owner?.user?.name }}</span>
                <span class="sub-info">{{ scope.row.owner?.user?.phone }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="代喂人员" width="180">
            <template slot-scope="scope">
              <div class="info-cell">
                <span class="main-info">{{ scope.row.sitter?.name }}</span>
                <span class="sub-info">{{ scope.row.sitter?.phone }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="服务要求"  show-overflow-tooltip></el-table-column>
          <el-table-column prop="expectedTime" label="期待服务时间" width="160">
            <template slot-scope="scope">
              <el-tooltip :content="formatTime(scope.row.expectedTime)" placement="top">
                <span>{{ formatTime(scope.row.expectedTime) }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="160">
            <template slot-scope="scope">
              <el-tooltip :content="formatTime(scope.row.createdAt)" placement="top">
                <span>{{ formatTime(scope.row.createdAt) }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template slot-scope="scope">
              <el-tooltip content="点击编辑" placement="top" :enterable="false">
                <el-button plain size="mini" type="primary" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
              </el-tooltip>
              <el-tooltip content="点击删除" placement="top" :enterable="false">
                <el-button plain size="mini" type="danger" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>
  
        <!-- 分页栏 -->
        <div class="pagination-container">
          <el-pagination
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page.sync="listQuery.pageNum"
            :page-sizes="[10, 20, 30, 50]"
            :page-size="listQuery.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total">
          </el-pagination>
        </div>
  
        <!-- 编辑弹窗 -->
        <el-dialog 
          :title="dialogForm.id ? '编辑请求' : '查看请求'" 
          :visible.sync="formVisible" 
          width="500px"
          :close-on-click-modal="false">
          <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
            <el-form-item label="业主" prop="ownerName">
              <el-input v-model="dialogForm.ownerName" disabled></el-input>
            </el-form-item>
            <el-form-item label="代喂人员" prop="sitterName">
              <el-input v-model="dialogForm.sitterName" disabled></el-input>
            </el-form-item>
            <el-form-item label="服务时间" prop="expectedTime">
              <el-date-picker
                v-model="dialogForm.expectedTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                format="yyyy-MM-dd HH:mm:ss"
                placeholder="选择期待服务时间">
              </el-date-picker>
            </el-form-item>
            <el-form-item label="服务要求" prop="description">
              <el-input type="textarea" v-model="dialogForm.description" :rows="3"></el-input>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="dialogForm.status" >
                <el-option label="待联系" value="PENDING" />
                <el-option label="已联系" value="CONTACTED" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
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
  import Request from '@/utils/request'
  import moment from 'moment'
  import excel from '@/utils/excel.js'
  
  export default {
    name: 'PetSittingRequestManager',
    data() {
      return {
        // 列表加载动画
        listLoading: true,
        // 查询参数
        listQuery: {
          ownerName: undefined,
          sitterName: undefined,
          status: undefined,
          pageNum: 1,
          pageSize: 10
        },
        // 表格数据
        tableData: [],
        total: 0,
        // 弹窗表单
        dialogForm: {
          id: undefined,
          ownerName: '',
          sitterName: '',
          expectedTime: '',
          description: '',
          status: ''
        },
        formVisible: false,
        isSubmit: false,
        // 表单校验规则
        formRules: {
          expectedTime: [
            { required: true, message: '请选择期待服务时间', trigger: 'change' }
          ],
          description: [
            { required: true, message: '请输入服务要求', trigger: 'blur' }
          ]
        },
        exportVisible: false,
      }
    },
    created() {
      this.fetchData()
    },
    methods: {
      // 获取数据列表
      fetchData() {
        this.listLoading = true
        const params = { ...this.listQuery }
        
        Request.get('/pet-sitting-request/page', { params }).then(response => {
          if (response.code === '0' && response.data) {
            this.tableData = response.data.records
            this.total = response.data.total
          } else {
            this.$message.error(response.msg || '获取数据失败')
          }
          this.listLoading = false
        }).catch(() => {
          this.listLoading = false
        })
      },
  
      // 编辑
      handleEdit(row) {
        this.dialogForm = {
          ...row,
          ownerName: row.owner?.user?.name,
          sitterName: row.sitter?.name
        }
        this.formVisible = true
      },
  
      // 删除
      handleDelete(row) {
        this.$confirm('确认删除该请求记录?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          Request.delete(`/pet-sitting-request/${row.id}`).then(response => {
            if (response.code === '0') {
              this.$message.success('删除成功')
              this.fetchData()
            } else {
              this.$message.error(response.msg || '删除失败')
            }
          })
        }).catch(() => {
          this.$message.info('已取消删除')
        })
      },
  
      // 保存
      handleSave(formName) {
        this.$refs[formName].validate(valid => {
          if (!valid) return
          
          this.isSubmit = true
          const data = {
            ...this.dialogForm,
            expectedTime: moment(this.dialogForm.expectedTime).format('YYYY-MM-DD HH:mm:ss')
          }
          
          Request.put(`/pet-sitting-request`, data).then(response => {
            if (response.code === '0') {
              this.$message.success('更新成功')

              this.formVisible = false
              this.fetchData()
            } else {
              this.$message.error(response.msg || '更新失败')
            }
          }).finally(() => {
            this.isSubmit = false
          })
        })
      },
  
      // 查询
      onSubmit() {
        this.listQuery.pageNum = 1
        this.fetchData()
      },
  
      // 重置
      onReset() {
        this.listQuery = {
          ownerName: undefined,
          sitterName: undefined,
          status: undefined,
          pageNum: 1,
          pageSize: 10
        }
        this.fetchData()
      },
  
      // 工具方法
      formatTime(time) {
        return moment(time).format('YYYY-MM-DD HH:mm:ss')
      },
      
      getStatusType(status) {
        const map = {
          'PENDING': 'warning',
          'CONTACTED': 'success',
          'COMPLETED': 'info',
          'CANCELLED': 'danger'
        }
        return map[status] || 'info'
      },
      
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
        this.listQuery.pageSize = val
        this.fetchData()
      },
      
      handleCurrentChange(val) {
        this.listQuery.pageNum = val
        this.fetchData()
      },

      // 导出数据
      exportTable(type) {
        if (this.tableData.length) {
          const params = {
            header: ['ID', '业主', '联系电话', '代喂人员', '联系方式', '服务要求', '期待服务时间', '状态', '创建时间'],
            key: ['id', 'ownerName', 'ownerPhone', 'sitterName', 'sitterPhone', 'description', 'expectedTime', 'status', 'createdAt'],
            data: this.tableData.map(item => ({
              id: item.id,
              ownerName: item.owner?.user?.name,
              ownerPhone: item.owner?.user?.phone,
              sitterName: item.sitter?.name,
              sitterPhone: item.sitter?.phone,
              description: item.description,
              expectedTime: this.formatTime(item.expectedTime),
              status: this.getStatusText(item.status),
              createdAt: this.formatTime(item.createdAt)
            })),
            autoWidth: true,
            fileName: '代喂请求数据表',
            bookType: type
          }
          excel.exportDataToExcel(params)
          this.exportVisible = false
        } else {
          this.$message.warning('暂无数据可导出')
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
  
    .search-form {
      padding: 24px 0;
      margin-bottom: 24px;
      border-bottom: 1px solid #ebeef5;
      display: flex;
      align-items: center;
      flex-wrap: wrap;
  
      .el-form-item {
        margin-bottom: 0;
        margin-right: 16px;
  
        &:last-child {
          margin-right: 0;
        }
      }
    }
  
    .pagination-container {
      margin-top: 20px;
      padding-top: 20px;
      border-top: 1px solid #ebeef5;
      text-align: right;
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

    .export-data {
      display: flex;
      flex-direction: row;
      justify-content: space-around;
      padding: 20px 0;
      
      .export-option {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 16px;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.3s;
        margin: 0 8px;

        &:hover {
          background-color: #f5f7fa;
        }

        i {
          font-size: 24px;
          margin-bottom: 8px;
          color: #409EFF;
        }

        span {
          font-size: 14px;
          color: #606266;
        }
      }
    }

    .empty-block {
      padding: 40px 0;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
      color: #909399;
      
      i {
        font-size: 60px;
        color: #dcdfe6;
        margin-bottom: 16px;
      }
      
      .empty-tips {
        .empty-text {
          font-size: 14px;
          margin-bottom: 8px;
        }
        
        .empty-sub-text {
          font-size: 12px;
          color: #c0c4cc;
        }
      }
    }

    .info-cell {
      display: flex;
      flex-direction: column;
      
      .main-info {
        font-size: 14px;
        color: #303133;
        margin-bottom: 4px;
      }
      
      .sub-info {
        font-size: 12px;
        color: #909399;
      }
    }

    // 美化表格样式
    .el-table {
      &::v-deep tr:hover > td {
        background-color: #f5f7fa !important;
      }
      
      &::v-deep td {
        padding: 12px 0;
      }
      
      &::v-deep .el-table__fixed-right::before,
      &::v-deep .el-table__fixed::before {
        background-color: #ebeef5;
      }

      .el-button + .el-button {
        margin-left: 8px;
      }

      .el-tag {
        padding: 0 8px;
        height: 24px;
        line-height: 22px;
      }
    }

    // 美化分页样式
    .pagination-container {
      padding: 20px 0;
      margin-top: 20px;
      border-top: 1px solid #ebeef5;
    }
  }
  </style>