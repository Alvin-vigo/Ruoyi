/**
 * 权限指令（本项目为「无鉴权」版本）
 *
 * 背景：本项目后端没有 Spring Security / SysUserController，也没有 perms 查询接口，
 * 因此不存在「当前用户拥有哪些权限」的数据来源。若不对 v-hasPermi 做注册，
 * 所有引用该指令的页面都会在运行时抛出
 *   vue.runtime.esm.js: [Vue warn]: Failed to resolve directive: hasPermi
 *
 * 处理策略：
 *   注册一个「全部放行」的同名指令 —— 既不改动业务页代码、不产生任何控制台警告，
 *   又保留 `v-hasPermi="['medical:xxx:add']"` 的完整语义。
 *   将来若接入若依标准鉴权体系，只需在本文件内改成真实的权限校验逻辑
 *   （读 store/modules/user.js 的 permissions 做 includes 判断），
 *   业务页面一行都不用动。
 *
 * 同理注册 v-hasRole（角色级），保持与若依官方写法一致。
 */
/**
 * 【将来接入鉴权时怎么改】
 *   1. store/modules/user.js 的 permissions 拿到当前用户权限集合；
 *   2. 把 hasPermission() 换成：
 *        const permissions = store.getters.permissions || []
 *        const required = Array.isArray(value) ? value : [value]
 *        return required.some(p => permissions.includes(p))
 *   3. 业务页面的 v-hasPermi 写法一行都不用改。
 */
function hasPermission(value) {
  // 无鉴权体系：一律放行
  void value
  return true
}

function apply(el, binding) {
  if (!hasPermission(binding.value)) {
    // 标准若依行为：无权限时直接移除该节点
    if (el.parentNode) {
      el.parentNode.removeChild(el)
    } else {
      el.style.display = 'none'
    }
  }
}

export const hasPermi = {
  inserted(el, binding) {
    apply(el, binding)
  },
  update(el, binding) {
    apply(el, binding)
  }
}

export const hasRole = {
  inserted(el, binding) {
    // 同上：无角色来源时一律放行
    void binding
  },
  update(el, binding) {
    void binding
  }
}
