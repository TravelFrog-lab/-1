<template>
    <div class="wrapper">
        <!-- 页面标题 -->
        <div class="page-header">
            <div class="header-content">
                <h2>{{ isHousekeeper ? '我的服务订单' : '家政服务订单管理' }}</h2>
                <div class="divider"></div>
                <p class="subtitle">{{ isHousekeeper ? 'My Service Orders' : 'Housekeeping Order Management' }}</p>
            </div>
        </div>

        <el-card class="menu-card" shadow="never">
            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="订单编号">
                    <el-input v-model="listQuery.orderNo" placeholder="请输入订单编号" size="medium" clearable />
                </el-form-item>
                <el-form-item label="订单状态">
                    <el-select v-model="listQuery.status" placeholder="请选择状态" size="medium" clearable>
                        <el-option label="待确认" value="PENDING"></el-option>
                        <el-option label="已确认" value="CONFIRMED"></el-option>
                        <el-option label="进行中" value="IN_PROGRESS"></el-option>
                        <el-option label="已完成" value="COMPLETED"></el-option>
                        <el-option label="已取消" value="CANCELLED"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" size="medium" plain icon="el-icon-search" @click="onSubmit">查询</el-button>
                    <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="onReset">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 表格栏 -->
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
                <el-table-column prop="orderNo" label="订单编号" width="180"></el-table-column>
                <el-table-column label="业主信息" width="150">
                    <template slot-scope="scope">
                        <div v-if="scope.row.owner">
                            {{ scope.row.owner.user.name }}
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="家政人员" width="150">
                    <template slot-scope="scope">
                        <div v-if="scope.row.housekeeper">
                            {{ scope.row.housekeeper.name }}
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="服务项目" width="150">
                    <template slot-scope="scope">
                        <div v-if="scope.row.service">
                            {{ scope.row.service.name }}
                        </div>
                    </template>
                </el-table-column>
          
                <el-table-column prop="appointmentTime" label="预约时间" width="180">
                    <template slot-scope="scope">
                        {{ formatDate(scope.row.appointmentTime) }}
                    </template>
                </el-table-column>
                <el-table-column prop="amount" label="订单金额" width="120">
                    <template slot-scope="scope">
                        {{ scope.row.amount }} 元
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="订单状态" width="100">
                    <template slot-scope="scope">
                        <el-tag :type="getStatusType(scope.row.status)">
                            {{ getStatusText(scope.row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="paymentStatus" label="支付状态" width="100">
                    <template slot-scope="scope">
                        <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
                            {{ getPaymentStatusText(scope.row.paymentStatus) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="180">
                    <template slot-scope="scope">
                        {{ formatDate(scope.row.createdAt) }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="280" fixed="right">
                    <template slot-scope="scope">
                        <el-button
                            plain size="small" type="primary" icon="el-icon-view"
                            @click="handleView(scope.row)">查看</el-button>
                        <el-button
                            v-if="!isHousekeeper && scope.row.status === 'PENDING'"
                            plain size="small" type="success" icon="el-icon-check"
                            @click="handleConfirm(scope.row)">确认</el-button>
                        <el-button
                            v-if="isHousekeeper && scope.row.status === 'CONFIRMED' && scope.row.paymentStatus === 'PAID'"
                            plain size="small" type="warning" icon="el-icon-video-play"
                            @click="handleStart(scope.row)">开始服务</el-button>
                        <el-button
                            v-if="isHousekeeper && scope.row.status === 'IN_PROGRESS'"
                            plain size="small" type="success" icon="el-icon-finished"
                            @click="handleComplete(scope.row)">完成服务</el-button>
                        <el-button
                            v-if="!isHousekeeper && scope.row.status !== 'COMPLETED' && scope.row.status !== 'CANCELLED'"
                            plain size="small" type="danger" icon="el-icon-close"
                            @click="handleCancel(scope.row)">取消</el-button>
                        <el-button
                            v-if="!isHousekeeper && scope.row.paymentStatus === 'UNPAID' && scope.row.status !== 'CANCELLED'"
                            plain size="small" type="primary" icon="el-icon-money"
                            @click="handlePay(scope.row)">支付</el-button>
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

            <!-- 订单详情对话框 -->
            <el-dialog 
                title="订单详情" 
                :visible.sync="detailVisible" 
                width="700px"
                :close-on-click-modal="false">
                <div v-if="currentOrder" class="order-detail">
                    <div class="detail-header">
                        <div class="order-no">订单编号：{{ currentOrder.orderNo }}</div>
                        <div class="order-status" style="width: auto;display: flex;justify-content:center;">
                            <el-tag :type="getStatusType(currentOrder.status)">
                                {{ getStatusText(currentOrder.status) }}
                            </el-tag>
                            <el-tag :type="getPaymentStatusType(currentOrder.paymentStatus)" style="margin-left: 10px;">
                                {{ getPaymentStatusText(currentOrder.paymentStatus) }}
                            </el-tag>
                        </div>
                    </div>
                    
                    <el-divider></el-divider>
                    
                    <div class="detail-section">
                        <h4>基本信息</h4>
                        <el-row :gutter="20">
                            <el-col :span="12">
                                <div class="detail-item">
                                    <span class="label">创建时间：</span>
                                    <span class="value">{{ formatDate(currentOrder.createdAt) }}</span>
                                </div>
                            </el-col>
                            <el-col :span="12">
                                <div class="detail-item">
                                    <span class="label">更新时间：</span>
                                    <span class="value">{{ formatDate(currentOrder.updatedAt) }}</span>
                                </div>
                            </el-col>
                        </el-row>
                        <el-row :gutter="20">
                            <el-col :span="12">
                                <div class="detail-item">
                                    <span class="label">预约时间：</span>
                                    <span class="value">{{ formatDate(currentOrder.appointmentTime) }}</span>
                                </div>
                            </el-col>
                            <el-col :span="12">
                                <div class="detail-item">
                                    <span class="label">服务时长：</span>
                                    <span class="value">{{ currentOrder.serviceDuration }} 分钟</span>
                                </div>
                            </el-col>
                        </el-row>
                        <el-row :gutter="20">
                            <el-col :span="12">
                                <div class="detail-item">
                                    <span class="label">订单金额：</span>
                                    <span class="value">{{ currentOrder.amount }} 元</span>
                                </div>
                            </el-col>
                        </el-row>
                    </div>
                    
                    <el-divider></el-divider>
                    
                    <div class="detail-section">
                        <h4>业主信息</h4>
                        <el-row :gutter="20" v-if="currentOrder.owner">
                            <el-col :span="12">
                                <div class="detail-item">
                                    <span class="label">姓名：</span>
                                    <span class="value">{{ currentOrder.owner.user.name }}</span>
                                </div>
                            </el-col>
                            <el-col :span="12">
                                <div class="detail-item">
                                    <span class="label">手机号：</span>
                                    <span class="value">{{ currentOrder.owner.user.phone }}</span>
                                </div>
                            </el-col>
                        </el-row>
                    </div>
                    
                    <el-divider></el-divider>
                    
                    <div class="detail-section">
                        <h4>家政人员信息</h4>
                        <el-row :gutter="20" v-if="currentOrder.housekeeper">
                            <el-col :span="12">
                                <div class="detail-item">
                                    <span class="label">姓名：</span>
                                    <span class="value">{{ currentOrder.housekeeper.name }}</span>
                                </div>
                            </el-col>
                            <el-col :span="12">
                                <div class="detail-item">
                                    <span class="label">手机号：</span>
                                    <span class="value">{{ currentOrder.housekeeper.phone }}</span>
                                </div>
                            </el-col>
                        </el-row>
                    </div>
                    
                    <el-divider></el-divider>
                    
                    <div class="detail-section">
                        <h4>服务信息</h4>
                        <el-row :gutter="20" v-if="currentOrder.service">
                            <el-col :span="12">
                                <div class="detail-item">
                                    <span class="label">服务项目：</span>
                                    <span class="value">{{ currentOrder.service.name }}</span>
                                </div>
                            </el-col>
                            <el-col :span="12">
                                <div class="detail-item">
                                    <span class="label">服务类别：</span>
                                    <span class="value">{{ currentOrder.service.category }}</span>
                                </div>
                            </el-col>
                        </el-row>
         
                    </div>
                    
                    <el-divider></el-divider>
                    
                    <div class="detail-section">
                        <h4>备注信息</h4>
                        <div class="detail-item">
                            <span class="value">{{ currentOrder.remark || '无' }}</span>
                        </div>
                    </div>
                </div>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="detailVisible = false">关 闭</el-button>
                    <el-button
                        v-if="currentOrder && currentOrder.status === 'PENDING'"
                        type="success" @click="handleConfirm(currentOrder)">确认订单</el-button>
                    <el-button
                        v-if="currentOrder && currentOrder.status === 'CONFIRMED' && currentOrder.paymentStatus === 'PAID'"
                        type="warning" @click="handleStart(currentOrder)">开始服务</el-button>
                    <el-button
                        v-if="currentOrder && currentOrder.status === 'IN_PROGRESS'"
                        type="success" @click="handleComplete(currentOrder)">完成服务</el-button>
                    <el-button
                        v-if="currentOrder && currentOrder.status !== 'COMPLETED' && currentOrder.status !== 'CANCELLED'"
                        type="danger" @click="handleCancel(currentOrder)">取消订单</el-button>
                    <el-button
                        v-if="currentOrder && currentOrder.paymentStatus === 'UNPAID' && currentOrder.status !== 'CANCELLED'"
                        type="primary" @click="handlePay(currentOrder)">支付订单</el-button>
                </div>
            </el-dialog>
        </el-card>
    </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Request from '@/utils/request'
import moment from 'moment'
import Pagination from '@/components/Pagination'

export default {
    name: 'HousekeepingOrderManager',
    components: { Pagination },
    data() {
        return {
            // 列表查询参数
            listQuery: {
                orderNo: '',
                status: '',
                pageNum: 1,
                pageSize: 10,
                housekeeperId: null
            },
            // 表格数据
            tableData: [],
            // 加载状态
            listLoading: false,
            // 总记录数
            total: 0,
            // 详情对话框可见性
            detailVisible: false,
            // 当前查看的订单
            currentOrder: null
        }
    },
    computed: {
        // 从Vuex获取当前用户信息
        ...mapGetters(['currentUser']),
        
        // 判断当前用户是否是服务人员
        isHousekeeper() {
            return this.currentUser && this.currentUser.role === 'KEEPER'
        }
    },
    created() {
        // 如果是服务人员，设置housekeeperId为当前用户ID
        if (this.isHousekeeper) {
            this.listQuery.housekeeperId = this.currentUser.id
        }
        this.fetchData()
    },
    methods: {
        // 格式化日期
        formatDate(date) {
            if (!date) return ''
            return moment(date).format('YYYY-MM-DD HH:mm:ss')
        },
        
        // 获取订单状态类型
        getStatusType(status) {
            switch (status) {
                case 'PENDING': return 'info'
                case 'CONFIRMED': return 'primary'
                case 'IN_PROGRESS': return 'warning'
                case 'COMPLETED': return 'success'
                case 'CANCELLED': return 'danger'
                default: return 'info'
            }
        },
        
        // 获取订单状态文本
        getStatusText(status) {
            switch (status) {
                case 'PENDING': return '待确认'
                case 'CONFIRMED': return '已确认'
                case 'IN_PROGRESS': return '进行中'
                case 'COMPLETED': return '已完成'
                case 'CANCELLED': return '已取消'
                default: return '未知'
            }
        },
        
        // 获取支付状态类型
        getPaymentStatusType(status) {
            switch (status) {
                case 'UNPAID': return 'danger'
                case 'PAID': return 'success'
                case 'REFUNDED': return 'info'
                default: return 'info'
            }
        },
        
        // 获取支付状态文本
        getPaymentStatusText(status) {
            switch (status) {
                case 'UNPAID': return '未支付'
                case 'PAID': return '已支付'
                case 'REFUNDED': return '已退款'
                default: return '未知'
            }
        },
        
        // 获取数据
        fetchData() {
            this.listLoading = true
            
            // 构建查询参数
            const params = {
                pageNum: this.listQuery.pageNum,
                pageSize: this.listQuery.pageSize,
                orderNo: this.listQuery.orderNo || undefined,
                status: this.listQuery.status || undefined
            }
            
            // 如果是服务人员，添加housekeeperId参数
            if (this.isHousekeeper) {
                params.housekeeperUserId = this.currentUser.id
            }
            
            Request.get('/housekeeping-order/page', { params }).then(response => {
                if (response.code === '0') {
                    this.tableData = response.data.records
                    this.total = response.data.total
                } else {
                    this.$message.error(response.msg || '获取数据失败')
                }
                this.listLoading = false
            }).catch(() => {
                this.listLoading = false
            })
        },
        
        // 查询
        onSubmit() {
            this.listQuery.pageNum = 1
            this.fetchData()
        },
        
        // 重置
        onReset() {
            const housekeeperId = this.isHousekeeper ? this.currentUser.id : null
            this.listQuery = {
                pageNum: 1,
                pageSize: 10,
                orderNo: '',
                status: '',
                housekeeperId: housekeeperId
            }
            this.fetchData()
        },
        
        // 查看订单详情
        handleView(row) {
            this.currentOrder = row
            this.detailVisible = true
        },
        
        // 确认订单
        handleConfirm(row) {
            this.$confirm('确认接受该订单?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.put(`/housekeeping-order/${row.id}/confirm`).then(response => {
                    if (response.code === '0') {
                        this.$message.success('订单已确认')
                        this.fetchData()
                        if (this.detailVisible) {
                            this.detailVisible = false
                        }
                    } else {
                        this.$message.error(response.msg || '操作失败')
                    }
                })
            }).catch(() => {})
        },
        
        // 开始服务
        handleStart(row) {
            this.$confirm('确认开始服务?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.put(`/housekeeping-order/${row.id}/start`).then(response => {
                    if (response.code === '0') {
                        this.$message.success('服务已开始')
                        this.fetchData()
                        if (this.detailVisible) {
                            this.detailVisible = false
                        }
                    } else {
                        this.$message.error(response.msg || '操作失败')
                    }
                })
            }).catch(() => {})
        },
        
        // 完成服务
        handleComplete(row) {
            this.$confirm('确认完成服务?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.put(`/housekeeping-order/${row.id}/complete`).then(response => {
                    if (response.code === '0') {
                        this.$message.success('服务已完成')
                        this.fetchData()
                        if (this.detailVisible) {
                            this.detailVisible = false
                        }
                    } else {
                        this.$message.error(response.msg || '操作失败')
                    }
                })
            }).catch(() => {})
        },
        
        // 取消订单
        handleCancel(row) {
            this.$confirm('确认取消该订单?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.put(`/housekeeping-order/${row.id}/cancel`).then(response => {
                    if (response.code === '0') {
                        this.$message.success('订单已取消')
                        this.fetchData()
                        if (this.detailVisible) {
                            this.detailVisible = false
                        }
                    } else {
                        this.$message.error(response.msg || '操作失败')
                    }
                })
            }).catch(() => {})
        },
        
        // 支付订单
        handlePay(row) {
            this.$confirm(`确认支付该订单? 金额: ${row.amount} 元`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.put(`/housekeeping-order/${row.id}/pay`).then(response => {
                    if (response.code === '0') {
                        this.$message.success('订单已支付')
                        this.fetchData()
                        if (this.detailVisible) {
                            this.detailVisible = false
                        }
                    } else {
                        this.$message.error(response.msg || '操作失败')
                    }
                })
            }).catch(() => {})
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

    .order-detail {
        .detail-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            
            .order-no {
                font-size: 18px;
                font-weight: bold;
                color: #303133;
            }
        }
        
        .detail-section {
            margin-bottom: 20px;
            
            h4 {
                margin: 0 0 15px 0;
                font-size: 16px;
                color: #303133;
            }
            
            .detail-item {
                margin-bottom: 10px;
                
                .label {
                    color: #909399;
                    margin-right: 5px;
                }
                
                .value {
                    color: #303133;
                }
            }
        }
    }
}
</style> 