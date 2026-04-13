<template>
  <div class="repair-page">
    <div class="content-wrapper">
      <div class="repair-container">
        <div class="page-header">
          <h2 class="page-title">报修服务</h2>
          <div class="page-desc">提供快捷的在线报修服务，专业维修人员及时响应</div>
        </div>

        <!-- 报修表单卡片 -->
        <el-card class="repair-form" shadow="never">
          <div class="form-header">
            <div class="form-header__left">
              <i class="el-icon-edit"></i>
              <span>提交报修</span>
            </div>
            <el-button
              type="default"
              size="small"
              plain
              icon="el-icon-document-copy"
              title="可选：根据一句话描述尝试匹配报修类型，提交前请自行核对"
              @click="openAiFillDialog"
            >
              智能辅助填写
            </el-button>
          </div>
          <el-form ref="repairForm" :model="repairForm" :rules="rules" label-width="100px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="报修类型" prop="type">
                  <el-select v-model="repairForm.type" placeholder="请选择报修类型" style="width: 100%">
                    <el-option v-for="item in repairTypes" :key="item.value" :label="item.label" :value="item.value">
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="期望时间" prop="expectTime">
                  <el-date-picker v-model="repairForm.expectTime" type="datetime" placeholder="选择期望上门时间" style="width: 100%" :picker-options="pickerOptions">
                  </el-date-picker>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="报修描述" prop="description">
              <el-input type="textarea" :rows="4" placeholder="请详细描述问题，以便维修人员更好地了解情况" v-model="repairForm.description">
              </el-input>
            </el-form-item>
            <!-- 选择维修师傅 + 提交 -->
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="维修师傅" prop="maintainerId">
                  <el-select v-model="repairForm.maintainerId" placeholder="请选择维修师傅" style="width: 100%">
                    <el-option
                      v-for="m in maintainerSelectList"
                      :key="m.id"
                      :label="maintainerOptionLabel(m)"
                      :value="m.id"
                    >
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item>
                  <el-button type="primary" @click="submitRepair" :loading="submitting">
                    提交报修
                  </el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>

        <!-- 报修记录表格 -->
        <el-card class="repair-records" shadow="never">
          <div slot="header" class="card-header">
            <div class="header-left">
              <i class="el-icon-document"></i>
              <span>报修记录</span>
            </div>
          </div>

          <el-tabs v-model="repairTab" class="repair-record-tabs">
            <el-tab-pane label="待处理" name="pending" />
            <el-tab-pane label="处理中" name="progress" />
            <el-tab-pane label="待评价" name="to_evaluate" />
            <el-tab-pane label="已完成" name="done" />
          </el-tabs>

          <el-table :data="repairRowsForTab" style="width: 100%" v-loading="loading" :empty-text="repairListEmptyText">
            <el-table-column prop="repairType.name" label="报修类型" width="120">
              <template slot-scope="scope">
                <el-tag>{{ (scope.row.repairType && scope.row.repairType.name) ? scope.row.repairType.name : '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="报修描述" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="110">
              <template slot-scope="scope">
                <el-tag :type="getStatusTag(scope.row.status, scope.row)">
                  {{ getStatusText(scope.row.status, scope.row) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="expectedTime" label="期望时间" width="160">
              <template slot-scope="scope">
                {{ scope.row.expectedTime | formatDateTime }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="110" align="center" fixed="right">
              <template slot-scope="scope">
                <el-button type="primary" size="small" plain @click="openDetail(scope.row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination background @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="pageNum"
              :page-sizes="[10, 20, 30, 50]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper" :total="total">
            </el-pagination>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 报修详情（进度 + 接单前修改 + 完工后评价） -->
    <el-dialog
      title="报修详情"
      :visible.sync="detailVisible"
      width="640px"
      class="repair-detail-dialog"
      :close-on-click-modal="false"
      @closed="onDetailClosed"
    >
      <div v-if="detailForm && detailForm.id" class="repair-detail-body">
        <el-alert
          v-if="detailForm.status === 'CANCELLED'"
          title="该报修已取消"
          type="info"
          :closable="false"
          show-icon
          class="detail-alert"
        />

        <el-descriptions :column="2" border size="small" class="detail-desc">
          <el-descriptions-item label="报修类型">
            {{ detailForm.repairType && detailForm.repairType.name ? detailForm.repairType.name : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTag(detailForm.status, detailForm)">{{ getStatusText(detailForm.status, detailForm) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="期望上门时间">{{ detailForm.expectedTime | formatDateTime }}</el-descriptions-item>
          <el-descriptions-item label="实际维修时间">{{ detailForm.actualTime | formatDateTime }}</el-descriptions-item>
          <el-descriptions-item label="维修人员" :span="2">
            <span v-if="detailForm.maintainer && detailForm.maintainer.user">{{ detailForm.maintainer.user.name }}</span>
            <el-button v-else type="text" @click="openMaintainerPicker(detailForm)">待指派（查看可选师傅）</el-button>
          </el-descriptions-item>
          <el-descriptions-item label="报修描述" :span="2">{{ detailForm.description || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="detailForm.rejectReason" label="拒单原因" :span="2">{{ detailForm.rejectReason }}</el-descriptions-item>
          <el-descriptions-item label="维修结果" :span="2">{{ detailForm.resultDescription || '—' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 接单前：业主可改类型与描述 -->
        <div
          v-if="detailForm.status === 'PENDING' && currentUser && currentUser.role === 'OWNER'"
          class="detail-edit-block"
        >
          <el-divider content-position="left">修改报修（维修人员接单前）</el-divider>
          <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="100px">
            <el-form-item label="报修类型" prop="repairTypeId">
              <el-select v-model="editForm.repairTypeId" placeholder="请选择报修类型" style="width: 100%">
                <el-option
                  v-for="item in repairTypes"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="报修描述" prop="description">
              <el-input
                v-model="editForm.description"
                type="textarea"
                :rows="4"
                placeholder="请详细描述问题"
              />
            </el-form-item>
          </el-form>
        </div>

        <!-- 师傅完工后进入「待评价」；业主提交评价后订单归入「已完成」 -->
        <div v-if="detailForm.status === 'COMPLETED'" class="detail-eval-block">
          <el-divider content-position="left">服务评价</el-divider>
          <p v-if="!detailForm.evaluationRating" class="eval-flow-hint">
            维修已完工、尚未评价时，该单会出现在「待评价」列表。提交评价后，订单将进入「已完成」。
          </p>
          <div v-if="detailForm.evaluationRating" class="eval-readonly">
            <div class="eval-stars-row">
              <span class="eval-label">星级</span>
              <el-rate :value="detailForm.evaluationRating" disabled show-score text-color="#ff9900" score-template="{value} 分" />
            </div>
            <p class="eval-text">{{ detailForm.evaluation || '—' }}</p>
          </div>
          <el-form v-else ref="evaluateForm" :model="evaluateForm" :rules="evaluateRules" label-width="80px">
            <el-form-item label="星级" prop="evaluationRating">
              <el-rate
                v-model="evaluateForm.evaluationRating"
                :max="5"
                show-text
                :texts="['很差', '较差', '一般', '满意', '非常满意']"
              />
            </el-form-item>
            <el-form-item label="评价" prop="evaluation">
              <el-input
                v-model="evaluateForm.evaluation"
                type="textarea"
                :rows="4"
                maxlength="500"
                show-word-limit
                placeholder="请分享本次维修体验"
              />
            </el-form-item>
          </el-form>
        </div>

        <el-steps
          v-if="detailForm.status !== 'CANCELLED'"
          :active="repairDetailStepActive"
          finish-status="success"
          align-center
          class="repair-progress-steps"
        >
          <el-step title="待接单" />
          <el-step title="物业已接单" description="已指派维修人员" />
          <el-step title="正在处理中" />
          <el-step title="完成维修" />
        </el-steps>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button
          v-if="detailForm.status === 'PENDING' && currentUser && currentUser.role === 'OWNER'"
          type="danger"
          plain
          @click="handleCancelFromDetail"
        >
          取消报修
        </el-button>
        <el-button
          v-if="detailForm.status === 'PENDING' && currentUser && currentUser.role === 'OWNER'"
          type="primary"
          :loading="editing"
          @click="submitEdit"
        >
          保存修改
        </el-button>
        <el-button
          v-if="detailForm.status === 'COMPLETED' && !detailForm.evaluationRating"
          type="primary"
          :loading="evaluating"
          @click="submitEvaluateFromDetail"
        >
          提交评价
        </el-button>
        <el-button @click="detailVisible = false">关 闭</el-button>
      </span>
    </el-dialog>

    <!-- 维修人员选择对话框（待指派时查看） -->
    <el-dialog title="可接单的维修工" :visible.sync="maintainerPickerVisible" width="520px" :close-on-click-modal="false">
      <div v-if="availableMaintainers && availableMaintainers.length">
        <el-table :data="availableMaintainers" style="width:100%">
          <el-table-column prop="user.name" label="姓名" width="140"></el-table-column>
          <el-table-column label="平均评分" width="100" align="center">
            <template slot-scope="scope">
              {{ maintainerAvgRatingText(scope.row) }}
            </template>
          </el-table-column>
          <el-table-column prop="position" label="岗位" width="120"></el-table-column>
          <el-table-column prop="user.phone" label="电话"></el-table-column>
        </el-table>
      </div>
      <div v-else style="color:#909399;text-align:center;padding:18px 0;">
        暂无可用维修工
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="maintainerPickerVisible = false">确定</el-button>
      </span>
    </el-dialog>

    <!-- 辅助填写：可选，匹配报修类型 -->
    <el-dialog
      title="辅助填写报修"
      :visible.sync="aiFillVisible"
      width="480px"
      append-to-body
      :close-on-click-modal="false"
      @closed="aiFillInput = ''"
    >
      <p class="ai-fill-hint">用一句话描述故障现象即可</p>
      <p class="ai-fill-hint ai-fill-hint--sub">例如：厨房水龙头一直漏水</p>
      <el-input
        v-model="aiFillInput"
        type="textarea"
        :rows="4"
        placeholder="可选，描述越具体，匹配越准确"
      />
      <p class="ai-fill-fail-hint">
        若无法识别：请改一句更直白的话重试，或直接关闭窗口，在下方表单手动选择「报修类型」并填写描述（与是否使用本功能无关，均可正常报修）。
      </p>
      <span slot="footer" class="dialog-footer">
        <el-button @click="aiFillVisible = false">取 消</el-button>
        <el-button type="primary" :loading="aiFillLoading" @click="submitAiFill">确 认</el-button>
      </span>
    </el-dialog>

    <page-footer></page-footer>
  </div>
</template>

<script>
import PageFooter from '@/components/front/PageFooter.vue'
import Request from '@/utils/request'
import moment from 'moment'
import { mapGetters } from 'vuex'
export default {
  name: 'Repair',
  components: {
    PageFooter
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'currentUser', 'ownerInfo']),
    /**
     * el-steps 的 active 为「当前步」下标（0 起）。
     * 若已完工仍设为 3，则第 4 步（下标 3）会一直处于 process，不会变绿；
     * 已结束时须 active > 最后一步下标（共 4 步时返回 4），四步才会全部 finish。
     */
    repairDetailStepActive() {
      const d = this.detailForm
      if (!d || !d.status) return 0
      if (d.status === 'COMPLETED') return 4
      if (d.status === 'IN_PROGRESS') return 2
      if (d.status === 'PENDING' && d.maintainerId) return 1
      if (d.status === 'PENDING' || d.status === 'WAIT_REASSIGN') return 0
      return 0
    },
    repairListEmptyText() {
      const m = {
        pending: '暂无待处理记录',
        progress: '暂无处理中的记录',
        to_evaluate: '暂无待评价记录',
        done: '暂无已完成记录'
      }
      return m[this.repairTab] || '暂无数据'
    },
    /**
     * 与当前 Tab 一致的数据行（防止后端未按 ownerTab 筛选时混入其它状态）
     */
    repairRowsForTab() {
      const rows = this.repairRecords || []
      const tab = this.normalizeRepairTab(this.repairTab)
      return rows.filter(r => this.rowMatchesOwnerTab(r, tab))
    }
  },
  data() {
    return {
     
      loading: false,
      submitting: false,
      evaluating: false,
      editing: false,
      // 报修表单
      repairForm: {
        type: '',
        expectTime: '',
        description: '',
        maintainerId: null
      },
      // 表单校验规则
      rules: {
        type: [
          { required: true, message: '请选择报修类型', trigger: 'change' }
        ],
        expectTime: [
          { required: true, message: '请选择期望时间', trigger: 'change' }
        ],
        maintainerId: [
          { required: true, message: '请选择维修师傅', trigger: 'change' }
        ],
        description: [
          { required: true, message: '请输入报修描述', trigger: 'blur' },
          { min: 10, message: '描述至少10个字符', trigger: 'blur' },
          { max: 500, message: '描述最多500个字符', trigger: 'blur' }
        ]
      },
      // 报修类型选项
      repairTypes: [],

      // 维修师傅选择列表
      maintainerSelectList: [],
      // 时间选择器配置
      pickerOptions: {
        disabledDate(time) {
          // 禁止选择过去时间
          return time.getTime() < Date.now()
        }
      },
      // 报修记录
      repairRecords: [],
      /** 列表分类：待处理 / 处理中 / 待评价 / 已完成 */
      repairTab: 'pending',
      pageNum: 1,
      pageSize: 10,
      total: 0,
      // 用于避免并发请求返回乱序导致“数据被旧请求覆盖”
      repairLoadSeq: 0,
      
      // 评价（详情弹窗内）：星级 + 文字
      evaluateForm: {
        evaluationRating: 0,
        evaluation: ''
      },
      evaluateRules: {
        evaluationRating: [
          {
            validator: (rule, value, callback) => {
              if (!value || value < 1) {
                callback(new Error('请选择1-5星评分'))
              } else {
                callback()
              }
            },
            trigger: 'change'
          }
        ],
        evaluation: [
          { required: true, message: '请填写评价内容', trigger: 'blur' },
          { min: 5, message: '评价内容至少5个字符', trigger: 'blur' },
          { max: 500, message: '评价内容最多500个字符', trigger: 'blur' }
        ]
      },

      // 详情弹窗
      detailVisible: false,
      detailForm: {},

      // 维修人员选择相关（待指派时查看）
      maintainerPickerVisible: false,
      availableMaintainers: [],

      editForm: {
        id: '',
        repairTypeId: '',
        description: ''
      },
      editRules: {
        repairTypeId: [
          { required: true, message: '请选择报修类型', trigger: 'change' }
        ],
        description: [
          { required: true, message: '请输入报修描述', trigger: 'blur' },
          { min: 10, message: '描述至少10个字符', trigger: 'blur' },
          { max: 500, message: '描述最多500个字符', trigger: 'blur' }
        ]
      },

      aiFillVisible: false,
      aiFillInput: '',
      aiFillLoading: false
    }
  },
  watch: {
    // ownerInfo 通常在登录/页面加载后异步写入，避免 created() 过早请求导致列表数据不完整
    ownerInfo: {
      handler(newVal) {
        if (newVal && newVal.id != null) {
          this.pageNum = 1
          this.getRepairRecords(this.repairTab)
        }
      }
    },
    /**
     * 必须在 v-model(repairTab) 更新后再拉列表。
     * 若用 @tab-click 且立刻请求，repairTab 往往还是上一个 Tab，会出现「待处理」选中却显示已完成/处理中。
     */
    repairTab(newVal, oldVal) {
      if (newVal === oldVal) return
      this.pageNum = 1
      this.getRepairRecords(newVal)
    }
  },
  filters: {
    formatDateTime(date) {
      if (!date) return '—'
      const now = moment()
      const target = moment(date)
      const diff = now.diff(target, 'days')
      
      if (diff === 0) {
        return target.format('今天 HH:mm')
      } else if (diff === 1) {
        return target.format('昨天 HH:mm')
      } else if (diff < 7) {
        return target.format('dddd HH:mm')
      } else if (target.year() === now.year()) {
        return target.format('MM-DD HH:mm')
      } else {
        return target.format('YYYY-MM-DD HH:mm')
      }
    }
  },
  created() {
    this.getRepairTypes()
    this.fetchMaintainerSelectList()
    if (this.ownerInfo && this.ownerInfo.id != null) {
      this.getRepairRecords('pending')
    }
  },
  methods: {
    async ensureOwnerInfoReady() {
      // ownerInfo 可能是 session 缓存的旧值；按当前登录用户强制对齐一次，避免查错业主导致数据不全
      if (!this.currentUser || this.currentUser.id == null) return null
      const needRefresh = !this.ownerInfo || this.ownerInfo.id == null || this.ownerInfo.userId !== this.currentUser.id
      if (!needRefresh) return this.ownerInfo
      try {
        const res = await Request.get(`/owner/user/${this.currentUser.id}`)
        if (res && res.code === '0' && res.data) {
          this.$store.commit('SET_OWNER_INFO', res.data)
          return res.data
        }
      } catch (e) {}
      return this.ownerInfo
    },
    maintainerAvgRatingText(m) {
      const v = m && m.avgEvaluationRating
      if (v == null || v === '' || Number.isNaN(Number(v))) return '暂无'
      return `${Number(v).toFixed(1)} 分`
    },
    maintainerOptionLabel(m) {
      const name = m.user && m.user.name ? m.user.name : '维修师傅'
      const v = m && m.avgEvaluationRating
      if (v == null || v === '' || Number.isNaN(Number(v))) {
        return `${name}（暂无评分）`
      }
      return `${name}（平均 ${Number(v).toFixed(1)} 分）`
    },
    async fetchMaintainerSelectList() {
      try {
        const staffRes = await Request.get('/maintenance-staff/all')
        if (!staffRes || staffRes.code !== '0') {
          this.maintainerSelectList = []
          return
        }
        this.maintainerSelectList = staffRes.data || []
      } catch (e) {
        this.maintainerSelectList = []
      }
    },
    // 获取维修类型
    getRepairTypes() {
      Request.get('/repair-types/all').then(response => {
        if (response.code === '0') {
          this.repairTypes = response.data.map(item => ({
            value: item.id, // 改为使用id作为value
            label: item.name
          }))
        } else {
          this.$message({
            message: response.msg,
            type: 'error'
          })
        }

      })
    },


    /** 与后端 ownerTab 一致：pending / progress / to_evaluate / done */
    normalizeRepairTab(t) {
      const s = t == null ? '' : String(t)
      if (s === 'pending' || s === 'progress' || s === 'to_evaluate' || s === 'done') return s
      return 'pending'
    },
    /** 当前 Tab 下列表行是否应展示（与后端 ownerTab 语义一致） */
    rowMatchesOwnerTab(row, tab) {
      if (!row || !row.status) return false
      const s = row.status
      if (tab === 'pending') {
        return s === 'PENDING' || s === 'WAIT_REASSIGN' || s === 'CANCELLED'
      }
      if (tab === 'progress') return s === 'IN_PROGRESS'
      if (tab === 'to_evaluate') {
        return s === 'COMPLETED' && row.evaluationRating == null
      }
      if (tab === 'done') {
        return s === 'COMPLETED' && row.evaluationRating != null
      }
      return true
    },
    /**
     * 业主列表：必须带 ownerTab，由后端强制筛选（避免仅 statuses 未绑定导致返回全部状态混在一起）
     * @param {string} [tabExplicit] 显式传入当前 Tab，切换时务必传 watch 的 newVal
     */
    async getRepairRecords(tabExplicit) {
      const owner = await this.ensureOwnerInfoReady()
      if (!owner || owner.id == null) return
      const seq = ++this.repairLoadSeq
      this.loading = true
      const tab = this.normalizeRepairTab(tabExplicit !== undefined ? tabExplicit : this.repairTab)
      const params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        applicantId: owner.id,
        ownerTab: tab
      }
      Request.get('/repair-records/page', {
        params
      }).then(response => {
        // 只接受最后一次请求的结果
        if (seq !== this.repairLoadSeq) return
        if (response.code === '0') {
          this.repairRecords = response.data.records
          this.total = response.data.total
        } else {
          this.$message({
            message: response.msg,
            type: 'error'
          })
        }
      }).finally(() => {
        if (seq !== this.repairLoadSeq) return
        this.loading = false
      })

    },

    openAiFillDialog() {
      this.aiFillInput = ''
      this.aiFillVisible = true
    },

    async submitAiFill() {
      const text = (this.aiFillInput || '').trim()
      if (!text) {
        this.$message.warning('请输入故障描述')
        return
      }
      if (!this.repairTypes || !this.repairTypes.length) {
        this.$message.warning('维修类型尚未加载完成，请稍后再试')
        return
      }
      this.aiFillLoading = true
      try {
        const res = await Request.post(
          '/ai/chat',
          { type: 'repair_parse', message: text },
          { timeout: 120000, skipGlobalError: true }
        )
        if (!res || String(res.code) !== '0' || res.data == null) {
          const msg = (res && res.msg) ? res.msg : '请求失败'
          this.$message.warning(msg + '。请稍后重试，或关闭窗口后在表单中手动选择报修类型。')
          return
        }
        const d = res.data
        if (!d.success) {
          if (d.reason === 'not_repair') {
            this.$message.warning(
              '当前描述更像投诉或非设施问题。若为投诉请前往「投诉建议」；若是报修，请改写成具体故障现象后点「确认」重试。'
            )
          } else {
            this.repairForm.description = text
            this.$message.warning(
              '未能自动匹配报修类型，已将您输入的内容填入「报修描述」。请手动选择「报修类型」后提交；也可修改说法后在此重试。'
            )
          }
          return
        }
        if (d.repairTypeId != null) {
          this.repairForm.type = d.repairTypeId
        }
        this.repairForm.description = text
        if (d.source === 'fallback') {
          this.$message.success('已按关键词辅助匹配报修类型，请核对后再提交')
        } else {
          this.$message.success('已辅助填写报修类型与描述，请核对后再提交')
        }
        this.aiFillVisible = false
      } catch (e) {
        const errMsg = (e && e.message) ? e.message : ''
        if (errMsg.includes('timeout')) {
          this.$message.error('请求超时，可稍后重试；或直接关闭窗口在表单中手动填写。')
        } else {
          this.$message.error(
            '请求失败：' + (errMsg || '网络异常') + '。请关闭窗口后在表单中手动填写，或稍后重试。'
          )
        }
      } finally {
        this.aiFillLoading = false
      }
    },

    // 提交报修
    submitRepair() {
      this.$refs.repairForm.validate(valid => {
        if (valid) {
          this.submitting = true
          const data = {
            applicantId: this.ownerInfo.id, // 添加申请人ID
            houseId: this.ownerInfo.houseId, // 添加房屋ID
            repairTypeId: this.repairForm.type,
            maintainerId: this.repairForm.maintainerId, // 选择的维修师傅
            description: this.repairForm.description,
            expectedTime: moment(this.repairForm.expectTime).format('YYYY-MM-DD HH:mm:ss')
          }
          Request.post('/repair-records', data).then(response => {
            if (response.code === '0') {
              this.$message.success('报修提交成功')
              this.$refs.repairForm.resetFields()
              this.pageNum = 1
              if (this.repairTab === 'pending') {
                this.getRepairRecords('pending')
              } else {
                this.repairTab = 'pending'
              }
            } else {
              this.$message({
                message: response.msg,
                type: 'error'
              })
            }
          }).finally(() => {
            this.submitting = false
          })
        }
      })
    },

    openDetail(row) {
      this.detailForm = JSON.parse(JSON.stringify(row || {}))
      this.editForm = {
        id: row.id,
        repairTypeId: row.repairTypeId,
        description: row.description
      }
      this.evaluateForm = {
        evaluationRating: 0,
        evaluation: row.evaluation && !row.evaluationRating ? row.evaluation : ''
      }
      this.detailVisible = true
      this.$nextTick(() => {
        if (this.$refs.editForm) this.$refs.editForm.clearValidate()
        if (this.$refs.evaluateForm) this.$refs.evaluateForm.clearValidate()
      })
    },
    onDetailClosed() {
      this.detailForm = {}
    },
    /** 保存/评价成功后刷新详情（带维修类型、师傅等关联信息） */
    syncDetailAfterSave(id) {
      if (!id) return
      Request.get(`/repair-records/${id}`).then(res => {
        if (res.code === '0' && res.data) {
          const row = res.data
          this.detailForm = JSON.parse(JSON.stringify(row))
          this.editForm = {
            id: row.id,
            repairTypeId: row.repairTypeId,
            description: row.description
          }
        }
      })
    },
    // 提交编辑（仅接单前）
    submitEdit() {
      if (!this.$refs.editForm) return
      this.$refs.editForm.validate(valid => {
        if (valid) {
          this.editing = true
          const data = {
            id: this.editForm.id,
            repairTypeId: this.editForm.repairTypeId,
            description: this.editForm.description,
            status: 'PENDING'
          }
          Request.put('/repair-records/edit-pending', data)
            .then(response => {
              if (response.code === '0') {
                this.$message.success('报修修改成功')
                this.getRepairRecords()
                this.syncDetailAfterSave(this.editForm.id)
              } else {
                this.$message({ message: response.msg, type: 'error' })
              }
            })
            .finally(() => {
              this.editing = false
            })
        }
      })
    },
    submitEvaluateFromDetail() {
      if (!this.$refs.evaluateForm) return
      this.$refs.evaluateForm.validate(valid => {
        if (!valid) return
        if (!this.ownerInfo || this.ownerInfo.id == null) return
        this.evaluating = true
        Request.put(
          `/repair-records/${this.detailForm.id}/evaluation?applicantId=${this.ownerInfo.id}`,
          {
            evaluationRating: this.evaluateForm.evaluationRating,
            evaluation: this.evaluateForm.evaluation
          }
        )
          .then(response => {
            if (response.code === '0') {
              this.$message.success('评价提交成功')
              this.pageNum = 1
              this.repairTab = 'done'
              this.getRepairRecords('done')
              this.syncDetailAfterSave(this.detailForm.id)
            } else {
              this.$message.error(response.msg || '提交失败')
            }
          })
          .finally(() => {
            this.evaluating = false
          })
      })
    },
    // 打开“待指派”维修工列表（供业主查看）
    openMaintainerPicker(row) {
      this.maintainerPickerVisible = true
      if (this.availableMaintainers && this.availableMaintainers.length) return
      Request.get('/maintenance-staff/all').then(response => {
        if (response.code === '0') {
          this.availableMaintainers = response.data || []
        } else {
          this.$message.error(response.msg || '获取维修人员失败')
        }
      })
    },
    handleCancel(row) {
      if (!row || !row.id) return
      if (!this.ownerInfo || this.ownerInfo.id == null) return
      this.$confirm('确定要取消该报修吗？取消后该记录将进入已取消状态。', '提示', {
        type: 'warning'
      }).then(() => {
        Request.put(`/repair-records/${row.id}/cancel?applicantId=${this.ownerInfo.id}`)
          .then(response => {
            if (response && response.code === '0') {
              this.$message.success('取消成功')
              this.getRepairRecords()
              if (this.detailVisible && this.detailForm && this.detailForm.id === row.id) {
                this.detailVisible = false
              }
            } else {
              this.$message.error((response && response.msg) || '取消失败')
            }
          })
          .catch(() => {})
      }).catch(() => {})
    },
    handleCancelFromDetail() {
      this.handleCancel(this.detailForm)
    },
    // 分页方法
    handleSizeChange(val) {
      this.pageSize = val
      this.getRepairRecords()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getRepairRecords()
    },
    // 状态显示方法（COMPLETED 时根据是否已评价区分「待评价 / 已完成」）
    getStatusText(status, row) {
      if (status === 'COMPLETED' && row && !row.evaluationRating) {
        return '待评价'
      }
      const statusMap = {
        'PENDING': '待接单',
        'IN_PROGRESS': '处理中',
        'WAIT_REASSIGN': '待重新指派',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || '未知'
    },
    getStatusTag(status, row) {
      if (status === 'COMPLETED' && row && !row.evaluationRating) {
        return 'primary'
      }
      const statusMap = {
        'PENDING': 'info',
        'IN_PROGRESS': 'warning',
        'WAIT_REASSIGN': 'danger',
        'COMPLETED': 'success',
        'CANCELLED': 'danger'
      }
      return statusMap[status] || 'info'
    }
  }
}
</script>

<style scoped>
/* 整体页面布局 */
.repair-page {
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

/* 页面标题 */
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

/* 报修表单卡片 */
.repair-form {
  background: #fff;
  padding: 30px;
  margin-bottom: 24px;
  transition: all 0.3s;
}

.repair-form:hover {
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.form-header {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 20px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.form-header__left {
  display: flex;
  align-items: center;
}

.form-header i {
  font-size: 20px;
  margin-right: 8px;
  color: #409EFF;
}

.form-header span {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.ai-fill-hint {
  margin: 0 0 8px;
  color: #606266;
  font-size: 14px;
  line-height: 1.55;
}

.ai-fill-hint--sub {
  margin: 0 0 12px;
  font-size: 13px;
  color: #909399;
}

.ai-fill-fail-hint {
  margin: 12px 0 0;
  padding: 10px 12px;
  font-size: 12px;
  line-height: 1.55;
  color: #909399;
  background: #f4f4f5;
  border-radius: 6px;
}

/* 表格卡片 */
.repair-records {
  background: #fff;
  padding: 20px;
  transition: all 0.3s;
}

.repair-records:hover {
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

/* 仅保留 el-card 头部的底边线，避免与 .el-card__header 默认边框叠成双线 */
.card-header {
  padding-bottom: 0;
  margin-bottom: 0;
}

.repair-record-tabs {
  margin-bottom: 16px;
}

.repair-record-tabs >>> .el-tabs__header {
  margin-bottom: 0;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-left i {
  font-size: 18px;
  margin-right: 8px;
  color: #409EFF;
}

.header-left span {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

/* 表格样式 */
.el-table {
  margin: 15px 0;
}

.el-table ::v-deep .el-table__header-wrapper th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 500;
}

/* 标签样式 */
.el-tag {
  border-radius: 4px;
  padding: 0 8px;
  height: 24px;
  line-height: 22px;
}

/* 状态标签 */
.el-tag.el-tag--info {
  background-color: #f4f4f5;
  border-color: #e9e9eb;
  color: #909399;
}

.el-tag.el-tag--warning {
  background-color: #fdf6ec;
  border-color: #faecd8;
  color: #e6a23c;
}

.el-tag.el-tag--success {
  background-color: #f0f9eb;
  border-color: #e1f3d8;
  color: #67c23a;
}

/* 按钮样式 */
.el-button--primary {
  padding: 10px 20px;
}

/* 评价对话框 */
.el-dialog {
  border-radius: 0;
}

.el-dialog ::v-deep .el-dialog__header {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.el-dialog ::v-deep .el-dialog__body {
  padding: 30px 20px;
}

.el-dialog ::v-deep .el-dialog__footer {
  padding: 15px 20px;
  border-top: 1px solid #ebeef5;
}

/* 分页 */
.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.pay-tip {
  font-size: 13px;
  color: #909399;
  margin-top: 8px;
}

.repair-detail-body {
  padding: 0 4px;
}

.detail-alert {
  margin-bottom: 16px;
}

.detail-edit-block,
.detail-eval-block {
  margin-top: 16px;
}

.eval-readonly .eval-stars-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.eval-readonly .eval-label {
  color: #606266;
  font-size: 14px;
}

.eval-readonly .eval-text {
  margin: 0;
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
}

.detail-eval-block .eval-flow-hint {
  margin: 0 0 12px;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}

.repair-progress-steps {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

/* 表单项 */
.el-form-item ::v-deep .el-form-item__label {
  color: #606266;
}

.el-input ::v-deep .el-input__inner,
.el-textarea ::v-deep .el-textarea__inner {
  border-color: #dcdfe6;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .content-wrapper {
    padding: 15px;
  }
  
  .repair-form,
  .repair-records {
    padding: 15px;
  }
  
  .el-col {
    width: 100% !important;
  }
  
  .form-header,
  .card-header {
    padding-bottom: 15px;
    margin-bottom: 15px;
  }
  
  .page-title {
    font-size: 20px;
  }
}
</style> 