<template>
  <div class="vote-detail-page">
    <nav-header></nav-header>
    <div class="content-wrapper">
      <el-card v-loading="loading" class="detail-card" shadow="never">
        <template v-if="detail">
          <h2 class="vote-title">{{ detail.vote.title }}</h2>
          <div class="vote-meta">
            <el-tag size="small">{{ detail.vote.voteType === 'SINGLE' ? '单选' : '多选' }}</el-tag>
            <el-tag size="small" :type="statusTag(detail.status)">{{ statusText(detail.status) }}</el-tag>
            <span class="time-range">{{ formatTime(detail.vote.startTime) }} 至 {{ formatTime(detail.vote.endTime) }}</span>
          </div>
          <div v-if="detail.vote.description" class="vote-desc" v-html="detail.vote.description"></div>

          <!-- 未投且可投：展示选项并提交 -->
          <div v-if="detail.canVote" class="vote-form">
            <div class="form-label">请选择：</div>
            <el-radio-group v-if="detail.vote.voteType === 'SINGLE'" v-model="selectedIds" class="option-group">
              <el-radio v-for="opt in detail.vote.options" :key="opt.id" :label="opt.id">{{ opt.optionText }}</el-radio>
            </el-radio-group>
            <el-checkbox-group v-else v-model="selectedIds" class="option-group">
              <el-checkbox v-for="opt in detail.vote.options" :key="opt.id" :label="opt.id">{{ opt.optionText }}</el-checkbox>
            </el-checkbox-group>
            <el-button type="primary" :loading="submitLoading" @click="submitVote" class="submit-btn">提交投票</el-button>
          </div>

          <!-- 已投或不可投：提示 -->
          <div v-else-if="detail.alreadyVoted" class="vote-tip">
            <i class="el-icon-success"></i> 您已参与过本投票
          </div>
          <div v-else-if="detail.status === 'NOT_STARTED'" class="vote-tip">
            <i class="el-icon-time"></i> 投票尚未开始
          </div>

          <!-- 结果展示 -->
          <div v-if="detail.showResult && resultList.length" class="result-section">
            <h3 class="result-title">投票结果</h3>
            <div class="result-list">
              <div v-for="(item, idx) in resultList" :key="idx" class="result-item">
                <span class="option-text">{{ item.optionText }}</span>
                <el-progress :percentage="item.percent" :stroke-width="12" />
                <span class="option-count">{{ item.count }} 票 ({{ item.percent }}%)</span>
              </div>
            </div>
          </div>
        </template>
        <div class="back-link">
          <el-button type="text" icon="el-icon-arrow-left" @click="$router.push('/vote')">返回列表</el-button>
        </div>
      </el-card>
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
  name: 'VoteDetail',
  components: { NavHeader, PageFooter },
  data() {
    return {
      loading: false,
      submitLoading: false,
      detail: null,
      selectedIds: [],
      resultList: []
    }
  },
  computed: {
    voteId() {
      return this.$route.params.id
    }
  },
  created() {
    this.fetchDetail()
  },
  methods: {
    statusText(s) {
      return { NOT_STARTED: '未开始', IN_PROGRESS: '进行中', ENDED: '已结束' }[s] || s
    },
    statusTag(s) {
      return { NOT_STARTED: 'info', IN_PROGRESS: 'success', ENDED: 'warning' }[s] || 'info'
    },
    formatTime(val) {
      return val ? moment(val).format('YYYY-MM-DD HH:mm') : '-'
    },
    fetchDetail() {
      this.loading = true
      Request.get('/votes/front/' + this.voteId).then(res => {
        if (res.code === '0' && res.data) {
          this.detail = res.data
          this.resultList = res.data.result || []
        } else {
          this.$message.error(res.msg || '投票不存在')
          this.$router.replace('/vote')
        }
      }).finally(() => { this.loading = false })
    },
    submitVote() {
      let ids = this.selectedIds
      if (!Array.isArray(ids)) ids = ids !== undefined && ids !== null ? [ids] : []
      if (!ids.length) {
        this.$message.warning('请至少选择一个选项')
        return
      }
      this.submitLoading = true
      Request.post('/votes/front/' + this.voteId + '/submit', { optionIds: ids }).then(res => {
        if (res.code === '0') {
          this.$message.success('投票成功')
          this.fetchDetail()
        } else {
          this.$message.error(res.msg || '提交失败')
        }
      }).finally(() => { this.submitLoading = false })
    }
  }
}
</script>

<style scoped>
.vote-detail-page { min-height: 100vh; background: #f5f7fa; display: flex; flex-direction: column; }
.content-wrapper { flex: 1; max-width: 720px; margin: 0 auto; padding: 20px; width: 100%; }
.detail-card { border-radius: 8px; padding: 24px; }
.vote-title { font-size: 22px; color: #303133; margin: 0 0 12px 0; }
.vote-meta { margin-bottom: 16px; }
.vote-meta .el-tag { margin-right: 8px; }
.time-range { font-size: 13px; color: #909399; margin-left: 8px; }
.vote-desc { font-size: 14px; color: #606266; line-height: 1.6; margin-bottom: 24px; white-space: pre-wrap; }
.vote-form .form-label { margin-bottom: 12px; font-weight: 500; }
.option-group { display: flex; flex-direction: column; margin-bottom: 20px; }
.option-group .el-radio, .option-group .el-checkbox { margin-bottom: 10px; }
.submit-btn { margin-top: 8px; }
.vote-tip { padding: 20px; background: #f0f9eb; color: #67c23a; border-radius: 6px; margin-bottom: 20px; }
.result-section { margin-top: 28px; padding-top: 20px; border-top: 1px solid #ebeef5; }
.result-title { font-size: 16px; margin: 0 0 16px 0; color: #303133; }
.result-item { margin-bottom: 16px; }
.result-item .option-text { display: block; margin-bottom: 4px; font-size: 14px; }
.result-item .option-count { font-size: 13px; color: #409EFF; margin-top: 4px; display: inline-block; }
.back-link { margin-top: 24px; }
</style>
