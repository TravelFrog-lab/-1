<template>
  <div class="register-wrapper">
    <el-card class="register-box">
      <div class="register-header">
        <div class="icon-wrapper">
          <i class="el-icon-plus"></i>
        </div>
        <h2>账号注册</h2>

      </div>
      <el-form ref="registerForm" :model="registerForm" :rules="rules">
        <div class="form-grid">
          <div class="form-column">
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" placeholder="用户名" :prefix-icon="'el-icon-user'" class="custom-input">
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input type="password" v-model="registerForm.password" placeholder="密码" :prefix-icon="'el-icon-lock'" class="custom-input">
              </el-input>
            </el-form-item>
            <el-form-item prop="name">
              <el-input v-model="registerForm.name" placeholder="姓名" :prefix-icon="'el-icon-user'" class="custom-input">
              </el-input>
            </el-form-item>
            <el-form-item prop="sex">
              <el-select v-model="registerForm.sex" placeholder="请选择性别" class="custom-select">
                <el-option label="男" value="MALE">
                  <i class="el-icon-male"></i> 男
                </el-option>
                <el-option label="女" value="FEMALE">
                  <i class="el-icon-female"></i> 女
                </el-option>
              </el-select>
            </el-form-item>
          </div>
          <div class="form-column">
            <el-form-item prop="phone">
              <el-input v-model="registerForm.phone" placeholder="手机号" :prefix-icon="'el-icon-mobile-phone'" class="custom-input">
              </el-input>
            </el-form-item>
            <el-form-item prop="code">
              <div class="verify-code">
                <el-input v-model="registerForm.code" placeholder="验证码" :prefix-icon="'el-icon-message'" class="custom-input">
                </el-input>
                <el-button type="primary" class="code-btn" :disabled="disabled" @click="sendVerificationCode">
                  {{ buttonContent }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item prop="role">
              <el-select v-model="registerForm.role" placeholder="请选择身份" class="custom-select">
                <el-option label="业主" value="OWNER">
                  <i class="el-icon-user"></i> 业主
                </el-option>
                <el-option label="管理员" value="ADMIN">
                  <i class="el-icon-s-custom"></i> 管理员
                </el-option>
                <el-option label="维修人员" value="MAINTENANCE">
                  <i class="el-icon-s-cooperation"></i> 维修人员
                </el-option>
              </el-select>
            </el-form-item>
            <template v-if="registerForm.role === 'ADMIN'">
              <el-form-item prop="invitationCode">
                <el-input v-model="registerForm.invitationCode" placeholder="邀请码" :prefix-icon="'el-icon-key'" class="custom-input">
                </el-input>
              </el-form-item>
            </template>
          </div>
        </div>
        <el-form-item>
          <el-button type="primary" class="submit-btn" @click="onRegister">
            <i class="el-icon-check"></i>
            注册
          </el-button>
        </el-form-item>
        <div class="login-link">
          已有账号？<el-link type="primary" @click="$router.push('/login')">立即登录</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import request from "@/utils/request";
export default {
  name: 'Register',
  data() {
    return {
      adminVitationCode: '123456',
      countdown: 0,
      disabled: false,
      timer: null,
      verificationCode: '',
      buttonContent: '发送验证码',
      registerForm: {
        username: '',
        password: '',
        name: '',
        sex: '',
        phone: '',
        code: '',
        role: 'OWNER',
        invitationCode: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' }
        ],
        sex: [
          { required: true, message: '请选择性别', trigger: 'change' }
        ],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入验证码', trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择身份', trigger: 'change' }
        ],
      
        invitationCode: [
          { required: true, message: '请输入邀请码', trigger: 'blur' },
          { min: 6, max: 6, message: '邀请码长度必须为6位', trigger: 'blur' }
        ]
      }
    };
  },
  methods: {
    sendVerificationCode() {
      if (this.disabled) return;

      if (!this.registerForm.phone) {
        this.$message.error('请先输入手机号');
        return;
      }

      // 修改验证码请求接口
      request.get(`/sms/code/${this.registerForm.phone}`).then(res => {
        if (res.code == '0') {
          this.$message({
            type: 'success',
            message: "验证码已发送到您的手机"
          })
          this.verificationCode = res.data;
          this.countdown = 60;
          this.disabled = true;
          this.buttonContent = `${this.countdown}秒后可重发`;

          this.timer = setInterval(() => {
            if (this.countdown > 0) {
              this.countdown--;
              this.buttonContent = `${this.countdown}秒后可重发`;
            } else {
              clearInterval(this.timer);
              this.countdown = 0;
              this.disabled = false;
              this.buttonContent = '发送验证码';
            }
          }, 1000);
        } else {
          this.$message({
            type: 'error',
            message: res.msg
          })
          return
        }
      })


    },
    onRegister() {
      this.$refs.registerForm.validate((valid) => {
        if (valid) {
          // if (this.registerForm.code != this.verificationCode) {
          //     this.$message({
          //         type: 'error',
          //         message: '验证码不正确'
          //     });
          //     return;
          // }
          if (this.registerForm.role == 'ADMIN') {
            if (this.registerForm.invitationCode != this.adminVitationCode) {
              this.$message({
                type: 'error',
                message: '邀请码错误'
              });
              return;
            }
          }
          request.post("/user/add", this.registerForm).then(res => {
            if (res.code == '0') {
              this.$message({
                type: 'success',
                message: "注册成功",
              })
              // 根据角色判断跳转路径
              if (this.registerForm.role === 'OWNER') {
                this.$router.push({
                  path: '/owner/info',
                  query: { userId: res.data.id }
                });
              } else {
                this.$router.push({
                  path: '/login',
                  query: { registerUser: res.data }
                });
              }
            } else {
              this.$message({
                type: 'error',
                message: res.msg
              });
            }
          })
        } else {
          console.error('注册失败: 表单校验失败');
          return false;
        }
      });
    }
  },
  beforeDestroy() {
    clearInterval(this.timer);
  },
};
</script>

<style scoped>
.register-wrapper {
  width: 100%;
  max-width: 800px;
  padding: 0 20px;
}

.register-box {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  padding: 2.5rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.register-header {
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

.register-header h2 {
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

.custom-select {
  width: 100%;
}

.custom-select :deep(.el-input__inner) {
  height: 45px;
  line-height: 45px;
  border: 1px solid #e0e7ff;
  transition: all 0.3s ease;
}

.verify-code {
  display: flex;
  gap: 12px;
}

.code-btn {
  width: 120px;
  background: linear-gradient(45deg, #1976d2, #2196f3) !important;
  border: none;
}

.code-btn:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(25, 118, 210, 0.3);
}

.submit-btn {
  width: 100%;
  height: 45px;
  font-size: 1.1rem;
  background: linear-gradient(45deg, #1976d2, #2196f3) !important;
  border: none;
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
.submit-btn:focus,
.code-btn:hover,
.code-btn:focus {
  background: linear-gradient(45deg, #1976d2, #2196f3) !important;
  color: white !important;
}

.login-link {
  text-align: center;
  margin-top: 1rem;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 2rem;
  margin-bottom: 1rem;
}

.form-column {
  flex: 1;
}

@media screen and (max-width: 768px) {
  .register-wrapper {
    max-width: 440px;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .register-box {
    padding: 2rem;
  }

  .icon-wrapper {
    width: 60px;
    height: 60px;
  }

  .icon-wrapper i {
    font-size: 30px;
  }

  .register-header h2 {
    font-size: 1.5rem;
  }
}
</style>