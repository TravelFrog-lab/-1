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
<el-table-column prop="updatedAt" label="更新时间" width="180">
    <template slot-scope="scope">
        {{ scope.row.updatedAt ? moment(scope.row.updatedAt).format('YYYY-MM-DD HH:mm:ss') : '-' }}
    </template>
</el-table-column>

<el-form-item label="状态" prop="status">
    <el-select v-model="dialogForm.status" placeholder="请选择状态">
        <el-option label="正常" value="ENABLED" />
        <el-option label="禁用" value="DISABLED" />
    </el-select>
</el-form-item>

dialogForm: {
    id: undefined,
    username: '',
    name: '',
    phone: '',
    role: '',
    sex: '',
    status: 'ENABLED',
    password: ''
}, 

<script>
import excel from '../utils/excel.js'
import Pagination from '../components/Pagination/index.vue'
import Request from '@utils/request.js'
import moment from 'moment'

export default {
    methods: {
        fetchData({ page, limit } = {}) {
            if (page) this.listQuery.currentPage = page
            if (limit) this.listQuery.pageSize = limit
            
            this.listLoading = true
            Request.get("/user/page", {
                params: {
                    ...this.listQuery
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

        exportTable(type) {
            if (this.tableData.length) {
                const exportData = this.tableData.map(item => ({
                    ...item,
                    createdAt: item.createdAt ? moment(item.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-',
                    updatedAt: item.updatedAt ? moment(item.updatedAt).format('YYYY-MM-DD HH:mm:ss') : '-'
                }))
                
                const params = {
                    header: ['ID', '用户名', '姓名', '手机号', '角色', '性别', '状态', '创建时间', '更新时间'],
                    key: ['id', 'username', 'name', 'phone', 'role', 'sex', 'status', 'createdAt', 'updatedAt'],
                    data: exportData,
                    autoWidth: true,
                    fileName: '用户数据表',
                    bookType: type
                }
                excel.exportDataToExcel(params)
                this.exportVisible = false
            }
        },

        moment(date) {
            return moment(date, moment.ISO_8601)
        }
    }
}
</script> 