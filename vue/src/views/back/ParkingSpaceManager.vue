<template>
    <div class="wrapper">
        <!-- 页面标题 -->
        <div class="page-header">
            <div class="header-content">
                <h2>车位管理</h2>
                <div class="divider"></div>
                <p class="subtitle">Parking Space Management</p>
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
                    <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增车位</el-button>
                </div>
            </div>

            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="车位编号">
                    <el-input v-model="listQuery.code" placeholder="请输入车位编号" size="medium" />
                </el-form-item>
                <el-form-item label="车位类型">
                    <el-select v-model="listQuery.type" placeholder="请选择车位类型" size="medium">
                        <el-option label="公共车位" value="PUBLIC" />
                        <el-option label="私人车位" value="PRIVATE" />
                    </el-select>
                </el-form-item>
                <el-form-item label="占用状态">
                    <el-select v-model="listQuery.status" placeholder="请选择占用状态" size="medium">
                        <el-option label="空闲" value="0" />
                        <el-option label="已占用" value="1" />
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
                <el-table-column prop="code" label="车位编号"></el-table-column>
                <el-table-column prop="type" label="车位类型">
                    <template slot-scope="scope">
                        {{ scope.row.type === 'PUBLIC' ? '公共车位' : '私人车位' }}
                    </template>
                </el-table-column>
                <el-table-column prop="location" label="车位位置"></el-table-column>
                <el-table-column prop="status" label="使用状态">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.status === '0' ? 'success' : 'warning'">
                            {{ scope.row.status === '0' ? '空置' : '已占用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="180">
                    <template slot-scope="scope">
                        {{ formatDateTime(scope.row.createdAt) }}
                    </template>
                </el-table-column>
                <el-table-column prop="updatedAt" label="更新时间" width="180">
                    <template slot-scope="scope">
                        {{ formatDateTime(scope.row.updatedAt) }}
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
                :title="dialogForm.id ? '编辑车位' : '新增车位'" 
                :visible.sync="formVisible" 
                width="500px"
                :close-on-click-modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="车位编号" prop="code">
                        <el-input v-model="dialogForm.code" placeholder="请输入车位编号"></el-input>
                    </el-form-item>
                    <el-form-item label="车位类型" prop="type">
                        <el-select v-model="dialogForm.type" placeholder="请选择车位类型">
                            <el-option label="公共车位" value="PUBLIC" />
                            <el-option label="私人车位" value="PRIVATE" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="车位位置" prop="location">
                        <el-input v-model="dialogForm.location" placeholder="请输入车位位置"></el-input>
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
    name: 'ParkingSpaceManager',
    components: { Pagination },
    data() {
        return {
            // 数据列表加载动画
            listLoading: true,
            
            // 查询列表参数对象
            listQuery: {
                code: undefined,
                type: undefined,
                status: undefined,
                pageNum: 1,
                pageSize: 10
            },

            // 新增/编辑提交表单对象
            dialogForm: {
                id: undefined,
                code: '',
                type: undefined,
                location: ''
            },

            // 表单校验规则
            formRules: {
                code: [
                    { required: true, message: '请输入车位编号', trigger: 'blur' },
                    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
                ],
                type: [
                    { required: true, message: '请选择车位类型', trigger: 'change' }
                ],
                location: [
                    { required: true, message: '请输入车位位置', trigger: 'blur' },
                    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
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
    methods: {
        // 获取数据列表
        fetchData({ page, limit } = {}) {
            if (page) this.listQuery.pageNum = page
            if (limit) this.listQuery.pageSize = limit
            
            this.listLoading = true
            Request.get("/parking-space/page", {
                params: {
                    ...this.listQuery
                }
            }).then(response => {
                if (response.code === '0') {
                    const { total, records } = response.data
                    this.total = total
                    this.tableData = records
                } else {
                    this.$message.error(response.msg || '获取数据失败')
                }
            }).finally(() => {
                this.listLoading = false
            })
        },

        // 多选操作
        handleSelectionChange(val) {
            this.multipleSelection = val.map(v => v.id)
        },

        // 新建数据
        handleCreate() {
            this.dialogForm = {
                code: '',
                type: undefined,
                location: ''
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
            this.$confirm('此操作将删除该车位, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete(`/parking-space/${row.id}`).then(response => {
                    if (response.code === '0') {
                        this.$message.success('删除成功!')
                        this.fetchData()
                    } else {
                        this.$message.error(response.msg || '删除失败')
                    }
                })
            }).catch(() => {
                this.$message.info('已取消删除')
            })
        },

        // 批量删除
        batchDelete() {
            if (!this.multipleSelection.length) {
                this.$message.warning('请选择要删除的数据')
                return
            }

            this.$confirm('此操作将删除选中的车位, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete('/parking-space/batch', {
                    data: this.multipleSelection
                }).then(response => {
                    if (response.code === '0') {
                        this.$message.success('批量删除成功!')
                        this.$refs.multipleTable.clearSelection()
                        this.fetchData()
                    } else {
                        this.$message.error(response.msg || '批量删除失败')
                    }
                })
            }).catch(() => {
                this.$message.info('已取消删除')
            })
        },

        // 保存数据
        handleSave(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) return false
                
                this.isSubmit = true
                const request = this.dialogForm.id 
                    ? Request.put(`/parking-space`, this.dialogForm)
                    : Request.post('/parking-space', this.dialogForm)

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

        // 查询数据
        onSubmit() {
            this.listQuery.pageNum = 1
            this.fetchData()
        },

        // 重置查询
        onReset() {
            this.listQuery = {
                code: undefined,
                type: undefined,
                status: undefined,
                pageNum: 1,
                pageSize: 10
            }
            this.fetchData()
        },

        // 导出数据
        exportTable(type) {
            if (this.tableData.length) {
                const params = {
                    header: ['ID', '车位编号', '车位类型', '车位位置', '使用状态', '创建时间', '更新时间'],
                    key: ['id', 'code', 'type', 'location', 'status', 'createdAt', 'updatedAt'],
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '车位数据表',
                    bookType: type
                }
                excel.exportDataToExcel(params)
                this.exportVisible = false
            } else {
                this.$message.warning('暂无数据可导出')
            }
        },

        // 格式化日期时间
        formatDateTime(date) {
            if (!date) return '-'
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

    .menu-card {
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
            gap: 16px;

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
            margin-bottom: 20px;
            
            .el-button {
                padding: 6px 12px;
            }
        }
    }

    .export-data {
        padding: 20px;
        display: flex;
        justify-content: space-around;

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
                color: #409eff;
                margin-bottom: 8px;
            }

            span {
                display: block;
                color: #606266;
            }
        }
    }
}

// 响应式适配
@media screen and (max-width: 768px) {
    .wrapper {
        padding: 10px;

        .page-header {
            margin-bottom: 20px;

            .header-content {
                h2 {
                    font-size: 24px;
                }

                .subtitle {
                    font-size: 14px;
                }
            }
        }

        .menu-card {
            .control-btns {
                flex-direction: column;
                gap: 16px;

                .left-btns,
                .right-btns {
                    width: 100%;
                    justify-content: space-between;
                }
            }

            .search-form {
                flex-direction: column;
                align-items: stretch;

                .el-form-item {
                    margin-right: 0;
                    
                    .el-input,
                    .el-select {
                        width: 100%;
                    }
                }
            }
        }
    }
}
</style> 