<template>
  <div class="wrapper">
    <div class="page-header">
      <div class="header-content">
        <h2>通知管理</h2>
        <div class="divider"></div>
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
          <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>发送通知</el-button>
        </div>
      </div>

      <!-- 查询栏 -->
      <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
        <el-form-item label="通知标题">
          <el-input v-model="listQuery.title" placeholder="请输入通知标题" size="medium" />
        </el-form-item>
        <el-form-item label="接收人类型">
          <el-select v-model="listQuery.receiverType" placeholder="请选择接收人类型" size="medium">
            <el-option label="业主" value="OWNER" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="维修人员" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="medium" plain icon="el-icon-search" @click="onSubmit">查询</el-button>
          <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格区 -->
      <el-table ref="multipleTable" v-loading="listLoading" :data="tableData" tooltip-effect="dark" row-key="id" style="width: 100%" size="medium"
        @selection-change="handleSelectionChange" :header-cell-style="{background:'#f8f9fa',color:'#606266'}" :cell-style="{padding:'12px 0'}" border>
        <el-table-column type="selection" width="50"></el-table-column>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="通知标题" show-overflow-tooltip></el-table-column>
        <el-table-column prop="content" label="通知内容" show-overflow-tooltip></el-table-column>
        <!-- 接收人姓名 -->
        <el-table-column label="接收人" show-overflow-tooltip>
          <template slot-scope="scope">
            <div style="display: flex; align-items: center; gap: 8px;">
              <span>{{ scope.row.receiver?.name }}</span>
              <el-tag size="mini" :type="getReceiverTypeTag(scope.row.receiver?.role)">
                {{ getReceiverTypeLabel(scope.row.receiver?.role) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="receiverType" label="发送类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.receiverType ? 'warning' : 'success'">
              {{ scope.row.receiverType ? '群发' : '单发' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isRead ? 'success' : 'info'" size="mini">
              {{ scope.row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发送时间" width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button plain size="small" type="primary" icon="el-icon-view" @click="handleView(scope.row)">查看</el-button>
            <el-button plain size="small" type="danger" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <Pagination v-show="total>0" :total="total" :page.sync="listQuery.currentPage" :limit.sync="listQuery.pageSize" @pagination="fetchData" />

      <!-- 发送通知弹窗 -->
      <el-dialog title="发送通知" :visible.sync="formVisible" width="600px" :close-on-click-modal="false">
        <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
          <el-form-item label="发送方式" prop="sendType">
            <el-radio-group v-model="dialogForm.sendType">
              <el-radio label="single">选择用户</el-radio>
              <el-radio label="batch">按角色发送</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 选择用户发送 -->
          <el-form-item label="接收用户" prop="receiverId" v-if="dialogForm.sendType === 'single'">
            <el-select v-model="dialogForm.receiverId" filterable remote reserve-keyword placeholder="请输入用户名，姓名或手机号搜索" :remote-method="remoteSearchUser"
              :loading="userLoading">
              <el-option v-for="item in userOptions" :key="item.id" :label="item.username + ' - ' + item.name + ' - ' + item.phone" :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>

          <!-- 按角色发送 -->
          <el-form-item label="接收角色" prop="receiverType" v-if="dialogForm.sendType === 'batch'">
            <el-select v-model="dialogForm.receiverType" placeholder="请选择接收人角色">
              <el-option label="业主" value="OWNER" />
              <el-option label="管理员" value="ADMIN" />
              <el-option label="维修人员" value="MAINTENANCE" />
            </el-select>
          </el-form-item>

          <el-form-item label="通知标题" prop="title">
            <el-input v-model="dialogForm.title" placeholder="请输入通知标题"></el-input>
          </el-form-item>

          <el-form-item label="通知内容" prop="content">
            <el-input type="textarea" v-model="dialogForm.content" placeholder="请输入通知内容" :rows="4">
            </el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="formVisible = false">取 消</el-button>
          <el-button type="primary" :loading="isSubmit" @click="handleSave('dialogForm')">发 送</el-button>
        </div>
      </el-dialog>

      <!-- 查看通知弹窗 -->
      <el-dialog title="通知详情" :visible.sync="viewVisible" width="500px">
        <div class="notification-detail">
          <h3>{{ viewData.title }}</h3>
          <div class="meta-info">
            <span>发送类型：{{ viewData.receiverType ? '群发' : '单发' }}</span>
            <span>接收人：{{ viewData.receiverType ? getReceiverTypeLabel(viewData.receiverType) + '的所有用户' : viewData.receiver?.name }}</span>
            <span>状态：{{ viewData.isRead ? '已读' : '未读' }}</span>
            <span>发送时间：{{ formatDateTime(viewData.createdAt) }}</span>
          </div>
          <div class="content">{{ viewData.content }}</div>
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
  name: 'NotificationManager',
  components: { Pagination },
  data() {
    return {
      // 列表加载状态
      listLoading: true,
      // 查询参数
      listQuery: {
        title: undefined,
        receiverType: undefined,
        currentPage: 1,
        pageSize: 10
      },
      // 表格数据
      tableData: [],
      total: 0,
      // 多选数据
      multipleSelection: [],
      // 弹窗显示控制
      formVisible: false,
      viewVisible: false,
      exportVisible: false,
      // 表单对象
      dialogForm: {
        sendType: 'single',
        receiverId: undefined,
        receiverType: undefined,
        title: '',
        content: ''
      },
      // 表单校验规则
      formRules: {
        title: [
          { required: true, message: '请输入通知标题', trigger: 'blur' },
          { max: 200, message: '标题长度不能超过200个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入通知内容', trigger: 'blur' }
        ],
        receiverId: [
          { required: true, message: '请选择接收用户', trigger: 'change' }
        ],
        receiverType: [
          { required: true, message: '请选择接收角色', trigger: 'change' }
        ]
      },
      // 用户选择相关
      userLoading: false,
      userOptions: [],
      // 查看详情数据
      viewData: {},
      // 提交状态
      isSubmit: false
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    // 获取接收人类型标签样式
    getReceiverTypeTag(type) {
      const map = {
        'OWNER': 'success',
        'ADMIN': 'primary',
        'MAINTENANCE': 'warning'
      }
      return map[type] || ''
    },
    // 获取接收人类型显示文本
    getReceiverTypeLabel(type) {
      const map = {
        'OWNER': '业主',
        'ADMIN': '管理员',
        'MAINTENANCE': '维修人员'
      }
      return map[type] || type
    },
    // 远程搜索用户
    remoteSearchUser(query) {
      if (query !== '') {
        this.userLoading = true
        Request.get('/user/search', {
          params: { keyword: query }
        }).then(response => {
          if (response.code === '0') {
            this.userOptions = response.data
          }
          this.userLoading = false
        })
      } else {
        this.userOptions = []
      }
    },
    // 获取数据列表
    fetchData({ page, limit } = {}) {
      if (page) this.listQuery.currentPage = page
      if (limit) this.listQuery.pageSize = limit

      this.listLoading = true
      Request.get("/notification/page", {
        params: {
          ...this.listQuery
        }
      }).then(response => {
        if (response.code === '0') {
          const { total, records } = response.data
          this.total = total
          this.tableData = records
        }
        this.listLoading = false
      })
    },
    // 查询
    onSubmit() {
      this.listQuery.currentPage = 1
      this.fetchData()
    },
    // 重置
    onReset() {
      this.listQuery = {
        title: undefined,
        receiverType: undefined,
        currentPage: 1,
        pageSize: 10
      }
      this.fetchData()
    },
    // 多选操作
    handleSelectionChange(val) {
      this.multipleSelection = val.map(v => v.id)
    },
    // 新建通知
    handleCreate() {
      this.dialogForm = {
        sendType: 'single',
        receiverId: undefined,
        receiverType: undefined,
        title: '',
        content: ''
      }
      this.formVisible = true
    },
    // 查看通知
    handleView(row) {
      this.viewData = row
      this.viewVisible = true
    },
    // 删除通知
    handleDelete(row) {
      this.$confirm('确认删除该通知?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.delete(`/notification/${row.id}`).then(response => {
          if (response.code === '0') {
            this.$message.success('删除成功')
            this.fetchData()
          }
        })
      })
    },
    // 批量删除
    batchDelete() {
      if (!this.multipleSelection.length) {
        this.$message.warning('请选择要删除的通知')
        return
      }
      this.$confirm('确认删除选中的通知?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.delete('/notification/batch', {
          data: this.multipleSelection
        }).then(response => {
          if (response.code === '0') {
            this.$message.success('批量删除成功')
            this.fetchData()
          }
        })
      })
    },
    // 保存/发送通知
    handleSave(formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) return

        this.isSubmit = true
        const api = this.dialogForm.sendType === 'single'
          ? '/notification'
          : '/notification/send-to-all'

        const data = this.dialogForm.sendType === 'single'
          ? {
            receiverId: this.dialogForm.receiverId,
            title: this.dialogForm.title,
            content: this.dialogForm.content
          }
          : {
            receiverType: this.dialogForm.receiverType,
            title: this.dialogForm.title,
            content: this.dialogForm.content
          }

        Request.post(api, data).then(response => {
          if (response.code === '0') {
            this.$message.success('发送成功')
            this.formVisible = false
            this.fetchData()
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
          header: ['ID', '通知标题', '通知内容', '接收人', '发送类型', '状态', '发送时间'],
          key: ['id', 'title', 'content', 'receiver.name', 'receiverType', 'isRead'],
          data: this.tableData.map(item => ({
            ...item,
            isRead: item.isRead ? '已读' : '未读'
          })),
          autoWidth: true,
          fileName: '通知数据表',
          bookType: type
        }
        excel.exportDataToExcel(params)
        this.exportVisible = false
      }
    },
    // 修改格式化时间的方法
    formatDateTime(date) {
      return date ? moment(date).format('YYYY-MM-DD HH:mm:ss') : ''
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

  .notification-detail {
    padding: 20px;

    h3 {
      margin: 0 0 16px;
      font-size: 20px;
      color: #303133;
    }

    .meta-info {
      margin-bottom: 16px;
      color: #909399;
      font-size: 14px;

      span {
        margin-right: 16px;
      }
    }

    .content {
      line-height: 1.6;
      color: #606266;
    }
  }

  .export-data {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .export-option {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background-color: #f5f7fa;
      }

      i {
        font-size: 20px;
        color: #409eff;
      }
    }
  }
}
</style> 