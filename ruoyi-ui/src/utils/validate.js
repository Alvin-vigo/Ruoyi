/**
 * 判断是否为外链
 */
export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}
