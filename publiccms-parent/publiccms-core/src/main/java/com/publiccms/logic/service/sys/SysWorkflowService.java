package com.publiccms.logic.service.sys;

import java.util.ArrayList;
import java.util.List;

// Generated 2023-8-16 by com.publiccms.common.generator.SourceGenerator

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.publiccms.common.base.BaseService;
import com.publiccms.common.handler.PageHandler;
import com.publiccms.entities.sys.SysWorkflow;
import com.publiccms.logic.dao.sys.SysWorkflowDao;

/**
 *
 * SysWorkflowService
 * 
 */
@Service
@Transactional
public class SysWorkflowService extends BaseService<SysWorkflow> {

    /**
     * @param siteId
     * @param name
     * @param disabled
     * @param orderType
     * @param pageIndex
     * @param pageSize
     * @return results page
     */
    @Transactional(readOnly = true)
    public PageHandler getPage(Short siteId, String name, Boolean disabled, String orderType, Integer pageIndex,
            Integer pageSize) {
        return dao.getPage(siteId, name, disabled, orderType, pageIndex, pageSize);
    }

    public void updateStartStepId(int workflowId, Long startStepId) {
        if (null != startStepId) {
            SysWorkflow entity = getEntity(workflowId);
            if (null != entity) {
                entity.setStartStepId(startStepId);
            }
        }
    }

    /**
     * @param siteId
     * @param ids
     * @return
     */
    public List<SysWorkflow> delete(short siteId, Integer[] ids) {
        List<SysWorkflow> entityList = new ArrayList<>();
        for (SysWorkflow entity : getEntitys(ids)) {
            if (siteId == entity.getSiteId() && !entity.isDisabled()) {
                entity.setDisabled(true);
                entityList.add(entity);
            }
        }
        return entityList;
    }

    @Resource
    private SysWorkflowDao dao;

}