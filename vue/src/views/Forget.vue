<template>
    <div class="forget-wrapper">
        <el-card class="forget-box">
            <div class="forget-header">
                <div class="icon-wrapper">
                    <i class="el-icon-refresh"></i>
                </div>
                <h2>找回密码</h2>
             
            </div>
            <el-form ref="resetForm" :model="resetForm" :rules="rules">
                <el-form-item prop="phone">
                    <el-input 
                        v-model="resetForm.phone" 
                        placeholder="手机号"
                        :prefix-icon="'el-icon-mobile-phone'"
                        class="custom-input">
                    </el-input>
                </el-form-item>
                <el-form-item prop="code">
                    <div class="verify-code">
                        <el-input 
                            v-model="resetForm.code" 
                            placeholder="验证码"
                            :prefix-icon="'el-icon-message'"
                            class="custom-input">
                        </el-input>
                        <el-button 
                            type="primary" 
                            class="code-btn" 
                            :disabled="disabled"
                            @click="sendVerificationCode">
                            {{ buttonContent }}
                        </el-button>
                    </div>
                </el-form-item>
                <el-form-item prop="newPassword">
                    <el-input 
                        type="password" 
                        v-model="resetForm.newPassword" 
                        placeholder="新密码"
                        :prefix-icon="'el-icon-lock'"
                        class="custom-input">
                    </el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" class="submit-btn" @click="onResetPassword">
                        <i class="el-icon-refresh-right"></i>
                        重置密码
                    </el-button>
                </el-form-item>
                <div class="login-link">
                    想起密码了？<el-link type="primary" @click="$router.push('/login')">返回登录</el-link>
                </div>
            </el-form>
        </el-card>
    </div>
</template>

<script>
import request from "@/utils/request";

export default {
    name: 'ResetPassword',
    data() {
        // 自定义手机号验证规则
        const validatePhone = (rule, value, callback) => {
            if (!value) {
                callback(new Error('请输入手机号'));
            } else if (!/^1[3-9]\d{9}$/.test(value)) {
                callback(new Error('请输入正确的手机号'));
            } else {
                callback();
            }
        };
        
        return {
            countdown: 0,
            disabled: false,
            timer: null,
            phoneCode: '',
            buttonContent: '发送验证码',
            resetForm: {
                phone: '',
                code: '',
                newPassword: '',
            },
            rules: {
                phone: [
                    { required: true, validator: validatePhone, trigger: 'blur' }
                ],
                code: [
                    { required: true, message: '请输入验证码', trigger: 'blur' }
                ],
                newPassword: [
                    { required: true, message: '请输入新密码', trigger: 'blur' },
                    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
                ]
            }
        };
    },
    methods: {
        sendVerificationCode() {
            if (this.disabled) return;

            // 验证手机号
            this.$refs.resetForm.validateField('phone', (valid) => {
                if (valid) return;
                
                // 修改验证码请求接口
                request.get(`/sms/code/${this.resetForm.phone}`)
                    .then(res => {
                        if (res.code == '0') {
                            this.$message({
                                type: 'success',
                                message: "验证码已发送到您的手机"
                            })
                            this.phoneCode = res.data;
                        } else {
                            this.$message({
                                type: 'error',
                                message: res.msg
                            })
                            return
                        }
                    })

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
            });
        },
        onResetPassword() {
            this.$refs.resetForm.validate((valid) => {
                if (valid) {
                    if (this.resetForm.code != this.phoneCode) {
                        this.$message({
                            type: 'error',
                            message: '验证码不正确'
                        });
                        return;
                    }
                    // 发送重置密码请求
                    request.get("/user/forget", {
                        params: {
                            phone: this.resetForm.phone,
                            newPassword: this.resetForm.newPassword,
                        },
                    }).then(res => {
                        if (res.code == '0') {
                            this.$message({
                                type: 'success',
                                message: "密码重置成功"
                            });
                            this.$router.push("/login");
                        } else {
                            this.$message({
                                type: 'error',
                                message: res.msg
                            });
                        }
                    });
                } else {
                    console.error('重置密码失败: 表单校验失败');
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
.forget-wrapper {
    width: 100%;
    max-width: 440px;
    padding: 0 20px;
}

.forget-box {
    backdrop-filter: blur(10px);
    background: rgba(255, 255, 255, 0.95);
    padding: 2.5rem;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.18);
}

.forget-header {
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

.forget-header h2 {
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

@media screen and (max-width: 480px) {
    .forget-box {
        padding: 2rem;
    }
    
    .icon-wrapper {
        width: 60px;
        height: 60px;
    }
    
    .icon-wrapper i {
        font-size: 30px;
    }
    
    .forget-header h2 {
        font-size: 1.5rem;
    }
}
</style>