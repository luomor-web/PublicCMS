package com.publiccms.logic.service.sys;

// Generated 2023-8-16 by com.publiccms.common.generator.SourceGenerator

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.publiccms.common.base.BaseService;
import com.publiccms.common.handler.PageHandler;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.sys.SysWorkflow;
import com.publiccms.entities.sys.SysWorkflowProcess;
import com.publiccms.entities.sys.SysWorkflowStep;
import com.publiccms.logic.dao.sys.SysWorkflowProcessDao;

/**
 *
 * SysWorkflowProcessService
 * 
 */
@Service
@Transactional
public class SysWorkflowProcessService extends BaseService<SysWorkflowProcess> {
    /**
     * 
     */
    public static final String ITEM_TYPE_CONTENT = "content";
    /**
     * 
     */
    public static final String ITEM_TYPE_PLACE = "place";
    /**
     * 
     */
    public static final String ITEM_TYPE_CERTIFICATION = "certification";
    /**
     * 
     */
    public static final String ITEM_TYPE_REFUND = "refund";
    /**
     * 
     */
    public static final String ITEM_TYPE_ORDER = "order";
    @Resource
    private SysWorkflowService workflowService;
    @Resource
    private SysWorkflowStepService workflowStepService;

    /**
     * @param siteId
     * @param itemType
     * @param itemId
     * @param closed
     * @param pageIndex
     * @param pageSize
     * @return results page
     */
    @Transactional(readOnly = true)
    public PageHandler getPage(Short siteId, String itemType, String itemId, Boolean closed, Integer pageIndex,
            Integer pageSize) {
        return dao.getPage(siteId, itemType, itemId, closed, pageIndex, pageSize);
    }

    public SysWorkflowProcess createProcess(short siteId, int workflowId, String itemType, String itemId) {
        SysWorkflow workflow = workflowService.getEntity(workflowId);
        if (null != workflow && siteId == workflow.getSiteId() && null != workflow.getStartStepId()) {
            SysWorkflowStep step = workflowStepService.getEntity(workflow.getStartStepId());
            SysWorkflowProcess entity = new SysWorkflowProcess(siteId, itemType, itemId, step.getId(), step.getRoleId(),
                    step.getDeptId(), step.getUserId(), false, CommonUtils.getDate());
            save(entity);
            return entity;
        }
        return null;
    }

    @Resource
    private SysWorkflowProcessDao dao;

}