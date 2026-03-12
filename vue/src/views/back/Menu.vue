<template>
  <div class="wrapper">
    <!-- 更新页面标题区样式 -->
    <div class="page-header">
      <div class="header-content">
        <h2>菜单管理</h2>
        <div class="divider"></div>
        <p class="subtitle">Menu Management</p>
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
          <el-button type="info" size="medium" plain icon="el-icon-refresh" @click="handleRefresh">刷新</el-button>
          <el-radio-group plain v-model="viewType" size="mini">
            <el-radio-button size="mini" label="tree">树形视图</el-radio-button>
            <el-radio-button size="mini" label="list">列表视图</el-radio-button>
          </el-radio-group>
        </div>

        <div class="right-btns">
          <el-button type="primary" size="medium" icon="el-icon-download" @click="exportVisible = true" plain>导出数据</el-button>
          <el-button type="primary" size="medium" icon="el-icon-plus" @click="openDialog('add')" plain>新增菜单项</el-button>
        </div>
      </div>

      <!-- 树形表格 -->
      <el-table v-if="viewType === 'tree'"
        ref="multipleTable"
        v-loading="listLoading"
        :data="tableData"
        row-key="id"
        :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
        style="width: 100%"
        size="medium"
        @selection-change="handleSelectionChange"
        :header-cell-style="{background:'#f8f9fa',color:'#606266'}"
        :cell-style="{padding:'12px 0'}"
        border>
        <el-table-column type="selection" width="50"></el-table-column>
        <el-table-column prop="id" label="id"></el-table-column>
        <el-table-column prop="name" label="菜单名称"></el-table-column>
        <el-table-column prop="path" label="菜单路径"></el-table-column>
        <el-table-column label="菜单图标">
          <template slot-scope="scope">
            <i :class="scope.row.icon" />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="菜单描述"></el-table-column>
        <el-table-column prop="pagePath" label="页面路径"></el-table-column>
        <el-table-column prop="role" label="菜单分配">
          <template slot-scope="scope">
            <div>{{ getMenuRole(scope.row.role) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="sortNum" label="排序"></el-table-column>
        <el-table-column label="操作" width="350">
          <template slot-scope="scope">
            <el-button plain size="small" type="success" class="el-icon-circle-plus" @click="addChildMenu(scope.row.id)"
              v-if="!scope.row.pid && !scope.row.path">新增子菜单</el-button>
            <el-button plain size="small" type="primary" class="el-icon-edit" @click="openDialog('edit', scope.row)">编辑</el-button>
            <el-button plain size="mini" type="danger" @click="handleDelete(scope.$index, scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 列表表格 -->
      <div v-else>
        <el-table
          ref="multipleTable"
          v-loading="listLoading"
          :data="pageData.records"
          row-key="id"
          style="width: 100%"
          size="medium"
          @selection-change="handleSelectionChange"
          :header-cell-style="{background:'#f8f9fa',color:'#606266'}"
          :cell-style="{padding:'12px 0'}"
          border>
          <el-table-column type="selection" width="50"></el-table-column>
          <el-table-column prop="id" label="id"></el-table-column>
          <el-table-column prop="name" label="菜单名称"></el-table-column>
          <el-table-column prop="path" label="菜单路径"></el-table-column>
          <el-table-column label="菜单图标">
            <template slot-scope="scope">
              <i :class="scope.row.icon" />
            </template>
          </el-table-column>
          <el-table-column prop="description" label="菜单描述"></el-table-column>
          <el-table-column prop="pagePath" label="页面路径"></el-table-column>
          <el-table-column prop="role" label="菜单分配">
            <template slot-scope="scope">
              <div>{{ getMenuRole(scope.row.role) }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="sortNum" label="排序"></el-table-column>
          <el-table-column label="操作" width="350">
            <template slot-scope="scope">
              <el-button plain size="small" type="success" class="el-icon-circle-plus" @click="addChildMenu(scope.row.id)"
                v-if="!scope.row.pid && !scope.row.path">新增子菜单</el-button>
              <el-button plain size="small" type="primary" class="el-icon-edit" @click="openDialog('edit', scope.row)">编辑</el-button>
              <el-button plain size="mini" type="danger" @click="handleDelete(scope.$index, scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页器 -->
        <div class="pagination-container">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page.sync="pageData.current"
            :page-sizes="[10, 20, 50, 100]"
            :page-size.sync="pageData.size"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pageData.total">
          </el-pagination>
        </div>
      </div>

      <!-- 对话框样式优化 -->
      <el-dialog :title="dialogMode === 'add' ? '新增菜单项' : '修改菜单项'" :visible.sync="formVisible" width="500px" :close-on-click-modal="false">
        <el-form :model="dialogForm" :rules="formRules" ref="dialogForm">
          <!-- 菜单类型选择 -->
          <el-form-item label="菜单类型" :label-width="formLabelWidth" v-if="dialogMode === 'add'">
            <el-radio-group v-model="menuType">
              <el-radio label="parent">父级菜单</el-radio>
              <el-radio label="child">子菜单</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 父级菜单选择 -->
          <el-form-item label="父级菜单" :label-width="formLabelWidth" v-if="menuType === 'child'">
            <el-select v-model="dialogForm.pid" placeholder="请选择父级菜单">
              <el-option
                v-for="item in parentMenus"
                :key="item.id"
                :label="item.name"
                :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="菜单名称" :label-width="formLabelWidth" prop="name">
            <el-input v-model="dialogForm.name" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item  label="菜单路径" :label-width="formLabelWidth">
            <el-input v-model="dialogForm.path" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="菜单图标" :label-width="formLabelWidth" prop="icon">
            <el-select v-model="dialogForm.icon" filterable placeholder="请选择图标">
              <el-option v-for="dict in iconDict" :key="dict.itemKey" :label="dict.itemKey" :value="dict.itemValue">
                <i :class="dict.itemValue"></i>&nbsp;&nbsp;{{ dict.itemKey }}
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item   label="页面路径" :label-width="formLabelWidth">
            <el-input v-model="dialogForm.pagePath" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="排序" :label-width="formLabelWidth" prop="sortNum">
            <el-input v-model="dialogForm.sortNum" autocomplete="off" type="number"></el-input>
          </el-form-item>
          <el-form-item label="菜单分配" :label-width="formLabelWidth" prop="role">
            <el-checkbox-group v-model="checkList">
              <el-checkbox key="OWNER" label="业主"></el-checkbox>
              <el-checkbox key="ADMIN" label="管理员"></el-checkbox>
              <el-checkbox key="MAINTENANCE" label="后勤人员"></el-checkbox>
              <el-checkbox key="KEEPER" label="家政人员"></el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="描述" :label-width="formLabelWidth">
            <el-input v-model="dialogForm.description" autocomplete="off"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="formVisible = false">取 消</el-button>
          <el-button type="primary" :disabled="isSubmit" @click="handleSave('dialogForm')">确 定</el-button>
        </div>
      </el-dialog>

      <!-- 导出对话框优化 -->
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
import Upload from '@/components/Upload/index.vue'
import Hints from '@/components/Hints/index.vue'
import Request from '@/utils/request.js'
export default {
  name: 'Menu',
  components: { Pagination, Upload, Hints },
  data() {
    return {
      // 数据列表加载动画
      listLoading: true,
      formLabelWidth: '80px',
      iconDict: {},
      checkList: [],
      // 新增/编辑提交表单对象
      dialogForm: {
        name: undefined,
        description: undefined,
      },
      // 数据总条数
      total: 0,
      // 表格数据数组
      tableData: [],
      // 多选数据暂存数组
      multipleSelection: [],
      // 新增/编辑 弹出框显示/隐藏
      formVisible: false,
      // 当前对话框模式
      dialogMode: 'add', // 'add' or 'edit'
      // 导出数据 弹出框显示/隐藏
      exportVisible: false,
      // 防止多次连续提交表单
      isSubmit: false,
      // 表单校验 trigger: ['blur', 'change'] 为多个事件触发
      formRules: {
        // 验证规则
        name: [
          { required: true, message: '请填写菜单名称', trigger: 'blur' }
        ],
        sortNum: [
          { required: true, message: '排序不能为空', trigger: 'blur' },
        ],
        icon: [
          { required: true, message: '请选择菜单图标', trigger: 'change' }
        ]
      },
      viewType: 'tree',
      menuType: 'parent',
      parentMenus: [],
      pageData: {
        records: [],
        total: 0,
        size: 10,
        current: 1
      },
    }
  },
  created() {
    this.fetchTreeData()
  },
  methods: {
    // 多选操作
    handleSelectionChange(val) {
      this.multipleSelection = val.map(v => v.id)
    },

    handleDelete(index, row) {
      console.log(index, row)
      this.$confirm('此操作将删除选中数据, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        Request.delete("/menu/deleteById/" + row.id).then(response => {
          if (response.code == 0) {
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            this.fetchTreeData()
          } else {
            this.$message({
              type: 'error',
              errpr: '删除失败!'
            })
          }
        });

      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        })
      })
    },
    addChildMenu(pid) {
      this.formVisible = true;
      this.dialogForm = {};
      this.menuType = 'child';
      this.dialogMode = 'add';
      this.loadIconList();
      this.getParentMenus(); // 获取父级菜单列表
      if (pid) {
        this.dialogForm.pid = pid;
      }
   
    },

    // 批量删除
    batchDelete() {
      if (this.multipleSelection.length < 1) {
        this.$message({
          message: '请先选择要删除的数据！',
          type: 'warning'
        })
      } else {
        console.log("deleteIDS：" + this.multipleSelection);
        Request.delete(`/menu/deleteBatch?ids=${this.multipleSelection.join(',')}`).then(res => {
          if (res.code === '0') {
            this.$message({
              showClose: true,
              message: '批量删除成功',
              type: 'success'
            });
            this.fetchTreeData()
          }
          else {
            this.$message({
              showClose: true,
              message: res.msg,
              type: 'error',
            });
          }
        })

      }
    },
    loadIconList() {
      Request.get("/dictitem/findByType", {
        params: {
          code: "icon"
        }
      }).then(response => {
        if (response.code === '0') {
          this.iconDict = response.data;
          console.log(this.iconDict)
        } else {
          this.$message({
            type: 'error',
            message: '图标列表失败!'
          })
        }
      });
    },
    // 获取树形数据
    async fetchTreeData() {
      this.listLoading = true
      try {
        const response = await Request.get("/menu/findAll")
        if (response.code === '0') {
          this.tableData = response.data
        } else {
          this.$message({
            showClose: true,
            message: response.msg,
            type: 'error',
          })
        }
      } catch (error) {
        console.error('Error fetching tree data:', error)
      }
      this.listLoading = false
    },

    openDialog(mode, row = {}) {
      this.dialogMode = mode;
      this.formVisible = true;
      this.loadIconList();
      this.getParentMenus(); // 获取父级菜单列表
      
      if (mode === 'edit') {
        this.dialogForm = JSON.parse(JSON.stringify(row));
        this.checkList = this.formatRolesToChinese(row.role);
        this.menuType = row.pid ? 'child' : 'parent';
      } else {
        this.dialogForm = {};
        this.checkList = [];
        this.menuType = 'parent';
      }
    },
    // 保存新增/编辑数据
    handleSave(formName) {
      this.dialogForm.role = this.formatRolesToEnglish(this.checkList);
      this.$refs[formName].validate(valid => {
        if (valid) {
          const request = this.dialogMode === 'add' ? Request.post("/menu/save", this.dialogForm) : Request.put("/menu/" + this.dialogForm.id, this.dialogForm);
          request.then(response => {
            if (response.code == 0) {
              this.$message({
                showClose: true,
                message: this.dialogMode === 'add' ? '添加成功' : '更新成功',
                type: 'success',
              });
              this.$emit('update:user', this.userInfo);
              this.formVisible = false;
              this.fetchTreeData();
            } else {
              this.$message({
                showClose: true,
                message: this.dialogMode === 'add' ? '添加失败' : '更新失败',
                type: 'error',
              });
            }
          });
        } else {
          this.isSubmit = false;
        }
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
    },
    getMenuRole(role) {
      const roleMap = {
        OWNER: '业主',
        ADMIN: '管理员',
        MAINTENANCE: '后勤人员',
        KEEPER: '家政人员'
      };

      if (!role) {
        return '';
      }

      const roles = role.split(',');
      const chineseRoles = roles.map((singleRole) => roleMap[singleRole]);
      return chineseRoles.join(',');
    },
    formatRolesToChinese(role) {
      const roleMap = {
        OWNER: '业主',
        ADMIN: '管理员',
        MAINTENANCE: '后勤人员',
        KEEPER: '家政人员'
      };

      if (!role) {
        return [];
      }

      const roles = role.split(',').map(singleRole => singleRole.trim().replace(/'/g, ''));
      const chineseRoles = roles.map(singleRole => roleMap[singleRole]);
      const validChineseRoles = chineseRoles.filter(role => role !== undefined);
      return validChineseRoles;
    },

    formatRolesToEnglish(chineseRoles) {
      const roleMap = {
        '业主': 'OWNER',
        '管理员': 'ADMIN',
        '后勤人员': 'MAINTENANCE',
        '家政人员': 'KEEPER'
      };

      if (!chineseRoles || chineseRoles.length === 0) {
        return '';
      }

      const englishRoles = chineseRoles.map(singleRole => roleMap[singleRole.trim()]);
      return englishRoles.join(',');
    },

    handleRefresh() {
      if (this.viewType === 'tree') {
        this.fetchTreeData()
      } else {
        this.fetchPageData()
      }
      this.$message({
        message: '刷新成功',
        type: 'success',
        duration: 1000
      })
    },

    // 获取父级菜单列表
    async getParentMenus() {
      try {
        const response = await Request.get("/menu/getParentMenus");
        if (response.code === '0') {
          this.parentMenus = response.data;
        }
      } catch (error) {
        console.error('Error fetching parent menus:', error);
      }
    },

    // 获取分页数据
    async fetchPageData() {
      this.listLoading = true
      try {
        const response = await Request.get("/menu/page", {
          params: {
            currentPage: this.pageData.current,
            size: this.pageData.size
          }
        })
        if (response.code === '0') {
          this.pageData = response.data
        } else {
          this.$message({
            showClose: true,
            message: response.msg,
            type: 'error',
          })
        }
      } catch (error) {
        console.error('Error fetching page data:', error)
      }
      this.listLoading = false
    },

    handleSizeChange(val) {
      this.pageData.size = val
      this.fetchPageData()
    },

    handleCurrentChange(val) {
      this.pageData.current = val
      this.fetchPageData()
    },
  },
  watch: {
    viewType(newVal) {
      if (newVal === 'list') {
        this.fetchPageData()
      } else {
        this.fetchTreeData()
      }
    },
    menuType(newVal) {
      if (newVal === 'parent') {
        this.dialogForm.pid = null;
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
    // border-radius: 10px;
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
            gap: 8px;  // 增加按钮间距

            .el-radio-group {
                margin-left: 8px;  // 与其他按钮保持一定距离
            }

            .el-button {
                min-width: 88px;  // 保持按钮最小宽度一致
            }
        }

        .right-btns {
            display: flex;
            gap: 8px;  // 增加按钮间距

            .el-button {
                min-width: 100px;  // 保持按钮最小宽度一致
            }
        }
    }

    .pagination-container {
        margin-top: 20px;
        display: flex;
        justify-content: flex-end;
    }
}
</style>