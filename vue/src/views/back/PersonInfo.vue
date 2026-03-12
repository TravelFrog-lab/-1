<template>
  <div class="wrapper">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h2>个人信息</h2>
        <div class="divider"></div>
        <p class="subtitle">Personal Information</p>
      </div>
    </div>

    <el-card class="info-card" shadow="never">
      <el-form 
        :model="userInfo" 
        :rules="rules" 
        ref="userInfoForm" 
        label-width="100px"
        class="user-form"
      >
        <el-form-item label="头像" prop="avatar">
          <div class="avatar-upload-wrap">
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="uploadAvatar"
            >
              <img v-if="userInfo.avatar" :src="avatarDisplayUrl" class="avatar-img" />
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <span class="avatar-tip">从本机选择 JPG/PNG 图片，大小不超过 2MB</span>
          </div>
        </el-form-item>

        <el-form-item label="用户名" prop="username">
          <el-input v-model="userInfo.username" disabled></el-input>
        </el-form-item>
        
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userInfo.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        
        <el-form-item label="性别" prop="sex">
          <el-select v-model="userInfo.sex" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" value="MALE"></el-option>
            <el-option label="女" value="FEMALE"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="电话" prop="phone">
          <el-input v-model="userInfo.phone" placeholder="请输入电话号码"></el-input>
        </el-form-item>
        
        <el-form-item label="角色">
          <el-tag type="success">{{ getRoleName(userInfo.role) }}</el-tag>
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading"
            @click="update"
            icon="el-icon-check"
          >
            {{ loading ? '保存中...' : '保存修改' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request';
import { mapGetters } from 'vuex'

export default {
  name: 'PersonInfo',
  
  data() {
    return {
      loading: false,
      userInfo: {
        username: '',
        name: '',
        sex: '',
        phone: '',
        role: '',
        avatar: ''
      },
      rules: {
        name: [
          { required: true, message: '姓名不能为空', trigger: 'blur' },
          { min: 2, max: 10, message: '姓名长度必须在2到10个字符之间', trigger: 'blur' }
        ],
        sex: [
          { required: true, message: '性别不能为空', trigger: 'change' }
        ],
        phone: [
          { required: true, message: '电话不能为空', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
      },
      roleMap: {
        'ADMIN': '管理员',
        'OWNER': '业主',
        'MAINTENANCE': '维修人员',
        'KEEPER': '家政人员'
      }
    };
  },

  computed: {
    ...mapGetters(['currentUser']),
    avatarDisplayUrl() {
      const avatar = this.userInfo.avatar
      if (!avatar) return ''
      if (avatar.startsWith('http')) return avatar
      return '/api' + (avatar.startsWith('/') ? avatar : '/' + avatar)
    }
  },

  created() {
    this.initUserInfo();
  },

  methods: {
    initUserInfo() {
      if (this.currentUser) {
        this.userInfo = { ...this.currentUser };
      }
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
        const res = await request.post('/file/upload/img', formData)
        if (res.code === '0' && res.data) {
          this.userInfo.avatar = res.data
          await request.put(`/user/${this.currentUser.id}`, this.userInfo)
          this.$store.commit('SET_CURRENT_USER', { ...this.currentUser, ...this.userInfo })
          this.$message.success('头像上传成功')
        } else {
          this.$message.error(res.msg || '上传失败')
        }
      } catch (err) {
        this.$message.error('上传失败，请稍后重试')
      }
    },

    getRoleName(role) {
      return this.roleMap[role] || '未知角色';
    },

    async update() {
      try {
        const valid = await this.$refs.userInfoForm.validate();
        if (!valid) return;

        this.loading = true;
        const response = await request.put(`/user/${this.currentUser.id}`, this.userInfo);
        
        if (response.code === '0') {
          this.$store.commit('SET_CURRENT_USER', {
            ...this.currentUser,
            ...this.userInfo
          });

          this.$message.success('信息保存成功!');
        } else {
          this.$message.error(response.msg || '保存失败');
        }
      } catch (error) {
        this.$message.error('操作失败：' + (error.message || '未知错误'));
      } finally {
        this.loading = false;
      }
    }
  }
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

  .info-card {
    max-width: 600px;
    margin: 0 auto;
    
    .user-form {
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
          }
        }
      }

      .el-tag {
        padding: 6px 16px;
        font-size: 14px;
      }
    }
  }

  .avatar-upload-wrap {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;

    .avatar-uploader {
      :deep(.el-upload) {
        border: 1px dashed #d9d9d9;
        border-radius: 50%;
        cursor: pointer;
        overflow: hidden;
        transition: border-color 0.3s;
        &:hover {
          border-color: #409eff;
        }
      }
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
  }
}
</style>