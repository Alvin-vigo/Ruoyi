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

-- 二级菜单：药品信息
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('药品信息', @medicalDirId, 3, 'drug', 'medical/drug/index', 1, 0, 'C', '0', '0', 'medical:drug:list', 'el-icon-first-aid-kit', 'admin', NOW(), '药品信息菜单');

SET @drugMenuId = LAST_INSERT_ID();

-- 三级按钮权限：查询/新增/修改/删除/导出
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('药品信息查询', @drugMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:query',  '#', 'admin', NOW(), ''),
       ('药品信息新增', @drugMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:add',    '#', 'admin', NOW(), ''),
       ('药品信息修改', @drugMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:edit',   '#', 'admin', NOW(), ''),
       ('药品信息删除', @drugMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:remove', '#', 'admin', NOW(), ''),
       ('药品信息导出', @drugMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:export', '#', 'admin', NOW(), '');
