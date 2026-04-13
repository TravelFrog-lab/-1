<template>
    <div class="wrapper">
        <!-- 页面标题 -->
        <div class="page-header">
            <div class="header-content">
                <h2>维修记录管理</h2>
                <div class="divider"></div>
            </div>
        </div>

        <el-card class="menu-card" shadow="never">
            <!-- 操作按钮区 -->
            <div class="control-btns">
                <div class="left-btns">
                    <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="fetchData">刷新</el-button>
                </div>

                <div class="right-btns">
                    <!-- 只有管理员可以看到这些按钮 -->
                    <template v-if="userInfo.role === 'ADMIN'">
                        <el-button type="primary" size="medium" icon="el-icon-download" @click="exportVisible = true" plain>导出数据</el-button>
                        <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增维修</el-button>
                    </template>
                </div>
            </div>

            <!-- 维修工：标签分类（待接单 / 待处理 / 处理中 / 处理完成） -->
            <el-tabs
                v-if="userInfo && userInfo.role === 'MAINTENANCE'"
                v-model="maintainerTab"
                class="maintainer-work-tabs"
                @tab-click="onMaintainerTabClick"
            >
                <el-tab-pane label="待接单" name="take" />
                <el-tab-pane label="待处理" name="wait_reassign" />
                <el-tab-pane label="处理中" name="progress" />
                <el-tab-pane label="处理完成" name="done" />
            </el-tabs>

            <!-- 管理员：按订单状态分类（全部 / 待处理 / 处理中 / 已完成；不含「待评价」） -->
            <el-tabs
                v-if="userInfo && userInfo.role === 'ADMIN'"
                v-model="adminTab"
                class="admin-repair-tabs"
                @tab-click="onAdminTabClick"
            >
                <el-tab-pane label="全部" name="all" />
                <el-tab-pane label="待处理" name="pending" />
                <el-tab-pane label="处理中" name="progress" />
                <el-tab-pane label="已完成" name="done" />
            </el-tabs>

            <!-- 管理员：申请人 / 类型；非维修工：可含状态下拉（管理员已用上方标签分类） -->
            <el-form v-if="userInfo && userInfo.role !== 'MAINTENANCE'" ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="申请人">
                    <el-input v-model="listQuery.applicantName" placeholder="请输入申请人姓名" size="medium" />
                </el-form-item>
                <el-form-item label="维修类型">
                    <el-select v-model="listQuery.repairTypeId" placeholder="请选择维修类型" size="medium" clearable>
                        <el-option
                            v-for="item in repairTypes"
                            :key="item.id"
                            :label="item.name"
                            :value="item.id">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item v-if="userInfo.role !== 'ADMIN'" label="状态">
                    <el-select v-model="listQuery.status" placeholder="请选择状态" size="medium" clearable>
                        <el-option label="待处理" value="PENDING"></el-option>
                            <el-option label="待重新指派" value="WAIT_REASSIGN"></el-option>
                        <el-option label="处理中" value="IN_PROGRESS"></el-option>
                        <el-option label="已完成" value="COMPLETED"></el-option>
                            <el-option label="已取消" value="CANCELLED"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" size="medium" plain icon="el-icon-search" @click="onSubmit">查询</el-button>
                    <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="onReset">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 数据表格（维修工端不使用操作列 fixed，避免出现底部横向滚动条） -->
            <el-table 
                ref="multipleTable"
                v-loading="listLoading"
                :data="tableData"
                tooltip-effect="dark"
                row-key="id"
                class="repair-record-table"
                style="width: 100%"
                size="medium"
                :header-cell-style="{background:'#f8f9fa',color:'#606266'}"
                :cell-style="{padding:'12px 0'}"
                border>
                <el-table-column prop="id" label="ID" width="72"></el-table-column>
                <el-table-column prop="applicant.user.name" label="申请人" width="100"></el-table-column>
                <el-table-column prop="house.address" label="房屋地址" min-width="140"></el-table-column>
                <el-table-column prop="repairType.name" label="维修类型" width="110"></el-table-column>
                <el-table-column prop="description" label="维修描述" min-width="120" show-overflow-tooltip></el-table-column>
                <el-table-column prop="status" label="状态" width="96">
                    <template slot-scope="scope">
                        <el-tag :type="getStatusType(scope.row.status)">{{getStatusText(scope.row.status)}}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="expectedTime" label="预约时间" width="156">
                    <template slot-scope="scope">
                        {{ scope.row.expectedTime | formatDate }}
                    </template>
                </el-table-column>
                <el-table-column prop="maintainer.user.name" label="维修人员" width="100"></el-table-column>
                <el-table-column prop="actualTime" label="完成时间" width="156">
                    <template slot-scope="scope">
                        {{ scope.row.actualTime | formatDate }}
                    </template>
                </el-table-column>
                <el-table-column
                    label="操作"
                    :width="isMaintainerDoneTab ? 120 : 280"
                    :fixed="operationColumnFixed"
                    v-if="userInfo.role=='ADMIN'||userInfo.role=='MAINTENANCE'"
                >
                    <template slot-scope="scope">
                        <!-- 维修工 · 处理完成：只查看业主评价 -->
                        <div v-if="isMaintainerDoneTab" class="operation-buttons operation-buttons--eval">
                            <el-button type="primary" size="small" plain @click="openEvaluationView(scope.row)">
                                查看评价
                            </el-button>
                        </div>
                        <div v-else class="operation-buttons">
                            <el-button 
                                v-if="(scope.row.status === 'PENDING' || scope.row.status === 'WAIT_REASSIGN') && userInfo.role=='ADMIN'"
                                plain size="small" 
                                type="success"
                                @click="handleAssign(scope.row)">分配维修</el-button>

                            <el-button
                                v-if="scope.row.status === 'PENDING' && userInfo.role=='MAINTENANCE'"
                                plain size="small"
                                type="primary"
                                @click="handleTake(scope.row)">接单</el-button>

                            <el-button
                                v-if="scope.row.status === 'PENDING' && userInfo.role=='MAINTENANCE'"
                                plain size="small"
                                type="danger"
                                @click="handleReject(scope.row)">拒单</el-button>

                            <el-button 
                                v-if="scope.row.status === 'IN_PROGRESS'"
                                plain size="small" 
                                type="warning"
                                @click="handleComplete(scope.row)">完成维修</el-button>

                            <el-button
                                v-if="userInfo.role=='ADMIN' && scope.row.status !== 'COMPLETED' && scope.row.status !== 'CANCELLED'"
                                plain size="small"
                                type="danger"
                                @click="handleForceComplete(scope.row)">强制完成</el-button>
                            
                            <!-- 管理员：更多下拉 -->
                            <el-dropdown
                                v-if="userInfo.role=='ADMIN'"
                                trigger="click"
                                @command="handleCommand($event, scope.row)"
                            >
                                <el-button plain size="small" type="info">
                                    更多<i class="el-icon-arrow-down el-icon--right"></i>
                                </el-button>
                                <el-dropdown-menu slot="dropdown">
                                    <el-dropdown-item command="detail">详细信息</el-dropdown-item>
                                    <el-dropdown-item command="edit" v-if="scope.row.status === 'PENDING' && userInfo.role=='ADMIN'">编辑</el-dropdown-item>
                                    <el-dropdown-item command="delete" v-if="scope.row.status === 'COMPLETED' && userInfo.role=='ADMIN'">删除</el-dropdown-item>
                                </el-dropdown-menu>
                            </el-dropdown>
                            <!-- 维修工：直接「详细信息」；处理中标签不显示 -->
                            <el-button
                                v-if="userInfo.role=='MAINTENANCE' && !isMaintainerProgressTab"
                                plain
                                size="small"
                                type="info"
                                @click="handleDetail(scope.row)"
                            >
                                详细信息
                            </el-button>
                        </div>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <Pagination
                v-show="total>0"
                :total="total"
                :page.sync="listQuery.pageNum"
                :limit.sync="listQuery.pageSize"
                @pagination="fetchData"
            />

            <!-- 新增/编辑弹窗 -->
            <el-dialog 
                :title="dialogForm.id ? '编辑维修记录' : '新增维修记录'" 
                :visible.sync="formVisible"
                width="500px"
                :close-on-click-modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="申请人" prop="applicantId">
                        <el-select v-model="dialogForm.applicantId" placeholder="请选择申请人" filterable>
                            <el-option
                                v-for="item in owners"
                                :key="item.id"
                                :label="item.user.name"
                                :value="item.id">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="房屋" prop="houseId">
                        <el-select v-model="dialogForm.houseId" placeholder="请选择房屋">
                            <el-option
                                v-for="item in houses"
                                :key="item.id"
                                :label="item.address"
                                :value="item.id">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="维修类型" prop="repairTypeId">
                        <el-select v-model="dialogForm.repairTypeId" placeholder="请选择维修类型">
                            <el-option
                                v-for="item in repairTypes"
                                :key="item.id"
                                :label="item.name"
                                :value="item.id">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="预约时间" prop="expectedTime">
                        <el-date-picker
                            v-model="dialogForm.expectedTime"
                            type="datetime"
                            placeholder="选择预约时间"
                            :picker-options="pickerOptions"
                            value-format="yyyy-MM-dd HH:mm:ss">
                        </el-date-picker>
                    </el-form-item>
                    <el-form-item label="维修描述" prop="description">
                        <el-input type="textarea" v-model="dialogForm.description" placeholder="请输入维修描述"></el-input>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="formVisible = false">取 消</el-button>
                    <el-button type="primary" :loading="isSubmit" @click="handleSave('dialogForm')">确 定</el-button>
                </div>
            </el-dialog>

            <!-- 分配维修人员弹窗 -->
            <el-dialog
                title="分配维修人员"
                :visible.sync="assignVisible"
                width="400px"
                :close-on-click-modal="false">
                <el-form :model="assignForm" :rules="assignRules" ref="assignForm" label-width="100px">
                    <el-form-item label="维修人员" prop="maintainerId">
                        <el-select v-model="assignForm.maintainerId" placeholder="请选择维修人员">
                            <el-option
                                v-for="item in maintainers"
                                :key="item.id"
                                :label="item.user.name"
                                :value="item.id">
                            </el-option>
                        </el-select>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="assignVisible = false">取 消</el-button>
                    <el-button type="primary" :loading="isSubmit" @click="submitAssign('assignForm')">确 定</el-button>
                </div>
            </el-dialog>

            <!-- 完成维修弹窗 -->
            <el-dialog
                title="完成维修"
                :visible.sync="completeVisible"
                width="400px"
                :close-on-click-modal="false">
                <el-form :model="completeForm" :rules="completeRules" ref="completeForm" label-width="100px">
                    <el-form-item label="维修结果" prop="resultDescription">
                        <el-input type="textarea" v-model="completeForm.resultDescription" placeholder="请输入维修结果描述"></el-input>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="completeVisible = false">取 消</el-button>
                    <el-button type="primary" :loading="isSubmit" @click="submitComplete('completeForm')">确 定</el-button>
                </div>
            </el-dialog>

            <!-- 拒单弹窗 -->
            <el-dialog
                title="拒单（需填写原因）"
                :visible.sync="rejectVisible"
                width="420px"
                :close-on-click-modal="false">
                <el-form :model="rejectForm" :rules="rejectRules" ref="rejectForm" label-width="100px">
                    <el-form-item label="拒单原因" prop="reason">
                        <el-input type="textarea" v-model="rejectForm.reason" placeholder="请输入拒单原因（如：时间冲突）"></el-input>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="rejectVisible = false">取 消</el-button>
                    <el-button type="primary" :loading="isSubmit" @click="submitReject('rejectForm')">确 定</el-button>
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

            <!-- 业主评价（维修工 · 处理完成） -->
            <el-dialog
                title="业主评价"
                :visible.sync="evaluationVisible"
                width="480px"
                append-to-body
                @closed="evaluationViewRow = null"
            >
                <div v-if="evaluationViewRow" class="evaluation-view">
                    <template v-if="evaluationViewRow.evaluationRating">
                        <div class="eval-stars-row">
                            <span class="eval-label">星级</span>
                            <el-rate
                                :value="evaluationViewRow.evaluationRating"
                                disabled
                                show-score
                                text-color="#ff9900"
                                score-template="{value} 分"
                            />
                        </div>
                        <p class="eval-text">{{ evaluationViewRow.evaluation || '—' }}</p>
                    </template>
                    <el-empty v-else description="业主尚未提交评价" :image-size="80" />
                </div>
                <span slot="footer" class="dialog-footer">
                    <el-button type="primary" @click="evaluationVisible = false">关 闭</el-button>
                </span>
            </el-dialog>

            <!-- 添加详细信息弹窗 -->
            <el-dialog title="维修记录详情" :visible.sync="detailVisible" width="600px">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="申请人">{{detailForm.applicant && detailForm.applicant.user ? detailForm.applicant.user.name : ''}}</el-descriptions-item>
                    <el-descriptions-item label="联系电话">{{detailForm.applicant?.user?.phone}}</el-descriptions-item>
                    <el-descriptions-item label="房屋地址">{{detailForm.house?.address}}</el-descriptions-item>
                    <el-descriptions-item label="维修类型">{{detailForm.repairType?.name}}</el-descriptions-item>
                    <el-descriptions-item label="维修状态">
                        <el-tag :type="getStatusType(detailForm.status)">{{getStatusText(detailForm.status)}}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="预约时间">{{detailForm.expectedTime}}</el-descriptions-item>
                    <el-descriptions-item label="维修人员">{{detailForm.maintainer?.user?.name || '-'}}</el-descriptions-item>
                    <el-descriptions-item label="拒单原因" :span="2">{{detailForm.rejectReason || '-'}}</el-descriptions-item>
                    <el-descriptions-item label="完成时间">{{detailForm.actualTime || '-'}}</el-descriptions-item>
                    <el-descriptions-item label="维修描述" :span="2">{{detailForm.description}}</el-descriptions-item>
                    <el-descriptions-item label="维修结果" :span="2" v-if="detailForm.resultDescription">{{detailForm.resultDescription}}</el-descriptions-item>
                </el-descriptions>
            </el-dialog>
        </el-card>
    </div>
