<template>
    <div class="wrapper">
        <div class="page-header">
            <div class="header-content">
                <h2>物业费管理</h2>
                <div class="divider"></div>
                <p class="subtitle">Property Fee Management</p>
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
                    <el-dropdown @command="handleCalculateCommand" style="margin-left: 8px;">
                        <el-button type="primary" size="medium" plain>
                            计算物业费<i class="el-icon-arrow-down el-icon--right"></i>
                        </el-button>
                        <el-dropdown-menu slot="dropdown">
                            <el-dropdown-item command="single">单个计算</el-dropdown-item>
                            <el-dropdown-item command="batch">批量计算</el-dropdown-item>
                        </el-dropdown-menu>
                    </el-dropdown>
                </div>
            </div>

            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="业主姓名">
                    <el-input v-model="listQuery.ownerName" placeholder="请输入业主姓名" size="medium" />
                </el-form-item>
                <el-form-item label="房屋地址">
                    <el-input v-model="listQuery.address" placeholder="请输入房屋地址" size="medium" />
                </el-form-item>
                <el-form-item label="缴费状态">
                    <el-select v-model="listQuery.status" placeholder="请选择" size="medium">
                        <el-option label="全部" value=""></el-option>
                        <el-option label="已缴费" value="PAID"></el-option>
                        <el-option label="未缴费" value="UNPAID"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="费用月份">
                    <el-date-picker
                        v-model="listQuery.feeDate"
                        type="month"
                        placeholder="选择月份"
                        format="yyyy-MM"
                        value-format="yyyy-MM"
                        size="medium">
                    </el-date-picker>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" size="medium" plain icon="el-icon-search" @click="onSubmit">查询</el-button>
                    <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="onReset">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 表格栏 -->
            <el-table ref="multipleTable" 
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
                <el-table-column prop="orderNo" label="订单编号" width="220"></el-table-column>
                <el-table-column label="业主信息" width="180">
                    <template slot-scope="scope">
                        <div>{{scope.row.owner?.user?.name}}</div>
                        <div style="font-size: 12px;color: #999;">{{scope.row.owner?.user?.phone}}</div>
                    </template>
                </el-table-column>
                <el-table-column label="房屋信息" width="200">
                    <template slot-scope="scope">
                        <div>{{scope.row.house?.address}}</div>
                        <div style="font-size: 12px;color: #999;">
                            {{scope.row.house?.houseType?.name}} 
                            ({{scope.row.house?.houseType?.area}}㎡)
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="feeDate" label="费用月份" width="120">
                    <template slot-scope="scope">
                        {{formatFeeDate(scope.row.feeDate)}}
                    </template>
                </el-table-column>
                <el-table-column prop="amount" label="费用金额" width="120">
                    <template slot-scope="scope">
                        ¥{{scope.row.amount}}
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="缴费状态" width="100">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.status === 'PAID' ? 'success' : 'warning'">
                            {{scope.row.status === 'PAID' ? '已缴费' : '未缴费'}}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="paymentTime" label="缴费时间" width="180">
                    <template slot-scope="scope">
                        {{ formatDateTime(scope.row.paymentTime) }}
                    </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="180">
                    <template slot-scope="scope">
                        {{ formatDateTime(scope.row.createdAt) }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                    <template slot-scope="scope">
                        <el-button v-if="scope.row.status === 'UNPAID'"
                            plain size="small" type="success" 
                            icon="el-icon-check" 
                            @click="handlePay(scope.row)">缴费</el-button>
                        <el-button v-if="scope.row.status === 'UNPAID'"
                            plain size="small" type="danger" 
                            icon="el-icon-delete" 
                            @click="handleDelete(scope.row)">删除</el-button>
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

            <!-- 单个计算物业费弹窗 -->
            <el-dialog 
                title="计算物业费" 
                :visible.sync="calculateVisible" 
                width="500px"
                :close-on-click-modal="false">
                <el-form :model="calculateForm" :rules="calculateRules" ref="calculateForm" label-width="100px">
                    <el-form-item label="业主" prop="ownerId">
                        <el-select v-model="calculateForm.ownerId" placeholder="请选择业主" 
                            filterable>
                            <el-option
                                v-for="item in ownerOptions"
                                :key="item.id"
                                :label="item.user.name"
                                :value="item.id">
                                <span>{{ item.user.name }}</span>
                                <span style="float: right; color: #8492a6; font-size: 13px">
                                    {{ item.house.address }}
                                </span>
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="费用月份" prop="feeDate">
                        <el-date-picker
                            v-model="calculateForm.feeDate"
                            type="month"
                            placeholder="选择月份"
                            format="yyyy-MM"
                            value-format="yyyy-MM">
                        </el-date-picker>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="calculateVisible = false">取 消</el-button>
                    <el-button type="primary" :loading="isCalculating" @click="handleCalculate('calculateForm')">确 定</el-button>
                </div>
            </el-dialog>

            <!-- 批量计算确认弹窗 -->
            <el-dialog 
                title="批量计算物业费" 
                :visible.sync="batchCalculateVisible" 
                width="400px">
                <el-form :model="batchCalculateForm" ref="batchCalculateForm" label-width="100px">
                    <el-form-item label="费用月份" prop="feeDate">
                        <el-date-picker
                            v-model="batchCalculateForm.feeDate"
                            type="month"
                            placeholder="选择月份"
                            format="yyyy-MM"
                            value-format="yyyy-MM">
                        </el-date-picker>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="batchCalculateVisible = false">取 消</el-button>
                    <el-button type="primary" :loading="isCalculating" @click="handleBatchCalculate">确 定</el-button>
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
    name: 'PropertyFeeManager',
    components: { Pagination },
    data() {
        return {
            // 数据列表加载动画
            listLoading: true,

            // 查询列表参数对象
            listQuery: {
                ownerName: undefined,
                address: undefined,
                status: undefined,
                feeDate: undefined,
                pageNum: 1,
                pageSize: 10
            },

            // 新增/编辑提交表单对象
            dialogForm: {
                id: undefined,
                ownerId: undefined,
                houseId: undefined,
                feeDate: undefined,
                amount: undefined
            },

            // 业主选项
            ownerOptions: [],

            // 数据总条数
            total: 0,
            // 表格数据数组
            tableData: [],
            // 多选数据暂存数组
            multipleSelection: [],
            // 新增/编辑 弹出框显示/隐藏
            formVisible: false,
            // 表单校验规则
            formRules: {
                ownerId: [
                    { required: true, message: '请选择业主', trigger: 'change' }
                ],
                feeDate: [
                    { required: true, message: '请选择费用月份', trigger: 'change' }
                ]
            },
            // 防止多次连续提交表单
            isSubmit: false,
            // 导出数据 弹出框显示/隐藏
            exportVisible: false,
            // 新增的data属性
            calculateVisible: false,
            batchCalculateVisible: false,
            isCalculating: false,
            calculateForm: {
                ownerId: undefined,
                feeDate: undefined
            },
            calculateRules: {
                ownerId: [
                    { required: true, message: '请选择业主', trigger: 'change' }
                ],
                feeDate: [
                    { required: true, message: '请选择费用月份', trigger: 'change' }
                ]
            },
            batchCalculateForm: {
                feeDate: undefined
            }
        }
    },
    created() {
        this.fetchData()
        this.fetchOwners()
    },
    methods: {
        // 获取业主列表
        fetchOwners() {
            Request.get("/owner/all").then(response => {
                if (response.code === '0') {
                    this.ownerOptions = response.data
                }
            })
        },

        // 业主选择变更
        handleOwnerChange(ownerId) {
            const owner = this.ownerOptions.find(o => o.id === ownerId)
            if (owner) {
                this.dialogForm.houseId = owner.houseId
                // 计算物业费
                this.calculateFee()
            }
        },

        // 计算物业费
        calculateFee() {
            if (this.dialogForm.ownerId && this.dialogForm.feeDate) {
                Request.get("/property-fees/calculate", {
                    params: {
                        ownerId: this.dialogForm.ownerId,
                        feeDate: this.dialogForm.feeDate
                    }
                }).then(response => {
                    if (response.code === '0') {
                        this.dialogForm.amount = response.data
                    }
                })
            }
        },

        // 多选操作
        handleSelectionChange(val) {
            this.multipleSelection = val.map(v => v.id)
        },

        // 新建数据
        handleCreate() {
            this.dialogForm = {
                ownerId: undefined,
                houseId: undefined,
                feeDate: undefined,
                amount: undefined
            }
            this.formVisible = true
        },

        // 编辑数据
        handleEdit(row) {
            this.dialogForm = JSON.parse(JSON.stringify(row))
            this.formVisible = true
        },

        // 缴费操作
        handlePay(row) {
            this.$confirm('确认完成缴费操作?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.post(`/property-fees/${row.id}/pay`).then(response => {
                    if (response.code === '0') {
                        this.$message({
                            type: 'success',
                            message: '缴费成功!'
                        })
                        this.fetchData()
                    }
                })
            })
        },

        // 删除数据
        handleDelete(row) {
            this.$confirm('此操作将删除该物业费记录, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete(`/property-fees/${row.id}`).then(response => {
                    if (response.code === '0') {
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        })
                        this.fetchData()
                    }else{
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

            this.$confirm('此操作将删除选中的物业费记录, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete('/property-fees/batch', {
                    data: this.multipleSelection
                }).then(res => {
                    if (res.code === '0') {
                        this.$message({
                            type: 'success',
                            message: '批量删除成功!'
                        })
                        this.$refs.multipleTable.clearSelection()
                        this.multipleSelection = []
                        this.fetchData()
                    }
                })
            })
        },

        // 获取数据列表
        fetchData({ page, limit } = {}) {
            if (page) this.listQuery.pageNum = page
            if (limit) this.listQuery.pageSize = limit
            
            this.listLoading = true
            Request.get("/property-fees/page", {
                params: this.listQuery
            }).then(response => {
                if (response.code === '0') {
                    const { total, records } = response.data
                    this.total = total
                    this.tableData = records
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
                ownerName: undefined,
                address: undefined,
                status: undefined,
                feeDate: undefined,
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
                    ? Request.put(`/property-fees`, this.dialogForm)
                    : Request.post("/property-fees", this.dialogForm)


                request.then(response => {
                    if (response.code === '0') {
                        this.$message({
                            message: this.dialogForm.id ? '更新成功' : '添加成功',
                            type: 'success'
                        })
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
                    header: ['ID', '订单编号', '业主姓名', '房屋地址', '费用月份', '费用金额', '缴费状态', '缴费时间', '创建时间'],
                    key: ['id', 'orderNo', 'owner.user.name', 'house.address', 'feeDate', 'amount', 'status', 'paymentTime', 'createdAt'],
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '物业费数据表',
                    bookType: type
                }
                excel.exportDataToExcel(params)
                this.exportVisible = false
            }
        },

        // 处理计算按钮命令
        handleCalculateCommand(command) {
            if (command === 'single') {
                this.calculateVisible = true
            } else if (command === 'batch') {
                this.batchCalculateVisible = true
            }
        },

        // 单个计算物业费
        handleCalculate(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) return false
                
                this.isCalculating = true
                Request.get("/property-fees/calculate", {
                    params: {
                        ownerId: this.calculateForm.ownerId,
                        feeDate: this.calculateForm.feeDate
                    }
                }).then(response => {
                    if (response.code === '0') {
                        this.$message({
                            type: 'success',
                            message: '计算成功'
                        })
                        this.calculateVisible = false
                        this.fetchData()
                    }else{
                        this.$message({
                            type: 'error',
                            message: response.msg
                        })
                    }

                }).finally(() => {
                    this.isCalculating = false
                })
            })
        },

        // 批量计算物业费
        handleBatchCalculate() {
            if (!this.batchCalculateForm.feeDate) {
                this.$message({
                    type: 'warning',
                    message: '请选择费用月份'
                })
                return
            }

            this.isCalculating = true
            Request.get("/property-fees/calculateBatch", {
                params: {
                    feeDate: this.batchCalculateForm.feeDate
                }
            }).then(response => {
                if (response.code === '0') {
                    this.$message({
                        type: 'success',
                        message: response.data
                    })
                    this.batchCalculateVisible = false
                    this.fetchData()
                }else{
                    this.$message({
                        type: 'error',
                        message: response.msg
                    })
                }

            }).finally(() => {
                this.isCalculating = false
            })
        },

        // 修改格式化费用月份方法
        formatFeeDate(feeDate) {
            if (!feeDate) return '';
            return moment(feeDate).format('YYYY-MM');
        },

        // 格式化日期时间
        formatDateTime(date) {
            if (!date) return '-'
            return moment(date).format('YYYY-MM-DD HH:mm:ss')
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
        flex-wrap: wrap;

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
                color: #409eff;
            }

            span {
                display: block;
                color: #606266;
            }
        }
    }
}
</style> 