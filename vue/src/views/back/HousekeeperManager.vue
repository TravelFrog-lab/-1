<template>
    <div class="wrapper">
        <!-- 页面标题 -->
        <div class="page-header">
            <div class="header-content">
                <h2>家政人员管理</h2>
                <div class="divider"></div>
            </div>
        </div>

        <el-card class="menu-card" shadow="never">
            <!-- 操作栏 -->
            <div class="control-btns">
                <div class="left-btns">
                    <el-popconfirm title="确认删除?" @confirm="handleBatchDelete">
                        <template #reference>
                            <el-button type="danger" size="medium" plain icon="el-icon-delete">批量删除</el-button>
                        </template>
                    </el-popconfirm>
                    <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="fetchData">刷新</el-button>
                </div>

                <div class="right-btns">
                    <el-button type="primary" size="medium" icon="el-icon-download" @click="exportVisible = true" plain>导出数据</el-button>
                    <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleAdd" plain>新增家政人员</el-button>
                </div>
            </div>

            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="姓名">
                    <el-input v-model="listQuery.name" placeholder="请输入姓名" size="medium" clearable />
                </el-form-item>
                <el-form-item label="手机号">
                    <el-input v-model="listQuery.phone" placeholder="请输入手机号" size="medium" clearable />
                </el-form-item>
                <el-form-item label="状态">
                    <el-select v-model="listQuery.status" placeholder="请选择状态" size="medium" clearable>
                        <el-option label="活跃" value="ACTIVE"></el-option>
                        <el-option label="禁用" value="DISABLED"></el-option>
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
                @selection-change="handleSelectionChange"
                :header-cell-style="{background:'#f8f9fa',color:'#606266'}"
                :cell-style="{padding:'12px 0'}"
                border>
                <el-table-column type="selection" width="50"></el-table-column>
                <el-table-column prop="id" label="ID" width="80"></el-table-column>
                <el-table-column prop="name" label="姓名" width="120"></el-table-column>
                <el-table-column prop="phone" label="手机号" width="120"></el-table-column>
                <el-table-column prop="idCard" label="身份证号" width="180"></el-table-column>
                <el-table-column prop="workYears" label="工作年限" width="100">
                    <template slot-scope="scope">
                        {{ scope.row.workYears }} 年
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
                            {{ scope.row.status === 'ACTIVE' ? '活跃' : '禁用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="个人描述">
                    <template slot-scope="scope">
                        <el-tooltip :content="scope.row.description" placement="top" effect="light">
                            <div class="description-cell">{{ scope.row.description }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="180">
                    <template slot-scope="scope">
                        {{ formatDateTime(scope.row.createdAt) }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="280" fixed="right">
                    <template slot-scope="scope">
                        <el-button
                            plain size="small" type="primary" icon="el-icon-edit"
                            @click="handleEdit(scope.row)">编辑</el-button>
                        <el-button
                            plain size="small" type="success" icon="el-icon-s-tools"
                            @click="handleServices(scope.row)">服务项目</el-button>
                        <el-button
                            plain size="small" type="danger" icon="el-icon-delete"
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

            <!-- 新增/编辑弹窗 -->
            <el-dialog 
                :title="dialogForm.id ? '编辑家政人员' : '新增家政人员'" 
                :visible.sync="formVisible" 
                width="500px"
                :close-on-click-modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="关联用户" prop="userId">
                        <el-select 
                            v-model="dialogForm.userId" 
                            filterable 
                            placeholder="请选择关联用户" 
                            style="width: 100%"
                            @change="handleUserChange">
                            <el-option
                                v-for="item in userOptions"
                                :key="item.id"
                                :label="item.username + ' (' + item.name + ')'"
                                :value="item.id">
                                <span>{{ item.username }} ({{ item.name }})</span>
                                <span v-if="item.role === 'KEEPER' && item.id !== dialogForm.id" style="color: #E6A23C; float: right;">已关联</span>
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="姓名" prop="name">
                        <el-input v-model="dialogForm.name" placeholder="请输入姓名"></el-input>
                    </el-form-item>
                    <el-form-item label="手机号" prop="phone">
                        <el-input v-model="dialogForm.phone" placeholder="请输入手机号"></el-input>
                    </el-form-item>
                    <el-form-item label="身份证号" prop="idCard">
                        <el-input v-model="dialogForm.idCard" placeholder="请输入身份证号"></el-input>
                    </el-form-item>
                    <el-form-item label="工作年限" prop="workYears">
                        <el-input-number v-model="dialogForm.workYears" :min="0" :max="50" style="width: 100%" placeholder="请输入工作年限"></el-input-number>
                    </el-form-item>
                    <el-form-item label="状态" prop="status">
                        <el-select v-model="dialogForm.status" placeholder="请选择状态" style="width: 100%">
                            <el-option label="活跃" value="ACTIVE"></el-option>
                            <el-option label="禁用" value="DISABLED"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="个人描述" prop="description">
                        <el-input type="textarea" v-model="dialogForm.description" :rows="4" placeholder="请输入个人描述"></el-input>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="formVisible = false">取 消</el-button>
                    <el-button type="primary" :loading="isSubmit" @click="submitForm('dialogForm')">确 定</el-button>
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

            <!-- 添加服务项目对话框 -->
            <el-dialog 
                title="服务项目管理" 
                :visible.sync="serviceVisible" 
                width="700px"
                :close-on-click-modal="false">
                <div v-loading="serviceLoading">
                    <div class="service-header">
                        <div class="service-info">
                            <h3>{{ currentHousekeeper.name }} 的服务项目</h3>
                            <p>可以为家政人员分配服务项目并设置价格</p>
                        </div>
                        <el-button type="primary" size="small" @click="handleAddService">
                            <i class="el-icon-plus"></i> 添加服务项目
                        </el-button>
                    </div>
                    
                    <!-- 服务项目列表 -->
                    <el-table
                        :data="serviceList"
                        border
                        style="width: 100%"
                        size="medium">
                        <el-table-column prop="service.name" label="服务名称" width="150"></el-table-column>
                        <el-table-column prop="service.category" label="服务类别" width="120"></el-table-column>
                        <el-table-column prop="service.basePrice" label="基础价格" width="120">
                            <template slot-scope="scope">
                                {{ scope.row.service.basePrice }} 元/{{ scope.row.service.unit }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="price" label="实际价格" width="120">
                            <template slot-scope="scope">
                                <el-input-number 
                                    v-model="scope.row.price" 
                                    :precision="2" 
                                    :step="5" 
                                    :min="0"
                                    size="small"
                                    @change="handlePriceChange(scope.row)">
                                </el-input-number>
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" width="100">
                            <template slot-scope="scope">
                                <el-button
                                    type="danger"
                                    size="mini"
                                    icon="el-icon-delete"
                                    @click="handleRemoveService(scope.row)">
                                    删除
                                </el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                    
                    <div v-if="serviceList.length === 0" class="empty-service">
                        <i class="el-icon-s-tools"></i>
                        <p>暂无服务项目，请点击添加</p>
                    </div>
                </div>
                
                <!-- 添加服务项目对话框 -->
                <el-dialog
                    title="添加服务项目"
                    :visible.sync="addServiceVisible"
                    width="500px"
                    append-to-body
                    :close-on-click-modal="false">
                    <el-form :model="serviceForm" :rules="serviceRules" ref="serviceForm" label-width="100px">
                        <el-form-item label="服务项目" prop="serviceId">
                            <el-select 
                                v-model="serviceForm.serviceId" 
                                filterable 
                                placeholder="请选择服务项目" 
                                style="width: 100%"
                                @change="handleServiceChange">
                                <el-option
                                    v-for="item in availableServices"
                                    :key="item.id"
                                    :label="item.name"
                                    :value="item.id">
                                    <span>{{ item.name }} ({{ item.category }})</span>
                                    <span style="float: right; color: #8492a6; font-size: 13px">
                                        {{ item.basePrice }}元/{{ item.unit }}
                                    </span>
                                </el-option>
                            </el-select>
                        </el-form-item>
                        <el-form-item label="服务价格" prop="price">
                            <el-input-number 
                                v-model="serviceForm.price" 
                                :precision="2" 
                                :step="10" 
                                :min="0"
                                style="width: 100%"
                                placeholder="请输入服务价格">
                            </el-input-number>
                        </el-form-item>
                    </el-form>
                    <div slot="footer" class="dialog-footer">
                        <el-button @click="addServiceVisible = false">取 消</el-button>
                        <el-button type="primary" :loading="isServiceSubmit" @click="submitServiceForm('serviceForm')">确 定</el-button>
                    </div>
                </el-dialog>
            </el-dialog>
        </el-card>
    </div>
</template>

<script>
import Request from '@/utils/request'
import moment from 'moment'
import Pagination from '@/components/Pagination'
import * as excel from '@/utils/excel'

export default {
    name: 'HousekeeperManager',
    components: { Pagination },
    data() {
        return {
            // 列表查询参数
            listQuery: {
                name: undefined,
                phone: undefined,
                status: undefined,
                pageNum: 1,
                pageSize: 10
            },
            // 表格数据
            tableData: [],
            // 加载状态
            listLoading: false,
            // 提交状态
            isSubmit: false,
            // 总记录数
            total: 0,
            // 多选选中的记录ID
            multipleSelection: [],
            // 表单弹窗可见性
            formVisible: false,
            // 导出弹窗可见性
            exportVisible: false,
            // 表单数据
            dialogForm: {
                id: undefined,
                userId: undefined,
                name: '',
                phone: '',
                idCard: '',
                workYears: 0,
                status: 'ACTIVE',
                description: ''
            },
            // 表单校验规则
            formRules: {
                userId: [
                    { required: true, message: '请选择关联用户', trigger: 'change' }
                ],
                name: [
                    { required: true, message: '请输入姓名', trigger: 'blur' },
                    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
                ],
                phone: [
                    { required: true, message: '请输入手机号', trigger: 'blur' },
                    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
                ],
                idCard: [
                    { required: true, message: '请输入身份证号', trigger: 'blur' },
                    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号码', trigger: 'blur' }
                ],
                workYears: [
                    { required: true, message: '请输入工作年限', trigger: 'blur' }
                ],
                status: [
                    { required: true, message: '请选择状态', trigger: 'change' }
                ]
            },
            // 用户选项
            userOptions: [],
            // 已关联的用户ID列表
            linkedUserIds: [],
            // 服务项目相关
            serviceVisible: false,
            serviceLoading: false,
            currentHousekeeper: {},
            serviceList: [],
            // 添加服务项目相关
            addServiceVisible: false,
            isServiceSubmit: false,
            serviceForm: {
                serviceId: undefined,
                price: 0
            },
            serviceRules: {
                serviceId: [
                    { required: true, message: '请选择服务项目', trigger: 'change' }
                ],
                price: [
                    { required: true, message: '请输入服务价格', trigger: 'blur' }
                ]
            },
            availableServices: [],
            allServices: []
        }
    },
    created() {
        this.fetchData()
    },
    methods: {
        // 格式化日期时间
        formatDateTime(date) {
            if (!date) return '-'
            return moment(date).format('YYYY-MM-DD HH:mm:ss')
        },
        
        // 获取数据
        fetchData() {
            this.listLoading = true
            Request.get('/housekeeper/page', {
                params: {
                    pageNum: this.listQuery.pageNum,
                    pageSize: this.listQuery.pageSize,
                    name: this.listQuery.name,
                    phone: this.listQuery.phone,
                    status: this.listQuery.status
                }
            }).then(response => {
                if (response.code === '0') {
                    this.tableData = response.data.records
                    this.total = response.data.total
                    
                    // 收集已关联的用户ID
                    this.linkedUserIds = this.tableData.map(item => item.userId)
                } else {
                    this.$message.error(response.msg || '获取数据失败')
                }
            }).finally(() => {
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
            this.listQuery = {
                name: undefined,
                phone: undefined,
                status: undefined,
                pageNum: 1,
                pageSize: 10
            }
            this.fetchData()
        },
        
        // 多选变化
        handleSelectionChange(selection) {
            this.multipleSelection = selection.map(item => item.id)
        },
        
        // 新增
        handleAdd() {
            this.dialogForm = {
                id: undefined,
                userId: undefined,
                name: '',
                phone: '',
                idCard: '',
                workYears: 0,
                status: 'ACTIVE',
                description: ''
            }
            this.loadUserOptions()
            this.formVisible = true
        },
        
        // 编辑
        handleEdit(row) {
            this.dialogForm = JSON.parse(JSON.stringify(row))
            this.loadUserOptions(row.userId)
            this.formVisible = true
        },
        
        // 加载用户选项
        loadUserOptions(currentUserId) {
            Request.get('/user/role-enum/KEEPER').then(response => {
                if (response.code === '0') {
                    // 过滤掉已经关联的用户，但保留当前编辑的用户
                    this.userOptions = response.data.filter(user => {
                        return user.id === currentUserId || 
                               !this.linkedUserIds.includes(user.id) || 
                               user.role === 'KEEPER'
                    })
                } else {
                    this.$message.error(response.msg || '获取用户列表失败')
                }
            })
        },
        
        // 用户选择变化
        handleUserChange(userId) {
            // 查找选中的用户
            const selectedUser = this.userOptions.find(user => user.id === userId)
            if (selectedUser) {
                // 如果用户已经是家政人员角色，给出提示
                if (selectedUser.role === 'KEEPER' && selectedUser.id !== this.dialogForm.userId) {
                    this.$confirm('该用户已关联其他家政人员，确定要更换关联吗?', '提示', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                    }).then(() => {
                        // 用户确认，继续操作
                    }).catch(() => {
                        // 用户取消，恢复原值
                        this.dialogForm.userId = undefined
                    })
                }
                
                // 自动填充姓名和手机号
                this.dialogForm.name = selectedUser.name
                this.dialogForm.phone = selectedUser.phone
            }
        },
        
        // 删除
        handleDelete(row) {
            this.$confirm('确认删除该家政人员?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete(`/housekeeper/${row.id}`).then(response => {
                    if (response.code === '0') {
                        this.$message.success('删除成功')
                        this.fetchData()
                    } else {
                        this.$message.error(response.msg || '删除失败')
                    }
                })
            }).catch(() => {})
        },
        
        // 批量删除
        handleBatchDelete() {
            if (this.multipleSelection.length === 0) {
                this.$message.warning('请选择要删除的记录')
                return
            }
            
            this.$confirm(`确认删除选中的 ${this.multipleSelection.length} 条记录?`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete('/housekeeper/batch', {
                    data: this.multipleSelection
                }).then(response => {
                    if (response.code === '0') {
                        this.$message.success('批量删除成功')
                        this.fetchData()
                    } else {
                        this.$message.error(response.msg || '批量删除失败')
                    }
                })
            }).catch(() => {})
        },
        
        // 提交表单
        submitForm(formName) {
            this.$refs[formName].validate((valid) => {
                if (valid) {
                    this.isSubmit = true
                    const method = this.dialogForm.id ? 'put' : 'post'
                    const url = '/housekeeper'
                    
                    Request[method](url, this.dialogForm).then(response => {
                        if (response.code === '0') {
                            this.$message.success(this.dialogForm.id ? '更新成功' : '新增成功')
                            this.formVisible = false
                            this.fetchData()
                        } else {
                            this.$message.error(response.msg || (this.dialogForm.id ? '更新失败' : '新增失败'))
                        }
                    }).finally(() => {
                        this.isSubmit = false
                    })
                } else {
                    return false
                }
            })
        },
        
        // 导出数据
        exportTable(type) {
            if (this.tableData.length) {
                const params = {
                    header: ['ID', '姓名', '手机号', '身份证号', '工作年限', '状态', '个人描述', '创建时间'],
                    key: ['id', 'name', 'phone', 'idCard', 'workYears', 'status', 'description', 'createdAt'],
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '家政人员信息表',
                    bookType: type
                }
                excel.exportDataToExcel(params)
                this.exportVisible = false
            }
        },
        
        // 打开服务项目管理对话框
        handleServices(row) {
            this.currentHousekeeper = row;
            this.serviceVisible = true;
            this.loadServices();
            this.loadAllServices();
        },
        
        // 加载家政人员的服务项目
        loadServices() {
            this.serviceLoading = true;
            Request.get(`/housekeeper-service/housekeeper/${this.currentHousekeeper.id}`).then(response => {
                if (response.code === '0') {
                    this.serviceList = response.data;
                } else {
                    this.$message.error(response.msg || '获取服务项目失败');
                }
            }).finally(() => {
                this.serviceLoading = false;
            });
        },
        
        // 加载所有服务项目
        loadAllServices() {
            Request.get('/housekeeping-service/list').then(response => {
                if (response.code === '0') {
                    this.allServices = response.data;
                    this.updateAvailableServices();
                } else {
                    this.$message.error(response.msg || '获取所有服务项目失败');
                }
            });
        },
        
        // 更新可用的服务项目（排除已分配的）
        updateAvailableServices() {
            const assignedServiceIds = this.serviceList.map(item => item.serviceId);
            this.availableServices = this.allServices.filter(service => 
                !assignedServiceIds.includes(service.id)
            );
        },
        
        // 打开添加服务项目对话框
        handleAddService() {
            this.serviceForm = {
                serviceId: undefined,
                price: 0
            };
            this.updateAvailableServices();
            this.addServiceVisible = true;
        },
        
        // 服务项目选择变化
        handleServiceChange(serviceId) {
            const service = this.allServices.find(item => item.id === serviceId);
            if (service) {
                this.serviceForm.price = service.basePrice;
            }
        },
        
        // 提交服务项目表单
        submitServiceForm(formName) {
            this.$refs[formName].validate((valid) => {
                if (valid) {
                    this.isServiceSubmit = true;
                    
                    const housekeeperService = {
                        housekeeperId: this.currentHousekeeper.id,
                        serviceId: this.serviceForm.serviceId,
                        price: this.serviceForm.price
                    };
                    
                    Request.post('/housekeeper-service', housekeeperService).then(response => {
                        if (response.code === '0') {
                            this.$message.success('添加服务项目成功');
                            this.addServiceVisible = false;
                            this.loadServices();
                        } else {
                            this.$message.error(response.msg || '添加服务项目失败');
                        }
                    }).finally(() => {
                        this.isServiceSubmit = false;
                    });
                } else {
                    return false;
                }
            });
        },
        
        // 价格变更
        handlePriceChange(row) {
            Request.put('/housekeeper-service', row).then(response => {
                if (response.code === '0') {
                    this.$message.success('更新价格成功');
                } else {
                    this.$message.error(response.msg || '更新价格失败');
                    // 恢复原价格
                    this.loadServices();
                }
            });
        },
        
        // 移除服务项目
        handleRemoveService(row) {
            this.$confirm('确认移除该服务项目?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete(`/housekeeper-service/${row.id}`).then(response => {
                    if (response.code === '0') {
                        this.$message.success('移除服务项目成功');
                        this.loadServices();
                    } else {
                        this.$message.error(response.msg || '移除服务项目失败');
                    }
                });
            }).catch(() => {});
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

        .el-form-item {
            margin-bottom: 0;
            margin-right: 16px;

            &:last-child {
                margin-right: 0;
            }
        }
    }

    .description-cell {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        max-width: 200px;
    }

    .export-data {
        display: flex;
        justify-content: space-around;
        padding: 20px;

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
                margin-bottom: 10px;
                color: #409EFF;
            }

            span {
                display: block;
                color: #606266;
            }
        }
    }

    .service-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        
        .service-info {
            h3 {
                margin: 0 0 5px 0;
                font-size: 18px;
                color: #303133;
            }
            
            p {
                margin: 0;
                font-size: 14px;
                color: #909399;
            }
        }
    }

    .empty-service {
        text-align: center;
        padding: 40px 0;
        color: #909399;
        
        i {
            font-size: 48px;
            margin-bottom: 10px;
        }
        
        p {
            font-size: 16px;
            margin: 0;
        }
    }
}
</style> 