<template>
  <div class="wrapper">
    <div class="page-header">
      <div class="header-content">
        <h2>志愿活动报名管理</h2>
        <div class="divider"></div>
        <p class="subtitle">Volunteer Registration Management</p>
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
        <el-form-item label="活动名称">
          <el-input v-model="listQuery.activityName" placeholder="请输入活动名称" size="medium" />
        </el-form-item>
        <el-form-item label="志愿者姓名">
          <el-input v-model="listQuery.volunteerName" placeholder="请输入志愿者姓名" size="medium" />
        </el-form-item>
        <el-form-item label="报名状态">
          <el-select v-model="listQuery.status" placeholder="请选择状态" size="medium">
            <el-option label="已报名" value="REGISTERED" />
            <el-option label="已签到" value="CHECKED_IN" />
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
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="activity.name" label="活动名称"></el-table-column>
        <el-table-column prop="activity.startTime" label="活动开始时间" width="180">
          <template slot-scope="scope">
            {{ scope.row.activity.startTime | formatDate }}
          </template>
        </el-table-column>
        <el-table-column prop="activity.endTime" label="活动结束时间" width="180">
          <template slot-scope="scope">
            {{ scope.row.activity.endTime | formatDate }}
          </template>
        </el-table-column>
        <el-table-column prop="volunteer.user.name" label="志愿者姓名"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'REGISTERED' ? 'success' : 'info'">
              {{ scope.row.status === 'REGISTERED' ? '已报名' : '已签到' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="报名时间" width="200">
          <template slot-scope="scope">
            {{ scope.row.createdAt | formatDate }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template slot-scope="scope">
            <!-- 签到按钮 -->
            <el-button 
              plain 
              size="small" 
              type="primary" 
              icon="el-icon-check" 
              @click="handleCheckIn(scope.row)"
              :disabled="scope.row.status === 'CHECKED_IN' || isActivityStarted(scope.row.activity.startTime) === false"
            >签到</el-button>
            <!-- 取消报名按钮 -->
            <el-button 
              plain 
              size="small" 
              type="danger" 
              icon="el-icon-delete" 
              @click="handleCancel(scope.row)"
              :disabled="isActivityStarted(scope.row.activity.startTime) || scope.row.status === 'CHECKED_IN'"
            >取消报名</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <Pagination v-show="total>0" :total="total" :page.sync="listQuery.pageNum" :limit.sync="listQuery.pageSize" @pagination="fetchData" />

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
  name: 'VolunteerRegistrationManager',
  components: { Pagination },
  filters: {
    formatDate(time) {
      return moment(time).format('YYYY-MM-DD HH:mm:ss')
    }
  },
  data() {
    return {
      // 数据列表加载动画
      listLoading: true,

      // 查询列表参数对象
      listQuery: {
        activityName: undefined,
        volunteerName: undefined,
        status: undefined,
        pageNum: 1,
        pageSize: 10
      },

      // 数据总条数
      total: 0,
      // 表格数据数组
      tableData: [],
      // 导出数据 弹出框显示/隐藏
      exportVisible: false
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    // 判断活动是否已开始
    isActivityStarted(startTime) {
      return new Date(startTime) <= new Date()
    },

    // 取消报名
    handleCancel(row) {
      if (this.isActivityStarted(row.activity.startTime)) {
        this.$message.warning('活动已开始，无法取消报名')
        return
      }

      if (row.status === 'CHECKED_IN') {
        this.$message.warning('已签到的活动无法取消报名')
        return
      }

      this.$confirm('确认取消报名该活动?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.post(`/volunteer-activity/${row.activity.id}/cancel-registration?volunteerId=${row.volunteer.id}`).then(response => {
          if (response.code === '0') {
            this.$message({
              type: 'success',
              message: '取消报名成功!'
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
          message: '已取消操作'
        });
      });
    },

    // 获取数据列表
    fetchData({ page, limit } = {}) {
      if (page) this.listQuery.pageNum = page;
      if (limit) this.listQuery.pageSize = limit;

      this.listLoading = true;
      Request.get("/volunteer-registration/page", {
        params: {
          name: this.listQuery.activityName,
          volunteerName: this.listQuery.volunteerName,
          status: this.listQuery.status,
          pageNum: this.listQuery.pageNum,
          pageSize: this.listQuery.pageSize,
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
      this.listQuery.pageNum = 1;
      this.fetchData();
    },

    // 重置查询
    onReset() {
      this.listQuery = {
        activityName: undefined,
        volunteerName: undefined,
        status: undefined,
        pageNum: 1,
        pageSize: 10
      };
      this.fetchData();
    },

    // 导出数据
    exportTable(type) {
      if (this.tableData.length) {
        const params = {
          header: ['ID', '活动名称', '活动开始时间', '活动结束时间', '志愿者姓名', '状态', '报名时间'],
          key: ['id', 'activity.name', 'activity.startTime', 'activity.endTime', 'volunteer.user.name', 'status', 'createdAt'],
          data: this.tableData,
          autoWidth: true,
          fileName: '志愿活动报名数据',
          bookType: type
        }
        excel.exportDataToExcel(params)
        this.exportVisible = false
      }
    },

    // 签到
    handleCheckIn(row) {
      if (!this.isActivityStarted(row.activity.startTime)) {
        this.$message.warning('活动未开始，无法签到')
        return
      }
      
      if (row.status === 'CHECKED_IN') {
        this.$message.warning('已完成签到')
        return
      }

      this.$confirm('确认签到该活动?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.post(`/volunteer-registration/${row.id}/check-in`).then(response => {
          if (response.code === '0') {
            this.$message({
              type: 'success',
              message: '签到成功!'
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
          message: '已取消操作'
        });
      });
    },
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

      span {
        font-size: 14px;
        color: #606266;
      }
    }
  }
}
</style> 