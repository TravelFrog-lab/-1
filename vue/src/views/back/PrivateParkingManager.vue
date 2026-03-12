<template>
  <div class="wrapper">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h2>私人车位管理</h2>
        <div class="divider"></div>
        <p class="subtitle">Private Parking Management</p>
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
          <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增记录</el-button>
        </div>
      </div>

      <!-- 查询栏 -->
      <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
        <el-form-item label="车位位置">
          <el-input v-model="listQuery.location" placeholder="请输入车位位置" size="medium" />
        </el-form-item>
        <el-form-item label="业主姓名">
          <el-input v-model="listQuery.ownerName" placeholder="请输入业主姓名" size="medium" />
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
        <el-table-column prop="parkingSpace.location" label="车位位置"></el-table-column>
        <el-table-column prop="owner.user.name" label="业主姓名"></el-table-column>
        <el-table-column prop="plateInfo" label="车牌信息"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button plain size="small" type="primary" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button plain size="small" type="danger" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <Pagination
        v-show="total>0"
        :total="total"
        :page.sync="listQuery.pageNum"
        :limit.sync="listQuery.pageSize"
        @pagination="fetchData"
      />

      <!-- 新增/编辑弹窗 -->
      <el-dialog 
        :title="dialogForm.id ? '编辑记录' : '新增记录'" 
        :visible.sync="formVisible"
        width="500px"
        :close-on-click-modal="false">
        <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
          <el-form-item label="车位" prop="parkingSpaceId">
            <el-select v-model="dialogForm.parkingSpaceId" placeholder="请选择车位" filterable>
              <el-option
                v-for="item in parkingSpaces"
                :key="item.id"
                :label="item.location"
                :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="业主" prop="ownerId">
            <el-select v-model="dialogForm.ownerId" placeholder="请选择业主" filterable @change="handleOwnerChange">
              <el-option
                v-for="item in owners"
                :key="item.id"
                :label="item.user.name"
                :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="车牌信息" prop="plateInfo">
            <el-input v-model="dialogForm.plateInfo" placeholder="请输入车牌信息"></el-input>
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

export default {
  name: 'PrivateParkingManager',
  components: { Pagination },
  data() {
    return {
      listLoading: true,
      formLabelWidth: '80px',

      // 查询列表参数
      listQuery: {
        location: undefined,
        ownerName: undefined,
        pageNum: 1,
        pageSize: 10
      },

      // 表单对象
      dialogForm: {
        id: undefined,
        parkingSpaceId: undefined,
        ownerId: undefined,
        plateInfo: ''
      },

      // 数据总数
      total: 0,
      // 表格数据
      tableData: [],
      // 车位列表
      parkingSpaces: [],
      // 业主列表
      owners: [],
      // 多选数据
      multipleSelection: [],
      // 弹窗显示
      formVisible: false,
      exportVisible: false,
      
      // 表单校验规则
      formRules: {
        parkingSpaceId: [
          { required: true, message: '请选择车位', trigger: 'change' }
        ],
        ownerId: [
          { required: true, message: '请选择业主', trigger: 'change' }
        ],
        plateInfo: [
          { required: true, message: '请输入车牌信息', trigger: 'blur' },
          { min: 7, max: 8, message: '车牌号长度必须为7-8位', trigger: 'blur' }
        ]
      },

      isSubmit: false
    }
  },
  created() {
    this.fetchData()
    this.loadParkingSpaces()
    this.loadOwners()
  },
  methods: {
    // 加载车位列表
    loadParkingSpaces() {
      Request.get('/parking-space/type/PRIVATE').then(response => {
        if (response.code === '0') {
          this.parkingSpaces = response.data
        }else{
          this.$message({
            type: 'error',
            message: response.msg
          })
        }
      })
    },

    // 加载业主列表
    loadOwners() {
      Request.get('/owner/all').then(response => {
        if (response.code === '0') {
          this.owners = response.data
        }else{
          this.$message({
            type: 'error',
            message: response.msg
          })
        }

      })
    },

    // 处理业主选择变化
    handleOwnerChange(ownerId) {
      const selectedOwner = this.owners.find(owner => owner.id === ownerId)
      if (selectedOwner && selectedOwner.plateNumber) {
        this.dialogForm.plateInfo = selectedOwner.plateNumber
      } else {
        this.dialogForm.plateInfo = ''
      }
    },

    // 多选操作
    handleSelectionChange(val) {
      this.multipleSelection = val.map(v => v.id)
    },

    // 新建数据
    handleCreate() {
      this.dialogForm = {
        parkingSpaceId: undefined,
        ownerId: undefined,
        plateInfo: ''
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
      this.$confirm('此操作将删除该记录, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.delete("/private-parking/" + row.id).then(response => {
          if (response.code === '0') {
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            this.fetchData()
          } else {
            this.$message({
              type: 'error',
              message: response.msg
            })
          }
        })
      })
    },

    // 批量删除
    batchDelete() {
      if (!this.multipleSelection.length) {
        this.$message({
          message: '请先选择要删除的数据！',
          type: 'warning'
        })
        return
      }

      this.$confirm('此操作将删除选中记录, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.delete('/private-parking/batch', {
          data: this.multipleSelection
        }).then(res => {
          if (res.code === '0') {
            this.$message({
              type: 'success',
              message: '批量删除成功'
            })
            this.$refs.multipleTable.clearSelection()
            this.multipleSelection = []
            this.fetchData()
          } else {
            this.$message({
              type: 'error',
              message: res.msg
            })
          }
        })
      })
    },

    // 获取数据列表
    fetchData({ page, limit } = {}) {
      if (page) this.listQuery.pageNum = page
      if (limit) this.listQuery.pageSize = limit
      
      this.listLoading = true
      Request.get("/private-parking/page", {
        params: {
          pageNum: this.listQuery.pageNum,
          pageSize: this.listQuery.pageSize,
          location: this.listQuery.location,
          ownerName: this.listQuery.ownerName
        }
      }).then(response => {
        if (response.code === '0') {
          const { total, records } = response.data
          this.total = total
          this.tableData = records
        } else {
          this.$message({
            type: 'error',
            message: response.msg || '获取数据失败'
          })
        }
        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
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
        location: undefined,
        ownerName: undefined,
        pageNum: 1,
        pageSize: 10
      }
      this.fetchData()
    },

    // 保存数据
    handleSave(formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) return false
        
        this.isSubmit = true
        const request = this.dialogForm.id 
          ? Request.put("/private-parking", this.dialogForm)
          : Request.post("/private-parking", this.dialogForm)


        request.then(response => {
          if (response.code === '0') {
            this.$message({
              type: 'success',
              message: this.dialogForm.id ? '更新成功' : '添加成功'
            })
            this.formVisible = false
            this.fetchData()
          } else {
            this.$message({
              type: 'error',
              message: response.msg
            })
          }
        }).finally(() => {
          this.isSubmit = false
        })
      })
    },

    // 导出数据
    exportTable(type) {
      if (this.tableData.length) {
        const params = {
          header: ['ID', '车位位置', '业主姓名', '车牌信息'],
          key: ['id', 'parkingSpace.location', 'owner.user.name', 'plateInfo'],
          data: this.tableData,
          autoWidth: true,
          fileName: '私人车位使用记录',
          bookType: type
        }
        excel.exportDataToExcel(params)
        this.exportVisible = false
      }
    }
  }
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
    .export-option {
      display: flex;
      align-items: center;
      padding: 12px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background-color: #f5f7fa;
      }

      i {
        font-size: 20px;
        margin-right: 8px;
      }
    }
  }
}
</style> 