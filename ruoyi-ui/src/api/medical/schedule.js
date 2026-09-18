import request from '@/utils/request'

// 号源排班列表
export function listSchedule(query) {
  return request({ url: '/medical/schedule/list', method: 'get', params: query })
}

// 新增排班
export function addSchedule(data) {
  return request({ url: '/medical/schedule', method: 'post', data })
}

// 修改排班
export function updateSchedule(data) {
  return request({ url: '/medical/schedule', method: 'put', data })
}

// 删除排班
export function delSchedule(ids) {
  return request({ url: '/medical/schedule/' + ids, method: 'delete' })
}
