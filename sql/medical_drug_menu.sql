-- ----------------------------
-- 药品管理 菜单权限 SQL（作业3：代码生成器实战）
-- 适用于 RuoYi-Vue v3.8.7（sys_menu 使用 path/component/is_frame/is_cache 字段）
-- 依赖：需先执行 ry_20231130.sql 初始化基表，且"医疗管理"目录已存在时可将 parent_id 指向该目录
-- 说明：本脚本按"是否存在医疗管理目录"自适应——存在则挂载到医疗管理下，否则新建顶级目录
-- ----------------------------

-- 一级菜单：医疗管理（存在则复用，不存在则创建）
SET @medicalDirId = (SELECT menu_id FROM sys_menu WHERE menu_name = '医疗管理' AND parent_id = 0 LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '医疗管理', 0, 10, 'medical', null, 1, 0, 'M', '0', '0', '', 'el-icon-s-grid', 'admin', NOW(), '医疗管理目录'
FROM DUAL WHERE @medicalDirId IS NULL;
SET @medicalDirId = (SELECT menu_id FROM sys_menu WHERE menu_name = '医疗管理' AND parent_id = 0 LIMIT 1);

-- 二级菜单：药品信息（幂等：已存在则跳过）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '药品信息', @medicalDirId, 3, 'drug', 'medical/drug/index', 1, 0, 'C', '0', '0', 'medical:drug:list', 'el-icon-first-aid-kit', 'admin', NOW(), '药品信息菜单'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @medicalDirId AND path = 'drug');

-- 【重要】必须按 path 反查，不能用 LAST_INSERT_ID()：
-- 若上面因已存在而跳过插入，LAST_INSERT_ID() 会残留上一次插入的 id，导致按钮权限挂到错误的父菜单上。
SET @drugMenuId = (SELECT menu_id FROM sys_menu WHERE parent_id = @medicalDirId AND path = 'drug' LIMIT 1);

-- 三级按钮权限：查询/新增/修改/删除/导出（按 perms 幂等判重）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '药品信息查询', @drugMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:query',  '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @drugMenuId AND perms = 'medical:drug:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '药品信息新增', @drugMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:add',    '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @drugMenuId AND perms = 'medical:drug:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '药品信息修改', @drugMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:edit',   '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @drugMenuId AND perms = 'medical:drug:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '药品信息删除', @drugMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:remove', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @drugMenuId AND perms = 'medical:drug:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '药品信息导出', @drugMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:export', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @drugMenuId AND perms = 'medical:drug:export');
