<template>
    <div class="wrapper">
        <div class="page-header">
            <div class="header-content">
                <h2>图标管理</h2>
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
                    <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增图标</el-button>
                </div>
            </div>

            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="图标名称">
                    <el-input v-model="listQuery.itemKey" placeholder="请输入图标名称" size="medium" />
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
                <el-table-column prop="itemKey" label="名称"></el-table-column>
                <el-table-column prop="itemValue" label="值"></el-table-column>
                <el-table-column label="图标" align="center" width="100">
                    <template slot-scope="scope">
                        <i :class="scope.row.itemValue" style="font-size: 20px;" />
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="描述"></el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
                <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
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
                :page.sync="listQuery.currentPage"
                :limit.sync="listQuery.pageSize"
                @pagination="fetchData"
            />

            <!-- 新增/编辑弹窗 -->
            <el-dialog 
                :title="dialogForm.id ? '编辑图标' : '新增图标'" 
                :visible.sync="formVisible" 
                width="500px"
                :close-on-click-modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="图标名称" prop="itemKey">
                        <el-input v-model="dialogForm.itemKey" placeholder="请输入图标名称"></el-input>
                    </el-form-item>
                    <el-form-item label="图标值" prop="itemValue">
                        <el-input v-model="dialogForm.itemValue" placeholder="请输入图标值">
                            <template slot="append">
                                <i :class="dialogForm.itemValue" style="font-size: 16px;" v-if="dialogForm.itemValue"/>
                            </template>
                        </el-input>
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
import excel from '../../utils/excel.js'
import Pagination from '../../components/Pagination/index.vue'
import Upload from '../../components/Upload/index.vue'
import Hints from '../../components/Hints/index.vue'
import Request from '../../utils/request.js'
export default {
    name: 'IconDict',
    components: { Pagination, Upload, Hints },
    data() {
        return {
            // 数据列表加载动画
            listLoading: true,
            formLabelWidth: '80px',

            // 查询列表参数对象
            listQuery: {
                itemKey: undefined,
                currentPage: 1,
                pageSize: 10
            },
            // 新增/编辑提交表单对象
            dialogForm: {
                id: undefined, // 需要有id字段以区分新增和编辑
                dictTypeCode: 'icon', // 字典类型Code，这里假设是固定的
                itemKey: '',
                itemValue: '',
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
            // 导入数据 弹出框 关闭事件
            // 表单校验 trigger: ['blur', 'change'] 为多个事件触发
            formRules: {
                itemKey: [
                    { required: true, message: '字典键不能为空', trigger: 'blur' },
                    { min: 1, max: 20, message: '字典键长度必须在1到20个字符之间', trigger: 'blur' }
                ],
                itemValue: [
                    { required: true, message: '字典值不能为空', trigger: 'blur' },
                    { min: 1, max: 50, message: '字典值长度必须在1到50个字符之间', trigger: 'blur' }
                ],

                description: [
                    { max: 100, message: '描述内容不能超过100个字符', trigger: 'blur' }
                ],

            },
            // 防止多次连续提交表单
            isSubmit: false,

            // 导出文件格式
            filesFormat: '.txt, .csv, .xls, .xlsx',
            // 导出数据 弹出框显示/隐藏
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
                dictTypeCode: 'icon',
                itemKey: '',
                itemValue: '',
                description: ''
            };
            this.formVisible = true;
        },
        // 编辑数据
        handleEdit(row) {
            this.dialogForm = JSON.parse(JSON.stringify(row));
            this.dialogForm.updateTime = undefined
            this.dialogForm.createTime = undefined
            this.formVisible = true;
        },
        cancel() {
            this.$message.success('取消操作成功');
        },
        // 删除数据
        handleDelete(row) {
            this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/dictitem/deleteById/" + row.id).then(response => {
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

            this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                // 将字符串ID转换为数字数组
                const idList = this.multipleSelection.map(id => parseInt(id));
                
                Request.delete('/dictitem/deleteBatch', {
                    data: idList  // 直接传递ID数组作为请求体
                }).then(res => {
                    if (res.code === '0') {
                        this.$message({
                            showClose: true,
                            message: '批量删除成功',
                            type: 'success'
                        });
                        // 清空选中数据
                        this.$refs.multipleTable.clearSelection();
                        this.multipleSelection = [];
                        this.fetchData();
                    } else {
                        this.$message({
                            showClose: true,
                            message: res.msg,
                            type: 'error',
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
            if (page) this.listQuery.currentPage = page;
            if (limit) this.listQuery.pageSize = limit;
            
            this.listLoading = true;
            Request.get("/dictitem/findPage", {
                params: {
                    code: "icon",
                    itemKey: this.listQuery.itemKey,
                    currentPage: this.listQuery.currentPage,
                    size: this.listQuery.pageSize,
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
            this.listQuery.currentPage = 1;
            this.fetchData();
        },
        onReset() {
            this.listQuery = {
                itemKey: undefined,
                currentPage: 1,
                pageSize: 10
            };
            this.fetchData();
        },

        // 统一保存方法
        handleSave(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) {
                    return false;
                }
                
                this.isSubmit = true;
                const request = this.dialogForm.id 
                    ? Request.put("/dictitem/" + this.dialogForm.id, this.dialogForm)
                    : Request.post("/dictitem/save", this.dialogForm);

                request.then(response => {
                    if (response.code === '0') {
                        this.$message({
                            showClose: true,
                            message: this.dialogForm.id ? '更新成功' : '添加成功',
                            type: 'success',
                        });
                        this.formVisible = false;
                        this.fetchData();
                    } else {
                        this.$message({
                            showClose: true,
                            message: response.msg,
                            type: 'error',

                        });
                    }
                }).finally(() => {
                    this.isSubmit = false;
                });
            });
        },

        // 导出数据--excle格式
        exportTable(type) {
            if (this.tableData.length) {
                const params = {
                    header: ['菜单ID', '菜单名', '菜单路径', '菜单图标', '描述', '父级菜单ID', '页面路径', '排序', '菜单所属角色'], // 修改表头以匹配类属性
                    key: ['id', 'name', 'path', 'icon', 'description', 'pid', 'pagePath', 'sortNum', 'role'], // 修改key以匹配类属性
                    data: this.tableData, // 假定this.tableData已经是格式化好的数组，每个元素是一个菜单对象
                    autoWidth: true,
                    fileName: '菜单数据表',
                    bookType: type
                }
                excel.exportDataToExcel(params)
                this.exportVisible = false
            }
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
}
</style>