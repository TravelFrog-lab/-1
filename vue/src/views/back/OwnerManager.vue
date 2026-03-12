<template>
    <div class="wrapper">
        <!-- 页面标题区 -->
        <div class="page-header">
            <div class="header-content">
                <h2>业主管理</h2>
                <div class="divider"></div>
                <p class="subtitle">Owner Management</p>
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
                    <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增业主</el-button>
                </div>
            </div>

            <!-- 查询表单区 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="身份证号">
                    <el-input v-model="listQuery.idCard" placeholder="请输入身份证号" size="medium" />
                </el-form-item>
                <el-form-item label="车牌号">
                    <el-input v-model="listQuery.plateNumber" placeholder="请输入车牌号" size="medium" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" size="medium" plain icon="el-icon-search" @click="onSubmit">查询</el-button>
                    <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="onReset">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 表格区 -->
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
                <el-table-column type="expand" width="50">
                    <template v-slot="props">
                        <el-form label-position="left" inline class="expanded-form">
                            <div class="expand-section">
                                <h4><i class="el-icon-user"></i> 家庭成员信息</h4>
                                <el-table
                                    v-if="props.row.familyMembers"
                                    :data="JSON.parse(props.row.familyMembers)"
                                    size="small"
                                    border
                                    style="width: 100%">
                                    <el-table-column prop="name" label="姓名"></el-table-column>
                                    <el-table-column prop="relation" label="关系"></el-table-column>
                                    <el-table-column prop="idCard" label="身份证号"></el-table-column>
                                    <el-table-column prop="phone" label="联系电话"></el-table-column>
                                </el-table>
                                <div v-else class="no-data">暂无家庭成员信息</div>
                            </div>

                            <div class="expand-section">
                                <h4><i class="el-icon-info"></i> 其他信息</h4>
                                <div class="info-grid">
                                    <div class="info-item">
                                        <span class="label">宠物信息：</span>
                                        <span class="value">{{ props.row.petInfo || '无' }}</span>
                                    </div>
                                    <div class="info-item">
                                        <span class="label">备注信息：</span>
                                        <span class="value">{{ props.row.remarks || '无' }}</span>
                                    </div>
                                </div>
                            </div>
                        </el-form>
                    </template>
                </el-table-column>
                <el-table-column type="selection" width="50"></el-table-column>
                <el-table-column prop="id" label="ID" width="80"></el-table-column>
                <el-table-column prop="user.username" label="用户名"></el-table-column>
                <el-table-column prop="user.name" label="姓名"></el-table-column>
                <el-table-column prop="user.phone" label="手机号"></el-table-column>
                <el-table-column prop="idCard" label="身份证号"></el-table-column>
                <el-table-column prop="house.address" label="房屋地址"></el-table-column>
                <el-table-column prop="plateNumber" label="车牌号"></el-table-column>
                <el-table-column prop="workplace" label="工作单位"></el-table-column>
                <el-table-column prop="checkInTime" label="入住时间" width="180"></el-table-column>
                <el-table-column prop="decorationStatus" label="装修情况">
                    <template slot-scope="scope">
                        <el-tag :type="decorationStatusType(scope.row.decorationStatus)">
                            {{ decorationStatusText(scope.row.decorationStatus) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态">
                    <template slot-scope="scope">
                        <el-switch
                            v-model="scope.row.status"
                            :active-value="'ENABLED'"
                            :inactive-value="'DISABLED'"
                            @change="handleStatusChange(scope.row)"
                        ></el-switch>
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
                :title="dialogForm.id ? '编辑业主' : '新增业主'" 
                :visible.sync="formVisible" 
                width="500px"
                :close-on-click-modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="用户" prop="userId">
                        <el-select v-model="dialogForm.userId" placeholder="请选择用户" filterable>
                            <el-option
                                v-for="item in userOptions"
                                :key="item.id"
                                :label="item.name"
                                :value="item.id">
                                <span>{{ item.name }} ({{ item.username }})</span>
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="房屋" prop="houseId">
                        <el-select v-model="dialogForm.houseId" placeholder="请选择房屋" filterable>
                            <el-option
                                v-for="item in houseOptions"
                                :key="item.id"
                                :label="item.address"
                                :value="item.id">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="身份证号" prop="idCard">
                        <el-input v-model="dialogForm.idCard" placeholder="请输入身份证号"></el-input>
                    </el-form-item>
                    <el-form-item label="车牌号" prop="plateNumber">
                        <el-input v-model="dialogForm.plateNumber" placeholder="请输入车牌号"></el-input>
                    </el-form-item>
                    <el-form-item label="工作单位" prop="workplace">
                        <el-input v-model="dialogForm.workplace" placeholder="请输入工作单位"></el-input>
                    </el-form-item>
                    <el-form-item label="入住时间" prop="checkInTime">
                        <el-date-picker
                            v-model="dialogForm.checkInTime"
                            type="datetime"
                            placeholder="选择入住时间"
                            format="yyyy-MM-dd HH:mm:ss"
                            value-format="yyyy-MM-dd HH:mm:ss"
                             >
                        </el-date-picker>
                    </el-form-item>
                    <el-form-item label="装修情况" prop="decorationStatus">
                        <el-select v-model="dialogForm.decorationStatus" placeholder="请选择装修情况">
                            <el-option label="未装修" value="NONE"></el-option>
                            <el-option label="简单装修" value="SIMPLE"></el-option>
                            <el-option label="精装修" value="LUXURY"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="宠物信息" prop="petInfo">
                        <el-input type="textarea" v-model="dialogForm.petInfo" placeholder="请输入宠物信息"></el-input>
                    </el-form-item>
                    <el-form-item label="备注信息" prop="remarks">
                        <el-input type="textarea" v-model="dialogForm.remarks" placeholder="请输入备注信息"></el-input>
                    </el-form-item>
                    <el-form-item label="家庭成员">
                        <div class="family-members">
                            <div class="members-list">
                                <div v-for="(member, index) in familyMembers" :key="index" class="member-item">
                                    <div class="member-header">
                                        <span class="member-title">成员 {{ index + 1 }}</span>
                                        <el-button type="text" @click="removeMember(index)" class="delete-btn">
                                            <i class="el-icon-delete"></i>
                                        </el-button>
                                    </div>
                                    <el-row :gutter="8">
                                        <el-col :span="6">
                                            <el-input v-model="member.name" placeholder="姓名"></el-input>
                                        </el-col>
                                        <el-col :span="6">
                                            <el-select v-model="member.relation" placeholder="关系">
                                                <el-option label="配偶" value="配偶"></el-option>
                                                <el-option label="子女" value="子女"></el-option>
                                                <el-option label="父母" value="父母"></el-option>
                                                <el-option label="其他" value="其他"></el-option>
                                            </el-select>
                                        </el-col>
                                        <el-col :span="6">
                                            <el-input v-model="member.idCard" placeholder="身份证号"></el-input>
                                        </el-col>
                                        <el-col :span="6">
                                            <el-input v-model="member.phone" placeholder="联系电话"></el-input>
                                        </el-col>
                                    </el-row>
                                </div>
                            </div>
                            <div class="add-member">
                                <el-button type="primary" @click="addMember" class="add-btn">
                                    <i class="el-icon-plus"></i> 添加家庭成员
                                </el-button>
                            </div>
                        </div>
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

export default {
    name: 'OwnerManager',
    components: { Pagination },
    data() {
        return {
            // 数据列表加载动画
            listLoading: true,
            
            // 查询列表参数对象
            listQuery: {
                idCard: undefined,
                plateNumber: undefined,
                pageNum: 1,
                pageSize: 10
            },

            // 新增/编辑提交表单对象
            dialogForm: {
                id: undefined,
                userId: undefined,
                houseId: undefined,
                idCard: '',
                plateNumber: '',
                workplace: '',
                checkInTime: '',
                decorationStatus: 'NONE',
                petInfo: '',
                remarks: ''
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
            // 用户选项
            userOptions: [],
            // 房屋选项
            houseOptions: [],
            
            // 表单校验规则
            formRules: {
                userId: [
                    { required: true, message: '请选择用户', trigger: 'change' }
                ],
                houseId: [
                    { required: true, message: '请选择房屋', trigger: 'change' }
                ],
                idCard: [
                    { required: true, message: '请输入身份证号', trigger: 'blur' },
                    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
                ],
                plateNumber: [
                    { required: true, message: '请输入车牌号', trigger: 'blur' }
                ],
                checkInTime: [
                    { required: true, message: '请选择入住时间', trigger: 'change' }
                ],
            
            },


            // 防止多次连续提交表单
            isSubmit: false,

            // 家庭成员相关
            familyMembers: [],

            // 展开行相关
            expandRows: []
        }
    },
    created() {
        this.fetchData()
        this.fetchUserOptions()
        this.fetchHouseOptions()
    },
    methods: {
        // 获取用户选项
        fetchUserOptions() {
            Request.get('/user/role/OWNER').then(response => {
                if (response.code === '0') {
                    this.userOptions = response.data
                }
            })
        },

        // 获取房屋选项
        fetchHouseOptions() {
            Request.get('/houses/all').then(response => {
                if (response.code === '0') {
                    this.houseOptions = response.data
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
            Request.get('/owner/page', {
                params: this.listQuery
            }).then(response => {
                if (response.code === '0') {
                    const { total, records } = response.data
                    this.total = total
                    this.tableData = records
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
                idCard: undefined,
                plateNumber: undefined,
                pageNum: 1,
                pageSize: 10
            }
            this.fetchData()
        },

        // 新建数据
        handleCreate() {
            this.dialogForm = {
                userId: undefined,
                houseId: undefined,
                idCard: '',
                plateNumber: '',
                workplace: '',
                checkInTime: '',
                decorationStatus: 'NONE',
                petInfo: '',
                remarks: ''
            }
            this.familyMembers = []
            this.formVisible = true
        },

        // 编辑数据
        handleEdit(row) {
            this.dialogForm = JSON.parse(JSON.stringify(row))
            // 解析家庭成员JSON字符串
            try {
                this.familyMembers = row.familyMembers ? JSON.parse(row.familyMembers) : []
            } catch (e) {
                this.familyMembers = []
                console.error('解析家庭成员数据失败:', e)
            }
            this.formVisible = true
        },

        // 删除数据
        handleDelete(row) {
            this.$confirm('此操作将删除该业主信息, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete('/owner/' + row.id).then(response => {
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
            })
        },

        // 批量删除
        batchDelete() {
            if (!this.multipleSelection.length) {
                this.$message({
                    message: '请选择要删除的数据',
                    type: 'warning'
                })
                return
            }

            this.$confirm('此操作将删除选中的业主信息, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete('/owner/batch', {
                    data: this.multipleSelection
                }).then(response => {
                    if (response.code === '0') {
                        this.$message({
                            type: 'success',
                            message: '批量删除成功!'
                        })
                        this.$refs.multipleTable.clearSelection()
                        this.fetchData()
                    } else {
                        this.$message({
                            type: 'error',
                            message: response.msg
                        })
                    }
                })
            })
        },

        // 保存数据
        handleSave(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) return false
                
                // 验证家庭成员信息
                if (this.familyMembers.length > 0) {
                    const isValid = this.validateFamilyMembers()
                    if (!isValid) return false
                }
                
                this.isSubmit = true
                // 构造提交数据，包含家庭成员
                const submitData = {
                    ...this.dialogForm,
                    familyMembers: JSON.stringify(this.familyMembers)
                }

                const request = this.dialogForm.id 
                    ? Request.put('/owner/' + this.dialogForm.id, submitData)
                    : Request.post('/owner', submitData)

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

        // 添加家庭成员验证方法
        validateFamilyMembers() {
            for (let i = 0; i < this.familyMembers.length; i++) {
                const member = this.familyMembers[i]
                if (!member.name || !member.relation || !member.idCard || !member.phone) {
                    this.$message.error(`请完整填写第${i + 1}个家庭成员的信息`)
                    return false
                }
                // 验证身份证号
                if (!/^(\d{15}$)|(^\d{18})|(^\d{17}(\d|X|x))$/.test(member.idCard)) {
                    this.$message.error(`第${i + 1}个家庭成员的身份证号格式不正确`)
                    return false
                }
                // 验证手机号
                if (!/^1[3-9]\d{9}$/.test(member.phone)) {
                    this.$message.error(`第${i + 1}个家庭成员的手机号格式不正确`)
                    return false
                }
            }
            return true
        },

        // 导出数据
        exportTable(type) {
            if (this.tableData.length) {
                const params = {
                    header: ['ID', '用户名', '姓名', '手机号', '身份证号', '房屋地址', '车牌号', '工作单位', '入住时间', '装修情况', '状态'],
                    key: ['id', 'user.username', 'user.name', 'user.phone', 'idCard', 'house.address', 'plateNumber', 'workplace', 'checkInTime', 'decorationStatus', 'status'],
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '业主数据表',
                    bookType: type
                }
                excel.exportDataToExcel(params)
                this.exportVisible = false
            }
        },

        // 装饰状态类型
        decorationStatusType(status) {
            const statusMap = {
                NONE: '',
                SIMPLE: 'success',
                LUXURY: 'warning'
            }
            return statusMap[status] || ''
        },

        // 装饰状态文本
        decorationStatusText(status) {
            const statusMap = {
                NONE: '未装修',
                SIMPLE: '简单装修',
                LUXURY: '精装修'
            }
            return statusMap[status] || ''
        },

        // 修改处理状态变化方法
        handleStatusChange(row) {
            this.$confirm('此操作将更改该业主状态, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                // 构造更新数据
                const updateData = {
                    ...row,
                    status: row.status
                }
                
                Request.put('/owner/' + row.id, updateData).then(response => {
                    if (response.code === '0') {
                        this.$message({
                            type: 'success',
                            message: '状态更新成功!'
                        })
                        this.fetchData()
                    } else {
                        // 更新失败时恢复原状态
                        row.status = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
                        this.$message({
                            type: 'error',
                            message: response.msg
                        })
                    }
                }).catch(() => {
                    // 发生错误时恢复原状态
                    row.status = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
                })
            }).catch(() => {
                // 取消操作时恢复原状态
                row.status = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
            })
        },

        // 添加家庭成员
        addMember() {
            this.familyMembers.push({
                name: '',
                relation: '',
                idCard: '',
                phone: ''
            })
        },

        // 移除家庭成员
        removeMember(index) {
            this.familyMembers.splice(index, 1)
        },

        // 修改展开行操作方法
        handleExpandChange(row, expanded) {
            // 如果是展开操作，就只保留当前展开的行
            // 如果是收起操作，就清空展开行数组
            this.expandRows = expanded ? [row.id] : []
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
                margin-bottom: 8px;
                color: #409EFF;
            }

            span {
                display: block;
                color: #606266;
            }
        }
    }

    .family-members {
        .members-list {
            .member-item {
                margin-bottom: 16px;
                .member-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 8px;
                    .member-title {
                        font-weight: 500;
                    }
                    .delete-btn {
                        color: #f56c6c;
                        background: none;
                        border: none;
                        padding: 0;
                        font: inherit;
                        cursor: pointer;
                        outline: inherit;
                    }
                }
            }
        }
        .add-member {
            text-align: right;
            margin-top: 16px;
            .add-btn {
                background-color: #409EFF;
                color: #fff;
                border: none;
                padding: 8px 16px;
                border-radius: 4px;
                cursor: pointer;
            }
        }
    }

    .expanded-form {
        padding: 20px 40px;
        
        .expand-section {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 4px;
            margin-bottom: 20px;
            
            &:last-child {
                margin-bottom: 0;
            }

            h4 {
                margin: 0 0 16px;
                color: #1e4976;
                font-size: 16px;
                display: flex;
                align-items: center;
                gap: 8px;

                i {
                    color: #1976d2;
                }
            }

            .el-table {
                margin-top: 12px;
            }
        }

        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 16px;
            padding: 12px;

            .info-item {
                .label {
                    color: #606266;
                    font-weight: 500;
                }

                .value {
                    color: #303133;
                    margin-left: 8px;
                }
            }
        }
    }

    .no-data {
        text-align: center;
        color: #909399;
    }
}
</style> 