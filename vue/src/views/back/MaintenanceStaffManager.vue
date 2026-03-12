<template>
    <div class="wrapper">
        <!-- 页面标题区 -->
        <div class="page-header">
            <div class="header-content">
                <h2>后勤人员管理</h2>
                <div class="divider"></div>
                <p class="subtitle">Maintenance Staff Management</p>
            </div>
        </div>

        <el-card class="menu-card" shadow="never">
            <!-- 操作按钮区 -->
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
                    <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增后勤人员</el-button>
                </div>
            </div>

            <!-- 查询表单区 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="职位">
                    <el-select v-model="listQuery.position" placeholder="请选择职位" size="medium">
                        <el-option label="后勤" value="MAINTENANCE" />
                        <el-option label="安保" value="SECURITY" />
                    </el-select>
                </el-form-item>
                <el-form-item label="名称">
                    <el-input v-model="listQuery.username" placeholder="请输入用户名" size="medium" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" size="medium" plain icon="el-icon-search" @click="onSubmit">查询</el-button>
                    <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="onReset">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 表格区 -->
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
                <el-table-column prop="user.username" label="用户名"></el-table-column>
                <el-table-column prop="user.name" label="姓名"></el-table-column>
                <el-table-column prop="user.phone" label="手机号"></el-table-column>
                <el-table-column prop="position" label="职位">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.position === 'MAINTENANCE' ? 'primary' : 'warning'">
                            {{ scope.row.position === 'MAINTENANCE' ? '后勤' : '安保' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="描述"></el-table-column>
                <el-table-column prop="user.sex" label="性别">
                    <template slot-scope="scope">
                        {{ scope.row.user?.sex === 'MALE' ? '男' : scope.row.user?.sex === 'FEMALE' ? '女' : '其他' }}
                    </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="180">
                    <template slot-scope="scope">
                        {{ scope.row.createdAt ? moment(scope.row.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-' }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                    <template slot-scope="scope">
                        <el-button plain size="small" type="primary" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
                        <el-button plain size="small" type="danger" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页区 -->
            <Pagination
                v-show="total>0"
                :total="total"
                :page.sync="listQuery.pageNum"
                :limit.sync="listQuery.pageSize"
                @pagination="fetchData"
            />

            <!-- 新增/编辑弹窗 -->
            <el-dialog 
                :title="dialogForm.id ? '编辑后勤人员' : '新增后勤人员'" 
                :visible.sync="formVisible" 
                width="500px"
                :close-on-click-modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="120px">
                    <!-- 是否替换用户选项（仅编辑时显示） -->
                    <el-form-item label="是否替换用户" v-if="dialogForm.id">
                        <el-switch v-model="dialogForm.replaceUser"></el-switch>
                    </el-form-item>

                    <!-- 用户选择方式（新增或编辑替换时显示） -->
                    <el-form-item label="用户选择方式" prop="userSelectType" v-if="!dialogForm.id || (dialogForm.id && dialogForm.replaceUser)">
                        <el-radio-group v-model="dialogForm.userSelectType">
                            <el-radio :label="'existing'">选择已有用户</el-radio>
                            <el-radio :label="'new'">创建新用户</el-radio>
                        </el-radio-group>
                    </el-form-item>

                    <!-- 显示当前关联用户（仅编辑且不替换时显示） -->
                    <el-form-item label="关联用户" v-if="dialogForm.id && !dialogForm.replaceUser">
                        <div class="current-user-info">
                            <span><b>用户名:</b> {{ dialogForm.user?.username }}</span>
                            <span><b>姓名:</b> {{ dialogForm.user?.name }}</span>
                            <span><b>手机号:</b> {{ dialogForm.user?.phone }}</span>
                        </div>
                    </el-form-item>

                    <!-- 选择已有用户（新增或编辑替换且选择已有用户时显示） -->
                    <el-form-item label="选择用户" prop="userId" 
                        v-if="(dialogForm.userSelectType === 'existing' && !dialogForm.id) || 
                              (dialogForm.id && dialogForm.replaceUser && dialogForm.userSelectType === 'existing')">
                        <el-select 
                            v-model="dialogForm.userId" 
                            filterable 
                            remote 
                            reserve-keyword 
                            placeholder="请输入关键词搜索用户" 
                            :remote-method="remoteSearchUsers"
                            :loading="userSearchLoading">
                            <el-option
                                v-for="item in userOptions"
                                :key="item.id"
                                :label="`${item.username} (${item.name || '无名称'})`"
                                :value="item.id"
                                :disabled="item.disabled">
                                <template v-if="item.disabled">
                                    <span style="color: #999;">{{ item.username }} ({{ item.name || '无名称' }}) - 已关联后勤人员</span>
                                </template>
                                <template v-else>
                                    {{ item.username }} ({{ item.name || '无名称' }})
                                </template>
                            </el-option>
                        </el-select>
                        <div class="form-tip" style="font-size: 12px; color: #909399; margin-top: 5px;">
                            注意: 每个用户只能关联一个后勤人员记录
                        </div>
                    </el-form-item>

                    <!-- 创建新用户（新增或编辑替换且创建新用户时显示） -->
                    <template v-if="(dialogForm.userSelectType === 'new' && !dialogForm.id) || 
                                   (dialogForm.id && dialogForm.replaceUser && dialogForm.userSelectType === 'new')">
                        <el-form-item label="用户名" prop="user.username">
                            <el-input v-model="dialogForm.user.username" placeholder="请输入用户名"></el-input>
                        </el-form-item>
                        <el-form-item label="姓名" prop="user.name">
                            <el-input v-model="dialogForm.user.name" placeholder="请输入姓名"></el-input>
                        </el-form-item>
                        <el-form-item label="手机号" prop="user.phone">
                            <el-input v-model="dialogForm.user.phone" placeholder="请输入手机号"></el-input>
                        </el-form-item>
                        <el-form-item label="性别" prop="user.sex">
                            <el-select v-model="dialogForm.user.sex" placeholder="请选择性别">
                                <el-option label="男" value="MALE" />
                                <el-option label="女" value="FEMALE" />
                                <el-option label="其他" value="OTHER" />
                            </el-select>
                        </el-form-item>
                        <el-form-item label="密码" prop="user.password">
                            <el-input type="password" v-model="dialogForm.user.password" placeholder="请输入密码"></el-input>
                        </el-form-item>
                    </template>

                    <!-- 共同属性 -->
                    <el-form-item label="职位" prop="position">
                        <el-select v-model="dialogForm.position" placeholder="请选择职位">
                            <el-option label="后勤" value="MAINTENANCE" />
                            <el-option label="安保" value="SECURITY" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="描述" prop="description">
                        <el-input type="textarea" v-model="dialogForm.description" placeholder="请输入描述"></el-input>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="formVisible = false">取 消</el-button>
                    <el-button type="primary" :loading="isSubmit" @click="handleSave('dialogForm')">确 定</el-button>
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
    name: 'MaintenanceStaffManager',
    components: { Pagination },
    data() {
        return {
            // 数据列表加载动画
            listLoading: true,
            // 用户搜索加载
            userSearchLoading: false,

            // 查询列表参数对象
            listQuery: {
                position: undefined,
                username: undefined,
                pageNum: 1,
                pageSize: 10
            },

            // 新增/编辑提交表单对象
            dialogForm: {
                id: undefined,
                userId: undefined,
                position: '',
                description: '',
                replaceUser: false, // 是否替换用户
                userSelectType: 'existing', // existing/new
                user: {
                    username: '',
                    name: '',
                    phone: '',
                    sex: '',
                    password: '',
                    role: 'MAINTENANCE' // 默认角色为后勤人员
                }
            },

            // 数据总条数
            total: 0,
            // 表格数据数组
            tableData: [],
            // 多选数据暂存数组
            multipleSelection: [],
            // 新增/编辑弹窗显示/隐藏
            formVisible: false,
            // 导出弹窗显示/隐藏
            exportVisible: false,
            // 防止多次提交
            isSubmit: false,
            // 用户选项列表 
            userOptions: [],

            // 表单校验规则
            formRules: {
                position: [
                    { required: true, message: '请选择职位', trigger: 'change' }
                ],
                userId: [
                    { 
                        required: true, 
                        validator: (rule, value, callback) => {
                            // 仅当选择已有用户时才验证
                            if ((this.dialogForm.userSelectType === 'existing' && !this.dialogForm.id) || 
                                (this.dialogForm.id && this.dialogForm.replaceUser && this.dialogForm.userSelectType === 'existing')) {
                                if (!value) {
                                    callback(new Error('请选择用户'));
                                } else {
                                    callback();
                                }
                            } else {
                                callback();
                            }
                        }, 
                        trigger: 'change' 
                    }
                ],
                'user.username': [
                    { 
                        required: true, 
                        validator: (rule, value, callback) => {
                            // 仅当选择创建新用户时才验证
                            if ((this.dialogForm.userSelectType === 'new' && !this.dialogForm.id) || 
                                (this.dialogForm.id && this.dialogForm.replaceUser && this.dialogForm.userSelectType === 'new')) {
                                if (!value) {
                                    callback(new Error('请输入用户名'));
                                } else if (value.length < 3 || value.length > 20) {
                                    callback(new Error('长度在 3 到 20 个字符'));
                                } else {
                                    callback();
                                }
                            } else {
                                callback();
                            }
                        }, 
                        trigger: 'blur' 
                    }
                ],
                'user.name': [
                    { 
                        required: true, 
                        validator: (rule, value, callback) => {
                            // 仅当选择创建新用户时才验证
                            if ((this.dialogForm.userSelectType === 'new' && !this.dialogForm.id) || 
                                (this.dialogForm.id && this.dialogForm.replaceUser && this.dialogForm.userSelectType === 'new')) {
                                if (!value) {
                                    callback(new Error('请输入姓名'));
                                } else {
                                    callback();
                                }
                            } else {
                                callback();
                            }
                        }, 
                        trigger: 'blur' 
                    }
                ],
                'user.phone': [
                    { 
                        required: true, 
                        validator: (rule, value, callback) => {
                            // 仅当选择创建新用户时才验证
                            if ((this.dialogForm.userSelectType === 'new' && !this.dialogForm.id) || 
                                (this.dialogForm.id && this.dialogForm.replaceUser && this.dialogForm.userSelectType === 'new')) {
                                if (!value) {
                                    callback(new Error('请输入手机号'));
                                } else if (!/^1[3-9]\d{9}$/.test(value)) {
                                    callback(new Error('请输入正确的手机号'));
                                } else {
                                    callback();
                                }
                            } else {
                                callback();
                            }
                        }, 
                        trigger: 'blur' 
                    }
                ],
                'user.sex': [
                    { 
                        required: true, 
                        validator: (rule, value, callback) => {
                            // 仅当选择创建新用户时才验证
                            if ((this.dialogForm.userSelectType === 'new' && !this.dialogForm.id) || 
                                (this.dialogForm.id && this.dialogForm.replaceUser && this.dialogForm.userSelectType === 'new')) {
                                if (!value) {
                                    callback(new Error('请选择性别'));
                                } else {
                                    callback();
                                }
                            } else {
                                callback();
                            }
                        }, 
                        trigger: 'change' 
                    }
                ],
                'user.password': [
                    { 
                        required: true, 
                        validator: (rule, value, callback) => {
                            // 仅当选择创建新用户时才验证
                            if ((this.dialogForm.userSelectType === 'new' && !this.dialogForm.id) || 
                                (this.dialogForm.id && this.dialogForm.replaceUser && this.dialogForm.userSelectType === 'new')) {
                                if (!value) {
                                    callback(new Error('请输入密码'));
                                } else if (value.length < 6 || value.length > 20) {
                                    callback(new Error('长度在 6 到 20 个字符'));
                                } else {
                                    callback();
                                }
                            } else {
                                callback();
                            }
                        }, 
                        trigger: 'blur' 
                    }
                ]
            }
        }
    },
    created() {
        this.fetchData()
    },
    watch: {
        // 监听用户选择类型变化
        'dialogForm.userSelectType': function(newVal) {
            // 当切换到"创建新用户"模式时，重置用户表单
            if (newVal === 'new') {
                this.dialogForm.user = {
                    username: '',
                    name: '',
                    phone: '',
                    sex: '',
                    password: '',
                    role: 'MAINTENANCE'
                };
            }
            // 当切换到"选择已有用户"模式时，加载用户列表
            else if (newVal === 'existing') {
                this.dialogForm.userId = undefined;
                this.fetchMaintenanceUsers();
            }
        }
    },
    methods: {
        // 远程搜索用户
        remoteSearchUsers(query) {
            if (query === '') {
                this.userOptions = []
                return
            }
            
            this.userSearchLoading = true
            Request.get("/user/search", {
                params: { keyword: query }
            }).then(response => {
                if (response.code === '0') {
                    // 只保留后勤人员角色的用户
                    const maintenanceUsers = response.data.filter(user => user.role === 'MAINTENANCE')
                    
                    // 检查用户是否已经关联后勤人员
                    Promise.all(maintenanceUsers.map(user => {
                        return Request.get(`/maintenance-staff/userId/${user.id}`).then(res => {
                            // 如果返回成功且有数据，说明用户已关联后勤人员
                            if (res.code === '0' && res.data) {
                                return { ...user, disabled: true }
                            }
                            return { ...user, disabled: false }
                        })
                    })).then(results => {
                        this.userOptions = results
                        this.userSearchLoading = false
                    })
                } else {
                    this.userSearchLoading = false
                }
            }).catch(() => {
                this.userSearchLoading = false
            })
        },

        // 获取后勤角色用户列表
        fetchMaintenanceUsers() {
            Request.get("/user/role-enum/MAINTENANCE").then(response => {
                if (response.code === '0') {
                    // 检查用户是否已经关联后勤人员
                    Promise.all(response.data.map(user => {
                        return Request.get(`/maintenance-staff/userId/${user.id}`).then(res => {
                            // 如果返回成功且有数据，说明用户已关联后勤人员
                            if (res.code === '0' && res.data) {
                                return { ...user, disabled: true }
                            }
                            return { ...user, disabled: false }
                        })
                    })).then(results => {
                        this.userOptions = results
                    })
                }
            })
        },

        // 多选操作
        handleSelectionChange(val) {
            this.multipleSelection = val.map(v => v.id)
        },

        // 获取数据列表
        fetchData({ page, limit } = {}) {
            if (page) this.listQuery.pageNum = page
            if (limit) this.listQuery.pageSize = limit
            
            this.listLoading = true
            Request.get("/maintenance-staff/page", {
                params: {
                    pageNum: this.listQuery.pageNum,
                    pageSize: this.listQuery.pageSize
                }
            }).then(response => {
                if (response.code === '0') {
                    const { total, records } = response.data
                    this.total = total
                    this.tableData = records
                } else {
                    this.$message({
                        type: 'error',
                        message: response.msg || '获取数据失败'
                    })
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
                position: undefined,
                username: undefined,
                pageNum: 1,
                pageSize: 10
            }
            this.fetchData()
        },

        // 新建数据
        handleCreate() {
            this.dialogForm = {
                id: undefined,
                userId: undefined,
                position: '',
                description: '',
                userSelectType: 'existing',
                replaceUser: false,
                user: {
                    username: '',
                    name: '',
                    phone: '',
                    sex: '',
                    password: '',
                    role: 'MAINTENANCE'
                }
            };
            
            // 清空选项数据
            this.userOptions = [];
            
            // 如果选择的是已有用户模式，则加载用户列表
            if (this.dialogForm.userSelectType === 'existing') {
                this.fetchMaintenanceUsers();
            }
            
            this.formVisible = true;
        },

        // 编辑数据
        handleEdit(row) {
            // 复制行数据，以防止直接修改表格数据
            const staffData = JSON.parse(JSON.stringify(row));
            
            // 初始化编辑表单
            this.dialogForm = {
                ...staffData,
                replaceUser: false,
                userSelectType: 'existing',
                user: staffData.user || {
                    username: '',
                    name: '',
                    phone: '',
                    sex: '',
                    password: '',
                    role: 'MAINTENANCE'
                }
            };
            
            // 如果存在关联的用户，保存用户ID
            if (staffData.user) {
                this.dialogForm.userId = staffData.user.id;
            }
            
            this.formVisible = true;
        },

        // 删除数据
        handleDelete(row) {
            this.$confirm('此操作将删除该后勤人员及其关联用户, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                // 使用后端提供的删除接口，而不是直接删除用户
                Request.delete("/maintenance-staff/" + row.id).then(response => {
                    if (response.code === '0') {
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        })
                        this.fetchData()
                    } else {
                        this.$message({
                            type: 'error',
                            message: response.msg || '删除失败，可能存在相关联的维修记录或投诉'
                        })
                    }
                })
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                })
            })
        },

        // 批量删除
        batchDelete() {
            if (!this.multipleSelection || this.multipleSelection.length < 1) {
                this.$message({
                    message: '请先选择要删除的数据！',
                    type: 'warning'
                })
                return
            }

            this.$confirm('此操作将删除选中的后勤人员及其关联用户, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                // 使用后端提供的批量删除接口
                Request.delete('/maintenance-staff/batch', {
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
                    } else {
                        this.$message({
                            type: 'error',
                            message: res.msg || '删除失败，可能存在相关联的维修记录或投诉'
                        })
                    }
                })
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                })
            })
        },

        // 保存数据
        handleSave(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) {
                    return false;
                }
                
                this.isSubmit = true;
                
                // 准备提交的数据
                const submitData = {
                    id: this.dialogForm.id,
                    position: this.dialogForm.position,
                    description: this.dialogForm.description
                };
                
                // 如果是编辑模式且不替换用户
                if (this.dialogForm.id && !this.dialogForm.replaceUser) {
                    Request.put("/maintenance-staff", submitData)
                        .then(this.handleSaveResponse)
                        .finally(() => {
                            this.isSubmit = false;
                        });
                    return;
                }
                
                // 如果是编辑模式且替换用户，选择已有用户
                if (this.dialogForm.id && this.dialogForm.replaceUser && this.dialogForm.userSelectType === 'existing') {
                    submitData.userId = this.dialogForm.userId;
                    
                    // 先检查用户是否已关联后勤人员
                    Request.get(`/maintenance-staff/userId/${submitData.userId}`)
                        .then(res => {
                            if (res.code === '0' && res.data) {
                                // 用户已关联后勤人员
                                this.$message({
                                    type: 'error',
                                    message: '该用户已关联后勤人员，请选择其他用户'
                                });
                                this.isSubmit = false;
                            } else {
                                // 用户未关联后勤人员，可以更新
                                return Request.put("/maintenance-staff", submitData)
                                    .then(this.handleSaveResponse)
                                    .finally(() => {
                                        this.isSubmit = false;
                                    });
                            }
                        })
                        .catch(error => {
                            this.$message({
                                type: 'error',
                                message: error.message || '操作失败'
                            });
                            this.isSubmit = false;
                        });
                    return;
                }
                
                // 如果是编辑模式且替换用户，创建新用户
                if (this.dialogForm.id && this.dialogForm.replaceUser && this.dialogForm.userSelectType === 'new') {
                    // 先创建用户
                    Request.post("/user/add", this.dialogForm.user)
                        .then(response => {
                            if (response.code === '0') {
                                // 用户创建成功，继续更新后勤人员
                                submitData.userId = response.data.id;
                                return Request.put("/maintenance-staff", submitData);
                            } else {
                                throw new Error(response.msg || '创建用户失败');
                            }
                        })
                        .then(this.handleSaveResponse)
                        .catch(error => {
                            this.$message({
                                type: 'error',
                                message: error.message || '操作失败'
                            });
                        })
                        .finally(() => {
                            this.isSubmit = false;
                        });
                    return;
                }
                
                // 如果是新建，且选择已有用户
                if (!this.dialogForm.id && this.dialogForm.userSelectType === 'existing') {
                    submitData.userId = this.dialogForm.userId;
                    
                    // 先检查用户是否已关联后勤人员
                    Request.get(`/maintenance-staff/userId/${submitData.userId}`)
                        .then(res => {
                            if (res.code === '0' && res.data) {
                                // 用户已关联后勤人员
                                this.$message({
                                    type: 'error',
                                    message: '该用户已关联后勤人员，请选择其他用户'
                                });
                                this.isSubmit = false;
                            } else {
                                // 用户未关联后勤人员，可以创建
                                return Request.post("/maintenance-staff", submitData)
                                    .then(this.handleSaveResponse)
                                    .finally(() => {
                                        this.isSubmit = false;
                                    });
                            }
                        })
                        .catch(error => {
                            this.$message({
                                type: 'error',
                                message: error.message || '操作失败'
                            });
                            this.isSubmit = false;
                        });
                    return;
                }
                
                // 如果是新建，且创建新用户
                if (!this.dialogForm.id && this.dialogForm.userSelectType === 'new') {
                    // 先创建用户
                    Request.post("/user/add", this.dialogForm.user)
                        .then(response => {
                            if (response.code === '0') {
                                // 用户创建成功，继续创建后勤人员
                                submitData.userId = response.data.id;
                                return Request.post("/maintenance-staff", submitData);
                            } else {
                                throw new Error(response.msg || '创建用户失败');
                            }
                        })
                        .then(this.handleSaveResponse)
                        .catch(error => {
                            this.$message({
                                type: 'error',
                                message: error.message || '操作失败'
                            });
                        })
                        .finally(() => {
                            this.isSubmit = false;
                        });
                }
            });
        },

        // 处理保存响应
        handleSaveResponse(response) {
            if (response.code === '0') {
                this.$message({
                    type: 'success',
                    message: this.dialogForm.id ? '更新成功' : '添加成功'
                })
                this.formVisible = false
                this.fetchData()
                return response
            } else {
                this.$message({
                    type: 'error',
                    message: response.msg || '操作失败'
                })
                throw new Error(response.msg || '操作失败')
            }
        },

        // 导出数据
        exportTable(type) {
            if (this.tableData.length) {
                const exportData = this.tableData.map(item => ({
                    id: item.id,
                    username: item.user?.username || '-',
                    name: item.user?.name || '-',
                    phone: item.user?.phone || '-',
                    position: item.position === 'MAINTENANCE' ? '后勤' : '安保',
                    description: item.description || '-',
                    createdAt: item.createdAt ? moment(item.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-'
                }))
                
                const params = {
                    header: ['ID', '用户名', '姓名', '手机号', '职位', '描述', '创建时间'],
                    key: ['id', 'username', 'name', 'phone', 'position', 'description', 'createdAt'],
                    data: exportData,
                    autoWidth: true,
                    fileName: '后勤人员数据表',
                    bookType: type
                }
                excel.exportDataToExcel(params)
                this.exportVisible = false
            }
        },

        // 添加moment方法
        moment(date) {
            return moment(date, moment.ISO_8601)
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
        
        .el-form-item {
            margin-bottom: 0;
            margin-right: 16px;

            &:last-child {
                margin-right: 0;
            }

            .el-input,
            .el-select {
                width: 200px;
            }
        }
    }

    .el-table {
        margin-bottom: 24px;
        
        .el-button {
            padding: 6px 12px;
            
            & + .el-button {
                margin-left: 8px;
            }
        }
    }

    .export-data {
        display: flex;
        flex-direction: column;
        gap: 16px;

        .export-option {
            display: flex;
            align-items: center;
            padding: 16px;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.3s;
            background-color: #f8f9fa;

            &:hover {
                background-color: #ecf5ff;
            }

            i {
                font-size: 24px;
                margin-right: 12px;
                color: #409eff;
            }

            span {
                font-size: 16px;
                color: #606266;
            }
        }
    }

    .el-dialog {
        .el-form {
            padding: 20px 0;
        }

        .dialog-footer {
            text-align: right;
        }
    }

    .current-user-info {
        display: flex;
        flex-direction: column;
        gap: 8px;
        padding: 8px 12px;
        background-color: #f8f9fa;
        border-radius: 4px;
        border-left: 3px solid #409eff;
        
        span {
            line-height: 1.5;
            color: #606266;
        }
    }
}

// 响应式布局
@media screen and (max-width: 768px) {
    .wrapper {
        .search-form {
            .el-form-item {
                margin-right: 0;
                margin-bottom: 16px;
                width: 100%;

                .el-input,
                .el-select {
                    width: 100%;
                }
            }
        }

        .control-btns {
            flex-direction: column;
            gap: 16px;

            .left-btns,
            .right-btns {
                width: 100%;
                justify-content: space-between;
            }
        }
    }
}
</style> 