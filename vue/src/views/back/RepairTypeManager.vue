<template>
    <div class="wrapper">
        <div class="page-header">
            <div class="header-content">
                <h2>维修类型管理</h2>
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
                    <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增类型</el-button>
                </div>
            </div>

            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="类型名称">
                    <el-input v-model="listQuery.name" placeholder="请输入类型名称" size="medium" />
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
                <el-table-column prop="name" label="类型名称"></el-table-column>
                <el-table-column prop="description" label="描述"></el-table-column>
                <el-table-column prop="price" label="定价">
                    <template slot-scope="scope">
                        ¥{{ scope.row.price }}
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
                :title="dialogForm.id ? '编辑维修类型' : '新增维修类型'" 
                :visible.sync="formVisible" 
                width="500px"
                :close-on-click-modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="类型名称" prop="name">
                        <el-input v-model="dialogForm.name" placeholder="请输入类型名称"></el-input>
                    </el-form-item>
                    <el-form-item label="定价" prop="price">
                        <el-input-number 
                            v-model="dialogForm.price" 
                            :precision="2" 
                            :step="10"
                            :min="0"
                            controls-position="right"
                            placeholder="请输入定价">
                        </el-input-number>
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
    name: 'RepairTypeManager',
    components: { Pagination },
    data() {
        return {
            listLoading: true,
            formLabelWidth: '80px',

            // 查询列表参数对象
            listQuery: {
                name: undefined,
                pageNum: 1,
                pageSize: 10
            },

            // 新增/编辑提交表单对象
            dialogForm: {
                id: undefined,
                name: '',
                description: '',
                price: 0
            },

            total: 0,
            tableData: [],
            multipleSelection: [],
            formVisible: false,
            
            formRules: {
                name: [
                    { required: true, message: '类型名称不能为空', trigger: 'blur' },
                    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
                ],
                price: [
                    { required: true, message: '定价不能为空', trigger: 'blur' }
                ],
                description: [
                    { max: 200, message: '描述内容不能超过200个字符', trigger: 'blur' }
                ]
            },

            isSubmit: false,
            exportVisible: false
        }
    },
    created() {
        this.fetchData()
    },
    methods: {
        handleSelectionChange(val) {
            this.multipleSelection = val.map(v => v.id)
        },

        handleCreate() {
            this.dialogForm = {
                name: '',
                description: '',
                price: 0
            };
            this.formVisible = true;
        },

        handleEdit(row) {
            this.dialogForm = JSON.parse(JSON.stringify(row));
            this.formVisible = true;
        },

        handleDelete(row) {
            this.$confirm('此操作将删除该维修类型, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/repair-types/" + row.id).then(response => {
                    if (response.code === '0') {
                        this.$message({
                            type: 'success',

                            message: '删除成功!'
                        });
                        this.fetchData();
                    } else {
                        this.$message({
                            type: 'error',
                            message: response.msg
                        });
                    }
                });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });
            });
        },

        batchDelete() {
            if (!this.multipleSelection || this.multipleSelection.length < 1) {
                this.$message({
                    message: '请先选择要删除的数据！',
                    type: 'warning'
                });
                return;
            }

            this.$confirm('此操作将删除选中的维修类型, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete('/repair-types/batch', {
                    data: this.multipleSelection
                }).then(res => {
                    if (res.code === '0') {

                        this.$message({
                            type: 'success',
                            message: '批量删除成功'
                        });
                        this.$refs.multipleTable.clearSelection();
                        this.multipleSelection = [];
                        this.fetchData();
                    } else {
                        this.$message({
                            type: 'error',
                            message: res.msg
                        });
                    }
                });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });
            });
        },

        fetchData({ page, limit } = {}) {
            if (page) this.listQuery.pageNum = page;
            if (limit) this.listQuery.pageSize = limit;
            
            this.listLoading = true;
            Request.get("/repair-types/page", {
                params: {
                    name: this.listQuery.name,
                    pageNum: this.listQuery.pageNum,
                    pageSize: this.listQuery.pageSize
                }
            }).then(response => {
                if (response.code === '0') {
                    const { total, records } = response.data;
                    this.total = total;
                    this.tableData = records;
                } else {
                    this.$message({
                        type: 'error',
                        message: response.msg || '获取数据失败'
                    });
                }
                this.listLoading = false;
            }).catch(() => {
                this.listLoading = false;
            });
        },

        onSubmit() {
            this.listQuery.pageNum = 1;
            this.fetchData();
        },

        onReset() {
            this.listQuery = {
                name: undefined,
                pageNum: 1,
                pageSize: 10
            };
            this.fetchData();
        },

        handleSave(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) {
                    return false;
                }
                
                this.isSubmit = true;
                const request = this.dialogForm.id 
                    ? Request.put("/repair-types", this.dialogForm)
                    : Request.post("/repair-types", this.dialogForm);


                request.then(response => {
                    if (response.code === '0') {
                        this.$message({
                            type: 'success',
                            message: this.dialogForm.id ? '更新成功' : '添加成功'
                        });
                        this.formVisible = false;
                        this.fetchData();
                    } else {
                        this.$message({
                            type: 'error',
                            message: response.msg
                        });
                    }
                }).finally(() => {
                    this.isSubmit = false;
                });
            });
        },

        exportTable(type) {
            if (this.tableData.length) {
                const params = {
                    header: ['ID', '类型名称', '描述', '定价', '创建时间', '更新时间'],
                    key: ['id', 'name', 'description', 'price', 'createdAt', 'updatedAt'],
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '维修类型数据表',
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
                margin-bottom: 8px;
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