<template>
    <div class="wrapper">
        <!-- 页面标题区 -->
        <div class="page-header">
            <div class="header-content">
                <h2>用户管理</h2>
                <div class="divider"></div>
                <p class="subtitle">User Management</p>
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
                    <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增用户</el-button>
                </div>
            </div>

            <!-- 查询表单区 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="用户名">
                    <el-input v-model="listQuery.username" placeholder="请输入用户名" size="medium" />
                </el-form-item>
                <el-form-item label="姓名">
                    <el-input v-model="listQuery.name" placeholder="请输入姓名" size="medium" />
                </el-form-item>
                <el-form-item label="角色">
                    <el-select v-model="listQuery.role" placeholder="请选择角色" size="medium">
                        <el-option label="业主" value="OWNER" />
                        <el-option label="管理员" value="ADMIN" />
                        <el-option label="后勤人员" value="MAINTENANCE" />
                        <el-option label="家政人员" value="KEEPER" />
                    </el-select>
                </el-form-item>
                <el-form-item label="性别">
                    <el-select v-model="listQuery.sex" placeholder="请选择性别" size="medium">
                        <el-option label="男" value="MALE" />
                        <el-option label="女" value="FEMALE" />
                        <el-option label="其他" value="OTHER" />
                    </el-select>
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
                <el-table-column prop="username" label="用户名"></el-table-column>
                <el-table-column prop="name" label="姓名"></el-table-column>
                <el-table-column prop="phone" label="手机号"></el-table-column>
                <el-table-column prop="role" label="角色">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : scope.row.role === 'MAINTENANCE' ? 'warning' : scope.row.role === 'KEEPER' ? 'info' : 'success'">
                            {{ scope.row.role === 'ADMIN' ? '管理员' : scope.row.role === 'MAINTENANCE' ? '后勤人员' : scope.row.role === 'KEEPER' ? '家政人员' : '业主' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="sex" label="性别">
                    <template slot-scope="scope">
                        {{ scope.row.sex === 'MALE' ? '男' : scope.row.sex === 'FEMALE' ? '女' : '其他' }}
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.status === 'ENABLED' ? 'success' : 'info'">
                            {{ scope.row.status === 'ENABLED' ? '正常' : '禁用' }}
                        </el-tag>
                    </template>

                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="180">
                    <template slot-scope="scope">
                        {{ scope.row.createdAt ? moment(scope.row.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-' }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="300" fixed="right">
                    <template slot-scope="scope">
                        <el-button plain size="small" type="primary" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
                        <el-button plain size="small" type="warning" icon="el-icon-key" @click="handleResetPwd(scope.row)">重置密码</el-button>
                        <el-button plain size="small" type="danger" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页区 -->
            <Pagination
                v-show="total>0"
                :total="total"
                :page.sync="listQuery.currentPage"
                :limit.sync="listQuery.pageSize"
                @pagination="fetchData"
            />

            <!-- 新增/编辑弹窗 -->
            <el-dialog 
                :title="dialogForm.id ? '编辑用户' : '新增用户'" 
                :visible.sync="formVisible" 
                width="500px"
                :close-on-click-modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="用户名" prop="username">
                        <el-input v-model="dialogForm.username" placeholder="请输入用户名"></el-input>
                    </el-form-item>
                    <el-form-item label="姓名" prop="name">
                        <el-input v-model="dialogForm.name" placeholder="请输入姓名"></el-input>
                    </el-form-item>
                    <el-form-item label="手机号" prop="phone">
                        <el-input v-model="dialogForm.phone" placeholder="请输入手机号"></el-input>
                    </el-form-item>
                    <el-form-item label="角色" prop="role">
                        <el-select v-model="dialogForm.role" placeholder="请选择角色">
                            <el-option label="业主" value="OWNER" />
                            <el-option label="管理员" value="ADMIN" />
                            <el-option label="后勤人员" value="MAINTENANCE" />
                            <el-option label="家政人员" value="KEEPER" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="性别" prop="sex">
                        <el-select v-model="dialogForm.sex" placeholder="请选择性别">
                            <el-option label="男" value="MALE" />
                            <el-option label="女" value="FEMALE" />
                            <el-option label="其他" value="OTHER" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="状态" prop="status">
                        <el-select v-model="dialogForm.status" placeholder="请选择状态">
                            <el-option label="正常" value="ENABLED" />
                            <el-option label="禁用" value="DISABLED" />
                        </el-select>
                    </el-form-item>
                    <el-form-item v-if="!dialogForm.id" label="密码" prop="password">
                        <el-input type="password" v-model="dialogForm.password" placeholder="请输入密码"></el-input>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="formVisible = false">取 消</el-button>
                    <el-button type="primary" :loading="isSubmit" @click="handleSave('dialogForm')">确 定</el-button>
                </div>
            </el-dialog>

            <!-- 重置密码弹窗 -->
            <el-dialog
                title="重置密码"
                :visible.sync="pwdVisible"
                width="400px"
                :close-on-click-modal="false">
                <el-form :model="pwdForm" :rules="pwdRules" ref="pwdForm" label-width="100px">
                    <el-form-item label="新密码" prop="newPassword">
                        <el-input type="password" v-model="pwdForm.newPassword" placeholder="请输入新密码"></el-input>
                    </el-form-item>
                    <el-form-item label="确认密码" prop="confirmPassword">
                        <el-input type="password" v-model="pwdForm.confirmPassword" placeholder="请确认新密码"></el-input>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="pwdVisible = false">取 消</el-button>
                    <el-button type="primary" :loading="isSubmit" @click="handleSavePwd('pwdForm')">确 定</el-button>
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
    name: 'User',
    components: { Pagination },
    data() {
        // 确认密码的验证规则
        const validateConfirmPassword = (rule, value, callback) => {
            if (value !== this.pwdForm.newPassword) {
                callback(new Error('两次输入的密码不一致!'))
            } else {
                callback()
            }
        }
        return {
            // 数据列表加载动画
            listLoading: true,

            // 查询列表参数对象
            listQuery: {
                username: undefined,
                name: undefined,
                role: undefined,
                sex: undefined,
                currentPage: 1,
                pageSize: 10
            },

            // 新增/编辑提交表单对象
            dialogForm: {
                id: undefined,
                username: '',
                name: '',
                phone: '',
                role: '',
                sex: '',
                status: 'ENABLED',
                password: ''
            },


            // 重置密码表单
            pwdForm: {
                id: undefined,
                newPassword: '',
                confirmPassword: ''
            },

            // 数据总条数
            total: 0,
            // 表格数据数组
            tableData: [],
            // 多选数据暂存数组
            multipleSelection: [],
            // 新增/编辑弹窗显示/隐藏
            formVisible: false,
            // 重置密码弹窗显示/隐藏
            pwdVisible: false,
            // 导出弹窗显示/隐藏
            exportVisible: false,
            // 防止多次提交
            isSubmit: false,

            // 表单校验规则
            formRules: {
                username: [
                    { required: true, message: '请输入用户名', trigger: 'blur' },
                    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
                ],
                name: [
                    { required: true, message: '请输入姓名', trigger: 'blur' }
                ],
                phone: [
                    { required: true, message: '请输入手机号', trigger: 'blur' },
                    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
                ],
                role: [
                    { required: true, message: '请选择角色', trigger: 'change' }
                ],
                sex: [
                    { required: true, message: '请选择性别', trigger: 'change' }
                ],
                status: [
                    { required: true, message: '请选择状态', trigger: 'change' }
                ],
                password: [
                    { required: true, message: '请输入密码', trigger: 'blur' },
                    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
                ]
            },

            // 重置密码校验规则
            pwdRules: {
                newPassword: [
                    { required: true, message: '请输入新密码', trigger: 'blur' },
                    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
                ],
                confirmPassword: [
                    { required: true, message: '请确认密码', trigger: 'blur' },
                    { validator: validateConfirmPassword, trigger: 'blur' }
                ]
            }
        }
    },
    created() {
        this.fetchData()
    },
    methods: {
        // 多选操作
        handleSelectionChange(val) {
            this.multipleSelection = val.map(v => v.id)
        },

        // 获取数据列表
        fetchData({ page, limit } = {}) {
            if (page) this.listQuery.currentPage = page
            if (limit) this.listQuery.pageSize = limit
            
            this.listLoading = true
            Request.get("/user/page", {
                params: {
                    ...this.listQuery,
                    size: this.listQuery.pageSize,
            
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
            this.listQuery.currentPage = 1
            this.fetchData()
        },

        // 重置查询
        onReset() {
            this.listQuery = {
                username: undefined,
                name: undefined,
                role: undefined,
                sex: undefined,
                currentPage: 1,
                pageSize: 10
            }
            this.fetchData()
        },

        // 新建数据
        handleCreate() {
            this.dialogForm = {
                username: '',
                name: '',
                phone: '',
                role: '',
                sex: '',
                status: 'ENABLED',
                password: ''
            }
            this.formVisible = true

        },

        // 编辑数据
        handleEdit(row) {
            this.dialogForm = JSON.parse(JSON.stringify(row))
            this.formVisible = true
        },

        // 删除数据
        handleDelete(row) {
            this.$confirm('此操作将删除该用户, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/user/delete/" + row.id).then(response => {
                    if (response.code === '0') {
                        this.$message({
                            type: 'success',
                            message: '删除成功!'
                        })
                        this.fetchData()
                    } else {
                        this.$message({
                            type: 'error',
                            message: response.msg
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

            this.$confirm('此操作将删除选中的用户, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete('/user/deleteBatch', {
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
                            message: res.msg
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
                    return false
                }
                
                this.isSubmit = true
                const request = this.dialogForm.id 
                    ? Request.put("/user/" + this.dialogForm.id, this.dialogForm)
                    : Request.post("/user/add", this.dialogForm)


                request.then(response => {
                    if (response.code === '0') {
                        this.$message({
                            type: 'success',
                            message: this.dialogForm.id ? '更新成功' : '添加成功'
                        })
                        this.formVisible = false
                        this.fetchData()
                    } else {
                        this.$message({
                            type: 'error',
                            message: response.msg
                        })
                    }
                }).finally(() => {
                    this.isSubmit = false
                })
            })
        },

        // 重置密码
        handleResetPwd(row) {
            this.pwdForm = {
                id: row.id,
                newPassword: '',
                confirmPassword: ''
            }
            this.pwdVisible = true
        },

        // 保存新密码
        handleSavePwd(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) {
                    return false
                }
                
                this.isSubmit = true
                Request.post("/user/reset-password", null, {
                    params: {
                        userId: this.pwdForm.id,
                        newPassword: this.pwdForm.newPassword
                    }
                }).then(response => {
                    if (response.code === '0') {
                        this.$message({
                            type: 'success',
                            message: '密码重置成功'
                        })
                        this.pwdVisible = false
                    } else {
                        this.$message({
                            type: 'error',
                            message: response.msg
                        })
                    }
                }).finally(() => {
                    this.isSubmit = false
                })
            })
        },

        // 导出数据
        exportTable(type) {
            if (this.tableData.length) {
                const exportData = this.tableData.map(item => ({
                    ...item,
                    createdAt: item.createdAt ? moment(item.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-'
                }))
                
                const params = {
                    header: ['ID', '用户名', '姓名', '手机号', '角色', '性别', '状态', '创建时间'],
                    key: ['id', 'username', 'name', 'phone', 'role', 'sex', 'status', 'createdAt'],
                    data: exportData,
                    autoWidth: true,
                    fileName: '用户数据表',
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