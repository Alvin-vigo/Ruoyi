<template>
  <div class="app-container ai-page">
    <el-alert
      v-if="modeText"
      :title="modeText"
      :type="llmReady ? 'success' : 'warning'"
      :closable="false"
      show-icon
      class="mb8"
    />
    <el-row :gutter="16">
      <el-col :span="17">
        <el-card shadow="never" class="chat-card">
          <div slot="header">
            <span>智能导诊问答</span>
            <el-tag v-if="lastEngine" size="mini" class="fr" :type="lastDegraded ? 'warning' : 'success'">
              {{ engineLabel }}
            </el-tag>
          </div>

          <div ref="chatBox" class="chat-box">
            <div v-for="(msg, idx) in messages" :key="idx" class="msg-wrap" :class="msg.role">
              <div class="avatar">{{ msg.role === 'user' ? '患' : 'AI' }}</div>
              <div class="bubble">
                <div class="bubble-text" v-html="formatText(msg.content)"></div>
                <div v-if="msg.references && msg.references.length" class="ref-block">
                  <div class="ref-title">引用来源（本地知识库）</div>
                  <ol class="ref-list">
                    <li v-for="(r, i) in msg.references" :key="i">{{ r }}</li>
                  </ol>
                </div>
                <div class="bubble-meta">
                  <span v-if="msg.elapsedMs">{{ msg.elapsedMs }} ms</span>
                  <span v-if="msg.hit === false" class="nohit">知识库未命中，已拒绝生成</span>
                </div>
              </div>
            </div>
            <div v-if="asking" class="msg-wrap ai">
              <div class="avatar">AI</div>
              <div class="bubble thinking">
                <i class="el-icon-loading" /> 正在思考，请稍等…
                <div class="thinking-sub">先检索本地知识库，再由大模型生成回答，约需十几秒到一分钟</div>
              </div>
            </div>
          </div>

          <div class="input-area">
            <el-input
              v-model="question"
              type="textarea"
              :rows="2"
              :maxlength="200"
              show-word-limit
              placeholder="输入您的健康咨询，例如：糖尿病有哪些典型症状？"
              @keyup.enter.native="handleEnter"
            />
            <div class="input-bar">
              <span class="tip">
                <el-checkbox v-model="showRef">显示引用来源</el-checkbox>
                <el-tooltip content="每次携带的知识条数，越多覆盖越全但生成越慢" placement="top">
                  <i class="el-icon-question" style="margin-left:8px" />
                </el-tooltip>
                <el-input-number v-model="topK" :min="1" :max="8" size="mini" controls-position="right" style="margin-left:6px;width:110px" />
              </span>
              <span>
                <el-button size="small" @click="clearChat">清空</el-button>
                <el-button type="primary" size="small" icon="el-icon-s-promotion" :loading="asking" @click="send()">发送</el-button>
              </span>
            </div>
            <div class="quick-q">
              <el-button
                v-for="q in quickQuestions"
                :key="q"
                size="mini"
                round
                plain
                @click="quickAsk(q)"
              >{{ q }}</el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="7">
        <el-card shadow="never">
          <div slot="header"><span>说明</span></div>
          <div class="side-tip">
            <p><strong>工作方式</strong>：先从本地 Chroma 向量库检索相关医学知识，再把检索结果和问题一起交给本地大模型作答。</p>
            <p><strong>防幻觉设计</strong>：知识库未命中时直接拒答，绝不让大模型凭空编造医学结论。</p>
            <p><strong>多轮对话</strong>：已启用上下文记忆（保留最近 5 轮），连续追问"要空腹吗"不会丢失上文；点「清空」即开启全新会话。</p>
            <p><strong>降级模式</strong>：大模型不可用时自动转为「纯检索」，直接返回知识库原文——可作为论文中的对比基线。</p>
            <p>知识内容请在「导诊知识库」菜单维护，并点击同步后生效。</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ragChat, aiHealth, unwrapAi } from '@/api/medical/ai'

