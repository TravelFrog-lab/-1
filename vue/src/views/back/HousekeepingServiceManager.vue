<template>
    <div class="wrapper">
        <!-- 页面标题 -->
        <div class="page-header">
            <div class="header-content">
                <h2>家政服务项目管理</h2>
                <div class="divider"></div>
                <p class="subtitle">Housekeeping Service Management</p>
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
                    <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleAdd" plain>新增服务项目</el-button>
                </div>
            </div>

            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="服务名称">
                    <el-input v-model="listQuery.name" placeholder="请输入服务名称" size="medium" clearable />
                </el-form-item>
                <el-form-item label="服务类别">
                    <el-select v-model="listQuery.category" placeholder="请选择服务类别" size="medium" clearable>
                        <el-option label="清洁服务" value="清洁服务"></el-option>
                        <el-option label="照护服务" value="照护服务"></el-option>
                        <el-option label="烹饪服务" value="烹饪服务"></el-option>
                        <el-option label="维修服务" value="维修服务"></el-option>
                        <el-option label="其他服务" value="其他服务"></el-option>
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
                <el-table-column prop="name" label="服务名称" width="150"></el-table-column>
                <el-table-column prop="category" label="服务类别" width="120"></el-table-column>
                <el-table-column prop="basePrice" label="基础价格" width="120">
                    <template slot-scope="scope">
                        {{ scope.row.basePrice }} 元
                    </template>
                </el-table-column>
                <el-table-column prop="unit" label="计价单位" width="100"></el-table-column>
                <el-table-column prop="description" label="服务描述">
                    <template slot-scope="scope">
                        <el-tooltip :content="scope.row.description" placement="top" effect="light">
                            <div class="description-cell">{{ scope.row.description }}</div>
                        </el-tooltip>
                    </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="180">
                    <template slot-scope="scope">
                        {{ formatDate(scope.row.createdAt) }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="220" fixed="right">
                    <template slot-scope="scope">
                        <el-button
                            plain size="small" type="primary" icon="el-icon-edit"
                            @click="handleEdit(scope.row)">编辑</el-button>
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
                :title="dialogForm.id ? '编辑服务项目' : '新增服务项目'" 
                :visible.sync="formVisible" 
                width="500px"
                :close-on-click-modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="服务名称" prop="name">
                        <el-input v-model="dialogForm.name" placeholder="请输入服务名称"></el-input>
                    </el-form-item>
                    <el-form-item label="服务类别" prop="category">
                        <el-select v-model="dialogForm.category" placeholder="请选择服务类别" style="width: 100%">
                            <el-option label="清洁服务" value="清洁服务"></el-option>
                            <el-option label="照护服务" value="照护服务"></el-option>
                            <el-option label="烹饪服务" value="烹饪服务"></el-option>
                            <el-option label="维修服务" value="维修服务"></el-option>
                            <el-option label="其他服务" value="其他服务"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="基础价格" prop="basePrice">
                        <el-input-number 
                            v-model="dialogForm.basePrice" 
                            :precision="2" 
                            :step="10" 
                            :min="0"
                            style="width: 100%"
                            placeholder="请输入基础价格">
                        </el-input-number>
                    </el-form-item>
                    <el-form-item label="计价单位" prop="unit">
                        <el-select v-model="dialogForm.unit" placeholder="请选择计价单位" style="width: 100%">
                            <el-option label="小时" value="小时"></el-option>
                            <el-option label="次" value="次"></el-option>
                            <el-option label="平米" value="平米"></el-option>
                            <el-option label="天" value="天"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="服务描述" prop="description">
                        <el-input type="textarea" v-model="dialogForm.description" :rows="4" placeholder="请输入服务描述"></el-input>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="formVisible = false">取 消</el-button>
                    <el-button type="primary" :loading="isSubmit" @click="submitForm('dialogForm')">确 定</el-button>
                </div>
            </el-dialog>

            <!-- 导出对话框 -->
            <el-dialog title="导出数据" :visible.sync="exportVisible" width="300px">
                <div class="export-data">
                    <div class="export-option" @click="exportTable('xlsx')">
                        <i class="el-icon-document"></i>
                        <span>导出为Excel</span>
                    </div>
                    <div class="export-option" @click="exportTable('csv')">
                        <i class="el-icon-tickets"></i>
                        <span>导出为CSV</span>
                    </div>
                </div>
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
    name: 'HousekeepingServiceManager',
    components: { Pagination },
    data() {
        return {
            // 列表查询参数
            listQuery: {
                name: undefined,
                category: undefined,
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
                name: '',
                category: '',
                basePrice: 0,
                unit: '',
                description: ''
            },
            // 表单校验规则
            formRules: {
                name: [
                    { required: true, message: '请输入服务名称', trigger: 'blur' },
                    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
                ],
                category: [
                    { required: true, message: '请选择服务类别', trigger: 'change' }
                ],
                basePrice: [
                    { required: true, message: '请输入基础价格', trigger: 'blur' }
                ],
                unit: [
                    { required: true, message: '请选择计价单位', trigger: 'change' }
                ],
                description: [
                    { required: true, message: '请输入服务描述', trigger: 'blur' }
                ]
            }
        }
    },
    created() {
        this.fetchData()
    },
    methods: {
        // 格式化日期
        formatDate(date) {
            if (!date) return ''
            return moment(date).format('YYYY-MM-DD HH:mm:ss')
        },
        
        // 获取数据
        fetchData() {
            this.listLoading = true
            Request.get('/housekeeping-service/page', {
                params: {
                    pageNum: this.listQuery.pageNum,
                    pageSize: this.listQuery.pageSize,
                    name: this.listQuery.name,
                    category: this.listQuery.category
                }
            }).then(response => {
                if (response.code === '0') {
                    this.tableData = response.data.records
                    this.total = response.data.total
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
                category: undefined,
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
                name: '',
                category: '',
                basePrice: 0,
                unit: '',
                description: ''
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
            this.$confirm('确认删除该服务项目?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete(`/housekeeping-service/${row.id}`).then(response => {
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
                Request.delete('/housekeeping-service/batch', {
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
                    const url = '/housekeeping-service'
                    
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
                    header: ['ID', '服务名称', '服务类别', '基础价格', '计价单位', '服务描述', '创建时间'],
                    key: ['id', 'name', 'category', 'basePrice', 'unit', 'description', 'createdAt'],
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '家政服务项目信息表',
                    bookType: type
                }
                excel.exportDataToExcel(params)
                this.exportVisible = false
            }
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
}
</style> 