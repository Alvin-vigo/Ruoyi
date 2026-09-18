import request from '@/utils/request'

// 查询诊疗信息列表
export function listDiagnosis(query) {
  return request({
    url: '/medical/diagnosis/list',
    method: 'get',
    params: query
  })
}

// 查询诊疗信息详细
export function getDiagnosis(diagnosisId) {
  return request({
    url: '/medical/diagnosis/' + diagnosisId,
    method: 'get'
  })
}

// 新增诊疗信息
export function addDiagnosis(data) {
  return request({
    url: '/medical/diagnosis',
    method: 'post',
    data: data
  })
}

// 修改诊疗信息
export function updateDiagnosis(data) {
  return request({
    url: '/medical/diagnosis',
    method: 'put',
    data: data
  })
}

// 删除诊疗信息
export function delDiagnosis(diagnosisIds) {
  return request({
    url: '/medical/diagnosis/' + diagnosisIds,
    method: 'delete'
  })
}

// 根据患者ID查询诊疗记录
export function listDiagnosisByPatientId(patientId) {
  return request({
    url: '/medical/diagnosis/patient/' + patientId,
    method: 'get'
  })
}

// 诊疗费用明细汇总（总数、总额、均值）
export function getFeeSummary(query) {
  return request({
    url: '/medical/diagnosis/fee-summary',
    method: 'get',
    params: query
  })
}
