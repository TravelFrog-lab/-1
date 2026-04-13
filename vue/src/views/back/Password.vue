<template>
  <div class="wrapper">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h2>修改密码</h2>
        <div class="divider"></div>
      </div>
    </div>

    <el-card class="password-card" shadow="never">
      <el-form 
        :model="passwordForm" 
        :rules="passwordRules" 
        ref="passwordForm" 
        label-width="100px"
        class="password-form"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input 
            type="password" 
            v-model="passwordForm.oldPassword" 
            placeholder="请输入旧密码"
            :show-password="true"
            autocomplete="off">
          </el-input>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            type="password" 
            v-model="passwordForm.newPassword" 
            placeholder="请输入新密码"
            :show-password="true"
            autocomplete="off">
          </el-input>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            type="password" 
            v-model="passwordForm.confirmPassword" 
            placeholder="请确认新密码"
            :show-password="true"
            autocomplete="off">
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            icon="el-icon-check"
            :loading="loading"
            @click="submitForm">
            {{ loading ? '提交中...' : '确认修改' }}
          </el-button>
          <el-button 
            icon="el-icon-refresh-right"
            @click="resetForm">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request';
import { mapGetters } from 'vuex';

export default {
  name: 'Password',

  computed: {
    ...mapGetters(['currentUser']),
  },

  data() {
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error('密码长度不能少于6位'));
      } else {
        callback();
      }
    };

    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致'));
      } else {
        callback();
      }
    };

    return {
      loading: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
      },
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入旧密码', trigger: 'blur' },
        ],
        newPassword: [
          { required: true, validator: validatePassword, trigger: 'blur' },
        ],
        confirmPassword: [
          { required: true, validator: validateConfirmPassword, trigger: 'blur' },
        ],
      },
    };
  },

  methods: {
    async submitForm() {
      try {
        const valid = await this.$refs.passwordForm.validate();
        if (!valid) return;

        this.loading = true;
        const response = await request.put('/user/password/' + this.currentUser.id, this.passwordForm);
        
        if (response.code === '0') {
          this.$message.success('密码修改成功！请重新登录！');
          this.resetForm();
          this.$store.dispatch('logout');
          this.$router.push('/login');
        } else {
          this.$message.error(response.msg || '修改失败');
        }
      } catch (error) {
        this.$message.error('操作失败：' + (error.message || '未知错误'));
      } finally {
        this.loading = false;
      }
    },

    resetForm() {
      this.$refs.passwordForm.resetFields();
    },
  },
};
</script>

<style lang="less" scoped>
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

  .password-card {
    max-width: 600px;
    margin: 0 auto;
    
    .password-form {
      padding: 20px 40px;

      :deep(.el-form-item) {
        margin-bottom: 24px;

        .el-form-item__label {
          font-weight: 500;
          color: #606266;
        }

        .el-input__inner {
          height: 40px;
          line-height: 40px;
        }

        &:last-child {
          margin-bottom: 0;
          text-align: center;

          .el-button {
            min-width: 120px;
            padding: 12px 20px;
            margin: 0 10px;
          }
        }
      }
    }
  }
}
</style>