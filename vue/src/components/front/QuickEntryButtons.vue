<template>
  <div class="quick-entry-buttons">
    <el-row :gutter="20">
      <el-col :span="8" v-for="(item, index) in quickEntries" :key="index">
        <div class="entry-item" @click="handleClick(item.path)">
          <el-card shadow="hover">
            <div class="entry-icon">
              <i :class="item.icon"></i>
            </div>
            <div class="entry-title">{{ item.title }}</div>
            <div class="entry-desc">{{ item.description }}</div>
          </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'QuickEntryButtons',
  data() {
    return {
      quickEntries: [
        {
          title: '物业缴费',
          description: '在线缴纳物业费',
          icon: 'el-icon-money',
          path: '/property-fee'
        },
        {
          title: '报修服务',
          description: '提交维修申请',
          icon: 'el-icon-service',
          path: '/repair'
        },
        {
          title: '投诉建议',
          description: '反馈问题和建议',
          icon: 'el-icon-message',
          path: '/complaint'
        },
        {
          title: '停车服务',
          description: '车位查询和缴费',
          icon: 'el-icon-location-information',
          path: '/parking'
        },
        {
          title: '志愿活动',
          description: '参与社区志愿服务',
          icon: 'el-icon-coordinate',
          path: '/volunteer'
        },
        {
          title: '宠物代喂',
          description: '宠物照看服务',
          icon: 'el-icon-present',
          path: '/pet-sitting'
        },
          {
          title: '家政服务',
          description: '家政服务',
          icon: 'el-icon-location-information',
          path: '/housekeeping'
        },
        {
          title: '投票表决',
          description: '业委会选举、公共事务决策、民意调查',
          icon: 'el-icon-s-data',
          path: '/vote'
        },
        {
          title: 'AI助手',
          description: '智能解答物业相关常见问题',
          icon: 'el-icon-chat-dot-round',
          path: '/ai-assistant'
        }
      ]
    }
  },
  methods: {
    handleClick(path) {
      this.$router.push(path)
    }
  }
}
</script>

<style scoped>
.quick-entry-buttons {
  padding: 20px 0;
}

.entry-item {
  cursor: pointer;
  margin-bottom: 20px;
}

.el-card {
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  transition: all 0.3s;
}

.el-card:hover {
  transform: translateY(-5px);
}

.entry-icon {
  font-size: 40px;
  color: #409EFF;
  margin-bottom: 15px;
}

.entry-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.entry-desc {
  font-size: 14px;
  color: #909399;
}

/* 自定义图标 */
.el-icon-parking:before {
  content: "\e644"; /* 使用Element UI的停车图标 */
}

.el-icon-pet:before {
  content: "\e6f9"; /* 使用Element UI的宠物图标 */
}
</style> 