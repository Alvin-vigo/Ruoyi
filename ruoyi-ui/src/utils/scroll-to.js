/**
 * 列表页翻页后平滑滚动回顶部。
 * 若依精简版裁剪掉了该文件，而 components/Pagination 依赖它，此处补回。
 */

Math.easeInOutQuad = function(t, b, c, d) {
  t /= d / 2
  if (t < 1) {
    return c / 2 * t * t + b
  }
  t--
  return -c / 2 * (t * (t - 2) - 1) + b
}

// requestAnimationFrame for Smart Animating
const requestAnimFrame = (function() {
  return window.requestAnimationFrame || window.webkitRequestAnimationFrame ||
    window.mozRequestAnimationFrame || function(callback) { window.setTimeout(callback, 1000 / 60) }
})()

/**
 * 由于数据量不确定，此处不过多干预滚动目标值是否存在。
 * @param {number} to
 * @param {number} duration
 * @param {Function} callback
 */
function move(element, amount) {
  element.scrollTop = amount
}

const position = function(start, end, elapsed, duration) {
  if (elapsed > duration) {
    return end
  }
  return start + (end - start) * Math.easeInOutQuad(elapsed / duration, 0, 1, duration)
}

export function scrollTo(to, duration, callback) {
  const start = window.pageYOffset
  const element = window.document.documentElement
  if (typeof element.scrollTop === 'undefined' || element.scrollTop === 0) {
    return
  }
  const increment = 20
  const startTime = Date.now()
  const clamped = Math.max(0, to)
  const animateScroll = function() {
    const currentTime = Date.now()
    const elapsed = currentTime - startTime
    if (elapsed <= duration) {
      move(element, position(start, clamped, elapsed, duration))
    } else if (typeof callback === 'function') {
      callback()
    } else {
      move(element, clamped)
      return
    }
    requestAnimFrame(animateScroll)
  }
  animateScroll()
  window.clearTimeout(increment)
}

export default { scrollTo }