export default {
  name: 'MedicalAiChat',
  data() {
    return {
      question: '',
      messages: [],
      asking: false,
      topK: 3,
      showRef: true,
      llmReady: null,
      lastEngine: '',
      lastDegraded: false,
      sessionId: '',
      quickQuestions: ['糖尿病有哪些典型症状？', '高血压怎么自我监测？', '感冒一般多久能好？', '空腹血糖多少算偏高？']
    }
  },
  computed: {
    modeText() {
      if (this.llmReady === null) return '正在检测 AI 服务状态…'
      return this.llmReady
        ? 'AI 服务在线：启用本地大模型进行导诊回答。'
        : '大模型暂不可用，将降级为「纯检索」模式（直接返回知识库原文）。'
    },
    engineLabel() {
      if (!this.lastEngine) return ''
      if (this.lastEngine === 'rag+ollama') return '检索增强 + 本地大模型'
      if (this.lastEngine === 'rag-retrieve-only') return '纯检索模式（未启用大模型）'
      return this.lastEngine
    }
  },
  created() {
    this.sessionId = this.genSessionId()
    aiHealth().then(raw => {
      const res = unwrapAi(raw)
      if (res && res.llm) this.llmReady = !!res.llm.ready
    }).catch(() => { this.llmReady = false })
  },
  methods: {
    formatText(text) {
      if (!text) return ''
      // 转义 HTML 防注入，再把换行转为 <br>
      return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/\n/g, '<br/>')
    },
    quickAsk(q) {
      this.question = q
      this.send()
    },
    /** Enter 发送、Shift+Enter 换行（显式接收事件对象，不依赖隐式全局 event） */
    handleEnter(e) {
      if (e && e.shiftKey) return
      if (e && e.preventDefault) e.preventDefault()
      this.send()
    },
    send() {
      const q = (this.question || '').trim()
      if (!q) {
        this.$message.warning('请输入咨询内容')
        return
      }
      if (this.asking) return

      this.messages.push({ role: 'user', content: q })
      this.question = ''
      this.asking = true
      this.scrollToBottom()

      ragChat({ question: q, topK: this.topK, session_id: this.sessionId }).then(raw => {
        const res = unwrapAi(raw)
        this.lastEngine = res.engine || ''
        this.lastDegraded = !!res.degraded
        this.asking = false
        this.messages.push({
          role: 'ai',
          content: res.answer,
          references: this.showRef ? (res.references || []) : [],
          hit: res.hit,
          elapsedMs: res.elapsed_ms
        })
        this.scrollToBottom()
        if (res.hit === false) {
          this.$notify.info({ title: '未命中知识库', message: '为避免误导，已拒答，请先补充相关知识', duration: 3000 })
        }
      }).catch(() => {
        this.asking = false
        this.messages.push({ role: 'ai', content: '抱歉，回答失败，请检查 AI 微服务是否已启动。' })
        this.scrollToBottom()
      })
    },
    clearChat() {
      this.messages = []
      this.lastEngine = ''
      // 换新会话 ID：服务端将开启一段全新、无上文记忆的对话
      this.sessionId = this.genSessionId()
    },
    genSessionId() {
      if (window.crypto && window.crypto.randomUUID) {
        return 'sess-' + window.crypto.randomUUID()
      }
      return 'sess-' + Date.now() + '-' + Math.random().toString(16).slice(2)
    },
    scrollToBottom() {
      this.$nextTick(() => {
        if (this.$refs.chatBox) {
          this.$refs.chatBox.scrollTop = this.$refs.chatBox.scrollHeight
        }
      })
    }
  }
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.fr { float: right; }
.chat-card { display: flex; flex-direction: column; }
.chat-box {
  height: 440px;
  overflow-y: auto;
  padding: 10px;
  background: #f7f8fa;
  border-radius: 4px;
}
.msg-wrap { display: flex; margin-bottom: 16px; }
.msg-wrap.user { flex-direction: row-reverse; }
.avatar {
  width: 32px; height: 32px; line-height: 32px; text-align: center;
  border-radius: 4px; font-size: 12px; flex-shrink: 0;
  background: #409EFF; color: #fff;
}
.msg-wrap.user .avatar { background: #67C23A; }
.bubble {
  max-width: 76%;
  padding: 10px 12px;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  margin: 0 10px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}
.bubble-text { white-space: normal; }
.ref-block { margin-top: 10px; padding-top: 8px; border-top: 1px dashed #DCDFE6; background: #fbfcfe; }
.ref-title { font-size: 12px; color: #909399; margin-bottom: 4px; }
.ref-list { margin: 0; padding-left: 18px; font-size: 12px; color: #606266; line-height: 1.6; }
.bubble-meta { margin-top: 6px; font-size: 11px; color: #C0C4CC; }
.nohit { color: #E6A23C; margin-left: 8px; }
.input-area { margin-top: 12px; }
.input-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.tip { display: flex; align-items: center; font-size: 12px; color: #909399; }
.quick-q { margin-top: 10px; }
.quick-q .el-button { margin-right: 6px; margin-bottom: 6px; }
.side-tip p { font-size: 13px; color: #606266; line-height: 1.8; margin: 0 0 10px; }
</style>
