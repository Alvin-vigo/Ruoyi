import request from '@/utils/request'

// 处方列表
export function listPrescription(query) {
  return request({ url: '/medical/prescription/list', method: 'get', params: query })
}

// 新增处方（含明细）
export function addPrescription(data) {
  return request({ url: '/medical/prescription', method: 'post', data })
}

// 修改处方
export function updatePrescription(data) {
  return request({ url: '/medical/prescription', method: 'put', data })
}

// 删除处方
export function delPrescription(ids) {
  return request({ url: '/medical/prescription/' + ids, method: 'delete' })
}

// 发药
export function dispensePrescription(data) {
  return request({ url: '/medical/prescription/dispense', method: 'post', data })
}

// 作废处方
export function cancelPrescription(id) {
  return request({ url: '/medical/prescription/cancel/' + id, method: 'post' })
}

// 退药（逆向回滚库存）
export function returnPrescription(id, operator) {
  return request({ url: '/medical/prescription/return/' + id, method: 'post', params: { operator } })
}

// 处方明细列表
export function listPrescriptionItems(id) {
  return request({ url: '/medical/prescription/items/' + id, method: 'get' })
}
