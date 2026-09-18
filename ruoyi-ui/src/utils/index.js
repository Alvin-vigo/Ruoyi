/**
 * 若依常用工具函数（精简版）。
 */

/**
 * 时间格式化
 * @param {(Object|string|number)} time
 * @param {string} pattern
 * @returns {string}
 */
export function parseTime(time, pattern) {
  if (arguments.length === 0 || !time) {
    return null
  }
  const format = pattern || '{y}-{m}-{d} {h}:{i}:{s}'
  let date
  if (typeof time === 'object') {
    date = time
  } else if (typeof time === 'string' && /^\d{4}-\d{1,2}-\d{1,2}T/.test(time)) {
    // ISO 8601 字符串（如 2026-09-18T03:51:50.000+00:00）必须用原生解析，
    // 若先替换 '-' 为 '/' 会变成 Invalid Date → 显示 0-0-0
    date = new Date(time)
  } else {
    if (typeof time === 'string' && /^[0-9]+$/.test(time)) {
      time = parseInt(time)
    } else if (typeof time === 'string') {
      time = time.replace(new RegExp(/-/gm), '/')
    }
    if (typeof time === 'number' && time.toString().length === 10) {
      time = time * 1000
    }
    date = new Date(time)
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }
  const time_str = format.replace(/{([ymdhisa])+}/g, (result, key) => {
    let value = formatObj[key]
    if (key === 'a') {
      return ['日', '一', '二', '三', '四', '五', '六'][value]
    }
    if (result.length > 0 && value < 10) {
      value = '0' + value
    }
    return value || 0
  })
  return time_str
}

/**
 * 给查询参数追加日期范围（beginTime/endTime）
 * @param {Object} params 查询参数对象
 * @param {Array} dateRange [开始, 结束]
 * @param {string} propName 字段前缀，如 'visitDate' -> visitDateBeginTime / visitDateEndTime
 * @returns {Object}
 */
export function addDateRange(params, dateRange, propName) {
  const search = params
  const value = []
  if (dateRange != null && dateRange.length > 0) {
    dateRange.forEach(item => {
      value.push(parseTime(item, '{y}-{m}-{d}'))
    })
    search.params = typeof search.params === 'object' && search.params !== null && !Array.isArray(search.params)
      ? search.params
      : {}
    search.params[propName + 'BeginTime'] = value[0]
    search.params[propName + 'EndTime'] = value[1]
  }
  return search
}

/**
 * 重置表单（挂在 Vue.prototype.resetForm 使用，this 指向组件）
 */
export function resetForm(refName) {
  if (this.$refs[refName]) {
    this.$refs[refName].resetFields()
  }
}

/**
 * 深拷贝
 */
export function deepClone(source) {
  if (!source && typeof source !== 'object') {
    throw new Error('error arguments deepClone')
  }
  const targetObj = source.constructor === Array ? [] : {}
  Object.keys(source).forEach(keys => {
    if (source[keys] && typeof source[keys] === 'object') {
      targetObj[keys] = deepClone(source[keys])
    } else {
      targetObj[keys] = source[keys]
    }
  })
  return targetObj
}

/**
 * 防抖
 */
export function debounce(func, wait, immediate) {
  let timeout
  return function() {
    const context = this
    const args = arguments
    if (timeout) clearTimeout(timeout)
    if (immediate) {
      const callNow = !timeout
      timeout = setTimeout(() => {
        timeout = null
      }, wait)
      if (callNow) func.apply(context, args)
    } else {
      timeout = setTimeout(() => {
        func.apply(context, args)
      }, wait)
    }
  }
}
