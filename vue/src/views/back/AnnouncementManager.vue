<template>
    <div class="wrapper">
        <div class="page-header">
            <div class="header-content">
                <h2>公告管理</h2>
                <div class="divider"></div>
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
                    <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增公告</el-button>
                </div>
            </div>

            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="公告标题">
                    <el-input v-model="listQuery.title" placeholder="请输入公告标题" size="medium" />
                </el-form-item>
                <el-form-item label="公告类型">
                    <el-select v-model="listQuery.type" placeholder="请选择公告类型" size="medium">
                        <el-option label="全部" value=""></el-option>
                        <el-option label="维修通知" value="REPAIR"></el-option>
                        <el-option label="活动通知" value="ACTIVITY"></el-option>
                        <el-option label="缴费通知" value="PAY"></el-option>
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
                <el-table-column prop="title" label="公告标题" show-overflow-tooltip></el-table-column>
                <el-table-column prop="publisher" label="发布人" width="120"></el-table-column>
                <el-table-column prop="type" label="公告类型" width="120">
                    <template slot-scope="scope">
                        <el-tag :type="getTypeTag(scope.row.type)">{{ getTypeLabel(scope.row.type) }}</el-tag>
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
                :title="dialogForm.id ? '编辑公告' : '新增公告'" 
                :visible.sync="formVisible" 
                width="600px"
                :close-on-click-modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="公告标题" prop="title">
                        <el-input v-model="dialogForm.title" placeholder="请输入公告标题"></el-input>
                    </el-form-item>
                    <el-form-item label="公告类型" prop="type">
                        <el-select v-model="dialogForm.type" placeholder="请选择公告类型" style="width: 100%">
                            <el-option label="维修通知" value="REPAIR"></el-option>
                            <el-option label="活动通知" value="ACTIVITY"></el-option>
                            <el-option label="缴费通知" value="PAY"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="发布人" prop="publisher">
                        <el-input v-model="dialogForm.publisher" placeholder="请输入发布人"></el-input>
                    </el-form-item>
                    <el-form-item label="公告内容" prop="content">
                        <el-input type="textarea" v-model="dialogForm.content" placeholder="请输入公告内容" :rows="6"></el-input>
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
    name: 'AnnouncementManager',
    components: { Pagination },
    data() {
        return {
            listLoading: true,
            formLabelWidth: '80px',

            // 查询列表参数对象
            listQuery: {
                title: undefined,
                type: undefined,
                pageNum: 1,
                pageSize: 10
            },

            // 新增/编辑提交表单对象
            dialogForm: {
                id: undefined,
                title: '',
                content: '',
                publisher: '',
                type: undefined
            },

            // 数据总条数
            total: 0,
            // 表格数据数组
            tableData: [],
            // 多选数据暂存数组
            multipleSelection: [],
            // 新增/编辑 弹出框显示/隐藏
            formVisible: false,
            // 导出数据 弹出框显示/隐藏
            exportVisible: false,
            // 防止多次连续提交表单
            isSubmit: false,

            // 表单校验规则
            formRules: {
                title: [
                    { required: true, message: '公告标题不能为空', trigger: 'blur' },
                    { min: 2, max: 200, message: '标题长度必须在2到200个字符之间', trigger: 'blur' }
                ],
                type: [
                    { required: true, message: '请选择公告类型', trigger: 'change' }
                ],
                publisher: [
                    { required: true, message: '发布人不能为空', trigger: 'blur' },
                    { max: 50, message: '发布人长度不能超过50个字符', trigger: 'blur' }
                ],
                content: [
                    { required: true, message: '公告内容不能为空', trigger: 'blur' }
                ]
            }
        }
    },
    created() {
        this.fetchData()
    },
    methods: {
        // 获取类型标签样式
        getTypeTag(type) {
            const tags = {
                'REPAIR': 'warning',
                'ACTIVITY': 'success',
                'PAY': 'danger'
            }
            return tags[type] || 'info'
        },

        // 获取类型显示文本
        getTypeLabel(type) {
            const labels = {
                'REPAIR': '维修通知',
                'ACTIVITY': '活动通知',
                'PAY': '缴费通知'
            }
            return labels[type] || type
        },

        // 多选操作
        handleSelectionChange(val) {
            this.multipleSelection = val.map(v => v.id)
        },

        // 新建数据
        handleCreate() {
            this.dialogForm = {
                title: '',
                content: '',
                publisher: '',
                type: undefined
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
            this.$confirm('此操作将删除该公告, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/announcements/" + row.id).then(response => {
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

            this.$confirm('此操作将删除选中的公告, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete('/announcements/batch', {
                    data: this.multipleSelection
                }).then(res => {
                    if (res.code === '0') {
                        this.$message({
                            type: 'success',
                            message: '批量删除成功'
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

        // 获取数据列表
        fetchData({ page, limit } = {}) {
            if (page) this.listQuery.pageNum = page
            if (limit) this.listQuery.pageSize = limit
            
            this.listLoading = true
            Request.get("/announcements/page", {
                params: {
                    pageNum: this.listQuery.pageNum,
                    pageSize: this.listQuery.pageSize,
                    title: this.listQuery.title,
                    type: this.listQuery.type
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
                title: undefined,
                type: undefined,
                pageNum: 1,
                pageSize: 10
            }
            this.fetchData()
        },

        // 保存数据
        handleSave(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) {
                    return false
                }
                
                this.isSubmit = true
                const request = this.dialogForm.id 
                    ? Request.put("/announcements" , this.dialogForm)
                    : Request.post("/announcements", this.dialogForm)

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

        // 导出数据
        exportTable(type) {
            if (this.tableData.length) {
                const params = {
                    header: ['ID', '公告标题', '发布人', '公告类型', '公告内容', '创建时间', '更新时间'],
                    key: ['id', 'title', 'publisher', 'type', 'content', 'createdAt', 'updatedAt'],
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '公告数据表',
                    bookType: type
                }
                excel.exportDataToExcel(params)
                this.exportVisible = false
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