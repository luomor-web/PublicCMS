package com.publiccms.logic.service.sys;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

// Generated 2023-8-16 by com.publiccms.common.generator.SourceGenerator

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.publiccms.common.base.BaseService;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.sys.SysWorkflowStep;
import com.publiccms.logic.dao.sys.SysWorkflowStepDao;

/**
 *
 * SysWorkflowStepService
 * 
 */
@Service
@Transactional
public class SysWorkflowStepService extends BaseService<SysWorkflowStep> {

    /**
     * @param workflowId
     * @param pageIndex
     * @param pageSize
     * @return results page
     */
    @Transactional(readOnly = true)
    public List<SysWorkflowStep> getList(Integer workflowId) {
        return dao.getList(workflowId);
    }

    
    /**
     * @param workflowId
     * @param entityList
     * @return first step id
     */
    public Long save(int workflowId, List<SysWorkflowStep> entityList) {
        Long firstStepId = null;
        if (CommonUtils.notEmpty(entityList)) {
            SysWorkflowStep entity = null;
            ListIterator<SysWorkflowStep> iterator = entityList.listIterator(entityList.size());
            while (iterator.hasPrevious()) {
                entity = iterator.previous();
                entity.setWorkflowId(workflowId);
                entity.setNextStepId(null == entity ? null : entity.getId());
                save(entity);
                if(null==firstStepId) {
                    firstStepId = entity.getId();
                }
            }
        }
        return firstStepId;
    }

    /**
     * @param workflowId
     * @param entitys
     * @param ignoreProperties
     * @return first step id
     */
    public Long update(int workflowId, List<SysWorkflowStep> entitys, String[] ignoreProperties) {
        Set<Long> idList = new HashSet<>();
        Long firstStepId = null;
        if (CommonUtils.notEmpty(entitys)) {
            SysWorkflowStep entity = null;
            ListIterator<SysWorkflowStep> iterator = entitys.listIterator(entitys.size());
            while (iterator.hasPrevious()) {
                entity = iterator.previous();
                if (null != entity.getId()) {
                    SysWorkflowStep oldEntity = getEntity(entity.getId());
                    if (workflowId == oldEntity.getWorkflowId()) {
                        entity.setNextStepId(null == entity ? null : entity.getId());
                        update(entity.getId(), entity, ignoreProperties);
                    }
                } else {
                    entity.setWorkflowId(workflowId);
                    entity.setNextStepId(null == entity ? null : entity.getId());
                    save(entity);
                }
                if(null==firstStepId) {
                    firstStepId = entity.getId();
                }
                idList.add(entity.getId());
            }
        }
        for (SysWorkflowStep step : getList(workflowId)) {
            if (!idList.contains(step.getId())) {
                delete(step.getId());
            }
        }
        return firstStepId;
    }

    /**
     * @param wordflowId
     */
    public void deleteByWorkflowId(Long wordflowId) {
        dao.deleteByWorkflowId(wordflowId);
    }

    @Resource
    private SysWorkflowStepDao dao;

}