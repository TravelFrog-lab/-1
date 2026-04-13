<template>
    <div class="wrapper">
        <div class="page-header">
            <div class="header-content">
                <h2>小区活动管理</h2>
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
                    <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增活动</el-button>
                </div>
            </div>

            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="活动名称">
                    <el-input v-model="listQuery.name" placeholder="请输入活动名称" size="medium" />
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
                <el-table-column prop="name" label="活动名称"></el-table-column>
                <el-table-column prop="startTime" label="开始时间" width="180">
                    <template slot-scope="scope">
                        {{ scope.row.startTime | formatDateTime }}
                    </template>
                </el-table-column>
                <el-table-column prop="endTime" label="结束时间" width="180">
                    <template slot-scope="scope">
                        {{ scope.row.endTime | formatDateTime }}
                    </template>
                </el-table-column>
                <el-table-column prop="location" label="活动地点"></el-table-column>
                <el-table-column prop="currentParticipants" label="报名人数">
                    <template slot-scope="scope">
                        {{ scope.row.currentParticipants }}/{{ scope.row.maxParticipants }}
                    </template>
                </el-table-column>
                <el-table-column prop="contactPerson" label="联系人"></el-table-column>
                <el-table-column prop="contactPhone" label="联系电话"></el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                    <template slot-scope="scope">
                        <el-button plain size="small" type="primary" icon="el-icon-edit" 
                            @click="handleEdit(scope.row)"
                            :disabled="isActivityEnded(scope.row)">编辑</el-button>
                        <el-button plain size="small" type="danger" icon="el-icon-delete" 
                            @click="handleDelete(scope.row)"
                            :disabled="hasParticipants(scope.row)">删除</el-button>
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
                :title="dialogForm.id ? '编辑活动' : '新增活动'" 
                :visible.sync="formVisible" 
                width="600px"
                :close-on-click-modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="活动名称" prop="name">
                        <el-input v-model="dialogForm.name" placeholder="请输入活动名称"></el-input>
                    </el-form-item>
                    <el-form-item label="活动时间" prop="timeRange">
                        <el-date-picker
                            v-model="dialogForm.timeRange"
                            type="datetimerange"
                            range-separator="至"
                            value-format="yyyy-MM-dd HH:mm:ss"
                            format="yyyy-MM-dd HH:mm:ss"
                            start-placeholder="开始时间"
                            end-placeholder="结束时间"
                            :default-time="['09:00:00', '18:00:00']">
                        </el-date-picker>
                    </el-form-item>
                    <el-form-item label="活动地点" prop="location">
                        <el-input v-model="dialogForm.location" placeholder="请输入活动地点"></el-input>
                    </el-form-item>
                    <el-form-item label="最大人数" prop="maxParticipants">
                        <el-input-number v-model="dialogForm.maxParticipants" :min="1" :max="999"></el-input-number>
                    </el-form-item>
                    <el-form-item label="联系人" prop="contactPerson">
                        <el-input v-model="dialogForm.contactPerson" placeholder="请输入联系人姓名"></el-input>
                    </el-form-item>
                    <el-form-item label="联系电话" prop="contactPhone">
                        <el-input v-model="dialogForm.contactPhone" placeholder="请输入联系电话"></el-input>
                    </el-form-item>
                    <el-form-item label="活动描述" prop="description">
                        <el-input type="textarea" v-model="dialogForm.description" placeholder="请输入活动描述" :rows="4"></el-input>
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
    name: 'VolunteerActivityManager',
    components: { Pagination },
    filters: {
        formatDateTime(val) {
            return val ? moment(val).format('YYYY-MM-DD HH:mm:ss') : ''
        }
    },
    data() {
        return {
            // 数据列表加载动画
            listLoading: true,

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
                timeRange: [],
                location: '',
                description: '',
                maxParticipants: 20,
                contactPerson: '',
                contactPhone: ''
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
                name: [
                    { required: true, message: '活动名称不能为空', trigger: 'blur' },
                    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
                ],
                timeRange: [
                    { required: true, message: '请选择活动时间', trigger: 'change' }
                ],
                location: [
                    { required: true, message: '活动地点不能为空', trigger: 'blur' }
                ],
                maxParticipants: [
                    { required: true, message: '请设置最大参与人数', trigger: 'blur' }
                ],
                contactPerson: [
                    { required: true, message: '联系人不能为空', trigger: 'blur' }
                ],
                contactPhone: [
                    { required: true, message: '联系电话不能为空', trigger: 'blur' },
                    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
                ],
                description: [
                    { required: true, message: '活动描述不能为空', trigger: 'blur' }
                ]
            }
        }
    },
    created() {
        this.fetchData()
    },
    methods: {
        // 判断活动是否已开始
        isActivityStarted(row) {
            return new Date(row.startTime) <= new Date()
        },

        // 判断活动是否已结束
        isActivityEnded(row) {
            return new Date(row.endTime) <= new Date()
        },

        // 判断活动是否有参与者
        hasParticipants(row) {
            return row.currentParticipants > 0
        },

        // 多选操作
        handleSelectionChange(val) {
            this.multipleSelection = val.map(v => v.id)
        },

        // 新建数据
        handleCreate() {
            this.dialogForm = {
                name: '',
                timeRange: [],
                location: '',
                description: '',
                maxParticipants: 20,
                contactPerson: '',
                contactPhone: ''
            }
            this.formVisible = true
        },

        // 编辑数据
        handleEdit(row) {
            this.dialogForm = {
                ...row,
                timeRange: [
                    // 如果活动已开始,开始时间不可修改
                    this.isActivityStarted(row) ? row.startTime : row.startTime,
                    row.endTime
                ]
            }
            this.formVisible = true
        },

        // 删除数据
        handleDelete(row) {
            this.$confirm('此操作将删除该活动, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/community-activity/" + row.id).then(response => {
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

            this.$confirm('此操作将删除选中活动, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete('/community-activity/batch', {
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
            })
        },

        // 获取数据列表
        fetchData({ page, limit } = {}) {
            if (page) this.listQuery.pageNum = page
            if (limit) this.listQuery.pageSize = limit
            
            this.listLoading = true
            
            const params = {
                name: this.listQuery.name,
                pageNum: this.listQuery.pageNum,
                pageSize: this.listQuery.pageSize
            }

            Request.get("/community-activity/page", { params }).then(response => {
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
                name: undefined,
                pageNum: 1,
                pageSize: 10
            }
            this.fetchData()
        },

        // 保存数据
        handleSave(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) return false
                
                this.isSubmit = true
                
                const data = {
                    ...this.dialogForm
                }

                // // 如果是编辑且活动已开始,保持原开始时间
                // if (this.dialogForm.id && this.isActivityStarted(this.dialogForm)) {
                //     // 确保使用正确的开始时间，不做任何修改
                //     data.startTime = this.dialogForm.startTime
                //     data.endTime = this.dialogForm.timeRange[1]
                // } else {
                    // 使用用户选择的时间范围
                    data.startTime = this.dialogForm.timeRange[0]
                    data.endTime = this.dialogForm.timeRange[1]
                // }
                delete data.timeRange

                const request = this.dialogForm.id 
                    ? Request.put("/community-activity", data)
                    : Request.post("/community-activity", data)

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
                    header: ['ID', '活动名称', '开始时间', '结束时间', '活动地点', '最大人数', '当前人数', '联系人', '联系电话', '活动描述'],
                    key: ['id', 'name', 'startTime', 'endTime', 'location', 'maxParticipants', 'currentParticipants', 'contactPerson', 'contactPhone', 'description'],
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '志愿活动数据表',
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