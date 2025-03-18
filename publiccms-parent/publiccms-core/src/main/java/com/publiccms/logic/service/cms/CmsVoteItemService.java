package com.publiccms.logic.service.cms;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Generated 2020-3-26 11:46:48 by com.publiccms.common.generator.SourceGenerator

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.publiccms.common.base.BaseService;
import com.publiccms.common.handler.PageHandler;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.cms.CmsVoteItem;
import com.publiccms.logic.dao.cms.CmsVoteItemDao;

/**
 *
 * CmsVoteItemService
 * 
 */
@Service
@Transactional
public class CmsVoteItemService extends BaseService<CmsVoteItem> {

    /**
     * @param voteId
     * @param orderField
     * @param orderType
     * @param pageIndex
     * @param pageSize
     * @return results page
     */
    @Transactional(readOnly = true)
    public PageHandler getPage(Long voteId, String orderField, String orderType, Integer pageIndex, Integer pageSize) {
        return dao.getPage(voteId, orderField, orderType, pageIndex, pageSize);
    }

    /**
     * @param id
     * @param votes
     * @return entity
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CmsVoteItem updateVotes(Serializable id, int votes) {
        CmsVoteItem entity = getEntity(id);
        if (null != entity) {
            entity.setVotes(entity.getVotes() + votes);
        }
        return entity;
    }

    /**
     * @param voteId
     * @param entitys
     * @param ignoreProperties
     */
    @SuppressWarnings("unchecked")
    public void update(long voteId, List<CmsVoteItem> entitys, String[] ignoreProperties) {
        Set<Long> idList = new HashSet<>();
        if (CommonUtils.notEmpty(entitys)) {
            for (CmsVoteItem entity : entitys) {
                if (null != entity.getId()) {
                    CmsVoteItem oldEntity = getEntity(entity.getId());
                    if (voteId == oldEntity.getVoteId()) {
                        update(entity.getId(), entity, ignoreProperties);
                    }
                } else {
                    entity.setVoteId(voteId);
                    save(entity);
                }
                idList.add(entity.getId());
            }
        }
        for (CmsVoteItem file : (List<CmsVoteItem>) getPage(voteId, null, null, null, null).getList()) {
            if (!idList.contains(file.getId())) {
                delete(file.getId());
            }
        }
    }

    /**
     * @param voteId
     */
    public void deleteByVoteId(Long voteId) {
        dao.deleteByVoteId(voteId);
    }

    @Resource
    private CmsVoteItemDao dao;

}