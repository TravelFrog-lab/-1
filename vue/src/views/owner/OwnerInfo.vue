<template>
  <div class="owner-info-wrapper">
    <el-card class="owner-info-box">
      <div class="info-header">
        <div class="icon-wrapper">
          <i class="el-icon-edit-outline"></i>
        </div>
        <h2>完善业主信息</h2>

      </div>

      <el-form ref="ownerForm" :model="ownerForm" :rules="rules">
        <div class="form-grid">
          <div class="form-column main-info">
            <div class="form-section">
              <h3 class="section-title">
                <i class="el-icon-user"></i> 基本信息
              </h3>
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="ownerForm.idCard" placeholder="请输入身份证号" :prefix-icon="'el-icon-document'" class="custom-input">
                </el-input>
              </el-form-item>

              <el-form-item label="手机号">
                <el-input v-model="ownerPhone" disabled :prefix-icon="'el-icon-phone'" class="custom-input">
                </el-input>
              </el-form-item>

              <el-form-item label="车牌号码" prop="plateNumber">
                <el-input v-model="ownerForm.plateNumber" placeholder="请输入车牌号码" :prefix-icon="'el-icon-truck'" class="custom-input">
                </el-input>
              </el-form-item>
            </div>

            <div class="form-section">
              <h3 class="section-title">
                <i class="el-icon-house"></i> 房屋信息
              </h3>
              <el-form-item label="楼栋" prop="buildingNo">
                <el-select v-model="ownerForm.buildingNo" placeholder="请选择楼栋" class="custom-select" @change="onBuildingChange">
                  <el-option v-for="b in buildingOptions" :key="b" :label="`${b}栋`" :value="b"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="单元" prop="unitNo">
                <el-select v-model="ownerForm.unitNo" placeholder="请选择单元" class="custom-select" @change="onUnitChange" :disabled="!ownerForm.buildingNo">
                  <el-option v-for="u in unitOptions" :key="u" :label="`${u}单元`" :value="u"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="房号" prop="houseId">
                <el-select v-model="ownerForm.houseId" placeholder="请选择房号" class="custom-select" :disabled="!ownerForm.unitNo">
                  <el-option v-for="h in roomOptions" :key="h.id" :label="`${h.roomNo}（${(h.houseType && h.houseType.name) || '未设置类型'}）`" :value="h.id"></el-option>
                </el-select>
              </el-form-item>

              <el-form-item label="入住时间" prop="checkInTime">
                <el-date-picker v-model="ownerForm.checkInTime" type="datetime" format="yyyy-MM-dd HH:mm:ss" value-format="yyyy-MM-dd HH:mm:ss"
                  placeholder="选择入住时间" class="custom-date-picker">
                </el-date-picker>
              </el-form-item>
            </div>
          </div>

          <div class="form-column side-info">
            <div class="form-section other-info">
              <h3 class="section-title">
                <i class="el-icon-notebook-2"></i> 其他信息
              </h3>
              <el-form-item label="宠物信息" prop="petInfo">
                <el-input type="textarea" v-model="ownerForm.petInfo" placeholder="请输入宠物信息" :rows="2" resize="none" class="custom-textarea">
                </el-input>
              </el-form-item>

              <el-form-item label="备注信息" prop="remarks">
                <el-input type="textarea" v-model="ownerForm.remarks" placeholder="请输入备注信息" :rows="2" resize="none" class="custom-textarea">
                </el-input>
              </el-form-item>
            </div>

            <div class="form-section family-info">
              <h3 class="section-title">
                <i class="el-icon-user-solid"></i> 家庭成员信息
              </h3>
              <el-form-item >
                <div class="family-members">
                  <div class="members-list">
                    <div v-for="(member, index) in familyMembers" :key="index" class="member-item">
                      <div class="member-header">
                        <span class="member-title">成员 {{ index + 1 }}</span>
                        <el-button type="text" @click="removeMember(index)" class="delete-btn">
                          <i class="el-icon-delete"></i>
                        </el-button>
                      </div>
                      <el-row :gutter="8">
                        <el-col :span="6">
                          <el-input v-model="member.name" placeholder="姓名" :prefix-icon="'el-icon-user'" class="custom-input">
                          </el-input>
                        </el-col>
                        <el-col :span="6">
                          <el-select v-model="member.relation" placeholder="关系" class="custom-select">
                            <el-option label="配偶" value="配偶"></el-option>
                            <el-option label="子女" value="子女"></el-option>
                            <el-option label="父母" value="父母"></el-option>
                            <el-option label="其他" value="其他"></el-option>
                          </el-select>
                        </el-col>
                        <el-col :span="6">
                          <el-input v-model="member.idCard" placeholder="身份证号" :prefix-icon="'el-icon-document'" class="custom-input">
                          </el-input>
                        </el-col>
                        <el-col :span="6">
                          <el-input v-model="member.phone" placeholder="联系电话" :prefix-icon="'el-icon-phone'" class="custom-input">
                          </el-input>
                        </el-col>
                      </el-row>
                    </div>
                  </div>
                  <div class="add-member">
                    <el-button type="primary" @click="addMember" class="add-btn">
                      <i class="el-icon-plus"></i> 添加家庭成员
                    </el-button>
                  </div>
                </div>
              </el-form-item>
            </div>
          </div>
        </div>

        <el-form-item class="form-buttons">
          <el-button type="primary" class="submit-btn" @click="submitForm">
            <i class="el-icon-check"></i> 提交
          </el-button>
          <el-button class="reset-btn" @click="resetForm">
            <i class="el-icon-refresh"></i> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'OwnerInfo',
  data() {
    const currentUser = JSON.parse(sessionStorage.getItem('currentUser') || '{}')
    return {
      ownerPhone: currentUser.phone || '',
      houses: [], // 房屋列表
      buildingOptions: [],
      unitOptions: [],
      roomOptions: [],
      familyMembers: [], // 家庭成员数组
      ownerForm: {
        userId: this.$route.query.userId || '',
        idCard: '',
        buildingNo: '',
        unitNo: '',
        houseId: '',
        plateNumber: '',
        petInfo: '',
        familyMembers: '', // 将作为JSON字符串存储
        checkInTime: '',
        remarks: '',
        status: 'DISABLED'
      },
      rules: {
        idCard: [
          { required: true, message: '请输入身份证号', trigger: 'blur' },
          { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
        ],
        buildingNo: [
          { required: true, message: '请选择楼栋', trigger: 'change' }
        ],
        unitNo: [
          { required: true, message: '请选择单元', trigger: 'change' }
        ],
        houseId: [
          { required: true, message: '请选择房号', trigger: 'change' }
        ],
        checkInTime: [
          { required: true, message: '请选择入住时间', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    if (!this.ownerForm.userId) {
      this.$message.error('缺少必要的用户信息');
      this.$router.push('/login');
      return;
    }
    this.getHouseList()
  },
  methods: {
    normalizePart(v) {
      if (v == null) return ''
      return String(v).trim().replace(/栋$/,'').replace(/单元$/,'').replace(/室$/,'')
    },
    splitAddressParts(address) {
      if (!address) return { buildingNo: '', unitNo: '', roomNo: '' }
      const m = String(address).trim().match(/^(.+?)栋[-\s]*(.+?)单元[-\s]*(.+)$/)
      if (m) {
        return {
          buildingNo: this.normalizePart(m[1]),
          unitNo: this.normalizePart(m[2]),
          roomNo: this.normalizePart(m[3])
        }
      }
      const parts = String(address).split('-')
      if (parts.length >= 3) {
        return {
          buildingNo: this.normalizePart(parts[0]),
          unitNo: this.normalizePart(parts[1]),
          roomNo: this.normalizePart(parts[2])
        }
      }
      return { buildingNo: '', unitNo: '', roomNo: '' }
    },
    rebuildBuildingOptions() {
      this.buildingOptions = Array.from(new Set(this.houses
        .map(h => this.normalizePart(h.buildingNo || this.splitAddressParts(h.address).buildingNo))
        .filter(Boolean)))
    },
    onBuildingChange() {
      const b = this.ownerForm.buildingNo
      this.ownerForm.unitNo = ''
      this.ownerForm.houseId = ''
      this.roomOptions = []
      this.unitOptions = Array.from(new Set(this.houses
        .filter(h => this.normalizePart(h.buildingNo || this.splitAddressParts(h.address).buildingNo) === b)
        .map(h => this.normalizePart(h.unitNo || this.splitAddressParts(h.address).unitNo))
        .filter(Boolean)))
    },
    onUnitChange() {
      const b = this.ownerForm.buildingNo
      const u = this.ownerForm.unitNo
      this.ownerForm.houseId = ''
      this.roomOptions = this.houses
        .map(h => {
          const p = this.splitAddressParts(h.address)
          return {
            ...h,
            buildingNo: this.normalizePart(h.buildingNo || p.buildingNo),
            unitNo: this.normalizePart(h.unitNo || p.unitNo),
            roomNo: this.normalizePart(h.roomNo || p.roomNo)
          }
        })
        .filter(h => h.buildingNo === b && h.unitNo === u)
    },
    getHouseList() {
      // 获取房屋列表
      request.get('/houses/all').then(res => {
        if (res.code === '0') {
          this.houses = res.data
          this.rebuildBuildingOptions()
        }
      })
    },
    submitForm() {
      this.$refs.ownerForm.validate((valid) => {
        if (valid) {
          // 验证家庭成员信息
          const isValidMembers = this.validateFamilyMembers()
          if (!isValidMembers) {
            return
          }

          // 将家庭成员数组转换为JSON字符串
          const submitData = {
            ...this.ownerForm,
            familyMembers: JSON.stringify(this.familyMembers)
          }

          request.post('/owner', submitData).then(res => {
            if (res.code === '0') {
              this.$message.success('信息提交成功,请等待管理员审核')
              this.$router.push('/login')
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    },
    resetForm() {
      this.$refs.ownerForm.resetFields()
      this.unitOptions = []
      this.roomOptions = []
    },
    // 添加家庭成员
    addMember() {
      this.familyMembers.push({
        name: '',
        relation: '',
        idCard: '',
        phone: ''
      })
    },
    // 移除家庭成员
    removeMember(index) {
      this.familyMembers.splice(index, 1)
    },
    // 验证家庭成员信息
    validateFamilyMembers() {
      for (let i = 0; i < this.familyMembers.length; i++) {
        const member = this.familyMembers[i]
        if (!member.name || !member.relation || !member.idCard || !member.phone) {
          this.$message.error('请完整填写家庭成员信息')
          return false
        }
        // 验证身份证号
        if (!/^(\d{15}$)|(^\d{18})|(^\d{17}(\d|X|x))$/.test(member.idCard)) {
          this.$message.error('请输入正确的身份证号')
          return false
        }
        // 验证手机号
        if (!/^1[3-9]\d{9}$/.test(member.phone)) {
          this.$message.error('请输入正确的手机号')
          return false
        }
      }
      return true
    }
  }
}
</script>

<style scoped>
.owner-info-wrapper {
  width: 100%;
  max-width: 1200px;
  padding: 0 20px;
}

.owner-info-box {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  padding: 2.5rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.info-header {
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

.info-header h2 {
  color: #1e4976;
  font-size: 1.8rem;
  margin: 0 0 0.5rem;
  font-weight: 600;
}


.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 1rem;
}

.form-column {
  flex: 1;
}

.custom-input :deep(.el-input__inner),
.custom-select :deep(.el-input__inner) {
  height: 45px;
  line-height: 45px;
  border: 1px solid #e0e7ff;
  padding-left: 45px;
  transition: all 0.3s ease;
}

.custom-input :deep(.el-input__inner:focus),
.custom-select :deep(.el-input__inner:focus) {
  border-color: #1976d2;
  box-shadow: 0 0 0 2px rgba(25, 118, 210, 0.1);
}

.custom-input :deep(.el-input__prefix) {
  left: 15px;
  color: #1976d2;
}

.custom-textarea :deep(.el-textarea__inner) {
  border: 1px solid #e0e7ff;
}

.custom-textarea :deep(.el-textarea__inner:focus) {
  border-color: #1976d2;
  box-shadow: 0 0 0 2px rgba(25, 118, 210, 0.1);
}

.family-members {
  background: transparent;
  padding: 0;
  height: 260px;
  display: flex;
  flex-direction: column;
}

.members-list {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 1rem;
  padding-right: 8px;
}

/* 自定义滚动条样式 */
.members-list::-webkit-scrollbar {
  width: 6px;
}

.members-list::-webkit-scrollbar-thumb {
  background-color: rgba(25, 118, 210, 0.2);
  border-radius: 3px;
}

.members-list::-webkit-scrollbar-track {
  background-color: transparent;
}

.member-item {
  background: white;
  padding: 1rem;
  margin-bottom: 0.8rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

/* 最后一个成员项去除底部间距 */
.member-item:last-child {
  margin-bottom: 0;
}

.member-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.6rem;
}

.member-title {
  font-weight: 500;
  color: #1e4976;
}

.delete-btn {
  color: #ff4d4f;
  padding: 4px 8px;
}

.delete-btn:hover {
  background: rgba(255, 77, 79, 0.1);
}

.add-btn {
  width: 180px;
  height: 45px;
  font-size: 1.1rem;
  border-radius: 8px;
  background: linear-gradient(45deg, #1976d2, #2196f3) !important;
  border: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.add-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(25, 118, 210, 0.3);
}

.add-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(25, 118, 210, 0.2);
}

/* 禁用 element-ui 的默认悬停效果 */
.add-btn:hover,
.add-btn:focus {
  background: linear-gradient(45deg, #1976d2, #2196f3) !important;
  color: white !important;
}

.add-member {
  text-align: center;
}

.form-buttons {
  text-align: center;
  margin-top: 1.5rem;
}

.submit-btn {
  width: 180px;
  height: 45px;
  font-size: 1.1rem;
  border-radius: 8px;
  background: linear-gradient(45deg, #1976d2, #2196f3) !important;
  border: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  margin-right: 1rem;
}

.reset-btn {
  width: 180px;
  height: 45px;
  font-size: 1.1rem;
  border-radius: 8px;
  background: white;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(25, 118, 210, 0.3);
}

.submit-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(25, 118, 210, 0.2);
}

.reset-btn:hover {
  border-color: #1976d2;
  color: #1976d2;
}

/* 禁用 element-ui 的默认悬停效果 */
.submit-btn:hover,
.submit-btn:focus {
  background: linear-gradient(45deg, #1976d2, #2196f3) !important;
  color: white !important;
}

.form-section {
  background: #f8faff;
  padding: 1.2rem;
  margin-bottom: 1rem;
}

.section-title {
  color: #1e4976;
  font-size: 1.1rem;
  font-weight: 500;
  margin: 0 0 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.section-title i {
  color: #1976d2;
  font-size: 1.2rem;
}

.main-info {
  display: flex;
  flex-direction: column;
}

.side-info {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.other-info,
.family-info {
  background: #f8faff;
  padding: 1.2rem;
}

@media screen and (max-width: 768px) {
  .owner-info-wrapper {
    max-width: 440px;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .form-section,
  .other-info,
  .family-info {
    padding: 0.8rem;
  }

  .owner-info-box {
    padding: 2rem;
  }

  .icon-wrapper {
    width: 60px;
    height: 60px;
  }

  .icon-wrapper i {
    font-size: 30px;
  }

  .info-header h2 {
    font-size: 1.5rem;
  }

  .form-buttons {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .submit-btn,
  .reset-btn {
    width: 100%;
    margin-right: 0;
  }

  .member-item .el-row {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .member-item .el-col {
    width: 100%;
  }

  .family-members {
    height: 280px;
  }

  .add-btn {
    width: 100%;
  }
}

/* 调整表单项的间距 */
:deep(.el-form-item) {
  margin-bottom: 0.8rem;
}

:deep(.el-form-item:last-child) {
  margin-bottom: 0;
}
</style> 