-- 2024-09-25 --
UPDATE sys_module SET authorized_url= 'cmsPlace/push,cmsPlace/add,cmsPlace/save,cmsContent/push_content,cmsContent/push_content_list,cmsContent/push_to_content,cmsContent/push_to_relation,cmsContent/related,cmsContent/unrelated,cmsPlace/delete' WHERE id ='content_push';
UPDATE sys_module SET authorized_url= 'cmsTemplate/save,cmsTemplate/saveMetaData,cmsWebFile/lookup,cmsTemplate/help' WHERE id ='template_content';
DROP TABLE IF EXISTS `sys_user_attribute`;
CREATE TABLE `sys_user_attribute` (
  `user_id` bigint(20) NOT NULL,
  `settings` text NULL COMMENT '设置JSON',
  `data` longtext COMMENT '数据JSON',
  PRIMARY KEY  (`user_id`)
) COMMENT='用户扩展';
-- 2024-11-04 --
ALTER TABLE `cms_dictionary_exclude`
    DROP INDEX `cms_dictionary_parent_value`,
    ADD INDEX `cms_dictionary_exclude_dictionary_id` (`dictionary_id`, `site_id`);
ALTER TABLE `cms_dictionary_exclude_value`
    DROP INDEX `cms_dictionary_parent_value`,
    ADD INDEX `cms_dictionary_exclude_value_dictionary_id` (`dictionary_id`, `site_id`);
ALTER TABLE `visit_item`
    DROP INDEX `visit_item_session_id`,
    ADD INDEX `visit_item_visit_date` (`site_id`, `visit_date`, `item_type`, `item_id`, `pv`);
-- 2024-11-07 --
UPDATE sys_module SET parent_id = 'vote_list' WHERE parent_id = 'content_vote';
-- 2024-11-28 --
DELETE FROM sys_module WHERE id IN ('log_login_delete','log_operate_delete');
DELETE FROM sys_module_lang WHERE module_id IN ('log_login_delete','log_operate_delete');
-- 2025-03-03 --
UPDATE sys_module SET parent_id = 'tag_list',sort=0, attached=NULL WHERE id = 'tag_type_list';
UPDATE sys_module SET parent_id = 'content_list',sort=0, attached=NULL WHERE id = 'content_recycle_list';
-- 2025-03-11 --
ALTER TABLE `sys_site` 
    ADD COLUMN `has_child` tinyint(1) NOT NULL COMMENT '拥有子站点' AFTER `dynamic_path`,
    ADD COLUMN `multiple` tinyint(1) NOT NULL COMMENT '站点群' AFTER `has_child`;
update sys_site s1, sys_site s2 SET s1.has_child = 1 WHERE s1.id = s2.parent_id;
update sys_site SET multiple = 1 WHERE id in(SELECT site_id FROM sys_domain WHERE multiple = 1);
ALTER TABLE `sys_domain` DROP COLUMN `multiple`;
ALTER TABLE `sys_dept` 
    ADD COLUMN `has_child` tinyint(1) NOT NULL COMMENT '拥有子部门' AFTER `user_id`;
UPDATE sys_dept d1,sys_dept d2 SET d1.has_child = 1 WHERE d1.id = d2.parent_id;
ALTER TABLE `sys_module` 
    ADD COLUMN `has_child` tinyint(1) NOT NULL COMMENT '拥有子模块' AFTER `menu`;
