<template>
    <div class="wrapper">
        <!-- 页面标题 -->
        <div class="page-header">
            <div class="header-content">
                <h2>维修记录管理</h2>
                <div class="divider"></div>
                <p class="subtitle">Repair Record Management</p>
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

            <!-- 查询表单 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
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
                <el-form-item label="状态">
                    <el-select v-model="listQuery.status" placeholder="请选择状态" size="medium" clearable>
                        <el-option label="待处理" value="PENDING"></el-option>
                        <el-option label="处理中" value="IN_PROGRESS"></el-option>
                        <el-option label="已完成" value="COMPLETED"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" size="medium" plain icon="el-icon-search" @click="onSubmit">查询</el-button>
                    <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="onReset">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 数据表格 -->
            <el-table 
                ref="multipleTable"
                v-loading="listLoading"
                :data="tableData"
                tooltip-effect="dark"
                row-key="id"
                style="width: 100%"
                size="medium"
                :header-cell-style="{background:'#f8f9fa',color:'#606266'}"
                :cell-style="{padding:'12px 0'}"
                border>
                <el-table-column prop="id" label="ID" width="80"></el-table-column>
                <el-table-column prop="applicant.user.name" label="申请人" width="120"></el-table-column>
                <el-table-column prop="house.address" label="房屋地址" width="180"></el-table-column>
                <el-table-column prop="repairType.name" label="维修类型" width="120"></el-table-column>
                <el-table-column prop="description" label="维修描述" show-overflow-tooltip></el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                    <template slot-scope="scope">
                        <el-tag :type="getStatusType(scope.row.status)">{{getStatusText(scope.row.status)}}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="expectedTime" label="预约时间" width="160">
                    <template slot-scope="scope">
                        {{ scope.row.expectedTime | formatDate }}
                    </template>
                </el-table-column>
                <el-table-column prop="maintainer.user.name" label="维修人员" width="120"></el-table-column>
                <el-table-column prop="actualTime" label="完成时间" width="160">
                    <template slot-scope="scope">
                        {{ scope.row.actualTime | formatDate }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="280" fixed="right" v-if="userInfo.role=='ADMIN'||userInfo.role=='MAINTENANCE'">
                    <template slot-scope="scope">
                        <div class="operation-buttons">
                            <el-button 
                                v-if="(scope.row.status === 'PENDING' || !scope.row.maintainerId) && userInfo.role=='ADMIN'"
                                plain size="small" 
                                type="success"
                                @click="handleAssign(scope.row)">分配维修</el-button>
                            <el-button 
                                v-if="scope.row.status === 'IN_PROGRESS'"
                                plain size="small" 
                                type="warning"
                                @click="handleComplete(scope.row)">完成维修</el-button>
                            
                            <el-dropdown trigger="click" @command="handleCommand($event, scope.row)">
                                <el-button plain size="small" type="info">
                                    更多<i class="el-icon-arrow-down el-icon--right"></i>
                                </el-button>
                                <el-dropdown-menu slot="dropdown">
                                    <el-dropdown-item command="detail">详细信息</el-dropdown-item>
                                    <el-dropdown-item command="edit" v-if="scope.row.status === 'PENDING' && userInfo.role=='ADMIN'">编辑</el-dropdown-item>
                                    <el-dropdown-item command="delete" v-if="scope.row.status === 'COMPLETED' && userInfo.role=='ADMIN'">删除</el-dropdown-item>
                                </el-dropdown-menu>
                            </el-dropdown>
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
                    <el-form-item label="维修费用" prop="feeAmount">
                        <el-input-number v-model="completeForm.feeAmount" :min="0" :precision="2" :step="10" style="width: 100%" />
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="completeVisible = false">取 消</el-button>
                    <el-button type="primary" :loading="isSubmit" @click="submitComplete('completeForm')">确 定</el-button>
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

            <!-- 添加详细信息弹窗 -->
            <el-dialog title="维修记录详情" :visible.sync="detailVisible" width="600px">
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="申请人">{{detailForm.applicant?.user?.name}}</el-descriptions-item>
                    <el-descriptions-item label="联系电话">{{detailForm.applicant?.user?.phone}}</el-descriptions-item>
                    <el-descriptions-item label="房屋地址">{{detailForm.house?.address}}</el-descriptions-item>
                    <el-descriptions-item label="维修类型">{{detailForm.repairType?.name}}</el-descriptions-item>
                    <el-descriptions-item label="维修状态">
                        <el-tag :type="getStatusType(detailForm.status)">{{getStatusText(detailForm.status)}}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="预约时间">{{detailForm.expectedTime}}</el-descriptions-item>
                    <el-descriptions-item label="维修人员">{{detailForm.maintainer?.user?.name || '-'}}</el-descriptions-item>
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

            // 查询参数
            listQuery: {
                applicantName: undefined,
                repairTypeId: undefined,
                status: undefined,
                pageNum: 1,
                pageSize: 10,
                maintainerId: undefined
            },

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
                feeAmount: undefined
            },

            // 数据总数
            total: 0,
            // 表格数据
            tableData: [],

            // 弹窗显示控制
            formVisible: false,
            assignVisible: false,
            completeVisible: false,
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

            detailVisible: false,
            detailForm: {}
        }
    },

    created() {
        this.initUserInfo();
        // 先获取用户信息和维修人员信息，再获取数据列表
        if (this.userInfo && this.userInfo.role === 'MAINTENANCE') {
            this.fetchMaintainerByUserId().then(() => {
                this.fetchData();
            });
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
  },
    methods: {
        initUserInfo() {
            if (this.currentUser) {
                this.userInfo = { ...this.currentUser };
                if(this.userInfo.role === 'MAINTENANCE'){
                    this.fetchMaintainerByUserId()
                    console.log("this.maintainer11",this.maintainer)
                }
            }
        },
        // 获取状态显示类型
        getStatusType(status) {
            const map = {
                'PENDING': 'warning',
                'IN_PROGRESS': 'primary',
                'COMPLETED': 'success'
            }
            return map[status]
        },

        // 获取状态显示文本
        getStatusText(status) {
            const map = {
                'PENDING': '待处理',
                'IN_PROGRESS': '处理中',
                'COMPLETED': '已完成'
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
                Request.get('/maintenance-staff/userId/' + this.userInfo.id).then(response => {
                    if (response.code === '0') {
                        this.maintainer = response.data;
                        console.log("获取到维修人员信息:", this.maintainer);
                        resolve();
                    } else {
                        this.$message.error(response.msg || '获取维修人员信息失败');
                        reject();
                    }
                }).catch(error => {
                    console.error("获取维修人员信息出错:", error);
                    reject(error);
                });
            });
        },

        // 获取数据列表
        fetchData({ page, limit } = {}) {
            if (page) this.listQuery.pageNum = page;
            if (limit) this.listQuery.pageSize = limit;
            
            // 如果是维修人员，添加维修人ID过滤条件
            if (this.userInfo && this.userInfo.role === 'MAINTENANCE' && this.maintainer) {
                this.listQuery.maintainerId = this.maintainer.id;
                console.log("使用维修人员ID过滤:", this.listQuery.maintainerId);
            }
            
            this.listLoading = true;
            Request.get('/repair-records/page', {
                params: this.listQuery
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
                pageSize: 10
            };
            
            // 如果是维修人员，保持维修人ID过滤条件
            if (this.userInfo && this.userInfo.role === 'MAINTENANCE' && this.maintainer) {
                this.listQuery.maintainerId = this.maintainer.id;
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
                feeAmount: row.feeAmount
            }
            this.completeVisible = true
        },

        // 提交完成
        submitComplete(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) return
                
                this.isSubmit = true
                const fee = this.completeForm.feeAmount != null ? `&fee=${this.completeForm.feeAmount}` : ''
                Request.put(`/repair-records/complete/${this.completeForm.id}?result=${this.completeForm.resultDescription}${fee}`).then(response => {
                    if (response.code === '0') {

                        this.$message.success('操作成功')
                        this.completeVisible = false
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