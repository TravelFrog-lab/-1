<template>
  <div class="login-wrapper">
    <el-card class="login-box">
      <div class="login-header">
        <div class="icon-wrapper">
          <i class="el-icon-user-solid"></i>
        </div>
        <h2>欢迎登录</h2>
   
      </div>
      <el-form ref="loginForm" :model="loginForm" :rules="rules">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名"
            :prefix-icon="'el-icon-user'"
            class="custom-input">
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            type="password" 
            v-model="loginForm.password" 
            placeholder="请输入密码"
            :prefix-icon="'el-icon-lock'"
            class="custom-input">
          </el-input>
        </el-form-item>
        <el-form-item>
          <div class="verify-code">
            <el-input 
              v-model="loginForm.validCode" 
              placeholder="验证码"
              :prefix-icon="'el-icon-key'"
              class="custom-input">
            </el-input>
            <ValidCode @input="createValidCode" class="code-component"/>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="submit-btn" @click="login">
            <i class="el-icon-right"></i>
            登录
          </el-button>
        </el-form-item>
        <div class="action-links">
          <el-link type="primary" @click="toRegister" class="register-link">
            <i class="el-icon-plus"></i> 注册账号
          </el-link>
          <el-link type="primary" @click="toForget" class="forget-link">
            <i class="el-icon-question"></i> 忘记密码
          </el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import ValidCode from "../components/Validate";
import request from "@/utils/request";
import { setRoutes } from "@/router";
import { mapActions } from 'vuex';

export default {
  name: 'Login',
  components: {
    ValidCode
  },
  data() {
    return {
      validCode: '',
      loginForm: {
        username: '',
        password: ''

      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        // 验证码
        validCode: [
          { required: true, message: '请输入验证码', trigger: 'blur' }
        ]
      }
    };
  },
  methods: {
    ...mapActions(['login']),
    toRegister() {
      this.$router.push("/register");
    },
    toForget() {
      this.$router.push("/forget");
    },
    createValidCode(data) {
      this.validCode = data
    },
    async login() {
      this.$refs.loginForm.validate(async (valid) => {
        if (valid) {
          if (this.loginForm.validCode.toLowerCase() !== this.validCode.toLowerCase()) {
            this.$message.error("验证码错误");
            return;
          }
          try {
            const res = await request.post("/user/login", this.loginForm);
            if (res.code === '0') {
              const loginResult = await this.$store.dispatch('login', res.data);
              
              if (loginResult.error) {
                this.$message.error(loginResult.message);
                return;
              }
              
              if (loginResult.needVerification) {
                this.$message.warning(loginResult.message);
                this.$router.push({
                  path: '/owner/info',
                  query: { userId: loginResult.userId }
                });
                return;
              }
              
              if (loginResult.success) {
                try {
                  // 等待路由加载完成
                  await setRoutes();
                  
                  // 使用 nextTick 确保 DOM 更新完成
                  this.$nextTick(() => {
                    if (res.data.role === 'OWNER') {
                      this.$router.replace('/home');
                    } else {
                      this.$router.replace('/showView');
                    }
                    this.$message.success('登录成功');
                  });
                } catch (error) {
                  console.error('路由加载失败:', error);
                  this.$message.error('系统初始化失败，请重试');
                }
              }
            } else {
              this.$message.error(res.msg);
            }
          } catch (error) {
            console.error('登录失败:', error);
            this.$message.error('登录失败，请重试');
          }
        }
      });
    }
  }
};
</script>

<style scoped>
.login-wrapper {
    width: 100%;
    max-width: 440px;
    padding: 0 20px;
}

.login-box {
    backdrop-filter: blur(10px);
    background: rgba(255, 255, 255, 0.95);
    padding: 2.5rem;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.18);
}

.login-header {
    text-align: center;
    margin-bottom: 2.5rem;
}

.icon-wrapper {
    width: 80px;
    height: 80px;
    margin: 0 auto 1rem;
    background: linear-gradient(45deg, #1976d2, #64b5f6);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.icon-wrapper i {
    font-size: 40px;
    color: white;
}

.login-header h2 {
    color: #1e4976;
    font-size: 1.8rem;
    margin: 0 0 0.5rem;
    font-weight: 600;
}



.custom-input :deep(.el-input__inner) {
    height: 45px;
    line-height: 45px;
    border: 1px solid #e0e7ff;
    padding-left: 45px;
    transition: all 0.3s ease;
}

.custom-input :deep(.el-input__inner:focus) {
    border-color: #1976d2;
    box-shadow: 0 0 0 2px rgba(25, 118, 210, 0.1);
}

.custom-input :deep(.el-input__prefix) {
    left: 15px;
    color: #1976d2;
}

.verify-code {
    display: flex;
    gap: 12px;
}

.code-component {
    flex-shrink: 0;
    width: 120px;
    overflow: hidden;
}

.submit-btn {
    width: 100%;
    height: 45px;
    font-size: 1.1rem;
    background: linear-gradient(45deg, #1976d2, #2196f3) !important;
    border: none;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
    position: relative;
    overflow: hidden;
}

.submit-btn:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 15px rgba(25, 118, 210, 0.3);
}

.submit-btn:active {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(25, 118, 210, 0.2);
}

/* 禁用 element-ui 的默认悬停效果 */
.submit-btn:hover,
.submit-btn:focus {
    background: linear-gradient(45deg, #1976d2, #2196f3) !important;
    color: white !important;
}

.action-links {
    display: flex;
    justify-content: space-between;
    margin-top: 1rem;
}

.register-link, .forget-link {
    font-size: 0.9rem;
    transition: all 0.3s ease;
}

.register-link:hover, .forget-link:hover {
    transform: translateY(-1px);
}

@media screen and (max-width: 480px) {
    .login-box {
        padding: 2rem;
    }
    
    .icon-wrapper {
        width: 60px;
        height: 60px;
    }
    
    .icon-wrapper i {
        font-size: 30px;
    }
    
    .login-header h2 {
        font-size: 1.5rem;
    }
}
</style>