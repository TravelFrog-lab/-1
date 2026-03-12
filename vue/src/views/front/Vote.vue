<template>
  <div class="vote-page">
    <nav-header></nav-header>
    <div class="content-wrapper">
      <div class="page-header">
        <h2 class="page-title">投票表决</h2>
        <div class="page-desc">业委会选举、公共事务决策、民意调查、预算审批等，请参与投票</div>
      </div>

      <el-card class="vote-card" shadow="never">
        <div class="form-header">
          <div class="header-left">
            <i class="el-icon-s-data"></i>
            <span>投票列表</span>
          </div>
          <div class="header-right">
            <el-radio-group v-model="statusFilter" size="small" @change="fetchList">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="IN_PROGRESS">进行中</el-radio-button>
              <el-radio-button label="ENDED">已结束</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <el-table :data="list" v-loading="loading">
          <el-table-column prop="title" label="投票主题" min-width="200" show-overflow-tooltip />
          <el-table-column label="详情" width="80" align="center">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="showDetail(scope.row)">详情</el-button>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="90">
            <template slot-scope="scope">{{ scope.row.voteType === 'SINGLE' ? '单选' : '多选' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="statusTag(scope.row)">{{ statusText(scope.row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="165">
            <template slot-scope="scope">{{ formatTime(scope.row.startTime) }}</template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="165">
            <template slot-scope="scope">{{ formatTime(scope.row.endTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" @click="goDetail(scope.row.id)">查看 / 投票</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="!list.length && !loading" class="empty-tip">暂无投票</div>
      </el-card>

      <!-- 详情弹窗：解释为什么要做这次投票 -->
      <el-dialog title="投票详情" :visible.sync="detailVisible" width="520px" class="detail-dialog">
        <div v-if="detailCurrent" class="detail-content">
          <div class="detail-title">{{ detailCurrent.title }}</div>
          <div class="detail-desc">{{ detailCurrent.description || '暂无说明' }}</div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="detailVisible = false">关闭</el-button>
          <el-button @click="goDetail(detailCurrent.id); detailVisible = false">去投票</el-button>
        </span>
      </el-dialog>
    </div>
    <page-footer></page-footer>
  </div>
</template>

<script>
import NavHeader from '@/components/front/NavHeader.vue'
import PageFooter from '@/components/front/PageFooter.vue'
import Request from '@/utils/request'
import moment from 'moment'

export default {
  name: 'Vote',
  components: { NavHeader, PageFooter },
  data() {
    return {
      loading: false,
      list: [],
      statusFilter: '',
      detailVisible: false,
      detailCurrent: null
    }
  },
  created() {
    this.fetchList()
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
    formatTime(val) {
      return val ? moment(val).format('YYYY-MM-DD HH:mm') : '-'
    },
    fetchList() {
      this.loading = true
      Request.get('/votes/front/list', {
        params: { status: this.statusFilter || undefined },
        skipGlobalError: true
      }).then(res => {
        if (res.code === '0') this.list = res.data || []
        else this.list = []
      }).catch(() => {
        this.list = []
        this.$message.warning('加载失败，请稍后重试')
      }).finally(() => { this.loading = false })
    },
    goDetail(id) {
      this.$router.push('/vote/' + id)
    },
    showDetail(row) {
      this.detailCurrent = row
      this.detailVisible = true
    }
  }
}
</script>

<style scoped>
.vote-page {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}
.content-wrapper {
  flex: 1;
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  width: 100%;
}
.page-header { text-align: center; margin-bottom: 24px; }
.page-title { font-size: 24px; color: #303133; margin: 0 0 8px 0; }
.page-desc { font-size: 14px; color: #909399; }
.vote-card { border-radius: 8px; }
.form-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.header-left { font-size: 16px; color: #303133; }
.header-left i { margin-right: 8px; color: #409EFF; }
.empty-tip { text-align: center; padding: 40px; color: #909399; }
.detail-dialog .detail-title { font-size: 16px; color: #303133; margin-bottom: 12px; }
.detail-dialog .detail-desc { font-size: 14px; color: #606266; line-height: 1.7; white-space: pre-wrap; }
</style>
