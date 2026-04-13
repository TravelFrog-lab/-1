<template>
    <div class="wrapper">
        <!-- 页面标题区 -->
        <div class="page-header">
            <div class="header-content">
                <h2>用户管理</h2>
                <div class="divider"></div>
            </div>
        </div>

        <el-card class="menu-card" shadow="never">
            <!-- 操作按钮区 -->
            <div class="control-btns">
                <div class="left-btns">
                    <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="fetchData">刷新</el-button>
                </div>

                <div class="right-btns">
                    <el-button type="primary" size="medium" icon="el-icon-download" @click="exportVisible = true" plain>导出数据</el-button>
                </div>
            </div>

            <!-- 查询表单区 -->
            <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
                <el-form-item label="用户名">
                    <el-input v-model="listQuery.username" placeholder="请输入用户名" size="medium" />
                </el-form-item>
                <el-form-item label="姓名">
                    <el-input v-model="listQuery.name" placeholder="请输入姓名" size="medium" />
                </el-form-item>
                <el-form-item label="角色">
                    <el-select v-model="listQuery.role" placeholder="请选择角色" size="medium">
                        <el-option label="业主" value="OWNER" />
                        <el-option label="管理员" value="ADMIN" />
                        <el-option label="后勤人员" value="MAINTENANCE" />
                        <el-option label="家政人员" value="KEEPER" />
                    </el-select>
                </el-form-item>
                <el-form-item label="性别">
                    <el-select v-model="listQuery.sex" placeholder="请选择性别" size="medium">
                        <el-option label="男" value="MALE" />
                        <el-option label="女" value="FEMALE" />
                        <el-option label="其他" value="OTHER" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" size="medium" plain icon="el-icon-search" @click="onSubmit">查询</el-button>
                    <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="onReset">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 表格区 -->
            <el-table ref="multipleTable"
                v-loading="listLoading"
                :data="tableData"
                tooltip-effect="dark"
                row-key="id"
                style="width: 100%"
                size="medium"
                :header-cell-style="{background:'#f8f9fa',color:'#606266'}"
                :cell-style="{padding:'12px 0'}"
                border>
                <el-table-column prop="id" label="ID" width="80"></el-table-column>
                <el-table-column prop="username" label="用户名"></el-table-column>
                <el-table-column prop="name" label="姓名"></el-table-column>
                <el-table-column prop="phone" label="手机号"></el-table-column>
                <el-table-column prop="role" label="角色">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : scope.row.role === 'MAINTENANCE' ? 'warning' : scope.row.role === 'KEEPER' ? 'info' : 'success'">
                            {{ scope.row.role === 'ADMIN' ? '管理员' : scope.row.role === 'MAINTENANCE' ? '后勤人员' : scope.row.role === 'KEEPER' ? '家政人员' : '业主' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="sex" label="性别">
                    <template slot-scope="scope">
                        {{ scope.row.sex === 'MALE' ? '男' : scope.row.sex === 'FEMALE' ? '女' : '其他' }}
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态">
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.status === 'ENABLED' ? 'success' : 'info'">
                            {{ scope.row.status === 'ENABLED' ? '正常' : '禁用' }}
                        </el-tag>
                    </template>

                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="180">
                    <template slot-scope="scope">
                        {{ scope.row.createdAt ? moment(scope.row.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-' }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="120" fixed="right">
                    <template slot-scope="scope">
                        <el-button plain size="small" type="primary" icon="el-icon-view" @click="goBusinessDetail(scope.row)">详情</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页区 -->
            <Pagination
                v-show="total>0"
                :total="total"
                :page.sync="listQuery.currentPage"
                :limit.sync="listQuery.pageSize"
                @pagination="fetchData"
            />

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

/** 从后台菜单中解析页面路由 path（与动态路由注册一致） */
function findPathByPagePath (menus, pagePath) {
    if (!Array.isArray(menus)) return null
    for (const m of menus) {
        if (m.pagePath === pagePath && m.path) {
            return m.path.startsWith('/') ? m.path : '/' + m.path
        }
        if (m.children && m.children.length) {
            const p = findPathByPagePath(m.children, pagePath)
            if (p) return p
        }
    }
    return null
}

export default {
    name: 'User',
    components: { Pagination },
    data () {
        return {
            listLoading: true,
            listQuery: {
                username: undefined,
                name: undefined,
                role: undefined,
                sex: undefined,
                currentPage: 1,
                pageSize: 10
            },
            total: 0,
            tableData: [],
            exportVisible: false
        }
    },
    created () {
        this.fetchData()
    },
    methods: {
        /** 与 setRoutes 一致：优先用 Vuex 里已 transform 的菜单 */
        getStoredMenus () {
            const fromStore = this.$store && this.$store.getters && this.$store.getters.userMenuList
            if (Array.isArray(fromStore) && fromStore.length) {
                return fromStore
            }
            try {
                return JSON.parse(sessionStorage.getItem('userMenuList') || '[]')
            } catch (e) {
                return []
            }
        },
        /** 按角色跳转到业主 / 家政 / 后勤管理页，并带上 userId 供目标页打开档案 */
        goBusinessDetail (row) {
            const role = row.role
            const pagePathMap = {
                OWNER: 'OwnerManager',
                KEEPER: 'HousekeeperManager',
                MAINTENANCE: 'MaintenanceStaffManager'
            }
            if (role === 'ADMIN') {
                this.$message.info('管理员账号无关联业务档案，无需跳转')
                return
            }
            const pagePath = pagePathMap[role]
            if (!pagePath) {
                this.$message.warning('未知角色，无法跳转')
                return
            }
            const path = findPathByPagePath(this.getStoredMenus(), pagePath)
            if (!path) {
                this.$message.error('未在菜单中找到对应页面，请检查后台菜单配置（pagePath：' + pagePath + '）')
                return
            }
            this.$router.push({
                path,
                query: {
                    userId: String(row.id)
                }
            })
        },
        fetchData ({ page, limit } = {}) {
            if (page) this.listQuery.currentPage = page
            if (limit) this.listQuery.pageSize = limit

            this.listLoading = true
            Request.get('/user/page', {
                params: {
                    ...this.listQuery,
                    size: this.listQuery.pageSize
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
        onSubmit () {
            this.listQuery.currentPage = 1
            this.fetchData()
        },
        onReset () {
            this.listQuery = {
                username: undefined,
                name: undefined,
                role: undefined,
                sex: undefined,
                currentPage: 1,
                pageSize: 10
            }
            this.fetchData()
        },
        exportTable (type) {
            if (this.tableData.length) {
                const exportData = this.tableData.map(item => ({
                    ...item,
                    createdAt: item.createdAt ? moment(item.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-'
                }))

                const params = {
                    header: ['ID', '用户名', '姓名', '手机号', '角色', '性别', '状态', '创建时间'],
                    key: ['id', 'username', 'name', 'phone', 'role', 'sex', 'status', 'createdAt'],
                    data: exportData,
                    autoWidth: true,
                    fileName: '用户数据表',
                    bookType: type
                }
                excel.exportDataToExcel(params)
                this.exportVisible = false
            }
        },
        moment (date) {
            return moment(date, moment.ISO_8601)
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
        margin-bottom: 24px;

        .el-button {
            padding: 6px 12px;

            & + .el-button {
                margin-left: 8px;
            }
        }
    }

    .export-data {
        display: flex;
        flex-direction: column;
        gap: 16px;

        .export-option {
            display: flex;
            align-items: center;
            padding: 16px;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.3s;
            background-color: #f8f9fa;

            &:hover {
                background-color: #ecf5ff;
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

    .el-dialog {
        .el-form {
            padding: 20px 0;
        }

        .dialog-footer {
            text-align: right;
        }
    }
}

@media screen and (max-width: 768px) {
    .wrapper {
        .search-form {
            .el-form-item {
                margin-right: 0;
                margin-bottom: 16px;
                width: 100%;

                .el-input,
                .el-select {
                    width: 100%;
                }
            }
        }

        .control-btns {
            flex-direction: column;
            gap: 16px;

            .left-btns,
            .right-btns {
                width: 100%;
                justify-content: space-between;
            }
        }
    }
}
</style>
