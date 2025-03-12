package com.publiccms.logic.service.sys;

// Generated 2025-3-11 by com.publiccms.common.generator.SourceGenerator

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.publiccms.common.base.BaseService;
import com.publiccms.entities.sys.SysWorkflowProcessItem;
import com.publiccms.logic.dao.sys.SysWorkflowProcessItemDao;

/**
 *
 * SysWorkflowProcessItemService
 * 
 */
@Service
@Transactional
public class SysWorkflowProcessItemService extends BaseService<SysWorkflowProcessItem> {
    @Resource
    private SysWorkflowProcessItemDao dao;

}