update sys_module m1,sys_module m2 SET m1.has_child = 1 WHERE m1.id = m2.parent_id;
-- 2025-03-12 --
UPDATE sys_module SET parent_id = 'myself_profile',sort=0, attached=NULL WHERE id = 'myself_password';
INSERT INTO `sys_module` VALUES ('category_add_more', 'cmsCategory/addMore', NULL, NULL, 'category_list', 0, 0, 0);
UPDATE sys_module SET parent_id = 'order_list', sort=0, authorized_url='sysUser/lookup', attached=NULL WHERE id = 'order_history_list';
UPDATE sys_module SET parent_id = 'account_list', sort=0, attached=NULL WHERE id = 'account_history_list';
UPDATE sys_module SET parent_id = 'trade_payment', sort=0, attached=NULL WHERE id = 'payment_history_list';
INSERT INTO `sys_module` VALUES ('task_template_list_export', NULL, 'taskTemplate/export', NULL, 'task_template_list', 0, 0, 0);
UPDATE sys_module SET authorized_url = 'taskTemplate/save,taskTemplate/upload,taskTemplate/doUpload,taskTemplate/chipLookup,cmsTemplate/help' WHERE id = 'task_template_content';
UPDATE sys_module SET authorized_url = 'sysUser/lookup' WHERE id = 'payment_list';
UPDATE sys_module SET authorized_url = 'cmsTemplate/saveMetadata,cmsTemplate/createDirectory' WHERE id = 'template_metadata';
UPDATE sys_module SET authorized_url = 'cmsTemplate/help,cmsTemplate/savePlace,cmsWebFile/lookup' WHERE id = 'place_template_content';
UPDATE sys_module SET url = NULL WHERE id = 'template_export';
INSERT INTO `sys_module_lang` VALUES ('category_add_more', 'en', 'Add/edit');
INSERT INTO `sys_module_lang` VALUES ('category_add_more', 'ja', '追加/変更');
INSERT INTO `sys_module_lang` VALUES ('category_add_more', 'zh', '增加/修改');
INSERT INTO `sys_module_lang` VALUES ('task_template_list_export', 'en', 'Export');
INSERT INTO `sys_module_lang` VALUES ('task_template_list_export', 'ja', '輸出');
INSERT INTO `sys_module_lang` VALUES ('task_template_list_export', 'zh', '导出');
INSERT INTO `sys_module` VALUES ('select_role', 'sysRole/lookup', NULL, NULL, 'common', 1, 0, 0);
INSERT INTO `sys_module_lang` VALUES ('select_role', 'en', 'Select role');
INSERT INTO `sys_module_lang` VALUES ('select_role', 'ja', '役割を選択');
INSERT INTO `sys_module_lang` VALUES ('select_role', 'zh', '选择角色');
INSERT INTO `sys_module` VALUES ('select_workflow', 'sysWorkflow/lookup', NULL, NULL, 'common', 1, 0, 0);
INSERT INTO `sys_module_lang` VALUES ('select_workflow', 'en', 'Select workflow');
INSERT INTO `sys_module_lang` VALUES ('select_workflow', 'ja', 'ワークフローを選択');
INSERT INTO `sys_module_lang` VALUES ('select_workflow', 'zh', '选择流程');
INSERT INTO `sys_module` VALUES ('system_workflow', 'sysWorkflow/list', NULL, 'bi bi-diagram-3', 'system', 1, 1, 0);
INSERT INTO `sys_module` VALUES ('system_workflow_add', 'sysWorkflow/add', 'sysWorkflow/save', NULL, 'system_workflow', 0, 0, 0);
INSERT INTO `sys_module` VALUES ('system_workflow_delete', NULL, 'sysWorkflow/delete', NULL, 'system_workflow', 1, 0, 0);
INSERT INTO `sys_module_lang` VALUES ('system_workflow', 'en', 'Workflow');
INSERT INTO `sys_module_lang` VALUES ('system_workflow', 'ja', 'ワークフロー');
INSERT INTO `sys_module_lang` VALUES ('system_workflow', 'zh', '工作流程');
INSERT INTO `sys_module_lang` VALUES ('system_workflow_add', 'en', 'Add');
INSERT INTO `sys_module_lang` VALUES ('system_workflow_add', 'ja', '追加');
INSERT INTO `sys_module_lang` VALUES ('system_workflow_add', 'zh', '增加');
INSERT INTO `sys_module_lang` VALUES ('system_workflow_delete', 'en', 'Delete');
INSERT INTO `sys_module_lang` VALUES ('system_workflow_delete', 'ja', '削除');
INSERT INTO `sys_module_lang` VALUES ('system_workflow_delete', 'zh', '删除');
UPDATE sys_module SET sort=8 WHERE id ='file';
UPDATE sys_module SET parent_id = 'report_visit', attached=NULL WHERE id in ('visit_day','visit_history','visit_item','visit_session','visit_url');
UPDATE sys_module SET parent_id = 'operation', sort=0 WHERE id = 'report_user';
