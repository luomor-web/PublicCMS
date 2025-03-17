package com.publiccms.logic.service.sys;

// Generated 2023-8-16 by com.publiccms.common.generator.SourceGenerator

import javax.annotation.Resource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.publiccms.common.base.BaseService;
import com.publiccms.common.constants.Constants;
import com.publiccms.common.handler.PageHandler;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.entities.sys.SysWorkflow;
import com.publiccms.entities.sys.SysWorkflowProcess;
import com.publiccms.entities.sys.SysWorkflowProcessHistory;
import com.publiccms.entities.sys.SysWorkflowStep;
import com.publiccms.logic.component.workflow.ProcessComponent;
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
    public static final String ITEM_TYPE_CUSTOM = "custom";
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
    private SysWorkflowProcessItemService itemService;
    @Resource
    private SysWorkflowStepService workflowStepService;
    @Resource
    private SysWorkflowProcessHistoryService historyService;
    @Resource
    private ProcessComponent processComponent;

    /**
     * @param siteId
     * @param itemType
     * @param itemId
     * @param roleId
     * @param deptId
     * @param userId
     * @param closed
     * @param pageIndex
     * @param pageSize
     * @return results page
     */
    @Transactional(readOnly = true)
    public PageHandler getPage(Short siteId, String itemType, String itemId, Integer roleId, Integer deptId, Long userId,
            Boolean closed, Integer pageIndex, Integer pageSize) {
        return dao.getPage(siteId, itemType, itemId, roleId, deptId, userId, closed, pageIndex, pageSize);
    }

    public SysWorkflowProcess createProcess(short siteId, int workflowId, String itemType, String itemId) {
        SysWorkflow workflow = workflowService.getEntity(workflowId);
        if (null != workflow && siteId == workflow.getSiteId() && null != workflow.getStartStepId()) {
            SysWorkflowStep step = workflowStepService.getEntity(workflow.getStartStepId());
            SysWorkflowProcess entity = new SysWorkflowProcess(siteId, itemType, itemId, step.getId(), false,
                    CommonUtils.getDate());
            entity.setRoleId(step.getRoleId());
            entity.setDeptId(step.getDeptId());
            entity.setUserId(step.getUserId());
            save(entity);
            if (CommonUtils.notEmpty(itemId)) {
                itemService.createOrUpdate(itemType, itemId, entity.getId());
            }
            return entity;
        }
        return null;
    }

    public SysWorkflowProcess dealProcess(short siteId, SysWorkflowProcessHistory history, SysUser user) {
        SysWorkflowProcess entity = getEntity(history.getProcessId());
        if (null != entity && siteId == entity.getSiteId() && !entity.isClosed() && entity.getStepId() == history.getStepId()
                && (null != entity.getRoleId()
                        && ArrayUtils.contains(StringUtils.split(user.getRoles(), Constants.COMMA), entity.getRoleId())
                        || null != entity.getDeptId() && user.getDeptId() == entity.getDeptId()
                        || null != entity.getUserId() && user.getId() == entity.getUserId())) {
            SysWorkflowStep step = workflowStepService.getEntity(entity.getStepId());
            if (null == step.getNextStepId()) {
                entity.setClosed(true);
                processComponent.process(entity, user, history);
            } else {
                entity.setStepId(step.getNextStepId());
                entity.setRoleId(step.getRoleId());
                entity.setDeptId(step.getDeptId());
                entity.setUserId(step.getUserId());
            }
            historyService.save(history);
            return entity;
        }
        return null;
    }

    @Resource
    private SysWorkflowProcessDao dao;

}