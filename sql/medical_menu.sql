-- 若依医疗管理 菜单权限
-- 适用于 RuoYi-Vue v3.8.7 (sys_menu 使用 path/component/is_frame/is_cache 等新字段)

-- 一级菜单：医疗管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('医疗管理', 0, 10, 'medical', null, 1, 0, 'M', '0', '0', '', 'el-icon-s-grid', 'admin', NOW(), '医疗管理目录');

SET @parentId = LAST_INSERT_ID();

-- 二级菜单：患者信息
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('患者信息', @parentId, 1, 'patient', 'medical/patient/index', 1, 0, 'C', '0', '0', 'medical:patient:list', 'el-icon-user', 'admin', NOW(), '患者信息菜单');

SET @patientMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('患者信息查询', @patientMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:patient:query', '#', 'admin', NOW(), ''),
       ('患者信息新增', @patientMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'medical:patient:add',   '#', 'admin', NOW(), ''),
       ('患者信息修改', @patientMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'medical:patient:edit',  '#', 'admin', NOW(), ''),
       ('患者信息删除', @patientMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'medical:patient:remove','#', 'admin', NOW(), ''),
       ('患者信息导出', @patientMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'medical:patient:export','#', 'admin', NOW(), '');

-- 二级菜单：诊疗信息
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('诊疗信息', @parentId, 2, 'diagnosis', 'medical/diagnosis/index', 1, 0, 'C', '0', '0', 'medical:diagnosis:list', 'el-icon-first-aid-kit', 'admin', NOW(), '诊疗信息菜单');

SET @diagnosisMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('诊疗信息查询', @diagnosisMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:diagnosis:query', '#', 'admin', NOW(), ''),
       ('诊疗信息新增', @diagnosisMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'medical:diagnosis:add',   '#', 'admin', NOW(), ''),
       ('诊疗信息修改', @diagnosisMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'medical:diagnosis:edit',  '#', 'admin', NOW(), ''),
       ('诊疗信息删除', @diagnosisMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'medical:diagnosis:remove','#', 'admin', NOW(), ''),
       ('诊疗信息导出', @diagnosisMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'medical:diagnosis:export','#', 'admin', NOW(), '');

-- 二级菜单：费用明细报表
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('费用明细报表', @parentId, 5, 'feeReport', 'medical/feeReport/index', 1, 0, 'C', '0', '0', 'medical:fee:list', 'el-icon-money', 'admin', NOW(), '诊疗费用明细报表菜单');

SET @feeMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('费用报表查询', @feeMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:fee:query', '#', 'admin', NOW(), ''),
       ('费用报表导出', @feeMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'medical:fee:export','#', 'admin', NOW(), '');

-- 二级菜单：药品信息
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('药品信息', @parentId, 3, 'drug', 'medical/drug/index', 1, 0, 'C', '0', '0', 'medical:drug:list', 'el-icon-s-management', 'admin', NOW(), '药品信息菜单');

SET @drugMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('药品信息查询', @drugMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:query',   '#', 'admin', NOW(), ''),
       ('药品信息新增', @drugMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:add',     '#', 'admin', NOW(), ''),
       ('药品信息修改', @drugMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:edit',    '#', 'admin', NOW(), ''),
       ('药品信息删除', @drugMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:remove',  '#', 'admin', NOW(), ''),
       ('药品信息导出', @drugMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:export',  '#', 'admin', NOW(), ''),
       ('药品低库存预警', @drugMenuId, 6, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:warn',   '#', 'admin', NOW(), ''),
       ('药品近效期预警', @drugMenuId, 7, '#', '', 1, 0, 'F', '0', '0', 'medical:drug:expiry', '#', 'admin', NOW(), '');

-- 二级菜单：医疗数据看板
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('医疗数据看板', @parentId, 4, 'statistics', 'medical/statistics/index', 1, 0, 'C', '0', '0', 'medical:statistics:list', 'el-icon-data-line', 'admin', NOW(), '医疗数据看板菜单');

SET @statisticsMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('看板查看', @statisticsMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:statistics:view', '#', 'admin', NOW(), '');

-- 二级菜单：患者随访
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('患者随访', @parentId, 6, 'followup', 'medical/followup/index', 1, 0, 'C', '0', '0', 'medical:followup:list', 'el-icon-phone-outline', 'admin', NOW(), '患者随访菜单');

SET @followupMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('随访记录查询', @followupMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:followup:query', '#', 'admin', NOW(), ''),
       ('随访记录新增', @followupMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'medical:followup:add',   '#', 'admin', NOW(), ''),
       ('随访记录修改', @followupMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'medical:followup:edit',  '#', 'admin', NOW(), ''),
       ('随访记录删除', @followupMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'medical:followup:remove','#', 'admin', NOW(), ''),
       ('随访记录导出', @followupMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'medical:followup:export','#', 'admin', NOW(), '');

-- 二级菜单：站内信
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('站内信', @parentId, 7, 'message', 'medical/message/index', 1, 0, 'C', '0', '0', 'medical:message:list', 'el-icon-chat-dot-round', 'admin', NOW(), '站内信消息中心菜单');

SET @messageMenuId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('消息查看', @messageMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:message:query', '#', 'admin', NOW(), ''),
       ('消息已读', @messageMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'medical:message:edit', '#', 'admin', NOW(), ''),
       ('消息删除', @messageMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'medical:message:remove','#', 'admin', NOW(), '');

-- ------------------------------------------------------------------
-- 监控管理：
-- 若依基础库 ry_20231130.sql 中已存在 "系统监控"(path=monitor) 目录与
-- "定时任务"(path=job) 菜单（menu_id=110），本脚本若直接新建会产生重复菜单。
-- 因此下面全部改为"先查再插"：已存在则复用其 menu_id，仅补缺失的按钮权限。
-- ------------------------------------------------------------------
SET @monitorParentId = (SELECT menu_id FROM sys_menu WHERE menu_type = 'M' AND path = 'monitor' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '监控管理', 0, 20, 'monitor', NULL, 1, 0, 'M', '0', '0', '', 'el-icon-monitor', 'admin', NOW(), '监控管理目录'
FROM dual WHERE @monitorParentId IS NULL;

SET @monitorParentId = IFNULL(@monitorParentId, LAST_INSERT_ID());

-- 二级菜单：定时任务（已存在则复用）
SET @qrtzJobMenuId = (SELECT menu_id FROM sys_menu WHERE menu_type = 'C' AND path = 'job' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '定时任务', @monitorParentId, 1, 'job', 'monitor/job/index', 1, 0, 'C', '0', '0', 'monitor:job:list', 'el-icon-time', 'admin', NOW(), '定时任务菜单'
FROM dual WHERE @qrtzJobMenuId IS NULL;

SET @qrtzJobMenuId = IFNULL(@qrtzJobMenuId, LAST_INSERT_ID());

-- 按钮权限：已存在的不重复插入（基础库已配 query/add/edit/remove/changeStatus/export）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '定时任务查询', @qrtzJobMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'monitor:job:query', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @qrtzJobMenuId AND perms = 'monitor:job:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '定时任务新增', @qrtzJobMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'monitor:job:add', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @qrtzJobMenuId AND perms = 'monitor:job:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '定时任务修改', @qrtzJobMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'monitor:job:edit', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @qrtzJobMenuId AND perms = 'monitor:job:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '定时任务删除', @qrtzJobMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'monitor:job:remove', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @qrtzJobMenuId AND perms = 'monitor:job:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '定时任务执行', @qrtzJobMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'monitor:job:run', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @qrtzJobMenuId AND perms = 'monitor:job:run');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '定时任务状态', @qrtzJobMenuId, 6, '#', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @qrtzJobMenuId AND perms = 'monitor:job:changeStatus');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '定时任务导出', @qrtzJobMenuId, 7, '#', '', 1, 0, 'F', '0', '0', 'monitor:job:export', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @qrtzJobMenuId AND perms = 'monitor:job:export');

-- 二级菜单：定时任务日志（已存在则复用）
SET @qrtzLogMenuId = (SELECT menu_id FROM sys_menu WHERE menu_type = 'C' AND path = 'job/log' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '定时任务日志', @monitorParentId, 2, 'job/log', 'monitor/job/log', 1, 0, 'C', '0', '0', 'monitor:job:log', 'el-icon-document', 'admin', NOW(), '定时任务日志菜单'
FROM dual WHERE @qrtzLogMenuId IS NULL;

SET @qrtzLogMenuId = IFNULL(@qrtzLogMenuId, LAST_INSERT_ID());

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '日志查询', @qrtzLogMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'monitor:job:log:query', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @qrtzLogMenuId AND perms = 'monitor:job:log:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '日志删除', @qrtzLogMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'monitor:job:log:remove', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @qrtzLogMenuId AND perms = 'monitor:job:log:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '日志清空', @qrtzLogMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'monitor:job:log:clean', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @qrtzLogMenuId AND perms = 'monitor:job:log:clean');

-- ------------------------------------------------------------------
-- 裁剪版后端未实现的监控菜单，隐藏以免点击后 404
-- （本项目后端只有 medical 6 个 Controller + quartz 2 个，不含
--   在线用户 / 数据监控 / 服务监控 / 缓存监控 / 缓存列表 / 操作日志 / 登录日志）
-- 若后续补齐了对应后端接口，把 visible 改回 '0' 即可重新显示
-- ------------------------------------------------------------------
UPDATE sys_menu SET visible = '1'
WHERE path IN ('online', 'druid', 'server', 'cache', 'cacheList', 'operlog', 'logininfor')
  AND menu_type = 'C';
