<template>
    <div class="wrapper">
        <!-- 页面标题 -->
        <div class="page-header">
            <div class="header-content">
                <h2>宠物代喂人员管理</h2>
                <div class="divider"></div>
                <p class="subtitle">Pet Sitter Management</p>
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
                    <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增人员</el-button>
                </div>
            </div>

            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="姓名">
                    <el-input v-model="listQuery.name" placeholder="请输入姓名" size="medium" />
                </el-form-item>
                <el-form-item label="手机号">
                    <el-input v-model="listQuery.phone" placeholder="请输入手机号" size="medium" />
                </el-form-item>
                <el-form-item label="状态">
                    <el-select v-model="listQuery.status" placeholder="请选择状态" size="medium">
                        <el-option label="可接单" value="ACTIVE" />
                        <el-option label="不可接单" value="DISABLED" />
                    </el-select>
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
                <el-table-column prop="name" label="姓名"></el-table-column>
                <el-table-column prop="phone" label="联系电话"></el-table-column>
                <el-table-column prop="idCard" label="身份证号"></el-table-column>
                <el-table-column prop="servicePrice" label="服务价格">
                    <template slot-scope="scope">
                        ¥{{ scope.row.servicePrice }}
                    </template>
                </el-table-column>
                <el-table-column label="状态" width="120">
                    <template slot-scope="scope">
                        <el-switch
                            v-model="scope.row.status"
                            active-value="ACTIVE"
                            inactive-value="DISABLED"
                    
                            @change="(val) => handleStatusChange(scope.row, val)">
                        </el-switch>
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="描述" show-overflow-tooltip></el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="180">
                    <template slot-scope="scope">
                        {{ formatDateTime(scope.row.createdAt) }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                    <template slot-scope="scope">
                        <el-button plain size="small" type="primary" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
                        <el-button plain size="small" type="danger" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
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
                :title="dialogForm.id ? '编辑代喂人员' : '新增代喂人员'" 
                :visible.sync="formVisible" 
                width="500px"
                :close-on-click-modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="姓名" prop="name">
                        <el-input v-model="dialogForm.name" placeholder="请输入姓名"></el-input>
                    </el-form-item>
                    <el-form-item label="手机号" prop="phone">
                        <el-input v-model="dialogForm.phone" placeholder="请输入手机号"></el-input>
                    </el-form-item>
                    <el-form-item label="身份证号" prop="idCard">
                        <el-input v-model="dialogForm.idCard" placeholder="请输入身份证号"></el-input>
                    </el-form-item>
                    <el-form-item label="服务价格" prop="servicePrice">
                        <el-input-number v-model="dialogForm.servicePrice" :precision="2" :step="0.1" :min="0"></el-input-number>
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
import { mapGetters, mapActions } from 'vuex'
import moment from 'moment'

export default {
    name: 'PetSitterManager',
    components: { Pagination },
    data() {
        // 手机号验证规则
        const validatePhone = (rule, value, callback) => {
            if (!/^1[3-9]\d{9}$/.test(value)) {
                callback(new Error('请输入正确的手机号'));
            } else {
                callback();
            }
        };
        // 身份证号验证规则
        const validateIdCard = (rule, value, callback) => {
            if (!/^\d{17}[\dX]$/.test(value)) {
                callback(new Error('请输入正确的身份证号'));
            } else {
                callback();
            }
        };

        return {
            // 数据列表加载动画
            listLoading: true,
            // 查询列表参数对象
            listQuery: {
                name: undefined,
                phone: undefined,
                status: undefined,
                pageNum: 1,
                pageSize: 10
            },
            // 新增/编辑提交表单对象
            dialogForm: {
                id: undefined,
                name: '',
                phone: '',
                idCard: '',
                status: 'ACTIVE',
                description: '',
                servicePrice: 0
            },
            // 表单校验规则
            formRules: {
                name: [
                    { required: true, message: '请输入姓名', trigger: 'blur' },
                    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
                ],
                phone: [
                    { required: true, message: '请输入手机号', trigger: 'blur' },
                    { validator: validatePhone, trigger: 'blur' }
                ],
                idCard: [
                    { required: true, message: '请输入身份证号', trigger: 'blur' },
                    { validator: validateIdCard, trigger: 'blur' }
                ],
                servicePrice: [
                    { required: true, message: '请输入服务价格', trigger: 'blur' }
                ],
                description: [
                    { max: 500, message: '描述内容不能超过500个字符', trigger: 'blur' }
                ]
            },
            // 数据总条数
            total: 0,
            // 表格数据数组
            tableData: [],
            // 多选数据暂存数组
            multipleSelection: [],
            // 新增/编辑弹出框显示/隐藏
            formVisible: false,
            // 导出数据弹出框显示/隐藏
            exportVisible: false,
            // 防止多次连续提交表单
            isSubmit: false
        }
    },
    created() {
        this.fetchData()
    },
    computed: {
        ...mapGetters(['isLoggedIn', 'currentUser']),
    },
    methods: {
        ...mapActions(['logout']),
        // 获取数据列表
        fetchData({ page, limit } = {}) {
            if (page) this.listQuery.pageNum = page;
            if (limit) this.listQuery.pageSize = limit;
            
            this.listLoading = true;
            Request.get("/pet-sitter/page", {
                params: {
                    ...this.listQuery
                }
            }).then(response => {
                if (response.code === '0') {
                    const { total, records } = response.data;
                    this.total = total;
                    this.tableData = records;
                } else {
                    this.$message.error(response.msg || '获取数据失败');
                }
            }).finally(() => {
                this.listLoading = false;
            });
        },

        // 多选操作
        handleSelectionChange(val) {
            this.multipleSelection = val.map(v => v.id);
        },

        // 新建数据
        handleCreate() {
            this.dialogForm = {
                name: '',
                phone: '',
                idCard: '',
                status: 'ACTIVE',
                description: '',
                servicePrice: 0
            };
            this.formVisible = true;
        },

        // 编辑数据
        handleEdit(row) {
            this.dialogForm = JSON.parse(JSON.stringify(row));
            this.formVisible = true;
        },

        // 删除数据
        handleDelete(row) {
            this.$confirm('此操作将删除该代喂人员, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete(`/pet-sitter/${row.id}`).then(response => {
                    if (response.code === '0') {
                        this.$message.success('删除成功!');
                        this.fetchData();
                    } else {
                        this.$message.error(response.msg || '删除失败');
                    }
                });
            });
        },

        // 批量删除
        batchDelete() {
            if (!this.multipleSelection.length) {
                this.$message.warning('请选择要删除的数据');
                return;
            }
            
            this.$confirm('此操作将删除选中的代喂人员, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete('/pet-sitter/batch', {
                    data: this.multipleSelection
                }).then(response => {
                    if (response.code === '0') {
                        this.$message.success('批量删除成功!');
                        this.$refs.multipleTable.clearSelection();
                        this.fetchData();
                    } else {
                        this.$message.error(response.msg || '批量删除失败');
                    }
                });
            });
        },

        // 更改状态
        handleStatusChange(row, val) {
            Request.put(`/pet-sitter`, {
                ...row,
                status: val
            }).then(response => {
                if (response.code === '0') {
                    this.$message.success('状态更新成功');
                } else {
                    // 如果更新失败，恢复为原来的状态
                    row.status = val === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
                    this.$message.error(response.msg || '状态更新失败');
                }
            }).catch(() => {
                // 发生错误时恢复状态
                row.status = val === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
                this.$message.error('状态更新失败');
            });
        },

        // 保存数据
        handleSave(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) return;
                
                this.isSubmit = true;
                const request = this.dialogForm.id 
                    ? Request.put(`/pet-sitter`, this.dialogForm)
                    : Request.post('/pet-sitter', this.dialogForm);

                request.then(response => {
                    if (response.code === '0') {
                        this.$message.success(this.dialogForm.id ? '更新成功' : '添加成功');
                        this.formVisible = false;
                        this.fetchData();
                    } else {
                        this.$message.error(response.msg || (this.dialogForm.id ? '更新失败' : '添加失败'));
                    }
                }).finally(() => {
                    this.isSubmit = false;
                });
            });
        },

        // 查询数据
        onSubmit() {
            this.listQuery.pageNum = 1;
            this.fetchData();
        },

        // 重置查询
        onReset() {
            this.listQuery = {
                name: undefined,
                phone: undefined,
                status: undefined,
                pageNum: 1,
                pageSize: 10
            };
            this.fetchData();
        },

        // 导出数据
        exportTable(type) {
            if (!this.tableData.length) {
                this.$message.warning('暂无数据可导出');
                return;
            }

            const params = {
                header: ['ID', '姓名', '联系电话', '身份证号', '服务价格', '状态', '描述', '创建时间'],
                key: ['id', 'name', 'phone', 'idCard', 'servicePrice', 'status', 'description', 'createdAt'],
                data: this.tableData,
                autoWidth: true,
                fileName: '代喂人员数据表',
                bookType: type
            }
            excel.exportDataToExcel(params)
            this.exportVisible = false
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
        display: flex;
        align-items: center;
        justify-content: flex-start;

        .el-form-item {
            margin-bottom: 0;
            margin-right: 16px;

            &:last-child {
                margin-right: 0;
            }
        }
    }

    .export-data {
        padding: 16px;

        .export-option {
            display: flex;
            align-items: center;
            padding: 16px;
            cursor: pointer;
            transition: all 0.3s;
            border-radius: 4px;

            &:hover {
                background-color: #f5f7fa;
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

    .el-table {
        margin: 24px 0;
        
        .el-button {
            padding: 6px 12px;
        }
    }

    .dialog-footer {
        display: flex;
        justify-content: flex-end;
        padding-top: 16px;
        gap: 12px;
    }

    .el-input-number {
        width: 100%;
    }
}

// 自定义表格样式
/deep/ .el-table {
    th {
        background-color: #f8f9fa !important;
        color: #606266;
        font-weight: 500;
        height: 48px;
    }

    td {
        padding: 12px 0;
    }
}

// 自定义分页样式
/deep/ .pagination-container {
    padding: 24px 0;
    display: flex;
    justify-content: flex-end;
}

// 自定义对话框样式
/deep/ .el-dialog {
    border-radius: 8px;
    
    .el-dialog__header {
        padding: 24px;
        border-bottom: 1px solid #ebeef5;
    }

    .el-dialog__body {
        padding: 24px;
    }

    .el-dialog__footer {
        padding: 16px 24px;
        border-top: 1px solid #ebeef5;
    }
}
</style> 