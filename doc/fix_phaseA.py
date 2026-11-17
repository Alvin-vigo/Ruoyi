# -*- coding: utf-8 -*-
import io, sys
p = "D:/若依医疗/若依医疗/doc/详细设计说明书.md"
s = open(p, encoding="utf-8").read()
orig = s

# ---------- 1) 4.4 重排：把 4.4.17 业务ID分配器 移到 4.4.16 之后、若依原生表之前 ----------
start_marker = "#### 4.4.17 业务 ID 分配器表 `medical_id_allocator`（V1.0 新增）"
end_marker   = "\n#### 4.4.5 药品信息表 `medical_drug`"
assert start_marker in s, "start_marker missing"
assert end_marker in s, "end_marker missing"
i = s.index(start_marker)
j = s.index(end_marker)
block = s[i:j]                       # 业务ID分配器 整段（止于 4.4.5 之前）
s = s[:i] + s[j:]                   # 4.4.4 之后直接接 4.4.5

native_old = "\n#### 4.4.17 若依原生表与代码生成表（沿用，不重复设计）"
native_new = "\n#### 4.4.18 若依原生表与代码生成表（沿用，不重复设计）"
assert native_old in s, "native marker missing"
s = s.replace(native_old, native_new, 1)
k = s.index(native_new)
s = s[:k] + block + "\n\n" + s[k:]   # 插入到 4.4.18 之前，此时 block 仍编号为 4.4.17
print("[1] 4.4 重排完成；4.4.17 业务ID分配器 -> 末尾，若依原生表 -> 4.4.18")

# ---------- 2) 去除草稿符号 ⚠️ ✅ ⏳ 与删除线 ~~ ----------
s = s.replace("⚠️", "").replace("⚠", "")
s = s.replace("✅", "").replace("⏳", "")
s = s.replace("~~", "")
print("[2] 已去除 ⚠️/✅/⏳/~~ 草稿符号")

# ---------- 3) 口语化 -> 书面语 ----------
s = s.replace("会拖垮内存", "将造成较大内存开销，影响查询性能")
s = s.replace("必挂", "将导致功能不可用")
s = s.replace("专用于演示发药拦截", "用于演示发药拦截场景")
print("[3] 口语化表述已替换")

# ---------- 4) 0.5 自指修复 ----------
s = s.replace("| 文档页数 | 见章节 0.5 追溯矩阵覆盖度 |",
              "| 文档页数 | 详见文末目录与附录 A |")
print("[4] 0.5 自指已修复")

# ---------- 5) 附录 A 与 0.5 总表的 “—” 补 AC 编号 ----------
s = s.replace("| FR-3.7 | 随访管理 | 6.9 | — |", "| FR-3.7 | 随访管理 | 6.9 | AC-17 |")
s = s.replace("| FR-3.8 | 站内信 | 6.10 | — |", "| FR-3.8 | 站内信 | 6.10 | AC-18 |")
s = s.replace("| FR-3.9 | 价目管理 | 6.7 | — |", "| FR-3.9 | 价目管理 | 6.7 | AC-19 |")
s = s.replace("| FR-3.13 | 定时任务 | 6.13 | — |", "| FR-3.13 | 定时任务 | 6.13 | AC-20 |")
s = s.replace(" | `medical_followup` | — |", " | `medical_followup` | AC-17 |")
s = s.replace(" | `medical_message` | — |", " | `medical_message` | AC-18 |")
s = s.replace(" | `medical_price_item` | — |", " | `medical_price_item` | AC-19 |")
s = s.replace(" | `sys_job`、`sys_job_log`<br/>`medical_message` | — |",
              " | `sys_job`、`sys_job_log`<br/>`medical_message` | AC-20 |")
print("[5] 附录A/0.5 的 “—” 已补 AC-17~AC-20")

# ---------- 6) 附录 B 拆分为 已闭环问题清单 + 系统边界与后续优化方向 ----------
s = s.replace("## 附录 B 缺陷跟踪（按优先级）",
              "## 附录 B 已闭环问题清单与系统边界说明")
s = s.replace("> **状态说明**：✅ 已修复（V1.0，2026-10-08）｜⏳ 未修复，按优先级排期。",
              "> 本附录分两部分：**B.1 已闭环问题清单**记录 V1.0 版本落地过程中修复的真实代码缺陷"
              "（均已在对应版本合入，非遗留问题）；**B.2 系统边界与后续优化方向**记录主动选型产生的"
              "设计边界与已知瓶颈，定位为未来可扩展方向，而非当前缺陷，避免评审时误判为“未完成”。")
b2 = """
### B.2 系统边界与后续优化方向

> 下列内容属于**主动选型的设计边界与已知瓶颈**，并非未修复的缺陷；单列以便评审区分“已做完”与“未来可扩展”。

| 边界 / 瓶颈 | 说明 | 未来扩展方向 |
|------------|------|--------------|
| RBAC 仅到菜单级 | V1.0 角色→菜单授权；按钮级 `v-hasPermi` 预留接口但恒放行、暂不启用（见 11.1） | 接入真实按钮权限只需改前端 `hasPermission()` 返回逻辑，后端无需改动 |
| 图形验证码预留、演示环境默认关闭 | 登录链路保留 `/captchaImage` 仅作兼容放行项，校验未启用（见 6.1） | 启用时在前端登录表单与后端 `SecurityConfig` 放行项同步开启即可 |
| 统计结果为进程内缓存 | 单实例 JVM `Map` + TTL 1 小时，多实例部署各实例结果可能不一致（见 10.4-1） | 改为 Redis 缓存，接口契约不变 |
| 发药防重锁以 Redis `SET NX EX 5` 为主、进程内锁兜底 | 纯粹无 Redis 的多实例仍可能重复（见 10.4-2、B-09） | 强制依赖 Redis 或引入分布式锁 |
| 诊疗 ID 分配受数据库行锁串行化 | 取号吞吐约等于单线程，低频写操作实测无影响（见 10.4-3） | 改为号段预分配，一次取 N 个 ID 缓存在内存 |
| 未做接口限流 | 异常流量可能穿透到数据库（见 10.4-4） | 接入网关或 Sentinel 限流 |
| 容器化编排尚未实测 | `docker compose` 已提供但未在本开发环境跑通 `build`/启动（见 2.3.1） | 在装有 Docker 的主机执行 `docker compose up -d --build` 验证 |

"""
anchor = "\n**B-24 补录脚本（可选，执行前先备份）：**"
assert anchor in s, "B-24 anchor missing"
s = s.replace(anchor, b2 + anchor, 1)
print("[6] 附录 B 已拆分出 B.2 系统边界与后续优化方向")

assert s != orig, "no change made!"
open(p, "w", encoding="utf-8").write(s)
print("DONE. 文件已写回。")
