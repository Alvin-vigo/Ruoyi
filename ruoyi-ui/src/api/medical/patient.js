import request from '@/utils/request'

// 查询患者信息列表
export function listPatient(query) {
  return request({
    url: '/medical/patient/list',
    method: 'get',
    params: query
  })
}

// 查询患者信息详细
export function getPatient(patientId) {
  return request({
    url: '/medical/patient/' + patientId,
    method: 'get'
  })
}

// 新增患者信息
export function addPatient(data) {
  return request({
    url: '/medical/patient',
    method: 'post',
    data: data
  })
}

// 修改患者信息
export function updatePatient(data) {
  return request({
    url: '/medical/patient',
    method: 'put',
    data: data
  })
}

// 删除患者信息
export function delPatient(patientIds) {
  return request({
    url: '/medical/patient/' + patientIds,
    method: 'delete'
  })
}

// 导出的请求已封装在 index.vue 中调用 this.download
