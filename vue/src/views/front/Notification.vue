<template>
  <div class="notification-page">
    <div class="content-wrapper">
      <div class="page-header">
        <h2 class="page-title">消息与待办</h2>
      </div>

      <el-tabs v-model="activeTab" class="center-tabs" @tab-click="syncQuery">
        <el-tab-pane label="站内消息" name="message">
          <notification-list ref="msgList" :full-page="true" />
        </el-tab-pane>
        <el-tab-pane label="我的待办" name="todo">
          <el-card shadow="hover" class="todo-card" v-loading="todoLoading">
            <div class="todo-filter-row">
              <span class="todo-filter-label">类型</span>
              <el-radio-group v-model="todoFilter" size="small" @change="onTodoFilterChange">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="fee">物业费</el-radio-button>
                <el-radio-button label="repair">报修服务</el-radio-button>
                <el-radio-button label="complaint">投诉建议</el-radio-button>
              </el-radio-group>
            </div>
            <el-table
              :data="filteredTodoList"
              style="width: 100%"
              :empty-text="todoEmptyText"
            >
              <el-table-column prop="title" label="事项" min-width="220">
                <template slot-scope="scope">
                  <div class="todo-title">
                    <i :class="scope.row.icon" class="todo-icon"></i>
                    {{ scope.row.title }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="说明" min-width="280" />
              <el-table-column prop="deadline" label="截止时间 / 时间" width="170" />
              <el-table-column label="操作" width="120" fixed="right">
                <template slot-scope="scope">
                  <el-button type="primary" size="small" @click="handleTodoAction(scope.row)">
                    {{ todoActionButtonLabel(scope.row) }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>
    <page-footer />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Request from '@/utils/request'
import { todoActionButtonLabel } from '@/utils/todoActionLabel'
import { navigateTodoItem } from '@/utils/todoNavigate'
import PageFooter from '@/components/front/PageFooter.vue'
import NotificationList from '@/components/front/NotificationList.vue'

export default {
  name: 'Notification',
  components: {
    PageFooter,
    NotificationList
  },
  data() {
    return {
      activeTab: 'message',
      todoList: [],
      todoLoading: false,
      /** all | fee | repair | complaint — 与后端 row.action 对应 */
      todoFilter: 'all'
    }
  },
  computed: {
    ...mapGetters(['currentUser']),
    filteredTodoList() {
      const list = this.todoList
      if (!list.length || this.todoFilter === 'all') return list
      const actionMap = {
        fee: '/property-fee',
        repair: '/repair',
        complaint: '/complaint'
      }
      const action = actionMap[this.todoFilter]
      if (!action) return list
      return list.filter((row) => row && row.action === action)
    },
    todoEmptyText() {
      if (this.todoFilter === 'all') return '暂无待办事项'
      return '暂无该类型待办事项'
    }
  },
  watch: {
    '$route.query.tab'(v) {
      if (v === 'todo' || v === 'message') {
        this.activeTab = v
      }
    },
    '$route.query.todoType'(v) {
      if (['all', 'fee', 'repair', 'complaint'].includes(v)) {
        this.todoFilter = v
      }
    },
    currentUser: {
      handler() {
        this.fetchTodos()
      },
      deep: true
    }
  },
  created() {
    const q = this.$route.query.tab
    if (q === 'todo' || q === 'message') {
      this.activeTab = q
    }
    const tf = this.$route.query.todoType
    if (['all', 'fee', 'repair', 'complaint'].includes(tf)) {
      this.todoFilter = tf
    }
    this.fetchTodos()
  },
  methods: {
    todoActionButtonLabel,
    isApiSuccess(response) {
      if (!response || response.code === undefined || response.code === null) return false
      return String(response.code) === '0'
    },
    fetchTodos() {
      const uid = this.currentUser && this.currentUser.id
      if (!uid) {
        this.todoList = []
        return
      }
      this.todoLoading = true
      Request.get('/dashboard/owner-pending-todos', { params: { userId: uid } })
        .then((response) => {
          if (this.isApiSuccess(response) && response.data != null) {
            const list = response.data.todos
            this.todoList = Array.isArray(list) ? list : []
          } else {
            this.todoList = []
          }
        })
        .catch(() => {
          this.todoList = []
        })
        .finally(() => {
          this.todoLoading = false
        })
    },
    handleTodoAction(item) {
      navigateTodoItem(this.$router, item)
    },
    onTodoFilterChange() {
      const q = { ...this.$route.query, tab: 'todo' }
      if (this.todoFilter === 'all') {
        delete q.todoType
      } else {
        q.todoType = this.todoFilter
      }
      this.$router.replace({ query: q }).catch(() => {})
    },
    syncQuery() {
      this.$nextTick(() => {
        const base = { ...this.$route.query, tab: this.activeTab }
        if (this.activeTab !== 'todo') {
          delete base.todoType
        } else if (this.todoFilter && this.todoFilter !== 'all') {
          base.todoType = this.todoFilter
        } else {
          delete base.todoType
        }
        this.$router.replace({ query: base }).catch(() => {})
      })
    }
  }
}
</script>

<style scoped>
.notification-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.content-wrapper {
  flex: 1;
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
  width: 100%;
}

.page-header {
  margin-bottom: 16px;
}

.page-title {
  font-size: 22px;
  color: #303133;
  margin: 0 0 16px;
  padding-left: 12px;
  border-left: 4px solid #409eff;
  font-weight: 600;
}

.center-tabs >>> .el-tabs__header {
  margin-bottom: 16px;
}

.todo-card {
  border-radius: 8px;
}

.todo-filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.todo-filter-label {
  font-size: 13px;
  color: #606266;
  flex-shrink: 0;
}

.todo-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.todo-icon {
  color: #409eff;
  font-size: 16px;
}
</style>
