<template>
  <div class="news-panel">
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
        <el-button type="primary" size="medium" icon="el-icon-plus" @click="handleCreate" plain>新增动态</el-button>
      </div>
    </div>

    <el-form ref="searchForm" :inline="true" :model="listQuery" class="search-form">
      <el-form-item label="标题">
        <el-input v-model="listQuery.title" placeholder="请输入标题关键词" size="medium" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="medium" plain icon="el-icon-search" @click="onSubmit">查询</el-button>
        <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="onReset">重置</el-button>
      </el-form-item>
    </el-form>

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
      border
    >
      <el-table-column type="selection" width="50"></el-table-column>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="title" label="标题" show-overflow-tooltip></el-table-column>
      <el-table-column label="封面" width="180">
        <template slot-scope="scope">
          <el-image style="width: 120px; height: 80px" :src="scope.row.img ? '/api' + scope.row.img : ''" fit="cover">
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

    <Pagination
      v-show="total>0"
      :total="total"
      :page.sync="listQuery.currentPage"
      :limit.sync="listQuery.pageSize"
      @pagination="fetchData"
    />

    <el-dialog :title="dialogForm.id ? '编辑小区动态' : '新增小区动态'" :visible.sync="formVisible" width="900px" :close-on-click-modal="false">
      <el-form :model="dialogForm" :rules="formRules" ref="dialogForm" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="dialogForm.title" placeholder="请输入标题"></el-input>
        </el-form-item>

        <el-form-item label="封面图片" prop="img">
          <div class="upload-container">
            <el-upload class="avatar-uploader" action="#" :show-file-list="false" :on-change="handleUpload" :auto-upload="false">
              <img v-if="dialogForm.img" :src="'/api' + dialogForm.img" class="avatar">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>

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

        <el-form-item label="正文内容" prop="content">
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
  name: 'NewsManagePanel',
  components: {
    Pagination,
    Editor,
    Toolbar,
    VueCropper
  },
  computed: {
    ...mapGetters(['currentUser'])
  },
  data () {
    return {
      listLoading: true,
      listQuery: {
        title: undefined,
        currentPage: 1,
        pageSize: 10
      },
      dialogForm: {
        id: undefined,
        title: '',
        content: '',
        img: '',
        publisher: ''
      },
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
      editor: null,
      mode: 'default',
      toolbarConfig: { excludeKeys: [] },
      editorConfig: {
        placeholder: '请输入正文内容...',
        MENU_CONF: {
          uploadImage: {
            base64LimitSize: 5 * 1024 * 1024,
            customUpload: async (file, insertFn) => {
              try {
                const isImage = file.type.startsWith('image/')
                if (!isImage) {
                  this.$message.error('上传文件只能是图片格式!')
                  return
                }
                const fileSize = file.size / 1024 / 1024
                if (fileSize > 10) {
                  this.$message.error('图片大小不能超过 10MB!')
                  return
                }
                const formData = new FormData()
                formData.append('file', file)
                const res = await Request.post('/file/upload/img', formData, {
                  headers: {
                    'Content-Type': 'multipart/form-data',
                    token: this.currentUser.token
                  }
                })
                if (res.code === '0') {
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
      cropperVisible: false,
      cropperImg: '',
      imgFile: null,
      total: 0,
      tableData: [],
      multipleSelection: [],
      formVisible: false,
      exportVisible: false,
      isSubmit: false
    }
  },
  created () {
    this.fetchData()
  },
  beforeDestroy () {
    if (this.editor) {
      this.editor.destroy()
    }
  },
  methods: {
    moment,
    handleCreated (editor) {
      this.editor = Object.seal(editor)
    },
    handleUpload (file) {
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
        this.cropperImg = reader.result
        this.cropperVisible = true
      }
      return false
    },
    async customUpload ({ file }) {
      const formData = new FormData()
      formData.append('file', file)
      try {
        const res = await Request.post('/file/upload/img', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
            token: this.currentUser.token
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
    handleChange (editor) {
      this.dialogForm.content = editor.getHtml()
    },
    cropImage () {
      this.$refs.cropper.getCropBlob(async (blob) => {
        const file = new File([blob], this.imgFile.name, { type: this.imgFile.type })
        await this.customUpload({ file })
        this.cropperVisible = false
      })
    },
    handleSelectionChange (val) {
      this.multipleSelection = val.map(v => v.id)
    },
    handleCreate () {
      this.dialogForm = {
        title: '',
        content: '',
        img: '',
        publisher: ''
      }
      this.formVisible = true
    },
    handleEdit (row) {
      this.dialogForm = JSON.parse(JSON.stringify(row))
      this.formVisible = true
    },
    handleDelete (row) {
      this.$confirm('此操作将删除该条小区动态, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.delete('/news/' + row.id).then(response => {
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
    batchDelete () {
      if (!this.multipleSelection.length) {
        this.$message.warning('请先选择要删除的数据！')
        return
      }
      this.$confirm('此操作将删除选中的小区动态, 是否继续?', '提示', {
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
    fetchData ({ page, limit } = {}) {
      if (page) this.listQuery.currentPage = page
      if (limit) this.listQuery.pageSize = limit
      this.listLoading = true
      Request.get('/news/page', {
        params: {
          title: this.listQuery.title,
          pageNum: this.listQuery.currentPage,
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
    onSubmit () {
      this.listQuery.currentPage = 1
      this.fetchData()
    },
    onReset () {
      this.listQuery = {
        title: undefined,
        currentPage: 1,
        pageSize: 10
      }
      this.fetchData()
    },
    handleSave (formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) return false
        this.isSubmit = true
        const req = this.dialogForm.id
          ? Request.put('/news', this.dialogForm)
          : Request.post('/news', this.dialogForm)
        req.then(response => {
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
    exportTable (type) {
      if (this.tableData.length) {
        const params = {
          header: ['ID', '标题', '内容', '封面', '发布人', '创建时间'],
          key: ['id', 'title', 'content', 'img', 'publisher', 'createdAt'],
          data: this.tableData,
          autoWidth: true,
          fileName: '小区动态数据表',
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
.news-panel {
  .control-btns {
    display: flex;
    justify-content: space-between;
    margin-bottom: 24px;
    align-items: center;
    .left-btns, .right-btns {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
  .search-form {
    padding: 16px 0;
    margin-bottom: 16px;
    border-bottom: 1px solid #ebeef5;
    .el-form-item {
      margin-bottom: 0;
      margin-right: 16px;
    }
  }
  .upload-container .avatar-uploader .el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    overflow: hidden;
    transition: all 0.3s;
    &:hover { border-color: #409eff; }
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
  .cropper-box {
    width: 100%;
    background: #f8f8f8;
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
      &:hover { background-color: #f5f7fa; }
      i { font-size: 24px; color: #409eff; }
    }
  }
  .editor-container {
    border: 1px solid #ccc;
    z-index: 100;
  }
}
</style>

<style lang="less">
.news-panel .w-e-menu-panel {
  z-index: 3000 !important;
}
</style>
