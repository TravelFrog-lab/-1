<template>
  <div class="profile-page">
    <nav-header></nav-header>

    <div class="content-wrapper">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2 class="page-title">个人中心</h2>
      </div>

      <!-- 基本信息卡片 -->
      <el-card class="info-card" shadow="never">
        <div class="form-header">
          <div class="header-left">
            <i class="el-icon-user"></i>
            <span>基本信息</span>
          </div>
          <div class="header-right">
            <el-button type="text" @click="handleEdit">
              <i class="el-icon-edit"></i> 编辑
            </el-button>
          </div>
        </div>

        <!-- 头像上传 -->
        <div class="avatar-section">
          <div class="avatar-label">头像</div>
          <div class="avatar-upload-wrap">
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="uploadAvatar"
            >
              <img v-if="currentOwnerInfo.user?.avatar" :src="avatarDisplayUrl" class="avatar-img" />
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <span class="avatar-tip">从本机选择 JPG/PNG 图片，大小不超过 2MB</span>
          </div>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">
            {{ currentOwnerInfo.user?.name || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ currentOwnerInfo.user?.username || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ getSexText(currentOwnerInfo.user?.sex) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ currentOwnerInfo.user?.phone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="身份证号">
            {{ currentOwnerInfo.idCard || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="工作单位">
            {{ currentOwnerInfo.workplace || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="车牌号">
            {{ currentOwnerInfo.plateNumber || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="入住时间">
            {{ formatTime(currentOwnerInfo.checkInTime) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="房屋地址">
            {{ currentOwnerInfo.house?.address || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="房屋类型">
            {{ currentOwnerInfo.house?.houseType?.name || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="装修情况">
            {{ getDecorationStatus(currentOwnerInfo.decorationStatus) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="宠物信息">
            {{ currentOwnerInfo.petInfo || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="家庭成员" :span="2">
            <el-table v-if="familyMembers.length" :data="familyMembers" style="width: 100%" border>
              <el-table-column prop="name" label="姓名"></el-table-column>
              <el-table-column prop="relation" label="关系"></el-table-column>
              <el-table-column prop="idCard" label="身份证号"></el-table-column>
              <el-table-column prop="phone" label="联系电话"></el-table-column>
            </el-table>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="备注信息" :span="2">
            {{ currentOwnerInfo.remarks || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 账号安全卡片 -->
      <el-card class="security-card" shadow="never">
        <div class="form-header">
          <div class="header-left">
            <i class="el-icon-lock"></i>
            <span>账号安全</span>
          </div>
        </div>

        <div class="security-list">
          <div class="security-item">
            <div class="item-left">
              <div class="item-title">登录密码</div>
              <div class="item-desc">定期修改密码可以保护账号安全</div>
            </div>
            <div class="item-right">
              <el-button type="text" @click="handlePassword">修改密码</el-button>
            </div>
          </div>
          <div class="security-item">
            <div class="item-left">
              <div class="item-title">绑定手机</div>
              <div class="item-desc">已绑定手机：{{ maskPhone(currentOwnerInfo.user?.phone) }}</div>
            </div>
            <div class="item-right">
              <el-button type="text" @click="handlePhone">更换手机</el-button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 编辑信息弹窗 -->
      <el-dialog title="编辑信息" :visible.sync="dialogVisible" width="800px" :close-on-click-modal="false">
        <el-form ref="form" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="姓名" prop="user.name">
            <el-input v-model="form.user.name"></el-input>
          </el-form-item>
          <el-form-item label="性别" prop="user.sex">
            <el-radio-group v-model="form.user.sex">
              <el-radio label="MALE">男</el-radio>
              <el-radio label="FEMALE">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="工作单位" prop="workplace">
            <el-input v-model="form.workplace"></el-input>
          </el-form-item>
          <el-form-item label="车牌号" prop="plateNumber">
            <el-input v-model="form.plateNumber"></el-input>
          </el-form-item>
          <el-form-item label="宠物信息" prop="petInfo">
            <el-input v-model="form.petInfo" type="textarea"></el-input>
          </el-form-item>
          <el-form-item label="装修情况" prop="decorationStatus">
            <el-select v-model="form.decorationStatus" placeholder="请选择">
              <el-option label="未装修" value="NONE"></el-option>
              <el-option label="简单装修" value="SIMPLE"></el-option>
              <el-option label="精装修" value="LUXURY"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="备注信息" prop="remarks">
            <el-input v-model="form.remarks" type="textarea"></el-input>
          </el-form-item>
          <el-form-item label="家庭成员">
            <div class="family-members">
              <div v-for="(member, index) in form.familyMembers" :key="index" class="member-item">
                <el-row :gutter="10">
                  <el-col :span="5">
                    <el-input v-model="member.name" placeholder="姓名"></el-input>
                  </el-col>
                  <el-col :span="5">
                    <el-select v-model="member.relation" placeholder="关系">
                      <el-option label="配偶" value="配偶"></el-option>
                      <el-option label="子女" value="子女"></el-option>
                      <el-option label="父母" value="父母"></el-option>
                      <el-option label="其他" value="其他"></el-option>
                    </el-select>
                  </el-col>
                  <el-col :span="7">
                    <el-input v-model="member.idCard" placeholder="身份证号"></el-input>
                  </el-col>
                  <el-col :span="6">
                    <el-input v-model="member.phone" placeholder="联系电话"></el-input>
                  </el-col>
                  <el-col :span="1">
                    <el-button type="text" @click="removeMember(index)" class="delete-btn">
                      <i class="el-icon-delete"></i>
                    </el-button>
                  </el-col>
                </el-row>
              </div>
              <div class="add-member">
                <el-button type="text" @click="addMember">
                  <i class="el-icon-plus"></i> 添加家庭成员
                </el-button>
              </div>
            </div>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            确 定
          </el-button>
        </div>
      </el-dialog>

      <!-- 修改密码弹窗 -->
      <el-dialog title="修改密码" :visible.sync="passwordVisible" width="500px" :close-on-click-modal="false">
        <el-form ref="passwordForm" :model="passwordForm" :rules="passwordRules" label-width="100px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password>
            </el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password>
            </el-input>
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password>
            </el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="passwordVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitPassword" :loading="passwordSubmitting">
            确 定
          </el-button>
        </div>
      </el-dialog>

      <!-- 更换手机号弹窗 -->
      <el-dialog title="更换手机号" :visible.sync="phoneVisible" width="500px" :close-on-click-modal="false">
        <el-form ref="phoneForm" :model="phoneForm" :rules="phoneRules" label-width="100px">
          <el-form-item label="新手机号" prop="newPhone">
            <el-input 
              v-model="phoneForm.newPhone" 
              placeholder="请输入新手机号"
              maxlength="11"
              show-word-limit
            >
              <i slot="prefix" class="el-icon-mobile-phone"></i>
            </el-input>
          </el-form-item>
          <el-form-item label="验证码" prop="code">
            <div class="verification-code-input">
              <el-input 
                v-model="phoneForm.code" 
                placeholder="请输入验证码" 
                maxlength="6"
                :disabled="!phoneForm.newPhone"
              >
                <i slot="prefix" class="el-icon-key"></i>
              </el-input>
              <el-button 
                type="primary" 
                class="send-code-btn"
                @click="sendCode"
                :disabled="countdown > 0 || codeSending || !phoneForm.newPhone"
                :loading="codeSending"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <div class="phone-tips">
            <i class="el-icon-info"></i>
            <span>更换手机号后，新手机号将作为登录账号使用</span>
          </div>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="phoneVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitPhone" :loading="phoneSubmitting">
            确 定
          </el-button>
        </div>
      </el-dialog>
    </div>

    <page-footer></page-footer>
  </div>
</template>

<script>
import NavHeader from '@/components/front/NavHeader.vue'
import PageFooter from '@/components/front/PageFooter.vue'
import Request from '@/utils/request'
import moment from 'moment'
import { mapGetters } from 'vuex'
export default {
  name: 'Profile',
  components: {
    NavHeader,
    PageFooter
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'currentUser', 'ownerInfo']),
    avatarDisplayUrl() {
      const avatar = this.currentOwnerInfo.user?.avatar
      if (!avatar) return ''
      if (avatar.startsWith('http')) return avatar
      return '/api' + (avatar.startsWith('/') ? avatar : '/' + avatar)
    }
  },
  data() {
    // 确认密码验证
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    return {
      // 编辑弹窗
      dialogVisible: false,
      submitting: false,
      familyMembers: [], // 家庭成员列表
      currentOwnerInfo: {},
      form: {
        user: {
          name: '',
          sex: 'MALE', // 默认值改为 MALE
        },
        workplace: '',
        plateNumber: '',
        petInfo: '',
        decorationStatus: 'NONE',
        remarks: '',
        familyMembers: [] // 添加家庭成员数组
      },

      rules: {
        'user.name': [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],

        'user.sex': [
          { required: true, message: '请选择性别', trigger: 'change' }
        ],

        'familyMembers.*.name': [
          { required: true, message: '请输入姓名', trigger: 'blur' }
        ],
        'familyMembers.*.relation': [
          { required: true, message: '请选择关系', trigger: 'change' }
        ],
        'familyMembers.*.idCard': [
          { required: true, message: '请输入身份证号', trigger: 'blur' },
          { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
        ],
        'familyMembers.*.phone': [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ]
      },

      // 修改密码弹窗
      passwordVisible: false,
      passwordSubmitting: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入原密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },

      // Add phone dialog data
      phoneVisible: false,
      phoneSubmitting: false,
      phoneForm: {
        newPhone: '',
        code: ''
      },
      phoneRules: {
        newPhone: [
          { required: true, message: '请输入新手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' },
          { 
            validator: (rule, value, callback) => {
              if (!value) {
                callback()
                return
              }
              if (value !== this.sentCode) {
                callback(new Error('验证码错误'))
              } else {
                callback()
              }
            }, 
            trigger: 'blur' 
          }
        ]
      },
      sentCode: '', // 存储发送的验证码
      codeSending: false,
      countdown: 0
    }
  },
  created() {
    this.getOwnerInfo()
  },
  mounted() {
    console.log(this.ownerInfo)
  },
  methods: {
    // 获取业主信息
    getOwnerInfo() {
      Request.get(`/owner/${this.ownerInfo.id}`).then(response => {
        if (response.code === '0' && response.data) {
          this.currentOwnerInfo = response.data
          // 解析家庭成员JSON
          try {
            this.familyMembers = JSON.parse(this.currentOwnerInfo.familyMembers || '[]')
          } catch (e) {
            this.familyMembers = []
          }
        } else {
          this.$message.error(response.msg || '获取业主信息失败')
        }
      })
    },

    beforeAvatarUpload(file) {
      const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isImage) {
        this.$message.error('头像只能是 JPG 或 PNG 格式')
        return false
      }
      if (!isLt2M) {
        this.$message.error('头像大小不能超过 2MB')
        return false
      }
      return true
    },

    async uploadAvatar({ file }) {
      const formData = new FormData()
      formData.append('file', file)
      try {
        const res = await Request.post('/file/upload/img', formData)
        if (res.code === '0' && res.data) {
          const userId = this.currentOwnerInfo.user?.id
          if (!userId) {
            this.$message.error('用户信息异常')
            return
          }
          const avatarPath = typeof res.data === 'string' ? res.data : (res.data.path || res.data.url || '')
          if (!avatarPath) {
            this.$message.error('上传返回路径异常')
            return
          }
          const updateRes = await Request.put(`/user/${userId}/avatar`, { avatar: avatarPath })
          if (updateRes.code === '0') {
            if (this.currentOwnerInfo.user) this.currentOwnerInfo.user.avatar = avatarPath
            this.$store.commit('SET_CURRENT_USER', { ...this.currentUser, avatar: avatarPath })
            this.$message.success('头像上传成功')
          } else {
            this.$message.error(updateRes.msg || '保存头像失败')
          }
        } else {
          this.$message.error(res.msg || '上传失败')
        }
      } catch (err) {
        const msg = (err.response && err.response.data && err.response.data.msg) ? err.response.data.msg : (err.message || '上传失败，请稍后重试')
        this.$message.error(msg)
      }
    },

    // 编辑信息
    handleEdit() {
      this.form = {
        user: {
          name: this.currentOwnerInfo.user?.name,
          sex: this.currentOwnerInfo.user?.sex || 'MALE'
        },
        workplace: this.currentOwnerInfo.workplace,
        plateNumber: this.currentOwnerInfo.plateNumber,
        petInfo: this.currentOwnerInfo.petInfo,
        decorationStatus: this.currentOwnerInfo.decorationStatus,
        remarks: this.currentOwnerInfo.remarks,
        
        familyMembers: JSON.parse(this.currentOwnerInfo.familyMembers || '[]')
      }
      this.dialogVisible = true
    },

    // 提交编辑
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.submitting = true

          // 更新用户信息
          const userId = this.currentOwnerInfo.user.id

          // 更新业主信息
          Request.put(`/owner/${this.currentOwnerInfo.id}`, {
            ...this.currentOwnerInfo,
            user: {
              ...this.currentOwnerInfo.user,
              name: this.form.user.name,
              sex: this.form.user.sex
            },
            workplace: this.form.workplace,
            plateNumber: this.form.plateNumber,
            petInfo: this.form.petInfo,
            decorationStatus: this.form.decorationStatus,
            remarks: this.form.remarks,
            familyMembers: JSON.stringify(this.form.familyMembers) // 转换为JSON字符串

          }).then(response => {
            if (response.code === '0') {
              this.$message.success('更新成功')
              this.submitting = false
              this.dialogVisible = false
              this.getOwnerInfo()
            } else {
              this.$message.error(response.msg || '更新失败')
              this.submitting = false
            }
          })

        }
      })

    },

    // 修改密码
    handlePassword() {
      this.passwordForm = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      this.passwordVisible = true
    },

    // 提交密码修改
    submitPassword() {
      this.$refs.passwordForm.validate(valid => {
        if (valid) {
          this.passwordSubmitting = true
          const userId = this.currentOwnerInfo.user.id
          Request.put(`/user/password/${userId}`, {
            oldPassword: this.passwordForm.oldPassword,
            newPassword: this.passwordForm.newPassword
          }).then(response => {
            if (response.code === '0') {
              this.$message.success('密码修改成功')
              this.passwordVisible = false
            } else {
              this.$message.error(response.msg || '密码修改失败')
            }
          }).finally(() => {
            this.passwordSubmitting = false
          })
        }
      })
    },

    // 更换手机号
    handlePhone() {
      this.phoneForm = {
        newPhone: '',
        code: ''
      }
      this.phoneVisible = true
    },

    // 修改发送验证码方法
    async sendCode() {
      if (!this.phoneForm.newPhone) {
        this.$message.warning('请先输入新手机号')
        return
      }
      
      if (!/^1[3-9]\d{9}$/.test(this.phoneForm.newPhone)) {
        this.$message.warning('请输入正确的手机号')
        return
      }

      this.codeSending = true
      try {
        const response = await Request.get(`/sms/code/${this.phoneForm.newPhone}`)
        if (response.code === '0') {
          this.sentCode = response.data // 保存服务器返回的验证码
          this.$message.success('验证码已发送')
          this.countdown = 60
          this.startCountdown()
        } else {
          this.$message.error(response.msg || '验证码发送失败')
        }
      } catch (error) {
        this.$message.error('验证码发送失败')
      }
      this.codeSending = false
    },

    // Countdown timer
    startCountdown() {
      if (this.countdown > 0) {
        setTimeout(() => {
          this.countdown--
          this.startCountdown()
        }, 1000)
      }
    },

    // 修改提交方法
    submitPhone() {
      this.$refs.phoneForm.validate(valid => {
        if (valid) {
          this.phoneSubmitting = true
          const userId = this.currentOwnerInfo.user.id
          
          // 构建更新用户信息的对象
          const updateUserData = {
            ...this.currentOwnerInfo.user,
            phone: this.phoneForm.newPhone
          }

          // 调用更新用户信息接口
          Request.put(`/user/${userId}`, updateUserData)
            .then(response => {
              if (response.code === '0') {
                this.$message.success('手机号更换成功')
                this.phoneVisible = false
                this.getOwnerInfo() // 刷新用户信息
              } else {
                this.$message.error(response.msg || '手机号更换失败')
              }
            })
            .finally(() => {
              this.phoneSubmitting = false
            })
        }
      })
    },

    // 格式化时间
    formatTime(time) {
      if (!time) return '-'
      return moment(time).format('YYYY-MM-DD HH:mm:ss')
    },

    // 手机号脱敏
    maskPhone(phone) {
      if (!phone) return '-'
      return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
    },

    // 获取装修状态文本
    getDecorationStatus(status) {
      const map = {
        'NONE': '未装修',
        'SIMPLE': '简单装修',
        'LUXURY': '精装修'
      }
      return map[status] || status
    },

    // 获取性别显示文本
    getSexText(sex) {
      const map = {
        'MALE': '男',
        'FEMALE': '女'
      }
      return map[sex] || sex
    },

    // 添加家庭成员
    addMember() {
      this.form.familyMembers.push({
        name: '',
        relation: '',
        idCard: '',
        phone: ''
      })
    },

    // 移除家庭成员
    removeMember(index) {
      this.form.familyMembers.splice(index, 1)
    }
  }
}
</script>

<style scoped>
/* 页面样式 */
.profile-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.content-wrapper {
  flex: 1;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

/* 页面标题 */
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
  font-weight: bold;
}

/* 卡片样式 */
.info-card,
.security-card {
  background: #fff;
  padding: 20px;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.info-card:hover,
.security-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

/* 表单头部 */
.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
  margin-bottom: 15px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-left i {
  font-size: 18px;
  margin-right: 8px;
  color: #409eff;
}

.header-left span {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

/* 安全设置列表 */
.security-list {
  padding: 0 20px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.security-item:last-child {
  border-bottom: none;
}

.item-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 8px;
}

.item-desc {
  font-size: 14px;
  color: #909399;
}

/* 家庭成员编辑样式 */
.family-members {
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.member-item {
  margin-bottom: 10px;
}

.member-item:last-child {
  margin-bottom: 0;
}

.delete-btn {
  color: #f56c6c;
  padding: 0;
}

.add-member {
  margin-top: 10px;
  text-align: center;
}

/* 头像上传 */
.avatar-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.avatar-section .avatar-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

.avatar-upload-wrap {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.avatar-upload-wrap .avatar-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-upload-wrap .avatar-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.avatar-uploader-icon {
  display: block;
  width: 100px;
  height: 100px;
  line-height: 100px;
  font-size: 24px;
  color: #8c939d;
  text-align: center;
  background-color: #fafafa;
  border-radius: 50%;
}

.avatar-img {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-tip {
  font-size: 12px;
  color: #909399;
}

/* 手机号更换弹窗样式 */
.verification-code-input {
  display: flex;
  align-items: center;
  gap: 12px;
}

.verification-code-input .el-input {
  flex: 1;
}

.send-code-btn {
  width: 120px;
  height: 40px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 4px;
  transition: all 0.3s;
}

/* 按钮悬停效果 */
.send-code-btn:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

/* 对话框底部按钮样式 */
.dialog-footer {
  padding-top: 10px;
}

.dialog-footer .el-button {
  padding: 12px 24px;
  font-size: 14px;
  font-weight: 500;
  min-width: 100px;
  border-radius: 4px;
  transition: all 0.3s;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
  border: none;
}

.dialog-footer .el-button--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
}

.dialog-footer .el-button--default {
  border: 1px solid #dcdfe6;
  color: #606266;
}

.dialog-footer .el-button--default:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

/* 输入框图标样式 */
.el-input__prefix {
  font-size: 16px;
  color: #909399;
}

.el-icon-mobile-phone {
  font-size: 18px;
}

/* 适配移动端 */
@media screen and (max-width: 768px) {
  .content-wrapper {
    padding: 15px;
  }

  .info-card,
  .security-card {
    padding: 15px;
  }

  .security-list {
    padding: 0;
  }

  .security-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .item-right {
    margin-top: 10px;
  }

  .el-dialog {
    width: 95% !important;
    margin: 10px auto !important;
  }

  .member-item .el-row {
    margin-left: 0 !important;
    margin-right: 0 !important;
  }

  .member-item .el-col {
    padding-left: 0 !important;
    padding-right: 0 !important;
    margin-bottom: 10px;
  }

  .member-item .el-col:last-child {
    margin-bottom: 0;
  }

  .member-item .el-select {
    width: 100%;
  }

  .verification-code-input {
    flex-direction: column;
    gap: 10px;
  }

  .send-code-btn {
    width: 100%;
    height: 40px;
  }

  .dialog-footer .el-button {
    width: 100%;
    margin: 5px 0 !important;
  }

  .phone-tips {
    margin-left: 0;
    padding: 0 20px;
  }
}
</style> 