</template>

<script>
import excel from '@/utils/excel.js'
import Pagination from '@/components/Pagination/index.vue'
import Request from '@/utils/request.js'
import { mapGetters } from 'vuex'
import moment from 'moment'
export default {
    name: 'RepairRecordManager',

    components: { Pagination },
    data() {
        return {
            // 列表加载动画
            listLoading: true,

            // 禁止选择过去时间（期望时间）
            pickerOptions: {
                disabledDate(time) {
                    return time.getTime() < Date.now()
                }
            },

            // 查询参数
            listQuery: {
                applicantName: undefined,
                repairTypeId: undefined,
                status: undefined,
                pageNum: 1,
                pageSize: 10,
                maintainerId: undefined
            },
            /** 维修工列表分类：take=待接单(PENDING) / wait_reassign=待处理(WAIT_REASSIGN) / progress / done */
            maintainerTab: 'take',
            /** 管理员列表：all=全部；其余与后端 ownerTab 一致 */
            adminTab: 'all',

            // 表单对象
            dialogForm: {
                id: undefined,
                applicantId: undefined,
                houseId: undefined,
                repairTypeId: undefined,
                description: '',
                expectedTime: undefined
            },

            // 分配维修人员表单
            assignForm: {
                id: undefined,
                maintainerId: undefined
            },

            // 完成维修表单
            completeForm: {
                id: undefined,
                resultDescription: '',
                expectedTime: undefined
            },

            // 是否为管理员强制完成
            forceCompleteMode: false,

            // 拒单表单
            rejectForm: {
                id: undefined,
                reason: ''
            },

            // 数据总数
            total: 0,
            // 表格数据
            tableData: [],

            // 弹窗显示控制
            formVisible: false,
            assignVisible: false,
            completeVisible: false,
            rejectVisible: false,
            exportVisible: false,
            maintainer:undefined,

            // 防止重复提交
            isSubmit: false,

            // 下拉选项数据
            repairTypes: [],
            owners: [],
            houses: [],
            userInfo:undefined,
            maintainers: [],

            // 表单验证规则
            formRules: {
                applicantId: [
                    { required: true, message: '请选择申请人', trigger: 'change' }
                ],
                houseId: [
                    { required: true, message: '请选择房屋', trigger: 'change' }
                ],
                repairTypeId: [
                    { required: true, message: '请选择维修类型', trigger: 'change' }
                ],
                expectedTime: [
                    { required: true, message: '请选择预约时间', trigger: 'change' }
                ],
                description: [
                    { required: true, message: '请输入维修描述', trigger: 'blur' },
                    { min: 1, max: 500, message: '长度在 1 到 500 个字符', trigger: 'blur' }
                ]
            },

            assignRules: {
                maintainerId: [
                    { required: true, message: '请选择维修人员', trigger: 'change' }
                ]
            },

            completeRules: {
                resultDescription: [
                    { required: true, message: '请输入维修结果描述', trigger: 'blur' },
                    { min: 1, max: 500, message: '长度在 1 到 500 个字符', trigger: 'blur' }
                ]
            },

            rejectRules: {
                reason: [
                    { required: true, message: '请输入拒单原因', trigger: 'blur' },
                    { min: 1, max: 300, message: '长度在 1 到 300 个字符', trigger: 'blur' }
                ]
            },

            detailVisible: false,
            detailForm: {},

            /** 维修工查看业主评价 */
            evaluationVisible: false,
            evaluationViewRow: null
        }
    },

    created() {
        this.initUserInfo();
        // 先获取用户信息和维修人员信息，再获取数据列表
        if (this.userInfo && this.userInfo.role === 'MAINTENANCE') {
            this.maintainerTab = 'take'
            this.syncMaintainerTabToQuery()
            this.fetchMaintainerByUserId()
                .then(() => {
                    this.fetchData()
                })
                .catch(() => {
                    // 请求被取消或失败时已在 fetchMaintainerByUserId 内处理，避免 Uncaught (in promise)
                })
        } else {
            this.fetchData();
        }
        this.fetchRepairTypes();
        this.fetchOwners();
        this.fetchHouses();
        this.fetchMaintainers();
    },
    computed: {
        ...mapGetters(['currentUser']),
        /** 维修工当前在「处理完成」标签 */
        isMaintainerDoneTab() {
            return (
                this.userInfo &&
                this.userInfo.role === 'MAINTENANCE' &&
                this.maintainerTab === 'done'
            )
        },
        /** 维修工当前在「处理中」标签（不显示「详细信息」） */
        isMaintainerProgressTab() {
            return (
                this.userInfo &&
                this.userInfo.role === 'MAINTENANCE' &&
                this.maintainerTab === 'progress'
            )
        },
        /** 仅管理员固定操作列在右侧；维修工不 fixed，避免表格出现横向滚动条 */
        operationColumnFixed() {
            return this.userInfo && this.userInfo.role === 'ADMIN' ? 'right' : undefined
        }
    },
    methods: {
        initUserInfo() {
            if (this.currentUser) {
                this.userInfo = { ...this.currentUser }
            }
            // 维修工档案请在 created 里只请求一次 fetchMaintainerByUserId，避免重复请求导致竞态或 Request aborted
        },
        // 获取状态显示类型
        getStatusType(status) {
            const map = {
                'PENDING': 'warning',
                'WAIT_REASSIGN': 'danger',
                'IN_PROGRESS': 'primary',
                'COMPLETED': 'success',
                'CANCELLED': 'danger'
            }
            return map[status]
        },

        // 获取状态显示文本
        getStatusText(status) {
            const map = {
                'PENDING': '待处理',
                'WAIT_REASSIGN': '待重新指派',
                'IN_PROGRESS': '处理中',
                'COMPLETED': '已完成',
                'CANCELLED': '已取消'
            }
            return map[status]
        },

        // 获取维修类型列表
        fetchRepairTypes() {
            Request.get('/repair-types/page', {
                params: {
                    pageSize: 100
                }
            }).then(response => {
                if (response.code === '0') {
                    this.repairTypes = response.data.records
                }
            })
        },

        // 获取业主列表
        fetchOwners() {
            Request.get('/owner/all').then(response => {
                if (response.code === '0') {
                    this.owners = response.data
                }
            })

        },

        // 获取房屋列表
        fetchHouses() {
            Request.get('/houses/all').then(response => {
                if (response.code === '0') {
                    this.houses = response.data
                }
            })

        },

        // 获取维修人员列表
        fetchMaintainers() {
            Request.get('/maintenance-staff/all').then(response => {
                if (response.code === '0') {
                    this.maintainers = response.data
                }
            })

        },
        fetchMaintainerByUserId() {
            return new Promise((resolve, reject) => {
                if (!this.userInfo || this.userInfo.id == null) {
                    reject(new Error('no user'))
                    return
                }
                Request.get('/maintenance-staff/userId/' + this.userInfo.id).then(response => {
                    if (response.code === '0') {
                        this.maintainer = response.data
                        resolve()
                    } else {
                        this.$message.error(response.msg || '获取维修人员信息失败')
                        reject(new Error(response.msg || 'fail'))
                    }
                }).catch(error => {
                    // 路由切换、组件销毁或重复请求时浏览器可能中止 XHR，不必当作业务错误刷屏
                    const msg = error && (error.message || error.code || '')
                    const aborted = String(msg).toLowerCase().includes('abort') || msg === 'ERR_CANCELED' || msg === 'canceled'
                    if (!aborted) {
                        console.error('获取维修人员信息出错:', error)
                    }
                    reject(error)
                })
            })
        },

        syncMaintainerTabToQuery() {
            const map = {
                take: 'PENDING',
                wait_reassign: 'WAIT_REASSIGN',
                progress: 'IN_PROGRESS',
                done: 'COMPLETED'
            }
            this.listQuery.status = map[this.maintainerTab] || 'PENDING'
        },

        onMaintainerTabClick(tab) {
            if (!this.userInfo || this.userInfo.role !== 'MAINTENANCE') return
            if (tab && tab.name) {
                this.maintainerTab = tab.name
            }
            this.syncMaintainerTabToQuery()
            this.listQuery.pageNum = 1
            this.fetchData()
        },

        onAdminTabClick(tab) {
            if (!this.userInfo || this.userInfo.role !== 'ADMIN') return
            if (tab && tab.name) {
                this.adminTab = tab.name
            }
            this.listQuery.pageNum = 1
            this.fetchData()
        },

        // 获取数据列表
        fetchData({ page, limit } = {}) {
            if (page) this.listQuery.pageNum = page;
            if (limit) this.listQuery.pageSize = limit;
            
            // 如果是维修人员，添加维修人ID过滤条件
            if (this.userInfo && this.userInfo.role === 'MAINTENANCE' && this.maintainer) {
                // 维修工：
                // - 待接单（PENDING）：应当是“已指派给自己”的那批
                // - 待处理（WAIT_REASSIGN）：待重新指派，不过滤到自己
                if (this.listQuery.status === 'WAIT_REASSIGN' || this.listQuery.status === 'CANCELLED') {
                    this.listQuery.maintainerId = undefined
                } else {
                    this.listQuery.maintainerId = this.maintainer.id
                }
            }
            
            this.listLoading = true;
            let params
            if (this.userInfo && this.userInfo.role === 'MAINTENANCE') {
                params = {
                    pageNum: this.listQuery.pageNum,
                    pageSize: this.listQuery.pageSize,
                    status: this.listQuery.status,
                    maintainerId: this.listQuery.maintainerId
                }
            } else if (this.userInfo && this.userInfo.role === 'ADMIN') {
                params = {
                    pageNum: this.listQuery.pageNum,
                    pageSize: this.listQuery.pageSize,
                    applicantName: this.listQuery.applicantName,
                    repairTypeId: this.listQuery.repairTypeId,
                    maintainerId: this.listQuery.maintainerId
                }
                if (this.adminTab && this.adminTab !== 'all') {
                    params.ownerTab = this.adminTab
                }
            } else {
                params = { ...this.listQuery }
            }
            Request.get('/repair-records/page', {
                params
            }).then(response => {
                if (response.code === '0') {
                    const { total, records } = response.data;
                    this.total = total;
                    this.tableData = records;
                } else {
                    this.$message.error(response.msg || '获取数据失败');
                }
                this.listLoading = false;
            }).catch(() => {
                this.listLoading = false;
            });
        },

        // 查询
        onSubmit() {
            this.listQuery.pageNum = 1
            this.fetchData()
        },

        // 重置
        onReset() {
            this.listQuery = {
                applicantName: undefined,
                repairTypeId: undefined,
                status: undefined,
                pageNum: 1,
                pageSize: 10,
                maintainerId: undefined
            };
            if (this.userInfo && this.userInfo.role === 'ADMIN') {
                this.adminTab = 'all'
            }
            this.fetchData();
        },

        // 新建
        handleCreate() {
        
            this.dialogForm = {
                applicantId: undefined,
                houseId: undefined,
                repairTypeId: undefined,
                description: '',
                expectedTime: undefined
            }
            this.formVisible = true
        },

        // 编辑
        handleEdit(row) {
            this.dialogForm = JSON.parse(JSON.stringify(row))
            this.formVisible = true
        },

        // 删除
        handleDelete(row) {
            this.$confirm('此操作将删除该维修记录, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete('/repair-records/' + row.id).then(response => {
                    if (response.code === '0') {
                        this.$message.success('删除成功')
                        this.fetchData()
                    } else {
                        this.$message.error(response.msg || '删除失败')
                    }
                })
            })
        },

        // 保存
        handleSave(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) return
                
                this.isSubmit = true
                const request = this.dialogForm.id 
                    ? Request.put('/repair-records/' + this.dialogForm.id, this.dialogForm)
                    : Request.post('/repair-records', this.dialogForm)

                request.then(response => {
                    if (response.code === '0') {
                        this.$message.success(this.dialogForm.id ? '更新成功' : '添加成功')
                        this.formVisible = false
                        this.fetchData()
                    } else {
                        this.$message.error(response.msg || (this.dialogForm.id ? '更新失败' : '添加失败'))
                    }
                }).finally(() => {
                    this.isSubmit = false
                })
            })
        },

        // 分配维修人员
        handleAssign(row) {
            this.assignForm = {
                id: row.id,
                maintainerId: undefined
            }
            this.assignVisible = true
        },

        // 提交分配
        submitAssign(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) return
                
                this.isSubmit = true
                Request.put(`/repair-records/assign/${this.assignForm.id}?maintainerId=${this.assignForm.maintainerId}`).then(response => {
                    if (response.code === '0') {
                        this.$message.success('分配成功')
                        this.assignVisible = false
                        this.fetchData()
                    } else {
                        this.$message.error(response.msg || '分配失败')
                    }
                }).finally(() => {
                    this.isSubmit = false
                })
            })
        },

        // 完成维修
        handleComplete(row) {
            this.completeForm = {
                id: row.id,
                resultDescription: '',
                expectedTime: row.expectedTime
            }
            this.forceCompleteMode = false
            this.completeVisible = true
        },

        // 维修人员接单
        handleTake(row) {
            if (!this.maintainer || !this.maintainer.id) {
                this.$message.error('未获取到当前维修人员信息')
                return
            }
            this.isSubmit = true
            Request.post(`/repair-records/${row.id}/take?maintainerId=${this.maintainer.id}`).then(response => {
                if (response.code === '0') {
                    this.$message.success('接单成功')
                    this.maintainerTab = 'progress'
                    this.syncMaintainerTabToQuery()
                    this.listQuery.pageNum = 1
                    this.fetchData()
                } else {
                    this.$message.error(response.msg || '接单失败')
                }
            }).finally(() => {
                this.isSubmit = false
            })
        },

        // 维修人员拒单
        handleReject(row) {
            if (!this.maintainer || !this.maintainer.id) {
                this.$message.error('未获取到当前维修人员信息')
                return
            }
            this.rejectForm = {
                id: row.id,
                reason: ''
            }
            this.rejectVisible = true
        },

        submitReject(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) return
                this.isSubmit = true
                const id = this.rejectForm.id
                const reason = encodeURIComponent(this.rejectForm.reason || '')
                Request.put(`/repair-records/${id}/reject?maintainerId=${this.maintainer.id}&reason=${reason}`)
                    .then(response => {
                        if (response.code === '0') {
                            this.$message.success('拒单成功')
                            this.rejectVisible = false
                            this.maintainerTab = 'wait_reassign'
                            this.syncMaintainerTabToQuery()
                            this.fetchData()
                        } else {
                            this.$message.error(response.msg || '拒单失败')
                        }
                    })
                    .finally(() => {
                        this.isSubmit = false
                    })
            })
        },

        // 管理员强制完成
        handleForceComplete(row) {
            this.completeForm = {
                id: row.id,
                resultDescription: '',
                expectedTime: row.expectedTime
            }
            this.forceCompleteMode = true
            this.completeVisible = true
        },

        // 提交完成
        submitComplete(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) return
                
                this.isSubmit = true

                // 前端校验：actualTime(当前时间) 不能早于 expectedTime
                if (this.completeForm.expectedTime) {
                    const expected = moment(this.completeForm.expectedTime)
                    const now = moment()
                    if (now.isBefore(expected)) {
                        this.$alert('维修时间不能早于期望时间，请确认', '提示', { type: 'error' })
                        this.isSubmit = false
                        return
                    }
                }

                const id = this.completeForm.id
                const result = encodeURIComponent(this.completeForm.resultDescription || '')
                const url = this.forceCompleteMode
                    ? `/repair-records/complete-force/${id}?result=${result}`
                    : `/repair-records/complete/${id}?result=${result}`

                Request.put(url).then(response => {
                    if (response.code === '0') {
                        const goMaintainerDone =
                            this.userInfo && this.userInfo.role === 'MAINTENANCE' && !this.forceCompleteMode
                        this.$message.success('操作成功')
                        this.completeVisible = false
                        this.forceCompleteMode = false
                        if (goMaintainerDone) {
                            this.maintainerTab = 'done'
                            this.syncMaintainerTabToQuery()
                            this.listQuery.pageNum = 1
                        }
                        this.fetchData()
                    } else {
                        this.$message.error(response.msg || '操作失败')
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
                    header: ['ID', '申请人', '房屋地址', '维修类型', '维修描述', '状态', '预约时间', '维修人员', '完成时间'],
                    key: ['id', 'applicant.user.name', 'house.address', 'repairType.name', 'description', 'status', 'expectedTime', 'maintainer.user.name', 'actualTime'],
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '维修记录数据表',
                    bookType: type
                }
                excel.exportDataToExcel(params)
                this.exportVisible = false
            }
        },

        // 处理下拉菜单命令
        handleCommand(command, row) {
            switch(command) {
                case 'detail':
                    this.handleDetail(row)
                    break
                case 'edit':
                    this.handleEdit(row)
                    break
                case 'delete':
                    this.handleDelete(row)
                    break
            }
        },

        // 查看详情
        handleDetail(row) {
            this.detailForm = JSON.parse(JSON.stringify(row))
            this.detailVisible = true
        },

        openEvaluationView(row) {
            if (!row || !row.id) return
            this.evaluationViewRow = JSON.parse(JSON.stringify(row))
            this.evaluationVisible = true
            Request.get(`/repair-records/${row.id}`).then(res => {
                if (res && res.code === '0' && res.data) {
                    this.evaluationViewRow = res.data
                }
            })
        }
    },
    filters: {
        formatDate(date) {
            if (!date) return ''
            return moment(date).format('YYYY-MM-DD HH:mm:ss')
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

    .maintainer-work-tabs,
    .admin-repair-tabs {
        margin-bottom: 16px;
    }

    .evaluation-view {
        min-height: 80px;
        .eval-stars-row {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 12px;
            .eval-label {
                color: #606266;
                font-size: 14px;
            }
        }
        .eval-text {
            margin: 0;
            line-height: 1.6;
            color: #303133;
            white-space: pre-wrap;
            word-break: break-word;
        }
    }

    .operation-buttons--eval {
        justify-content: flex-end;
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
        padding: 20px 0;

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

    .operation-buttons {
        display: flex;
        align-items: center;
        gap: 12px;
    }
}
</style> 