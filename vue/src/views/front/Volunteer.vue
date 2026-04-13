<template>
  <div class="community-activity-page">
    <div class="content-wrapper">
      <div class="page-header">
        <h2 class="page-title">小区活动</h2>
        <div class="page-desc">参与邻里小区活动，共建美好家园</div>
      </div>

      <!-- 活动列表 -->
      <el-card class="activity-card" shadow="never">
        <div class="form-header">
          <div class="header-left">
            <i class="el-icon-s-flag"></i>
            <span>活动列表</span>
          </div>
          <div class="header-right">
            <span class="total-text">共 {{ total }} 个活动</span>
          </div>
        </div>

        <el-table :data="activities" v-loading="loading">
          <el-table-column prop="name" label="活动名称" min-width="120" />
          <el-table-column prop="location" label="活动地点" min-width="100" />
          <el-table-column label="详情" width="72" align="center">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="openDetailDialog(scope.row)">查看</el-button>
            </template>
          </el-table-column>
          <el-table-column prop="contactPerson" label="负责人" width="90" />
          <el-table-column prop="contactPhone" label="联系电话" width="110" />
          <el-table-column prop="startTime" label="开始时间" width="150">
            <template slot-scope="scope">
              {{ formatTime(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="150">
            <template slot-scope="scope">
              {{ formatTime(scope.row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column label="报名人数" width="110">
            <template slot-scope="scope">
              {{ scope.row.currentParticipants }}/{{ scope.row.maxParticipants }} 人
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="200" align="center" fixed="right">
            <template slot-scope="scope">
              <div class="table-actions">
                <template v-if="!canUseOwnerFeatures">
                  <span class="text-muted">登录后可操作</span>
                </template>
                <template v-else>
                  <!-- 已结束 + 未报名 -->
                  <span v-if="activityPhase(scope.row) === 'ended' && !getRegistrationForActivity(scope.row.id)" class="text-muted">已结束</span>
                  <!-- 进行中 + 未报名 + 未满员：报名已截止 -->
                  <span
                    v-else-if="!getRegistrationForActivity(scope.row.id) && activityPhase(scope.row) === 'ongoing' && !isActivityFull(scope.row)"
                    class="text-muted"
                  >报名已截止</span>
                  <!-- 未报名 + 未结束 + 已满员 -->
                  <span
                    v-else-if="!getRegistrationForActivity(scope.row.id) && activityPhase(scope.row) !== 'ended' && isActivityFull(scope.row)"
                    class="text-muted"
                  >已满员</span>
                  <!-- 未开始 + 未满 + 未报名 -->
                  <el-button
                    v-else-if="!getRegistrationForActivity(scope.row.id) && activityPhase(scope.row) === 'upcoming' && !isActivityFull(scope.row)"
                    type="text"
                    size="small"
                    @click="handleRegister(scope.row)"
                  >
                    报名
                  </el-button>
                  <!-- 未开始 + 已报名：取消报名 -->
                  <el-button
                    v-else-if="getRegistrationForActivity(scope.row.id) && activityPhase(scope.row) === 'upcoming' && regStatus(scope.row.id) === 'REGISTERED'"
                    type="text"
                    size="small"
                    @click="handleCancelByActivity(scope.row)"
                  >
                    取消报名
                  </el-button>
                  <!-- 进行中 + 已报名 + 未签到 -->
                  <el-button
                    v-else-if="getRegistrationForActivity(scope.row.id) && activityPhase(scope.row) === 'ongoing' && regStatus(scope.row.id) === 'REGISTERED'"
                    type="text"
                    size="small"
                    @click="handleCheckInByActivity(scope.row)"
                  >
                    签到
                  </el-button>
                  <!-- 进行中 + 已签到 -->
                  <span v-else-if="getRegistrationForActivity(scope.row.id) && activityPhase(scope.row) === 'ongoing' && regStatus(scope.row.id) === 'CHECKED_IN'" class="text-muted">已签到</span>
                  <!-- 已结束 + 已报名：评价（占位） -->
                  <el-button
                    v-else-if="getRegistrationForActivity(scope.row.id) && activityPhase(scope.row) === 'ended'"
                    type="text"
                    size="small"
                    @click="openReviewPlaceholder(scope.row)"
                  >
                    评价
                  </el-button>
                  <span v-else class="text-muted">—</span>
                </template>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[10, 20, 30, 50]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
          />
        </div>
      </el-card>

      <!-- 我的报名：仅登录且存在业主信息时展示 -->
      <el-card v-if="canUseOwnerFeatures" class="registration-card" shadow="never">
        <div class="form-header">
          <div class="header-left">
            <i class="el-icon-s-order"></i>
            <span>我的报名</span>
          </div>
        </div>

        <el-table :data="registrations" v-loading="registrationLoading" empty-text="暂无报名记录">
          <el-table-column label="活动名称" min-width="120">
            <template slot-scope="scope">
              {{ scope.row.activity && scope.row.activity.name ? scope.row.activity.name : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="活动地点" min-width="100">
            <template slot-scope="scope">
              {{ scope.row.activity && scope.row.activity.location ? scope.row.activity.location : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="活动开始" width="150">
            <template slot-scope="scope">
              {{ scope.row.activity ? formatTime(scope.row.activity.startTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="活动结束" width="150">
            <template slot-scope="scope">
              {{ scope.row.activity ? formatTime(scope.row.activity.endTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="registerTime" label="报名时间" width="150">
            <template slot-scope="scope">
              {{ formatTime(scope.row.registerTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="200" align="center" fixed="right">
            <template slot-scope="scope">
              <div class="table-actions" v-if="scope.row.activity">
                <!-- 未开始：可取消 -->
                <el-button
                  v-if="activityPhase(scope.row.activity) === 'upcoming' && scope.row.status === 'REGISTERED'"
                  type="text"
                  size="small"
                  @click="handleCancel(scope.row)"
                >
                  取消报名
                </el-button>
                <!-- 进行中 + 未签到：签到 -->
                <el-button
                  v-else-if="activityPhase(scope.row.activity) === 'ongoing' && scope.row.status === 'REGISTERED'"
                  type="text"
                  size="small"
                  @click="handleCheckInRegistration(scope.row)"
                >
                  签到
                </el-button>
                <!-- 进行中 + 已签到 -->
                <span v-else-if="activityPhase(scope.row.activity) === 'ongoing' && scope.row.status === 'CHECKED_IN'" class="text-muted">已签到</span>
                <!-- 已结束：回顾 + 评价（占位） -->
                <template v-else-if="activityPhase(scope.row.activity) === 'ended'">
                  <el-button type="text" size="small" @click="openDetailDialog(scope.row.activity, 'review')">查看回顾</el-button>
                  <el-button type="text" size="small" @click="openReviewPlaceholder(scope.row.activity)">评价</el-button>
                </template>
                <span v-else class="text-muted">—</span>
              </div>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container" v-if="registrationTotal > 0">
          <el-pagination
            background
            @size-change="handleRegistrationSizeChange"
            @current-change="handleRegistrationCurrentChange"
            :current-page="registrationPageNum"
            :page-sizes="[10, 20, 30, 50]"
            :page-size="registrationPageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="registrationTotal"
          />
        </div>
      </el-card>

      <el-alert
        v-else
        class="login-hint"
        title="登录业主账号后可查看「我的报名」并参与活动报名。"
        type="info"
        :closable="false"
        show-icon
      />

      <!-- 活动详情 / 报名 -->
      <el-dialog
        :title="activityDialogTitle"
        :visible.sync="dialogVisible"
        width="600px"
        :close-on-click-modal="false"
        @close="onActivityDialogClose"
      >
        <div v-if="currentActivity" class="activity-detail activity-detail--intro-only">
          <p class="intro-text">{{ currentActivity.description || '暂无活动介绍' }}</p>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">关 闭</el-button>
          <el-button
            v-if="activityDialogMode === 'register' && canUseOwnerFeatures && currentActivity && !isRegistered(currentActivity.id)"
            type="primary"
            @click="confirmRegister"
            :loading="submitting"
            :disabled="isActivityFull(currentActivity) || activityPhase(currentActivity) !== 'upcoming'"
          >
            确认报名
          </el-button>
        </div>
      </el-dialog>

      <!-- 评价占位（后续可接真实评价接口） -->
      <el-dialog title="活动评价" :visible.sync="reviewDialogVisible" width="480px" append-to-body>
        <p class="review-tip">感谢参与！活动评价功能正在完善中，敬请期待。</p>
        <p v-if="reviewTargetActivity" class="review-act-name">{{ reviewTargetActivity.name }}</p>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="reviewDialogVisible = false">知道了</el-button>
        </span>
      </el-dialog>
    </div>

    <page-footer></page-footer>
  </div>
</template>

<script>
import PageFooter from '@/components/front/PageFooter.vue'
import Request from '@/utils/request'
import moment from 'moment'
import { mapGetters } from 'vuex'

export default {
  name: 'CommunityActivity',
  components: { PageFooter },
  computed: {
    ...mapGetters(['isLoggedIn', 'currentUser', 'ownerInfo']),
    /** 已登录且能拿到业主 id，才可报名、查我的报名 */
    canUseOwnerFeatures() {
      return this.isLoggedIn && this.ownerInfo && this.ownerInfo.id != null
    },
    activityDialogTitle() {
      if (this.activityDialogMode === 'register') return '活动报名'
      if (this.activityDialogPurpose === 'review') return '活动回顾'
      return '活动详情'
    }
  },
  data() {
    return {
      loading: false,
      registrationLoading: false,
      activities: [],
      registrations: [],
      registeredActivityIds: [],
      /** activityId -> 报名记录（用于活动列表操作，需与列表页数据同步） */
      registrationByActivityId: {},
      pageNum: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      /** view：仅查看；register：从「报名」打开可确认报名 */
      activityDialogMode: 'view',
      /** default | review：影响弹窗标题（活动详情 / 活动回顾） */
      activityDialogPurpose: 'default',
      submitting: false,
      currentActivity: null,
      registrationPageNum: 1,
      registrationPageSize: 10,
      registrationTotal: 0,
      reviewDialogVisible: false,
      reviewTargetActivity: null
    }
  },
  watch: {
    isLoggedIn() {
      this.onAuthChange()
    },
    ownerInfo: {
      handler() {
        this.onAuthChange()
      },
      deep: true
    }
  },
  created() {
    this.getActivities()
    this.refreshOwnerSection()
  },
  methods: {
    isApiSuccess(response) {
      if (!response || response.code === undefined || response.code === null) return false
      return String(response.code) === '0'
    },
    onAuthChange() {
      this.refreshOwnerSection()
    },
    refreshOwnerSection() {
      if (this.canUseOwnerFeatures) {
        this.loadRegisteredActivityIds()
        this.loadRegistrationMap().finally(() => {
          this.getRegistrations()
        })
      } else {
        this.registrations = []
        this.registrationTotal = 0
        this.registeredActivityIds = []
        this.registrationByActivityId = {}
      }
    },
    /** 拉取本人报名记录（多取一些）用于活动列表上的操作按钮 */
    loadRegistrationMap() {
      if (!this.ownerInfo || !this.ownerInfo.id) {
        this.registrationByActivityId = {}
        return Promise.resolve()
      }
      return Request.get('/community-registration/page', {
        params: {
          volunteerId: this.ownerInfo.id,
          pageNum: 1,
          pageSize: 500
        }
      })
        .then((response) => {
          if (this.isApiSuccess(response) && response.data && response.data.records) {
            const map = {}
            response.data.records.forEach((r) => {
              const aid = r.activityId != null ? r.activityId : r.activity && r.activity.id
              if (aid != null) map[aid] = r
            })
            this.registrationByActivityId = map
          } else {
            this.registrationByActivityId = {}
          }
        })
        .catch(() => {
          this.registrationByActivityId = {}
        })
    },
    getRegistrationForActivity(activityId) {
      if (activityId == null) return null
      const id = Number(activityId)
      return this.registrationByActivityId[id] || this.registrationByActivityId[activityId] || null
    },
    regStatus(activityId) {
      const r = this.getRegistrationForActivity(activityId)
      return r && r.status ? r.status : null
    },
    /**
     * 活动阶段：未开始 / 进行中 / 已结束
     */
    activityPhase(activity) {
      if (!activity || !activity.startTime || !activity.endTime) return 'unknown'
      const now = moment()
      const start = moment(activity.startTime)
      const end = moment(activity.endTime)
      if (now.isBefore(start)) return 'upcoming'
      if (now.isAfter(end)) return 'ended'
      return 'ongoing'
    },
    openDetailDialog(activity, purpose) {
      if (!activity) return
      this.currentActivity = activity
      if (purpose === 'register') {
        this.activityDialogMode = 'register'
        this.activityDialogPurpose = 'default'
      } else {
        this.activityDialogMode = 'view'
        this.activityDialogPurpose = purpose === 'review' ? 'review' : 'default'
      }
      this.dialogVisible = true
    },
    onActivityDialogClose() {
      this.currentActivity = null
      this.activityDialogMode = 'view'
      this.activityDialogPurpose = 'default'
    },
    openReviewPlaceholder(activity) {
      this.reviewTargetActivity = activity
      this.reviewDialogVisible = true
    },
    handleCancelByActivity(activity) {
      const reg = this.getRegistrationForActivity(activity.id)
      if (reg) this.handleCancel(reg)
    },
    handleCheckInByActivity(activity) {
      const reg = this.getRegistrationForActivity(activity.id)
      if (reg) this.handleCheckInRegistration(reg)
    },
    handleCheckInRegistration(registration) {
      if (!registration || registration.id == null) return
      this.$confirm('确认现场签到？', '签到', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      })
        .then(() => {
          Request.post(`/community-registration/${registration.id}/check-in`).then((response) => {
            if (this.isApiSuccess(response)) {
              this.$message.success('签到成功')
              this.loadRegistrationMap().finally(() => {
                this.loadRegisteredActivityIds()
                this.getActivities()
                this.getRegistrations()
              })
            } else {
              this.$message.error(response.msg || '签到失败')
            }
          })
        })
        .catch(() => {})
    },
    loadRegisteredActivityIds() {
      if (!this.ownerInfo || !this.ownerInfo.id) return
      Request.get('/community-registration/my-activity-ids', {
        params: { volunteerId: this.ownerInfo.id }
      }).then((response) => {
        if (this.isApiSuccess(response) && Array.isArray(response.data)) {
          this.registeredActivityIds = response.data
        } else {
          this.registeredActivityIds = []
        }
      }).catch(() => {
        this.registeredActivityIds = []
      })
    },
    isRegistered(activityId) {
      if (activityId == null) return false
      const id = Number(activityId)
      return this.registeredActivityIds.some((x) => Number(x) === id)
    },
    getActivities() {
      this.loading = true
      Request.get('/community-activity/page', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
      })
        .then((response) => {
          if (this.isApiSuccess(response) && response.data) {
            this.activities = response.data.records || []
            this.total = response.data.total || 0
          } else {
            this.$message.error(response.msg || '获取活动列表失败')
          }
        })
        .finally(() => {
          this.loading = false
        })
    },
    getRegistrations() {
      if (!this.canUseOwnerFeatures) return
      this.registrationLoading = true
      Request.get('/community-registration/page', {
        params: {
          volunteerId: this.ownerInfo.id,
          pageNum: this.registrationPageNum,
          pageSize: this.registrationPageSize
        }
      })
        .then((response) => {
          if (this.isApiSuccess(response) && response.data) {
            this.registrations = response.data.records || []
            this.registrationTotal = response.data.total || 0
          } else {
            this.$message.error(response.msg || '获取报名记录失败')
          }
        })
        .finally(() => {
          this.registrationLoading = false
        })
    },
    handleRegister(activity) {
      if (!this.canUseOwnerFeatures) {
        this.$message.warning('请先登录业主账号后再报名')
        return
      }
      if (this.isRegistered(activity.id)) {
        this.$message.info('您已报名该活动')
        return
      }
      this.currentActivity = activity
      this.activityDialogMode = 'register'
      this.dialogVisible = true
    },
    confirmRegister() {
      if (!this.currentActivity || !this.ownerInfo || !this.ownerInfo.id) return
      this.submitting = true
      Request.post(
        `/community-activity/${this.currentActivity.id}/register?volunteerId=${this.ownerInfo.id}`
      )
        .then((response) => {
          if (this.isApiSuccess(response)) {
            this.$message.success('报名成功')
            this.dialogVisible = false
            this.loadRegistrationMap().finally(() => {
              this.loadRegisteredActivityIds()
              this.getActivities()
              this.getRegistrations()
            })
          } else {
            this.$message.error(response.msg || '报名失败')
          }
        })
        .finally(() => {
          this.submitting = false
        })
    },
    handleCancel(registration) {
      this.$confirm('确认取消该活动的报名？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          Request.delete(`/community-registration/${registration.id}`).then((response) => {
            if (this.isApiSuccess(response)) {
              this.$message.success('已取消报名')
              this.loadRegistrationMap().finally(() => {
                this.loadRegisteredActivityIds()
                this.getActivities()
                this.getRegistrations()
              })
            } else {
              this.$message.error(response.msg || '取消失败')
            }
          })
        })
        .catch(() => {})
    },
    isActivityFull(activity) {
      return activity && activity.currentParticipants >= activity.maxParticipants
    },
    /** 已开始（含进行中、已结束），用于兼容旧逻辑 */
    isActivityStarted(activity) {
      return activity && activity.startTime && moment(activity.startTime).isBefore(moment())
    },
    getStatusType(status) {
      const map = {
        REGISTERED: 'warning',
        CHECKED_IN: 'success'
      }
      return map[status] || 'info'
    },
    getStatusText(status) {
      const map = {
        REGISTERED: '已报名',
        CHECKED_IN: '已签到'
      }
      return map[status] || status
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getActivities()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getActivities()
    },
    formatTime(time) {
      if (!time) return '-'
      return moment(time).format('YYYY-MM-DD HH:mm')
    },
    handleRegistrationSizeChange(val) {
      this.registrationPageSize = val
      this.getRegistrations()
    },
    handleRegistrationCurrentChange(val) {
      this.registrationPageNum = val
      this.getRegistrations()
    }
  }
}
</script>

<style scoped>
.community-activity-page {
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

.activity-card,
.registration-card {
  background: #fff;
  padding: 20px;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.login-hint {
  margin-bottom: 20px;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  color: #409eff;
}

.header-left span {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.header-right .total-text {
  color: #909399;
  font-size: 14px;
}

.el-table {
  margin: 15px 0;
}

.el-table ::v-deep th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 500;
}

.pagination-container {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  text-align: right;
}

.activity-detail {
  padding: 0 12px;
}

.detail-item {
  margin-bottom: 14px;
}

.detail-item label {
  font-weight: bold;
  color: #606266;
  margin-right: 10px;
}

.detail-item .description {
  margin-top: 8px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}

.activity-detail--intro-only {
  padding: 4px 0;
}

.activity-detail--intro-only .intro-text {
  margin: 0;
  color: #606266;
  font-size: 15px;
  line-height: 1.75;
  white-space: pre-wrap;
}

.text-muted {
  color: #c0c4cc;
  font-size: 13px;
}

.table-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 4px 8px;
}

.review-tip {
  color: #606266;
  line-height: 1.6;
  margin: 0 0 8px;
}

.review-act-name {
  font-weight: 600;
  color: #303133;
  margin: 0;
}

@media screen and (max-width: 768px) {
  .content-wrapper {
    padding: 15px;
  }
}
</style>
