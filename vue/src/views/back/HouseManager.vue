<template>
    <div class="wrapper">
        <!-- 页面标题 -->
        <div class="page-header">
            <div class="header-content">
                <h2>房屋管理</h2>
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
                    <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增房屋</el-button>
                </div>
            </div>

            <!-- 查询栏 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="房屋地址">
                    <el-input v-model="listQuery.address" placeholder="请输入房屋地址" size="medium" />
                </el-form-item>
                <el-form-item label="房屋状态">
                    <el-select v-model="listQuery.status" placeholder="请选择状态" size="medium">
                        <el-option label="空置" value="VACANT" />
                        <el-option label="已入住" value="OCCUPIED" />
                        <el-option label="已出租" value="RENTED" />
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
                <el-table-column prop="buildingNo" label="楼栋" width="90"></el-table-column>
                <el-table-column prop="unitNo" label="单元" width="90"></el-table-column>
                <el-table-column prop="roomNo" label="房号" width="100"></el-table-column>
                <el-table-column prop="address" label="完整地址" min-width="180"></el-table-column>
                <el-table-column prop="houseType.name" label="房屋类型"></el-table-column>
                <el-table-column prop="area" label="面积(㎡)" width="100"></el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                    <template slot-scope="scope">
                        <el-tag :type="getStatusType(scope.row.status)">
                            {{ getStatusText(scope.row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>
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
                :title="dialogForm.id ? '编辑房屋' : '新增房屋'" 
                :visible.sync="formVisible" 
                width="500px"
                :close-on-click-modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="楼栋" prop="buildingNo">
                        <el-input v-model="dialogForm.buildingNo" placeholder="如：1"></el-input>
                    </el-form-item>
                    <el-form-item label="单元" prop="unitNo">
                        <el-input v-model="dialogForm.unitNo" placeholder="如：2"></el-input>
                    </el-form-item>
                    <el-form-item label="房号" prop="roomNo">
                        <el-input v-model="dialogForm.roomNo" placeholder="如：301"></el-input>
                    </el-form-item>
                    <el-form-item label="房屋类型" prop="houseTypeId">
                        <el-select
                            v-model="dialogForm.houseTypeId"
                            placeholder="请选择房屋类型"
                            style="width: 100%"
                            :disabled="!!dialogForm.id"
                            @change="onHouseTypeChange">
                            <el-option
                                v-for="item in houseTypes"
                                :key="item.id"
                                :label="item.name"
                                :value="item.id">
                            </el-option>
                        </el-select>
                        <div v-if="dialogForm.id" class="field-tip">
                            <i class="el-icon-lock"></i>
                            <span>已建档，房屋类型不可修改</span>
                        </div>
                    </el-form-item>
                    <el-form-item label="房屋面积" prop="area">
                        <el-input-number
                            v-model="dialogForm.area"
                            :precision="2"
                            :step="0.1"
                            :min="0"
                            style="width: 100%"
                            :disabled="true"
                            placeholder="随房屋类型自动带出">
                        </el-input-number>
                        <div class="field-tip">
                            <i class="el-icon-info"></i>
                            <span>面积由所选房屋类型决定，不可手动修改</span>
                        </div>
                    </el-form-item>
                    <el-form-item label="房屋状态" prop="status">
                        <el-select v-model="dialogForm.status" placeholder="请选择状态" style="width: 100%" :disabled="!!dialogForm.id">
                            <el-option label="空置" value="VACANT" />
                            <el-option label="已入住" value="OCCUPIED" />
                            <el-option label="已出租" value="RENTED" />
                        </el-select>
                        <div v-if="dialogForm.id" class="status-tip">
                            <i class="el-icon-info"></i>
                            <span>编辑模式下不可修改房屋状态</span>
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
import moment from 'moment'
import { getHouseTypeReferenceAreaM2ByName } from '@/constants/houseTypeReferenceArea'

export default {
    name: 'HouseManager',
    components: { Pagination },
    data() {
        return {
            // 数据列表加载动画
            listLoading: true,
            
            // 查询列表参数对象
            listQuery: {
                address: undefined,
                status: undefined,
                pageNum: 1,
                pageSize: 10
            },

            // 新增/编辑提交表单对象
            dialogForm: {
                id: undefined,
                buildingNo: '',
                unitNo: '',
                roomNo: '',
                houseTypeId: undefined,
                area: undefined,
                status: 'VACANT'
            },

            // 房屋类型列表
            houseTypes: [],

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
                buildingNo: [
                    { required: true, message: '楼栋不能为空', trigger: 'blur' }
                ],
                unitNo: [
                    { required: true, message: '单元不能为空', trigger: 'blur' }
                ],
                roomNo: [
                    { required: true, message: '房号不能为空', trigger: 'blur' }
                ],
                houseTypeId: [
                    { required: true, message: '请选择房屋类型', trigger: 'change' }
                ],
                area: [
                    { required: true, message: '请先选择房屋类型以带出面积', trigger: 'change' },
                    { type: 'number', message: '面积必须为数字', trigger: 'blur' }
                ],
                status: [
                    { required: true, message: '请选择房屋状态', trigger: 'change' }
                ]
            },

            // 防止多次连续提交表单
            isSubmit: false,

            // 导出对话框显示控制
            exportVisible: false
        }
    },
    created() {
        this.fetchData()
        this.loadHouseTypes()
    },
    methods: {
        // 加载房屋类型数据
        loadHouseTypes() {
            Request.get("/house-types/page", {
                params: {
                    pageNum: 1,
                    pageSize: 100
                }
            }).then(response => {
                if (response.code === '0') {
                    this.houseTypes = response.data.records;
                }
            });
        },

        // 获取状态显示文本
        getStatusText(status) {
            const statusMap = {
                'VACANT': '空置',
                'OCCUPIED': '已入住',
                'RENTED': '已出租'
            }
            return statusMap[status] || status
        },

        // 获取状态标签类型
        getStatusType(status) {
            const typeMap = {
                'VACANT': 'info',
                'OCCUPIED': 'success',
                'RENTED': 'warning'
            }
            return typeMap[status] || ''
        },

        // 多选操作
        handleSelectionChange(val) {
            this.multipleSelection = val.map(v => v.id)
        },

        resolveAreaFromHouseType(t) {
            if (!t) return undefined
            if (t.referenceArea != null && t.referenceArea !== '') {
                return Number(t.referenceArea)
            }
            const m = getHouseTypeReferenceAreaM2ByName(t.name)
            return m != null ? m : undefined
        },
        onHouseTypeChange(val) {
            const t = this.houseTypes.find(x => x.id === val)
            this.dialogForm.area = this.resolveAreaFromHouseType(t)
        },
        // 新建数据
        handleCreate() {
            this.dialogForm = {
                buildingNo: '',
                unitNo: '',
                roomNo: '',
                houseTypeId: undefined,
                area: undefined,
                status: 'VACANT'
            };
            this.formVisible = true;
        },

        // 编辑数据
        handleEdit(row) {
            const copy = JSON.parse(JSON.stringify(row))
            this.dialogForm = {
                id: copy.id,
                buildingNo: copy.buildingNo || '',
                unitNo: copy.unitNo || '',
                roomNo: copy.roomNo || '',
                houseTypeId: copy.houseTypeId,
                area: copy.area != null ? Number(copy.area) : undefined,
                status: copy.status
            }
            this.formVisible = true
        },

        // 删除数据
        handleDelete(row) {
            this.$confirm('此操作将删除该房屋信息, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/houses/" + row.id).then(response => {
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

            this.$confirm('此操作将删除选中的房屋信息, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete('/houses/batch', {
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
            Request.get("/houses/page", {
                params: {
                    address: this.listQuery.address,
                    status: this.listQuery.status,
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
                address: undefined,
                status: undefined,
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
                const submitData = {
                    ...this.dialogForm,
                    // 兼容旧后端：即使后端还未升级，也能靠 address 正常入库
                    address: `${this.dialogForm.buildingNo}栋-${this.dialogForm.unitNo}单元-${this.dialogForm.roomNo}`
                }
                const request = this.dialogForm.id 
                    ? Request.put("/houses", submitData)
                    : Request.post("/houses", submitData);


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
                    header: ['ID', '楼栋', '单元', '房号', '完整地址', '房屋类型', '面积', '状态', '创建时间'],
                    key: ['id', 'buildingNo', 'unitNo', 'roomNo', 'address', 'houseType.name', 'area', 'status', 'createdAt'],
                    data: this.tableData,
                    autoWidth: true,
                    fileName: '房屋信息表',
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
                margin-bottom: 10px;
                color: #409EFF;
            }

            span {
                display: block;
                color: #606266;
            }
        }
    }

    .status-tip {
        margin-top: 5px;
        font-size: 12px;
        color: #909399;
        display: flex;
        align-items: center;
        
        i {
            margin-right: 4px;
            color: #E6A23C;
        }
    }

    .field-tip {
        margin-top: 6px;
        font-size: 12px;
        color: #909399;
        display: flex;
        align-items: center;
        line-height: 1.4;

        i {
            margin-right: 4px;
            color: #909399;
        }
    }
}
</style> 