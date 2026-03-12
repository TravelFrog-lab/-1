<template>
  <div class="wrapper">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h2>投诉建议管理</h2>
        <div class="divider"></div>
        <p class="subtitle">Complaint Management</p>
      </div>
    </div>

    <el-card class="menu-card" shadow="never">
      <!-- 操作栏 -->
      <div class="control-btns">
        <div class="left-btns">
          <el-popconfirm title="确认删除?" @confirm="batchDelete">
            <template #reference>
              <el-button type="danger" size="medium" plain icon="el-icon-delete">批量删除</el-button>
            </template>
          </el-popconfirm>
          <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="fetchData">刷新</el-button>
        </div>

        <div class="right-btns">
          <el-button type="primary" size="medium" icon="el-icon-download" @click="exportVisible = true" plain>导出数据</el-button>
        </div>
      </div>

      <!-- 查询栏 -->
      <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
        <el-form-item label="投诉人">
          <el-input v-model="listQuery.complainantName" placeholder="请输入投诉人姓名" size="medium" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="listQuery.status" placeholder="请选择状态" size="medium">
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已完成" value="RESOLVED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="medium" plain icon="el-icon-search" @click="onSubmit">查询</el-button>
          <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格栏 -->
      <el-table 
        ref="multipleTable" 
        v-loading="listLoading" 
        :data="tableData" 
        tooltip-effect="dark" 
        row-key="id"
        style="width: 100%" 
        size="medium"
        @selection-change="handleSelectionChange"
        :header-cell-style="{background:'#f8f9fa',color:'#606266'}"
        :cell-style="{padding:'12px 0'}"
        border>
        <el-table-column type="selection" width="50"></el-table-column>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="complainant.user.name" label="投诉人" width="120"></el-table-column>
        <el-table-column prop="content" label="投诉内容" show-overflow-tooltip></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handler.name" label="处理人" width="120"></el-table-column>
        <el-table-column prop="result" label="处理结果" show-overflow-tooltip></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template slot-scope="scope">
            {{ scope.row.createdAt | formatDateTime }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template slot-scope="scope">
            <el-button 
              v-if="scope.row.status === 'PENDING' && currentUser.role === 'ADMIN'"
              plain size="small" 
              type="primary" 
              icon="el-icon-edit" 
              @click="handleStart(scope.row)">开始处理</el-button>
            <el-button 
              v-if="scope.row.status === 'PROCESSING' && 
                   ((currentUser.role === 'MAINTENANCE' && scope.row.handlerId === currentUser.id) || 
                    currentUser.role === 'ADMIN')"
              plain size="small" 
              type="success" 
              icon="el-icon-check" 
              @click="handleComplete(scope.row)">处理完成</el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING'"
              plain size="small" 
              type="danger" 
              icon="el-icon-delete" 
              @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <Pagination
        v-show="total>0"
        :total="total"
        :page.sync="listQuery.currentPage"
        :limit.sync="listQuery.pageSize"
        @pagination="fetchData"
      />

      <!-- 处理弹窗 -->
      <el-dialog 
        title="处理投诉" 
        :visible.sync="handleVisible" 
        width="500px"
        :close-on-click-modal="false">
        <el-form :model="handleForm" :rules="handleRules" ref="handleForm" label-width="100px">
          <el-form-item label="处理结果" prop="result">
            <el-input type="textarea" v-model="handleForm.result" placeholder="请输入处理结果"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="handleVisible = false">取 消</el-button>
          <el-button type="primary" :loading="isSubmit" @click="submitHandle('handleForm')">确 定</el-button>
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
import {  mapGetters } from 'vuex'
import moment from 'moment'

export default {
  name: 'ComplaintManager',
  components: { Pagination },
 
  data() {
    return {
      // 数据列表加载动画
      listLoading: true,

      // 查询列表参数对象
      listQuery: {
        complainantName: undefined,
        status: undefined,
        currentPage: 1,
        pageSize: 10
      },

      // 处理表单对象
      handleForm: {
        id: undefined,
        handlerId: undefined,
        result: ''
      },

      // 数据总条数
      total: 0,
      // 表格数据数组
      tableData: [],
      // 多选数据暂存数组
      multipleSelection: [],
      // 处理弹窗显示/隐藏
      handleVisible: false,
      // 导出弹窗显示/隐藏
      exportVisible: false,

      // 处理人员列表
      handlers: [],

      // 表单校验规则
      handleRules: {
        result: [
          { required: true, message: '请输入处理结果', trigger: 'blur' },
          { min: 1, max: 500, message: '长度在1到500个字符', trigger: 'blur' }
        ]
      },

      // 防止多次连续提交表单
      isSubmit: false
    }
  },
  created() {
    this.fetchData()
    this.fetchHandlers()
  },
  computed: {
    ...mapGetters(['currentUser'])
  },
  methods: {
    // 获取状态类型
    getStatusType(status) {
      const map = {
        'PENDING': 'warning',
        'PROCESSING': 'primary',
        'RESOLVED': 'success'
      }
      return map[status]
    },

    // 获取状态文本
    getStatusText(status) {
      const map = {
        'PENDING': '待处理',
        'PROCESSING': '处理中',
        'RESOLVED': '已完成'
      }
      return map[status]
    },

    // 多选操作
    handleSelectionChange(val) {
      this.multipleSelection = val.map(v => v.id)
    },

    // 获取处理人员列表
    fetchHandlers() {
      // 获取维护人员列表
      Request.get("/user/role/MAINTENANCE").then(response => {
        if (response.code === '0') {
          this.handlers = response.data
          
          // 再获取管理员列表并合并
          Request.get("/user/role/ADMIN").then(adminResponse => {
            if (adminResponse.code === '0') {
              this.handlers = [...this.handlers, ...adminResponse.data]
            }
          })
        }
      })
    },

    // 开始处理投诉
    handleStart(row) {
      this.$confirm('确认开始处理该投诉?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        Request.put("/complaints", {
          id: row.id,
          handlerId: this.currentUser.id,
          status: 'PROCESSING'  // 更新状态为处理中
        }).then(response => {
          if (response.code === '0') {
            this.$message({
              type: 'success',
              message: '已开始处理'
            });
            this.fetchData();
          } else {
            this.$message({
              type: 'error',
              message: response.msg
            });
          }
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消'
        });
      });
    },

    // 完成处理投诉
    handleComplete(row) {
      this.handleForm = {
        id: row.id,
        handlerId: row.handlerId || this.currentUser.id,
        result: ''
      }
      this.handleVisible = true
    },

    // 提交处理结果
    submitHandle(formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) return false;
        
        this.isSubmit = true;
        Request.put("/complaints", {
          ...this.handleForm,
          status: 'RESOLVED'  // 更新状态为已完成
        }).then(response => {
          if (response.code === '0') {
            this.$message({
              type: 'success',
              message: '处理完成'
            });
            this.handleVisible = false;
            this.fetchData();
          } else {
            this.$message({
              type: 'error',
              message: response.msg
            });
          }
        }).finally(() => {
          this.isSubmit = false;
        });
      });
    },

    // 删除数据
    handleDelete(row) {
      this.$confirm('此操作将删除该投诉记录, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.delete("/complaints/" + row.id).then(response => {
          if (response.code === '0') {
            this.$message({
              type: 'success',
              message: '删除成功!'
            });
            this.fetchData();
          } else {
            this.$message({
              type: 'error',
              message: response.msg
            });
          }
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },

    // 批量删除
    batchDelete() {
      if (!this.multipleSelection || this.multipleSelection.length < 1) {
        this.$message({
          message: '请先选择要删除的数据！',
          type: 'warning'
        });
        return;
      }

      this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.delete('/complaints/batch', {
          data: this.multipleSelection
        }).then(res => {
          if (res.code === '0') {
            this.$message({
              type: 'success',
              message: '批量删除成功'
            });
            this.$refs.multipleTable.clearSelection();
            this.multipleSelection = [];
            this.fetchData();
          } else {
            this.$message({
              type: 'error',
              message: res.msg
            });
          }
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },

    // 获取数据列表
    fetchData({ page, limit } = {}) {
      if (page) this.listQuery.currentPage = page;
      if (limit) this.listQuery.pageSize = limit;
      
      this.listLoading = true;
      Request.get("/complaints/page", {
        params: {
          complainantName: this.listQuery.complainantName,
          status: this.listQuery.status,
          pageNum: this.listQuery.currentPage,
          pageSize: this.listQuery.pageSize
        }
      }).then(response => {
        if (response.code === '0') {
          const { total, records } = response.data;
          this.total = total;
          this.tableData = records;
        } else {
          this.$message({
            type: 'error',
            message: response.msg || '获取数据失败'
          });
        }
        this.listLoading = false;
      }).catch(() => {
        this.listLoading = false;
      });
    },

    // 查询数据
    onSubmit() {
      this.listQuery.currentPage = 1;
      this.fetchData();
    },

    // 重置查询
    onReset() {
      this.listQuery = {
        complainantName: undefined,
        status: undefined,
        currentPage: 1,
        pageSize: 10
      };
      this.fetchData();
    },

    // 导出数据
    exportTable(type) {
      if (this.tableData.length) {
        const params = {
          header: ['ID', '投诉人', '投诉内容', '状态', '处理人', '处理结果', '创建时间'],
          key: ['id', 'complainant.user.name', 'content', 'status', 'handler.user.name', 'result', 'createdAt'],
          data: this.tableData,
          autoWidth: true,
          fileName: '投诉记录数据表',
          bookType: type
        }
        excel.exportDataToExcel(params)
        this.exportVisible = false
      }
    }
  },
      filters: {
      formatDateTime(date) {
        if (!date) return '-'
        return moment(date).format('YYYY-MM-DD HH:mm:ss')
        
      }
    },
}
</script>

<style lang="less">
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
    border-bottom: 1px solid #ebeef5;
    display: flex;
    align-items: center;

    .el-form-item {
      margin-bottom: 0;
      margin-right: 16px;

      &:last-child {
        margin-right: 0;
      }
    }
  }

  .export-data {
    display: flex;
    justify-content: space-around;
    padding: 20px;

    .export-option {
      text-align: center;
      cursor: pointer;
      padding: 20px;
      border-radius: 4px;
      transition: all 0.3s;

      &:hover {
        background-color: #f5f7fa;
      }

      i {
        font-size: 32px;
        margin-bottom: 8px;
        color: #409EFF;
      }

      span {
        display: block;
        color: #606266;
      }
    }
  }
}
</style> 