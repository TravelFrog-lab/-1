<template>
    <div class="wrapper">
        <div class="page-header">
            <div class="header-content">
                <h2>房屋类型管理</h2>
                <div class="divider"></div>
                <p class="subtitle">House Type Management</p>
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
                <el-table-column prop="propertyFee" label="物业费单价">
                    <template slot-scope="scope">
                        {{ scope.row.propertyFee }} 元/㎡
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="描述"></el-table-column>
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
                :title="dialogForm.id ? '编辑房屋类型' : '新增房屋类型'" 
                :visible.sync="formVisible" 
                width="500px"
                :close-on-click-modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="类型名称" prop="name">
                        <el-input v-model="dialogForm.name" placeholder="请输入类型名称"></el-input>
                    </el-form-item>
                    <el-form-item label="物业费单价" prop="propertyFee">
                        <el-input-number 
                            v-model="dialogForm.propertyFee" 
                            :precision="2" 
                            :step="0.1" 
                            :min="0"
                            controls-position="right"
                            style="width: 100%;">
                            <template slot="append">元/㎡</template>
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
    name: 'HouseTypeManager',
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
                propertyFee: 0,
                description: '',
            },

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
                name: [
                    { required: true, message: '类型名称不能为空', trigger: 'blur' },
                    { min: 1, max: 50, message: '类型名称长度必须在1到50个字符之间', trigger: 'blur' }
                ],
                propertyFee: [
                    { required: true, message: '物业费单价不能为空', trigger: 'blur' },
                    { type: 'number', min: 0, message: '物业费单价不能小于0', trigger: 'blur' }
                ],
                description: [
                    { max: 200, message: '描述内容不能超过200个字符', trigger: 'blur' }
                ],
            },

            // 防止多次连续提交表单
            isSubmit: false,

            // 导出相关
            exportVisible: false
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

        // 新建数据
        handleCreate() {
            this.dialogForm = {
                name: '',
                propertyFee: 0,
                description: ''
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
            this.$confirm('此操作将删除该房屋类型, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/house-types/" + row.id).then(response => {
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

        // 批量删除
        batchDelete() {
            if (!this.multipleSelection || this.multipleSelection.length < 1) {
                this.$message({
                    message: '请先选择要删除的数据！',
                    type: 'warning'
                });
                return;
            }

            this.$confirm('此操作将删除选中的房屋类型, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete('/house-types/batch', {
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

        // 获取数据列表
        fetchData({ page, limit } = {}) {
            if (page) this.listQuery.pageNum = page;
            if (limit) this.listQuery.pageSize = limit;
            
            this.listLoading = true;
            Request.get("/house-types/page", {
                params: {
                    name: this.listQuery.name,
                    pageNum: this.listQuery.pageNum,
                    pageSize: this.listQuery.pageSize,
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

        // 查询数据
        onSubmit() {
            this.listQuery.pageNum = 1;
            this.fetchData();
        },

        // 重置查询
        onReset() {
            this.listQuery = {
                name: undefined,
                pageNum: 1,
                pageSize: 10
            };
            this.fetchData();
        },

        // 保存数据
        handleSave(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) {
                    return false;
                }
                
                this.isSubmit = true;
                const request = this.dialogForm.id 
                    ? Request.put("/house-types" , this.dialogForm)
                    : Request.post("/house-types", this.dialogForm);


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

        // 导出数据
        exportTable(type) {
            if (this.tableData.length) {
                const params = {
                    header: ['ID', '类型名称', '物业费单价', '描述', '创建时间'],
                    key: ['id', 'name', 'propertyFee', 'description', 'createdAt'],
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '房屋类型数据表',
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