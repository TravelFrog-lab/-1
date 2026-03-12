<template>
  <div class="wrapper">
    <div class="page-header">
      <div class="header-content">
        <h2>投票表决管理</h2>
        <div class="divider"></div>
        <p class="subtitle">Vote Management</p>
      </div>
    </div>

    <el-card class="menu-card" shadow="never">
      <div class="control-btns">
        <div class="left-btns">
          <el-popconfirm title="确认删除所选投票?" @confirm="batchDelete">
            <template #reference>
              <el-button type="danger" size="medium" plain icon="el-icon-delete">批量删除</el-button>
            </template>
          </el-popconfirm>
          <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="fetchData">刷新</el-button>
        </div>
        <div class="right-btns">
          <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增投票</el-button>
        </div>
      </div>

      <el-form :inline="true" :model="listQuery" class="search-form">
        <el-form-item label="投票主题">
          <el-input v-model="listQuery.title" placeholder="请输入主题" size="medium" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="medium" plain icon="el-icon-search" @click="fetchData">查询</el-button>
          <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        ref="multipleTable"
        v-loading="listLoading"
        :data="tableData"
        row-key="id"
        style="width: 100%"
        size="medium"
        @selection-change="handleSelectionChange"
        :header-cell-style="{background:'#f8f9fa',color:'#606266'}"
        border>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="投票主题" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="90">
          <template slot-scope="scope">
            {{ scope.row.voteType === 'SINGLE' ? '单选' : '多选' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="statusTag(scope.row)">{{ statusText(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="165">
          <template slot-scope="scope">{{ formatDateTime(scope.row.startTime) }}</template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="165">
          <template slot-scope="scope">{{ formatDateTime(scope.row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button plain size="small" type="success" icon="el-icon-data-analysis" @click="showResult(scope.row)">结果</el-button>
            <el-button plain size="small" type="primary" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button plain size="small" type="danger" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-show="total > 0"
        :total="total"
        :page.sync="listQuery.pageNum"
        :limit.sync="listQuery.pageSize"
        @pagination="fetchData"
      />

      <!-- 新增/编辑弹窗 -->
      <el-dialog
        :title="dialogForm.id ? '编辑投票' : '新增投票'"
        :visible.sync="formVisible"
        width="640px"
        :close-on-click-modal="false">
        <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="120px">
          <el-form-item label="投票主题" prop="title">
            <el-input v-model="dialogForm.title" placeholder="请输入投票主题" />
          </el-form-item>
          <el-form-item label="详细描述" prop="description">
            <el-input type="textarea" v-model="dialogForm.description" placeholder="选填" :rows="3" />
          </el-form-item>
          <el-form-item label="投票类型" prop="voteType">
            <el-radio-group v-model="dialogForm.voteType">
              <el-radio label="SINGLE">单选</el-radio>
              <el-radio label="MULTIPLE">多选</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="dialogForm.startTime"
              type="datetime"
              value-format="yyyy-MM-dd HH:mm:ss"
              placeholder="选择开始时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="dialogForm.endTime"
              type="datetime"
              value-format="yyyy-MM-dd HH:mm:ss"
              placeholder="选择结束时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="投票资格" prop="eligibility">
            <el-radio-group v-model="dialogForm.eligibility">
              <el-radio label="OWNER">仅业主可投</el-radio>
              <el-radio label="ALL">全部用户</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="结果展示">
            <el-checkbox v-model="dialogForm.resultAfterEndOnly">仅截止后显示结果</el-checkbox>
          </el-form-item>
          <el-form-item label="投票选项" prop="options">
            <div class="option-list">
              <div v-for="(opt, idx) in dialogForm.options" :key="idx" class="option-row">
                <el-input v-model="opt.optionText" placeholder="选项文案" size="small" style="width: 360px" />
                <el-button type="text" icon="el-icon-delete" @click="removeOption(idx)" v-if="dialogForm.options.length > 1">删除</el-button>
              </div>
              <el-button type="text" icon="el-icon-plus" @click="addOption">添加选项</el-button>
            </div>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="formVisible = false">取 消</el-button>
          <el-button type="primary" :loading="isSubmit" @click="handleSave">确 定</el-button>
        </div>
      </el-dialog>

      <!-- 结果弹窗 -->
      <el-dialog title="投票结果" :visible.sync="resultVisible" width="520px">
        <div v-if="resultList.length" class="result-list">
          <div v-for="(item, idx) in resultList" :key="idx" class="result-item">
            <span class="option-text">{{ item.optionText }}</span>
            <span class="option-count">{{ item.count }} 票 ({{ item.percent }}%)</span>
          </div>
        </div>
        <div slot="footer">
          <el-button @click="exportResult" type="primary" size="small">导出结果</el-button>
          <el-button @click="resultVisible = false">关闭</el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import Request from '@/utils/request'
import Pagination from '@/components/Pagination/index.vue'
import { exportDataToExcel } from '@/utils/excel'
import moment from 'moment'

export default {
  name: 'VoteManager',
  components: { Pagination },
  data() {
    return {
      listLoading: false,
      listQuery: {
        title: '',
        pageNum: 1,
        pageSize: 10
      },
      tableData: [],
      total: 0,
      selectedIds: [],
      formVisible: false,
      isSubmit: false,
      dialogForm: {
        id: undefined,
        title: '',
        description: '',
        voteType: 'SINGLE',
        startTime: '',
        endTime: '',
        eligibility: 'OWNER',
        resultAfterEndOnly: false,
        options: [{ optionText: '' }, { optionText: '' }]
      },
      formRules: {
        title: [{ required: true, message: '请输入投票主题', trigger: 'blur' }]
      },
      resultVisible: false,
      resultList: [],
      currentVoteId: null
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    statusOf(row) {
      if (!row.startTime || !row.endTime) return 'UNKNOWN'
      const now = moment()
      const start = moment(row.startTime)
      const end = moment(row.endTime)
      if (now.isBefore(start)) return 'NOT_STARTED'
      if (now.isAfter(end)) return 'ENDED'
      return 'IN_PROGRESS'
    },
    statusText(row) {
      const s = this.statusOf(row)
      return { NOT_STARTED: '未开始', IN_PROGRESS: '进行中', ENDED: '已结束', UNKNOWN: '-' }[s] || '-'
    },
    statusTag(row) {
      const s = this.statusOf(row)
      return { NOT_STARTED: 'info', IN_PROGRESS: 'success', ENDED: 'warning', UNKNOWN: 'info' }[s] || 'info'
    },
    formatDateTime(val) {
      return val ? moment(val).format('YYYY-MM-DD HH:mm') : '-'
    },
    fetchData() {
      this.listLoading = true
      Request.get('/votes/page', {
        params: {
          pageNum: this.listQuery.pageNum,
          pageSize: this.listQuery.pageSize,
          title: this.listQuery.title || undefined
        }
      }).then(res => {
        if (res.code === '0') {
          this.tableData = res.data.records || []
          this.total = res.data.total || 0
        }
      }).finally(() => { this.listLoading = false })
    },
    onReset() {
      this.listQuery.title = ''
      this.listQuery.pageNum = 1
      this.fetchData()
    },
    handleSelectionChange(rows) {
      this.selectedIds = (rows || []).map(r => r.id)
    },
    addOption() {
      this.dialogForm.options.push({ optionText: '' })
    },
    removeOption(idx) {
      this.dialogForm.options.splice(idx, 1)
    },
    handleCreate() {
      this.dialogForm = {
        id: undefined,
        title: '',
        description: '',
        voteType: 'SINGLE',
        startTime: '',
        endTime: '',
        eligibility: 'OWNER',
        resultAfterEndOnly: false,
        options: [{ optionText: '' }, { optionText: '' }]
      }
      this.formVisible = true
      this.$nextTick(() => this.$refs.dialogForm && this.$refs.dialogForm.clearValidate())
    },
    handleEdit(row) {
      Request.get('/votes/' + row.id).then(res => {
        if (res.code === '0' && res.data) {
          const v = res.data
          this.dialogForm = {
            id: v.id,
            title: v.title,
            description: v.description || '',
            voteType: v.voteType || 'SINGLE',
            startTime: v.startTime || '',
            endTime: v.endTime || '',
            eligibility: v.eligibility || 'OWNER',
            resultAfterEndOnly: v.resultAfterEndOnly === true,
            options: (v.options && v.options.length) ? v.options.map(o => ({ id: o.id, optionText: o.optionText })) : [{ optionText: '' }, { optionText: '' }]
          }
          this.formVisible = true
        }
      })
    },
    handleSave() {
      const opts = (this.dialogForm.options || []).filter(o => o.optionText && o.optionText.trim())
      if (!opts.length) {
        this.$message.warning('请至少添加一个有效选项')
        return
      }
      this.$refs.dialogForm.validate(valid => {
        if (!valid) return
        this.isSubmit = true
        const payload = {
          ...this.dialogForm,
          options: opts.map(o => ({ optionText: o.optionText.trim() }))
        }
        const req = this.dialogForm.id ? Request.put('/votes', payload) : Request.post('/votes', payload)
        req.then(res => {
          if (res.code === '0') {
            this.$message.success(this.dialogForm.id ? '更新成功' : '新增成功')
            this.formVisible = false
            this.fetchData()
          } else {
            this.$message.error(res.msg || '操作失败')
          }
        }).finally(() => { this.isSubmit = false })
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该投票？删除后投票记录将一并清除。', '提示', {
        type: 'warning'
      }).then(() => {
        Request.delete('/votes/' + row.id).then(res => {
          if (res.code === '0') {
            this.$message.success('删除成功')
            this.fetchData()
          } else {
            this.$message.error(res.msg || '删除失败')
          }
        })
      }).catch(() => {})
    },
    batchDelete() {
      if (!this.selectedIds.length) {
        this.$message.warning('请先选择要删除的投票')
        return
      }
      this.$confirm('确认删除所选投票？', '提示', { type: 'warning' }).then(() => {
        Request.delete('/votes/batch', { data: this.selectedIds }).then(res => {
          if (res.code === '0') {
            this.$message.success('删除成功')
            this.fetchData()
          } else {
            this.$message.error(res.msg || '删除失败')
          }
        })
      }).catch(() => {})
    },
    showResult(row) {
      this.currentVoteId = row.id
      Request.get('/votes/' + row.id + '/result').then(res => {
        if (res.code === '0') {
          this.resultList = res.data || []
          this.resultVisible = true
        }
      })
    },
    exportResult() {
      if (!this.resultList.length || !this.currentVoteId) return
      const header = ['选项', '得票数', '得票率(%)']
      const key = ['optionText', 'count', 'percent']
      const data = this.resultList.map(o => ({ ...o }))
      exportDataToExcel({ header, key, data, fileName: '投票结果-' + this.currentVoteId })
      this.$message.success('导出成功')
    }
  }
}
</script>

<style scoped>
.option-list .option-row { display: flex; align-items: center; margin-bottom: 8px; }
.option-list .option-row .el-input { margin-right: 8px; }
.result-list { max-height: 360px; overflow-y: auto; }
.result-item { padding: 10px 0; border-bottom: 1px solid #ebeef5; display: flex; justify-content: space-between; }
.result-item .option-text { flex: 1; margin-right: 12px; }
.result-item .option-count { color: #409EFF; font-weight: 500; }
.search-form { margin-bottom: 16px; }
.control-btns { display: flex; justify-content: space-between; margin-bottom: 16px; }
</style>
