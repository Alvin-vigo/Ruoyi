import request from '@/utils/request'

// 挂号列表
export function listRegistration(query) {
  return request({ url: '/medical/registration/list', method: 'get', params: query })
}

// 新增挂号（占用号源）
export function addRegistration(data) {
  return request({ url: '/medical/registration', method: 'post', data })
}

// 修改挂号
export function updateRegistration(data) {
  return request({ url: '/medical/registration', method: 'put', data })
}

// 删除挂号
export function delRegistration(ids) {
  return request({ url: '/medical/registration/' + ids, method: 'delete' })
}

// 退号（释放号源）
export function refundRegistration(id) {
  return request({ url: '/medical/registration/refund/' + id, method: 'post' })
}

// 开始就诊：由挂号生成诊疗草稿，返回 diagnosisId
export function startVisit(id) {
  return request({ url: '/medical/registration/start-visit/' + id, method: 'post' })
}
