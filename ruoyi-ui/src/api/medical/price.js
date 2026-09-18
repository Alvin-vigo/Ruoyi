import request from '@/utils/request'

// 费用价目列表
export function listPrice(query) {
  return request({ url: '/medical/price/list', method: 'get', params: query })
}

// 新增价目
export function addPrice(data) {
  return request({ url: '/medical/price', method: 'post', data })
}

// 修改价目
export function updatePrice(data) {
  return request({ url: '/medical/price', method: 'put', data })
}

// 删除价目
export function delPrice(itemIds) {
  return request({ url: '/medical/price/' + itemIds, method: 'delete' })
}
