-- AI 模块菜单权限
-- 执行顺序：medical_menu.sql 之后、quartz.sql 之前
--
-- 说明：
--   1. "医疗管理"一级目录在 medical_menu.sql 中已创建，这里**不重复新建**，
--      先查出其 menu_id 再挂载子菜单（避免与既有脚本冲突产生重复菜单）；
--   2. 全部使用 NOT EXISTS 幂等判定，脚本重复执行不会叠加菜单；
--   3. order_num 从 8 起，避开已有菜单（患者1/诊疗2/药品3/看板4/报表5/随访6/站内信7）。

SET @aiParentId = (SELECT menu_id FROM sys_menu WHERE menu_type = 'M' AND path = 'medical' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '医疗管理', 0, 10, 'medical', NULL, 1, 0, 'M', '0', '0', '', 'el-icon-s-grid', 'admin', NOW(), '医疗管理目录'
FROM dual WHERE @aiParentId IS NULL;

SET @aiParentId = IFNULL(@aiParentId, LAST_INSERT_ID());

-- ---------------- 二级菜单：AI病历书写 ----------------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 'AI病历书写', @aiParentId, 8, 'ai-record', 'medical/ai/record', 1, 0, 'C', '0', '0', 'medical:ai:record:list', 'el-icon-edit-outline', 'admin', NOW(), 'AI智能病历辅助书写菜单'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiParentId AND path = 'ai-record');

SET @aiRecordMenuId = (SELECT menu_id FROM sys_menu WHERE parent_id = @aiParentId AND path = 'ai-record' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '生成病历', @aiRecordMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:ai:record:generate', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiRecordMenuId AND perms = 'medical:ai:record:generate');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '采纳入病历', @aiRecordMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'medical:ai:record:adopt', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiRecordMenuId AND perms = 'medical:ai:record:adopt');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '记录删除', @aiRecordMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'medical:ai:record:remove', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiRecordMenuId AND perms = 'medical:ai:record:remove');

-- ---------------- 二级菜单：疾病风险预测 ----------------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '疾病风险预测', @aiParentId, 9, 'ai-risk', 'medical/ai/risk', 1, 0, 'C', '0', '0', 'medical:ai:risk:list', 'el-icon-data-analysis', 'admin', NOW(), '常见病风险预测菜单'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiParentId AND path = 'ai-risk');

SET @aiRiskMenuId = (SELECT menu_id FROM sys_menu WHERE parent_id = @aiParentId AND path = 'ai-risk' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '发起预测', @aiRiskMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:ai:risk:predict', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiRiskMenuId AND perms = 'medical:ai:risk:predict');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '记录删除', @aiRiskMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'medical:ai:risk:remove', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiRiskMenuId AND perms = 'medical:ai:risk:remove');

-- ---------------- 二级菜单：胸片AI初筛 ----------------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '胸片AI初筛', @aiParentId, 10, 'ai-xray', 'medical/ai/xray', 1, 0, 'C', '0', '0', 'medical:ai:xray:list', 'el-icon-picture-outline', 'admin', NOW(), '胸片影像AI初筛菜单'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiParentId AND path = 'ai-xray');

SET @aiXrayMenuId = (SELECT menu_id FROM sys_menu WHERE parent_id = @aiParentId AND path = 'ai-xray' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '影像上传识别', @aiXrayMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:ai:xray:upload', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiXrayMenuId AND perms = 'medical:ai:xray:upload');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '记录删除', @aiXrayMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'medical:ai:xray:remove', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiXrayMenuId AND perms = 'medical:ai:xray:remove');

-- ---------------- 二级菜单：RAG导诊问答 ----------------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '智能导诊问答', @aiParentId, 11, 'ai-chat', 'medical/ai/chat', 1, 0, 'C', '0', '0', 'medical:ai:chat:list', 'el-icon-chat-dot-round', 'admin', NOW(), 'RAG智能导诊问答菜单'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiParentId AND path = 'ai-chat');

SET @aiChatMenuId = (SELECT menu_id FROM sys_menu WHERE parent_id = @aiParentId AND path = 'ai-chat' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '发起咨询', @aiChatMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:ai:chat:ask', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiChatMenuId AND perms = 'medical:ai:chat:ask');

-- ---------------- 二级菜单：导诊知识库 ----------------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '导诊知识库', @aiParentId, 12, 'ai-knowledge', 'medical/ai/knowledge', 1, 0, 'C', '0', '0', 'medical:ai:knowledge:list', 'el-icon-collection', 'admin', NOW(), '导诊知识库维护菜单'
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiParentId AND path = 'ai-knowledge');

SET @aiKbMenuId = (SELECT menu_id FROM sys_menu WHERE parent_id = @aiParentId AND path = 'ai-knowledge' ORDER BY menu_id LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '知识新增', @aiKbMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'medical:ai:knowledge:add', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiKbMenuId AND perms = 'medical:ai:knowledge:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '知识修改', @aiKbMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'medical:ai:knowledge:edit', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiKbMenuId AND perms = 'medical:ai:knowledge:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '知识删除', @aiKbMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'medical:ai:knowledge:remove', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiKbMenuId AND perms = 'medical:ai:knowledge:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '同步向量库', @aiKbMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'medical:ai:knowledge:sync', '#', 'admin', NOW(), ''
FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @aiKbMenuId AND perms = 'medical:ai:knowledge:sync');

-- 菜单 SQL 结束，无需 commit（依赖当前会话自动提交或外层统一提交）
