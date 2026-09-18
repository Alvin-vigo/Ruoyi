import request from '@/utils/request'

// 查询随访记录列表
export function listFollowup(query) {
  return request({
    url: '/medical/followup/list',
    method: 'get',
    params: query
  })
}

// 查询随访记录详细
export function getFollowup(followupId) {
  return request({
    url: '/medical/followup/' + followupId,
    method: 'get'
  })
}

// 新增随访记录
export function addFollowup(data) {
  return request({
    url: '/medical/followup',
    method: 'post',
    data: data
  })
}

// 修改随访记录
export function updateFollowup(data) {
  return request({
    url: '/medical/followup',
    method: 'put',
    data: data
  })
}

// 删除随访记录
export function delFollowup(followupIds) {
  return request({
    url: '/medical/followup/' + followupIds,
    method: 'delete'
  })
}

// 根据患者ID查询随访记录
export function listFollowupByPatientId(patientId) {
  return request({
    url: '/medical/followup/patient/' + patientId,
    method: 'get'
  })
}
