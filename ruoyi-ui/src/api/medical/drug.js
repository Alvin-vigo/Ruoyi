import request from '@/utils/request'

// 查询药品信息列表
export function listDrug(query) {
  return request({
    url: '/medical/drug/list',
    method: 'get',
    params: query
  })
}

// 查询药品信息详细
export function getDrug(drugId) {
  return request({
    url: '/medical/drug/' + drugId,
    method: 'get'
  })
}

// 新增药品信息
export function addDrug(data) {
  return request({
    url: '/medical/drug',
    method: 'post',
    data: data
  })
}

// 修改药品信息
export function updateDrug(data) {
  return request({
    url: '/medical/drug',
    method: 'put',
    data: data
  })
}

// 删除药品信息
export function delDrug(drugIds) {
  return request({
    url: '/medical/drug/' + drugIds,
    method: 'delete'
  })
}

// 查询低库存药品预警列表
export function listDrugLowStock(query) {
  return request({
    url: '/medical/drug/low-stock',
    method: 'get',
    params: query
  })
}

// 查询近效期药品预警列表
export function listDrugNearExpiry(query) {
  return request({
    url: '/medical/drug/near-expiry',
    method: 'get',
    params: query
  })
}
