<template>
  <div class="ai-assistant-page">
    <div class="content-wrapper">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="intro-card" shadow="never">
            <h2 class="title">AI助手</h2>
            <p class="subtitle">一站式智能问答，帮您快速了解社区服务</p>
            <div class="section-title">我能帮您做什么？</div>
            <ul class="feature-list">
              <li><span class="dot"></span>物业费怎么交？</li>
              <li><span class="dot"></span>家政服务怎么约？</li>
              <li><span class="dot"></span>如何报修？</li>
              <li><span class="dot"></span>小区活动有哪些？</li>
            </ul>
            <div class="tip">
              直接输入您的问题，我会尽力为您解答。未匹配到的问题将自动记录，由物业人员补充。
            </div>
          </el-card>
        </el-col>
        <el-col :span="16">
          <el-card class="chat-card" shadow="never">
            <div class="chat-header">
              <span class="chat-title">与 AI 助手对话</span>
              <span class="chat-subtitle">可以试着问：物业费怎么交？怎么报修？家政服务怎么约？</span>
            </div>
            <div class="chat-window" ref="chatWindow">
              <div v-for="(msg, index) in messages" :key="index" class="chat-item" :class="msg.role">
                <div class="avatar">
                  <i v-if="msg.role === 'assistant'" class="el-icon-service"></i>
                  <i v-else class="el-icon-user"></i>
                </div>
                <div class="bubble" v-html="msg.content"></div>
              </div>
            </div>
            <div class="chat-input">
              <el-input
                v-model="question"
                type="textarea"
                :rows="2"
                placeholder="请输入您的问题，例如：怎么交物业费？"
                @keyup.enter.native.exact.prevent="handleSend"
              />
              <div class="chat-actions">
                <span class="hint">提示：问题越具体，答案越准确。</span>
                <el-button type="primary" :loading="sending" @click="handleSend">发送</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    <page-footer></page-footer>
  </div>
</template>

<script>
import PageFooter from '@/components/front/PageFooter.vue'
import Request from '@/utils/request'

