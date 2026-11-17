import request from '@/utils/request'
import { getToken } from '@/utils/auth'

// 注意：request.js 的响应拦截器返回的是整个响应体（AjaxResult：{msg, code, data}），
// 而 FastAPI 微服务自身又包了一层 {code, msg, data}，Java 转发时未拆。
// 因此 AI 接口的取值必须经过 unwrapAi 解包，否则拿到的是包装对象，字段全为 undefined。

/**
 * 逐层解包 {code, msg, data} 包装，最多 3 层，直到拿到真实业务数据。
 * 适用于 aiHealth / generateRecord / riskPredict / chestXray / ragChat 等接口。
 */
export function unwrapAi(res) {
  let d = res
  let depth = 0
  while (d && typeof d === 'object' && depth < 3 &&
         d.code !== undefined && d.msg !== undefined && Object.prototype.hasOwnProperty.call(d, 'data')) {
    d = d.data
    depth++
  }
  return d
}

// AI 服务与各模块就绪状态（联调第一步看这个）
export function aiHealth() {
  return request({
    url: '/medical/ai/health',
    method: 'get'
  })
}

// ============ 模块① 流式生成（SSE，打字机效果） ============
// 流式接口走浏览器直连 FastAPI（FastAPI 已开启 CORS，允许所有来源），
// 不走 Java 代理，避免后端 SSE 转发带来的复杂度；生产构建同样可用。
const AI_BASE = process.env.VUE_APP_AI_BASE || 'http://127.0.0.1:8000'

/**
 * 流式生成病历：逐 token 回调 onDelta(全文)，结束时回调 onDone(meta)。
 * meta: { content, engine, model, degraded, elapsed_ms, patient_name, patient_id }
 * 任何网络错误都会 reject，调用方应回退到非流式 generateRecord。
 */
export function generateRecordStream(payload, onDelta, onDone) {
  // 本链路绕过后端直连 FastAPI，因此不会经过 axios 的请求拦截器，
  // 必须在这里手动带上令牌，否则 AI 侧无从识别调用方。
  // FastAPI 已配置 allow_headers=["*"]，可正常接收 Authorization。
  const headers = { 'Content-Type': 'application/json' }
  const token = getToken()
  if (token) {
    headers['Authorization'] = 'Bearer ' + token
  }
  return fetch(`${AI_BASE}/api/ai/generate_record_stream`, {
    method: 'POST',
    headers,
    body: JSON.stringify(payload)
  }).then(resp => {
    if (!resp.ok) {
      throw new Error('流式接口返回 ' + resp.status)
    }
    const reader = resp.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    let full = ''
    const step = ({ done, value }) => {
      if (done) return
      buffer += decoder.decode(value, { stream: true })
      let idx
      while ((idx = buffer.indexOf('\n\n')) >= 0) {
        const frame = buffer.slice(0, idx)
        buffer = buffer.slice(idx + 2)
        const dataLine = frame.split('\n').find(l => l.startsWith('data:'))
        if (!dataLine) continue
        let json
        try {
          json = JSON.parse(dataLine.slice(5).trim())
        } catch (e) {
          continue
        }
        if (json.type === 'delta') {
          full += json.text
          if (onDelta) onDelta(full)
        } else if (json.type === 'done') {
          if (onDone) onDone(json)
        }
      }
      return reader.read().then(step)
    }
    return reader.read().then(step)
  })
}

// 流式生成结束后，把完整文本回传后端落库（等价于 generateRecord 的保存逻辑，但不二次推理）
export function saveAiRecord(data) {
  return request({
    url: '/medical/ai/record/save',
    method: 'post',
    data: data,
    timeout: 30000
  })
}

// ============ 模块① 智能病历书写 ============
export function generateRecord(data) {
  return request({
    url: '/medical/ai/generateRecord',
    method: 'post',
    data: data,
    timeout: 150000
  })
}

// AI 病历记录列表
export function listAiRecord(query) {
  return request({
    url: '/medical/ai/record/list',
    method: 'get',
    params: query
  })
}

export function updateAiRecord(data) {
  return request({
    url: '/medical/ai/record',
    method: 'put',
    data: data
  })
}

export function delAiRecord(ids) {
  return request({
    url: '/medical/ai/record/' + ids,
    method: 'delete'
  })
}

// ============ 模块② 疾病风险预测 ============
// 获取模型要求的真实特征顺序
export function riskFeatures() {
  return request({
    url: '/medical/ai/riskFeatures',
    method: 'get'
  })
}

export function riskPredict(data) {
  return request({
    url: '/medical/ai/riskPredict',
    method: 'post',
    data: data,
    timeout: 60000
  })
}

export function listRiskPredict(query) {
  return request({
    url: '/medical/ai/risk/list',
    method: 'get',
    params: query
  })
}

export function delRiskPredict(ids) {
  return request({
    url: '/medical/ai/risk/' + ids,
    method: 'delete'
  })
}

// ============ 模块③ 胸片 AI 初筛 ============
export function chestXray(formData, patientId) {
  return request({
    url: '/medical/ai/chestXray' + (patientId ? '?patientId=' + patientId : ''),
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}

export function listChestXray(query) {
  return request({
    url: '/medical/ai/xray/list',
    method: 'get',
    params: query
  })
}

export function delChestXray(ids) {
  return request({
    url: '/medical/ai/xray/' + ids,
    method: 'delete'
  })
}

// ============ 模块④ RAG 导诊问答 ============
export function ragChat(data) {
  return request({
    url: '/medical/ai/ragChat',
    method: 'post',
    data: data,
    timeout: 150000
  })
}

export function ragAddDocs(docs) {
  return request({
    url: '/medical/ai/ragAddDocs',
    method: 'post',
    data: docs
  })
}

// ============ 导诊知识库 ============
export function listKnowledge(query) {
  return request({
    url: '/medical/ai/knowledge/list',
    method: 'get',
    params: query
  })
}

export function addKnowledge(data) {
  return request({
    url: '/medical/ai/knowledge',
    method: 'post',
    data: data
  })
}

export function updateKnowledge(data) {
  return request({
    url: '/medical/ai/knowledge',
    method: 'put',
    data: data
  })
}

export function delKnowledge(ids) {
  return request({
    url: '/medical/ai/knowledge/' + ids,
    method: 'delete'
  })
}

// 把未同步的知识条目推送进 Chroma 向量库
export function syncKnowledge() {
  return request({
    url: '/medical/ai/knowledge/sync',
    method: 'post',
    timeout: 180000
  })
}
