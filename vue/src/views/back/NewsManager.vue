<template>
  <div class="wrapper">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h2>新闻管理</h2>
        <div class="divider"></div>
        <p class="subtitle">News Management</p>
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
          <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增新闻</el-button>
        </div>
      </div>

      <!-- 查询栏 -->
      <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
        <el-form-item label="新闻标题">
          <el-input v-model="listQuery.title" placeholder="请输入新闻标题" size="medium" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="medium" plain icon="el-icon-search" @click="onSubmit">查询</el-button>
          <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格栏 -->
      <el-table ref="multipleTable" v-loading="listLoading" :data="tableData" tooltip-effect="dark" row-key="id" style="width: 100%" size="medium"
        @selection-change="handleSelectionChange" :header-cell-style="{background:'#f8f9fa',color:'#606266'}" :cell-style="{padding:'12px 0'}" border>
        <el-table-column type="selection" width="50"></el-table-column>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="标题" show-overflow-tooltip></el-table-column>
        <el-table-column label="封面" width="180">
          <template slot-scope="scope">
            <el-image style="width: 120px; height: 80px" :src="scope.row.img ? 'api' + scope.row.img : ''" fit="cover">
              <div slot="error" class="image-slot">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="publisher" label="发布人" width="120"></el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template slot-scope="scope">
            {{ moment(scope.row.createdAt).format('YYYY-MM-DD HH:mm:ss') }}
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
      <Pagination v-show="total>0" :total="total" :page.sync="listQuery.currentPage" :limit.sync="listQuery.pageSize" @pagination="fetchData" />

      <!-- 新增/编辑弹窗 -->
      <el-dialog :title="dialogForm.id ? '编辑新闻' : '新增新闻'" :visible.sync="formVisible" width="900px" :close-on-click-modal="false" >
        <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
          <el-form-item label="新闻标题" prop="title">
            <el-input v-model="dialogForm.title" placeholder="请输入新闻标题"></el-input>
          </el-form-item>

          <el-form-item label="封面图片" prop="img">
            <div class="upload-container">
              <el-upload class="avatar-uploader" action="#" :show-file-list="false" :on-change="handleUpload" :auto-upload="false">
                <img v-if="dialogForm.img" :src="'/api' + dialogForm.img" class="avatar">
                <i v-else class="el-icon-plus avatar-uploader-icon"></i>
              </el-upload>

              <!-- 图片裁剪组件 -->
              <el-dialog width="800px" append-to-body title="图片裁剪" :visible.sync="cropperVisible" :close-on-click-modal="false" :destroy-on-close="true"
                custom-class="cropper-dialog">
                <div class="cropper-box">
                  <vue-cropper ref="cropper" :img="cropperImg" :info="true" :autoCrop="true" :fixed="true" :fixedNumber="[3, 2]" :centerBox="true"
                    :infoTrue="true" :canScale="false" :full="false" mode="contain" :high="true" style="width: 100%; height: 400px;"></vue-cropper>
                </div>
                <div slot="footer" class="dialog-footer">
                  <el-button @click="cropperVisible = false">取 消</el-button>
                  <el-button type="primary" @click="cropImage">确 定</el-button>
                </div>
              </el-dialog>
            </div>
          </el-form-item>

          <el-form-item label="新闻内容" prop="content">
            <div class="editor-container">
              <Toolbar style="border-bottom: 1px solid #ccc" :editor="editor" :defaultConfig="toolbarConfig" :mode="mode" />
              <Editor style="height: 500px" v-model="dialogForm.content" :defaultConfig="editorConfig" :mode="mode" @onCreated="handleCreated" @onChange="handleChange" />
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
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { VueCropper } from 'vue-cropper'
import moment from 'moment'
import { mapGetters } from 'vuex'
export default {
  name: 'NewsManager',
  components: {
    Pagination,
    Editor,
    Toolbar,
    VueCropper
  },
  computed: {
    ...mapGetters(['currentUser'])
  },

  data() {
    return {
      // 数据列表加载动画
      listLoading: true,

      // 查询列表参数对象
      listQuery: {
        title: undefined,
        currentPage: 1,
        pageSize: 10
      },

      // 新增/编辑提交表单对象
      dialogForm: {
        id: undefined,
        title: '',
        content: '',
        img: '',
        publisher: ''
      },

      // 表单校验规则
      formRules: {
        title: [
          { required: true, message: '标题不能为空', trigger: 'blur' },
          { min: 2, max: 100, message: '标题长度在2-100个字符之间', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '内容不能为空', trigger: 'blur' }
        ],
        img: [
          { 
            required: true, 
            validator: (rule, value, callback) => {
              if (!value && !this.dialogForm.id) {
                callback(new Error('请上传封面图片'))
              } else {
                callback()
              }
            }, 
            trigger: 'change' 
          }
        ]
      },

      // 富文本编辑器实例
      editor: null,
      // 编辑器模式
      mode: 'default',
      // 工具栏配置
      toolbarConfig: {
        excludeKeys: []
      },
      // 编辑器配置
      editorConfig: {
        placeholder: '请输入商品描述...',
        MENU_CONF: {
          uploadImage: {
            // 小于 5M 的图片转为 base64
            base64LimitSize: 5 * 1024 * 1024,
            // 自定义上传
            customUpload: async (file, insertFn) => {
              try {
                // 检查文件类型
                const isImage = file.type.startsWith('image/')
                if (!isImage) {
                  this.$message.error('上传文件只能是图片格式!')
                  return
                }

                // 检查文件大小
                const fileSize = file.size / 1024 / 1024 // 转换为 MB
                if (fileSize > 10) {
                  this.$message.error('图片大小不能超过 5MB!')
                  return
                }

                // 如果图片小于 10MB，编辑器会自动转为 base64
                // 只有大于 10MB 的图片才会走到这里
                // 创建 FormData
                const formData = new FormData()
                formData.append('file', file)

                // 发送请求
                const res = await Request.post('/file/upload/img', formData, {
                  headers: {
                    'Content-Type': 'multipart/form-data',
                    'token': this.currentUser.token
                  }
                })

                if (res.code === '0') {
                  // 上传成功，插入图片
                  insertFn(`/api${res.data}`)
                  this.$message.success('图片上传成功')
                } else {
                  this.$message.error(res.msg || '图片上传失败')
                }
              } catch (error) {
                console.error('上传图片失败:', error)
                this.$message.error('图片上传失败')
              }
            }
          }
        }
      
      },

      // 图片裁剪相关
      cropperVisible: false,
      cropperImg: '',
      imgFile: null,

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
    }
  },
  created() {
    this.fetchData()
  },
  beforeDestroy() {
    if (this.editor) {
      this.editor.destroy()
    }
  },
  methods: {
    moment,
    // 富文本编辑器创建完成
    handleCreated(editor) {
      this.editor = Object.seal(editor)
    },

    // 图片上传前检查
    handleUpload(file) {
      const isImage = file.raw.type.startsWith('image/')
      const isLt2M = file.raw.size / 1024 / 1024 < 2

      if (!isImage) {
        this.$message.error('上传文件只能是图片格式!')
        return false
      }
      if (!isLt2M) {
        this.$message.error('上传图片大小不能超过 2MB!')
        return false
      }

      this.imgFile = file.raw
      const reader = new FileReader()
      reader.readAsDataURL(file.raw)
      reader.onload = () => {
        // this.cropperImg = ''  // 先清空
        // this.$nextTick(() => {
          this.cropperImg = reader.result
          this.cropperVisible = true
        // })
      }
      return false
    },
    // 自定义上传
    async customUpload({ file }) {
      const formData = new FormData()
      formData.append('file', file)

      try {
        const res = await Request.post('/file/upload/img', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
                'token': this.currentUser.token
            }
        })


        if (res.code === '0') {
          this.dialogForm.img = res.data
          this.$message.success('上传成功')
        } else {
          this.$message.error(res.msg || '上传失败')
        }
      } catch (error) {
        this.$message.error('上传失败')
      }
    },
    handleChange(editor) {
      this.dialogForm.content = editor.getHtml()
    },

    // 修改裁剪图片方法
    cropImage() {
      this.$refs.cropper.getCropBlob(async (blob) => {
        const file = new File([blob], this.imgFile.name, { type: this.imgFile.type })
        await this.customUpload({ file })
        this.cropperVisible = false
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
        content: '',
        img: '',
        publisher: ''
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
      this.$confirm('此操作将删除该新闻, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.delete("/news/" + row.id).then(response => {
          if (response.code === '0') {
            this.$message.success('删除成功!')
            this.fetchData()
          } else {
            this.$message.error(response.msg)
          }
        })
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },
    // 批量删除
    batchDelete() {
      if (!this.multipleSelection.length) {
        this.$message.warning('请先选择要删除的数据！')
        return
      }

      this.$confirm('此操作将删除选中的新闻, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.delete('/news/batch', {
          data: this.multipleSelection
        }).then(res => {
          if (res.code === '0') {
            this.$message.success('批量删除成功')
            this.$refs.multipleTable.clearSelection()
            this.multipleSelection = []
            this.fetchData()
          } else {
            this.$message.error(res.msg)
          }
        })
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },
    // 获取数据列表
    fetchData({ page, limit } = {}) {
      if (page) this.listQuery.currentPage = page
      if (limit) this.listQuery.pageSize = limit

      this.listLoading = true
      Request.get("/news/page", {
        params: {
          title: this.listQuery.title,
          currentPage: this.listQuery.currentPage,
          pageSize: this.listQuery.pageSize
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
    // 查询数据
    onSubmit() {
      this.listQuery.currentPage = 1
      this.fetchData()
    },
    // 重置查询
    onReset() {
      this.listQuery = {
        title: undefined,
        currentPage: 1,
        pageSize: 10
      }
      this.fetchData()
    },
    // 保存数据
    handleSave(formName) {
        console.log(this.dialogForm)
      this.$refs[formName].validate(valid => {
        if (!valid) return false

        this.isSubmit = true
        const request = this.dialogForm.id
          ? Request.put("/news" , this.dialogForm)
          : Request.post("/news", this.dialogForm)

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
    // 导出数据
    exportTable(type) {
      if (this.tableData.length) {
        const params = {
          header: ['ID', '标题', '内容', '封面', '发布人', '创建时间'],
          key: ['id', 'title', 'content', 'img', 'publisher', 'createdAt'],
          data: this.tableData,
          autoWidth: true,
          fileName: '新闻数据表',
          bookType: type
        }
        excel.exportDataToExcel(params)
        this.exportVisible = false
      }
    }
  },

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

  .upload-container {
    .avatar-uploader {
      .el-upload {
        border: 1px dashed #d9d9d9;
        border-radius: 6px;
        cursor: pointer;
        position: relative;
        overflow: hidden;
        transition: all 0.3s;

        &:hover {
          border-color: #409eff;
        }
      }

      .avatar-uploader-icon {
        font-size: 28px;
        color: #8c939d;
        width: 180px;
        height: 120px;
        line-height: 120px;
        text-align: center;
      }

      .avatar {
        width: 180px;
        height: 120px;
        display: block;
      }
    }
  }

  .cropper-dialog {
    .el-dialog__body {
      padding: 20px;
    }
  }

  .cropper-box {
    width: 100%;
    background: #f8f8f8;
    position: relative;
  }

  .export-data {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .export-option {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background-color: #f5f7fa;
      }

      i {
        font-size: 24px;
        color: #409eff;
      }

      span {
        font-size: 16px;
        color: #606266;
      }
    }
  }

  .editor-container {
    border: 1px solid #ccc;
    z-index: 100;

    .w-e-toolbar {
      border-bottom: 1px solid #ccc;
      padding: 8px;
      background-color: #fafafa;
    }

    .w-e-text-container {
      height: 500px !important;
      background-color: #fff;

      .w-e-text {
        padding: 12px;
        min-height: 450px !important;
      }
    }
  }

  .w-e-menu-panel {
    z-index: 3000 !important;
  }

  .w-e-text-container [data-w-e-toolbar] {
    z-index: 2000 !important;
  }
}
</style> 