export default {
  name: 'AIAssistant',
  components: { PageFooter },
  data() {
    return {
      question: '',
      sending: false,
      messages: [
        {
          role: 'assistant',
          content: '您好，我是小区 AI 助手，可以帮您解答物业费、报修流程、小区活动等常见问题，有什么想咨询的？'
        }
      ],
      faqList: [
        {
          keywords: ['物业费', '怎么交', '缴费', '交物业费'],
          answer:
            '目前支持三种物业费缴纳方式：<br/>' +
            '1）<b>线上缴费</b>：登录本系统 → 进入“物业缴费” → 选择未缴账单 → 点击“缴费”，可通过支付宝完成支付；<br/>' +
            '2）<b>线下缴费</b>：工作日 9:00-17:30 携带房号到物业服务中心前台现金/扫码缴费；<br/>' +
            '3）<b>银行转账</b>：可联系物业获取指定对公账户，备注“房号+姓名”。'
        },
        {
          keywords: ['报修', '维修', '怎么报', '水管', '电路'],
          answer:
            '您可以通过以下方式报修：<br/>' +
            '1）<b>系统报修</b>：在首页点击“报修服务”，填写报修内容、上传现场照片并提交；<br/>' +
            '2）<b>电话报修</b>：工作时间拨打物业服务电话 400-xxxx-xxx；<br/>' +
            '3）报修后可在“报修服务”中查看处理进度，如有紧急情况请直接电话联系物业。'
        },
        {
          keywords: ['活动', '小区活动', '亲子', '社区活动'],
          answer:
            '近期社区活动可以在首页“社区公告”和“活动通知”中查看：<br/>' +
            '1）固定每月会组织 1-2 场亲子或公益活动；<br/>' +
            '2）报名方式一般为线上报名或到物业前台登记；<br/>' +
            '3）如您有活动建议，也欢迎通过“投诉建议”模块反馈给我们。'
        },
        {
          keywords: ['办公时间', '上班时间', '物业', '客服'],
          answer:
            '物业服务中心的工作时间为：<br/>' +
            '1）工作日：9:00-12:00，14:00-17:30；<br/>' +
            '2）周末及节假日安排值班人员处理紧急事件；<br/>' +
            '3）24 小时服务热线：400-xxxx-xxx。'
        }
      ],
      unmatchedLog: []
    }
  },
  methods: {
    handleSend() {
      const text = (this.question || '').trim()
      if (!text) {
        this.$message.warning('请先输入问题')
        return
      }
      this.sending = true
      const userMsg = { role: 'user', content: text }
      this.messages.push(userMsg)
      this.question = ''
      this.$nextTick(this.scrollToBottom)

      // 调用后端 AI 接口（DeepSeek 由后端转发），失败时回退到本地 FAQ 匹配
      Request.post('/ai/ask', { question: text })
        .then(res => {
          if (res && res.code === '0' && res.data && res.data.answer) {
            this.messages.push({ role: 'assistant', content: res.data.answer.replace(/\n/g, '<br/>') })
          } else {
            const answer = this.findAnswer(text)
            this.messages.push({ role: 'assistant', content: answer })
          }
        })
        .catch(() => {
          const answer = this.findAnswer(text)
          this.messages.push({ role: 'assistant', content: answer })
        })
        .finally(() => {
          this.$nextTick(this.scrollToBottom)
          this.sending = false
        })
    },
    findAnswer(text) {
      const lower = text.toLowerCase()
      let best = null
      let bestScore = 0
      this.faqList.forEach(item => {
        let score = 0
        item.keywords.forEach(k => {
          if (lower.includes(k.toLowerCase())) {
            score += 1
          }
        })
        if (score > bestScore) {
          bestScore = score
          best = item
        }
      })
      if (best && bestScore > 0) {
        return best.answer
      }
      // 未命中：记录到本地日志，提示转人工
      this.unmatchedLog.push({
        question: text,
        time: new Date().toISOString()
      })
      return (
        '抱歉，暂时没有找到该问题的标准答案，已为您记录并转给物业工作人员，后续将补充到知识库中。' +
        '<br/>您也可以尝试换一种问法，例如：“物业费怎么交？ / 报修在哪里提交？ / 家政服务怎么约？”。'
      )
    },
    scrollToBottom() {
      const el = this.$refs.chatWindow
      if (el) {
        el.scrollTop = el.scrollHeight
      }
    }
  }
}
</script>

<style scoped>
.ai-assistant-page {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}
.content-wrapper {
  flex: 1;
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
  width: 100%;
}
.intro-card {
  height: 100%;
}
.title {
  font-size: 22px;
  margin-bottom: 6px;
  color: #303133;
}
.subtitle {
  font-size: 14px;
  color: #909399;
  margin-bottom: 16px;
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}
.feature-list {
  list-style: none;
  padding: 0;
  margin: 0 0 16px 0;
}
.feature-list li {
  display: flex;
  align-items: flex-start;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}
.feature-list .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #409eff;
  margin-right: 8px;
  margin-top: 6px;
}
.tip {
  font-size: 13px;
  color: #909399;
  background: #f5f7fa;
  border-radius: 4px;
  padding: 8px 10px;
}
.chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.chat-header {
  margin-bottom: 12px;
}
.chat-title {
  font-size: 16px;
  font-weight: 500;
  margin-right: 8px;
}
.chat-subtitle {
  font-size: 13px;
  color: #909399;
}
.chat-window {
  flex: 1;
  min-height: 260px;
  max-height: 420px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px;
  overflow-y: auto;
  background: #fafafa;
  margin-bottom: 12px;
}
.chat-item {
  display: flex;
  margin-bottom: 10px;
}
.chat-item.assistant {
  flex-direction: row;
}
.chat-item.user {
  flex-direction: row-reverse;
}
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin: 0 8px;
  flex-shrink: 0;
}
.chat-item.user .avatar {
  background: #67c23a;
}
.bubble {
  max-width: 80%;
  border-radius: 4px;
  padding: 8px 10px;
  font-size: 14px;
  line-height: 1.6;
  background: #fff;
  color: #303133;
}
.chat-item.user .bubble {
  background: #409eff;
  color: #fff;
}
.chat-input {
  border-top: 1px solid #ebeef5;
  padding-top: 8px;
}
.chat-actions {
  margin-top: 6px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chat-actions .hint {
  font-size: 12px;
  color: #909399;
}
</style>

