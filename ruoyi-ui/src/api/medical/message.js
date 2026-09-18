import request from '@/utils/request'

// 查询站内信消息列表
export function listMessage(query) {
  return request({
    url: '/medical/message/list',
    method: 'get',
    params: query
  })
}

// 查询站内信消息详情
export function getMessage(messageId) {
  return request({
    url: '/medical/message/' + messageId,
    method: 'get'
  })
}

// 标记单条消息已读
export function readMessage(messageId) {
  return request({
    url: '/medical/message/read/' + messageId,
    method: 'put'
  })
}

// 标记全部已读
export function readAllMessage(receiver) {
  return request({
    url: '/medical/message/readAll?receiver=' + receiver,
    method: 'put'
  })
}

// 查询未读消息数
export function unreadCount(receiver) {
  return request({
    url: '/medical/message/unreadCount?receiver=' + receiver,
    method: 'get'
  })
}

// 查询最近若干条消息（顶部小红点弹层）
export function getLatestMessages(receiver, limit) {
  return request({
    url: '/medical/message/latest?receiver=' + receiver + '&limit=' + (limit || 5),
    method: 'get'
  })
}

// 删除站内信消息
export function delMessage(messageIds) {
  return request({
    url: '/medical/message/' + messageIds,
    method: 'delete'
  })
}
