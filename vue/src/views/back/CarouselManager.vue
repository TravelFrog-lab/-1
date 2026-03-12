<template>
    <div class="wrapper">
        <!-- 页面标题区 -->
        <div class="page-header">
            <div class="header-content">
                <h2>轮播图管理</h2>
                <div class="divider"></div>
                <p class="subtitle">Carousel Management</p>
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
                    <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增轮播图</el-button>
                </div>
            </div>

            <!-- 表格区 -->
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
                <el-table-column prop="title" label="标题"></el-table-column>
                <el-table-column prop="imageUrl" label="图片">
                    <template slot-scope="scope">
                        <el-image 
                            style="width: 200px; height: 86px"
                            :src="getFullImageUrl(scope.row.imageUrl)"
                            fit="cover"
                            :preview-src-list="[getFullImageUrl(scope.row.imageUrl)]">
                        </el-image>
                    </template>
                </el-table-column>
                <el-table-column prop="tag" label="标签" width="120"></el-table-column>
                <el-table-column prop="description" label="描述"></el-table-column>
                <el-table-column prop="sortOrder" label="排序" width="80"></el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="180">
                    <template slot-scope="scope">
                        {{ scope.row.createdAt ? moment(scope.row.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-' }}
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
                :page.sync="listQuery.currentPage"
                :limit.sync="listQuery.pageSize"
                @pagination="fetchData"
            />

            <!-- 新增/编辑弹窗 -->
            <el-dialog 
                :title="dialogForm.id ? '编辑轮播图' : '新增轮播图'" 
                :visible.sync="formVisible" 
                width="700px"
                :close-on-click-modal="false">
                <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
                    <el-form-item label="标题" prop="title">
                        <el-input v-model="dialogForm.title" placeholder="请输入标题"></el-input>
                    </el-form-item>
                    <el-form-item label="图片" prop="imageUrl">
                        <div class="upload-box">
                            <el-upload
                                class="avatar-uploader"
                                action="#"
                                :show-file-list="false"
                                :before-upload="beforeAvatarUpload"
                                :http-request="uploadImage">
                                <img v-if="dialogForm.imageUrl" :src="getFullImageUrl(dialogForm.imageUrl)" class="avatar">
                                <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                            </el-upload>
                            <div class="upload-tip">建议尺寸: 2100 x 900</div>
                        </div>
                    </el-form-item>
                    <el-form-item label="标签" prop="tag">
                        <el-input v-model="dialogForm.tag" placeholder="请输入标签"></el-input>
                    </el-form-item>
                    <el-form-item label="描述" prop="description">
                        <el-input type="textarea" v-model="dialogForm.description" 
                            :rows="3" placeholder="请输入描述"></el-input>
                    </el-form-item>
                    <el-form-item label="排序" prop="sortOrder">
                        <el-input-number v-model="dialogForm.sortOrder" :min="1" :max="99"></el-input-number>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="formVisible = false">取 消</el-button>
                    <el-button type="primary" :loading="isSubmit" @click="handleSave('dialogForm')">确 定</el-button>
                </div>
            </el-dialog>

            <!-- 图片裁剪弹窗 -->
            <el-dialog 
                title="图片裁剪" 
                :visible.sync="cropperVisible" 
                width="800px"
                append-to-body
                :close-on-click-modal="false">
                <div class="cropper-container">
                    <vue-cropper
                        ref="cropper"
                        :img="cropImg"
                        :outputSize="1"
                        :outputType="'png'"
                        :info="true"
                        :full="false"
                        :canMove="false"
                        :canMoveBox="true"
                        :original="false"
                        :autoCrop="true"
                        :fixed="true"
                        :fixedNumber="[21, 9]"
                        :centerBox="true"
                        :infoTrue="true"
                        :high="true"
                        style="width: 100%; height: 500px;"
                    ></vue-cropper>
                </div>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="cropperVisible = false">取 消</el-button>
                    <el-button type="primary" @click="cropImage">确 定</el-button>
                </div>
            </el-dialog>
        </el-card>
    </div>
</template>

<script>
import Pagination from '@/components/Pagination/index.vue'
import Request from '@/utils/request.js'
import moment from 'moment'
import { VueCropper } from 'vue-cropper'

export default {
    name: 'CarouselManager',
    components: { 
        Pagination,
        VueCropper
    },
    data() {
        return {
            // 数据列表加载动画
            listLoading: true,

            // 查询列表参数对象
            listQuery: {
                currentPage: 1,
                pageSize: 10
            },

            // 新增/编辑提交表单对象
            dialogForm: {
                id: undefined,
                title: '',
                imageUrl: '',
                tag: '',
                description: '',
                sortOrder: 1
            },

            // 数据总条数
            total: 0,
            // 表格数据数组
            tableData: [],
            // 多选数据暂存数组
            multipleSelection: [],
            // 新增/编辑弹窗显示/隐藏
            formVisible: false,
            // 裁剪弹窗显示/隐藏
            cropperVisible: false,
            // 裁剪图片的url
            cropImg: '',
            // 防止多次提交
            isSubmit: false,

            // 表单校验规则
            formRules: {
                title: [
                    { required: true, message: '请输入标题', trigger: 'blur' },
                    { max: 50, message: '长度不能超过50个字符', trigger: 'blur' }
                ],
                imageUrl: [
                    { required: true, message: '请上传图片', trigger: 'change' }
                ],
                tag: [
                    { max: 20, message: '长度不能超过20个字符', trigger: 'blur' }
                ],
                description: [
                    { max: 200, message: '长度不能超过200个字符', trigger: 'blur' }
                ],
                sortOrder: [
                    { required: true, message: '请输入排序号', trigger: 'blur' }
                ]
            }
        }
    },
    created() {
        this.fetchData()
    },
    methods: {
        // 获取数据列表
        fetchData({ page, limit } = {}) {
            if (page) this.listQuery.currentPage = page
            if (limit) this.listQuery.pageSize = limit
            
            this.listLoading = true
            Request.get("/carousels/page", {
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
                this.listLoading = false
            }).catch(() => {
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
                title: '',
                imageUrl: '',
                tag: '',
                description: '',
                sortOrder: 1
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
            this.$confirm('此操作将删除该轮播图, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete("/carousels/" + row.id).then(response => {
                    if (response.code === '0') {
                        this.$message.success('删除成功!')
                        this.fetchData()
                    } else {
                        this.$message.error(response.msg)
                    }
                })
            })
        },

        // 批量删除
        batchDelete() {
            if (!this.multipleSelection.length) {
                this.$message.warning('请先选择要删除的数据！')
                return
            }

            this.$confirm('此操作将删除选中的轮播图, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                Request.delete('/carousels/batch', {
                    data: this.multipleSelection
                }).then(res => {
                    if (res.code === '0') {
                        this.$message.success('批量删除成功!')
                        this.$refs.multipleTable.clearSelection()
                        this.multipleSelection = []
                        this.fetchData()
                    } else {
                        this.$message.error(res.msg)
                    }
                })
            })
        },

        // 保存数据
        handleSave(formName) {
            this.$refs[formName].validate(valid => {
                if (!valid) return false
                
                this.isSubmit = true
                const request = this.dialogForm.id 
                    ? Request.put("/carousels/update" ,this.dialogForm)
                    : Request.post("/carousels/add", this.dialogForm)

                request.then(response => {
                    if (response.code === '0') {
                        this.$message.success(this.dialogForm.id ? '更新成功' : '添加成功')
                        this.formVisible = false
                        this.fetchData()
                    } else {
                        this.$message.error(response.msg)
                    }
                }).finally(() => {
                    this.isSubmit = false
                })
            })
        },

        // 上传前校验
        beforeAvatarUpload(file) {
            const isImage = file.type.indexOf('image/') === 0
            const isLt5M = file.size / 1024 / 1024 < 5

            if (!isImage) {
                this.$message.error('只能上传图片文件!')
                return false
            }
            if (!isLt5M) {
                this.$message.error('图片大小不能超过 5MB!')
                return false
            }

            // 读取图片文件
            const reader = new FileReader()
            reader.readAsDataURL(file)
            reader.onload = () => {
                this.cropImg = reader.result
                this.cropperVisible = true
            }
            return false
        },

        // 裁剪图片
        cropImage() {
            this.$refs.cropper.getCropData(data => {
                // 将base64转为文件对象
                const arr = data.split(',')
                const mime = arr[0].match(/:(.*?);/)[1]
                const bstr = atob(arr[1])
                let n = bstr.length
                const u8arr = new Uint8Array(n)
                while (n--) {
                    u8arr[n] = bstr.charCodeAt(n)
                }
                const file = new File([u8arr], 'cropped.png', { type: mime })
                
                // 上传文件
                const formData = new FormData()
                formData.append('file', file)
                
                Request.post('/file/upload/img', formData).then(res => {
                    if (res.code === '0') {
                        this.dialogForm.imageUrl = res.data
                        this.cropperVisible = false
                        this.$message.success('上传成功')
                    } else {
                        this.$message.error(res.msg || '上传失败')
                    }
                })
            })
        },

        // 添加获取完整图片URL的方法
        getFullImageUrl(url) {
            if (!url) return ''
            if (url.startsWith('http')) return url
            // 如果路径中已经包含 /images/，则移除它
            const cleanUrl = url.replace('/images/', '/')
            return '/api/' + cleanUrl
        },

        // 添加自定义上传方法
        uploadImage(options) {
            const formData = new FormData()
            formData.append('file', options.file)
            
            Request.post('/file/upload/img', formData).then(res => {
                if (res.code === '0') {
                    this.dialogForm.imageUrl = res.data
                    this.$message.success('上传成功')
                } else {
                    this.$message.error(res.msg || '上传失败')
                }
            }).catch(error => {
                this.$message.error('上传失败')
            })
        },

        moment(date) {
            return moment(date)
        }
    }
}
</script>

<style lang="less">
.cropper-container {
    width: 100%;
    height: 500px;

    .vue-cropper {
        width: 100% !important;
        height: 100% !important;
    }
}

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

        .left-btns, .right-btns {
            display: flex;
            gap: 8px;
        }
    }

    .upload-box {
        .avatar-uploader {
            border: 1px dashed #d9d9d9;
            border-radius: 6px;
            cursor: pointer;
            position: relative;
            overflow: hidden;
            width: 420px;
            height: 180px;
            
            &:hover {
                border-color: #409EFF;
            }
        }

        .avatar-uploader-icon {
            font-size: 28px;
            color: #8c939d;
            width: 420px;
            height: 180px;
            line-height: 180px;
            text-align: center;
        }

        .avatar {
            width: 420px;
            height: 180px;
            display: block;
        }

        .upload-tip {
            color: #909399;
            font-size: 12px;
            margin-top: 8px;
        }
    }
}
</style> 