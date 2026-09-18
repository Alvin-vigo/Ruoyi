import request from '@/utils/request'

// 获取医疗数据看板统计
export function getDashboard(query) {
  return request({
    url: '/medical/statistics/dashboard',
    method: 'get',
    params: query
  })
}
