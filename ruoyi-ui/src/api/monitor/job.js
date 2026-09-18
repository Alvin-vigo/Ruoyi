import request from '@/utils/request'

// 查询定时任务列表
export function listJob(query) {
  return request({
    url: '/monitor/job/list',
    method: 'get',
    params: query
  })
}

// 查询定时任务详细
export function getJob(jobId) {
  return request({
    url: '/monitor/job/' + jobId,
    method: 'get'
  })
}

// 新增定时任务
export function addJob(data) {
  return request({
    url: '/monitor/job',
    method: 'post',
    data: data
  })
}

// 修改定时任务
export function updateJob(data) {
  return request({
    url: '/monitor/job',
    method: 'put',
    data: data
  })
}

// 删除定时任务
export function delJob(jobIds) {
  return request({
    url: '/monitor/job/' + jobIds,
    method: 'delete'
  })
}

// 修改任务状态（暂停/恢复）
export function changeJobStatus(data) {
  return request({
    url: '/monitor/job/changeStatus',
    method: 'put',
    data: data
  })
}

// 立即执行一次任务
export function runJob(data) {
  return request({
    url: '/monitor/job/run',
    method: 'put',
    data: data
  })
}

// 校验 cron 表达式
export function checkCronExpression(cronExpression) {
  return request({
    url: '/monitor/job/checkCronExpression',
    method: 'get',
    params: { cronExpression }
  })
}

// 查询定时任务日志列表
export function listJobLog(query) {
  return request({
    url: '/monitor/job/log/list',
    method: 'get',
    params: query
  })
}

// 删除定时任务日志
export function delJobLog(jobLogIds) {
  return request({
    url: '/monitor/job/log/' + jobLogIds,
    method: 'delete'
  })
}

// 清空定时任务日志
export function cleanJobLog() {
  return request({
    url: '/monitor/job/log/clean',
    method: 'delete'
  })
}
