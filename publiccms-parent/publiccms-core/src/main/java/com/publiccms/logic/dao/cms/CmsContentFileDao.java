package com.publiccms.logic.dao.cms;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.publiccms.common.base.BaseDao;
import com.publiccms.common.constants.Constants;
import com.publiccms.common.handler.PageHandler;
import com.publiccms.common.handler.QueryHandler;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.cms.CmsContentFile;

/**
 *
 * CmsContentFileDao
 * 
 */
@Repository
public class CmsContentFileDao extends BaseDao<CmsContentFile> {

    /**
     * @param contentId
     * @param userId
     * @param fileTypes
     * @param orderField
     * @param orderType
     * @param pageIndex
     * @param pageSize
     * @return results page
     */
    public PageHandler getPage(Long contentId, Long userId, String[] fileTypes, String orderField, String orderType,
            Integer pageIndex, Integer pageSize) {
        QueryHandler queryHandler = getQueryHandler("from CmsContentFile bean");
        if (CommonUtils.notEmpty(contentId)) {
            queryHandler.condition("bean.contentId = :contentId").setParameter("contentId", contentId);
        }
        if (CommonUtils.notEmpty(userId)) {
            queryHandler.condition("bean.userId = :userId").setParameter("userId", userId);
        }
        if (null != fileTypes) {
            queryHandler.condition("bean.fileType in :fileTypes").setParameter("fileTypes", fileTypes);
        }
        if (!ORDERTYPE_ASC.equalsIgnoreCase(orderType)) {
            orderType = ORDERTYPE_DESC;
        }
        if (null == orderField) {
            orderField = Constants.BLANK;
        }
        switch (orderField) {
        case "size":
            queryHandler.order("bean.size").append(orderType);
            break;
        case "clicks":
            queryHandler.order("bean.clicks").append(orderType);
            break;
        default:
            queryHandler.order("bean.sort asc,bean.id asc");
        }
        return getPage(queryHandler, pageIndex, pageSize);
    }

    /**
     * @param contentId
     * @param fileTypes
     * @return results list
     */
    public List<CmsContentFile> getList(long contentId, String[] fileTypes) {
        QueryHandler queryHandler = getQueryHandler("from CmsContentFile bean");
        queryHandler.condition("bean.contentId = :contentId").setParameter("contentId", contentId);
        queryHandler.condition("bean.fileType in :fileTypes").setParameter("fileTypes", fileTypes);
        return getEntityList(queryHandler);
    }

    public void deleteByContentId(Long contentId) {
        if (null != contentId) {
            QueryHandler queryHandler = getQueryHandler("delete from CmsContentFile bean");
            queryHandler.condition("bean.contentId = :contentId").setParameter("contentId", contentId);
            delete(queryHandler);
        }
    }

    @Override
    protected CmsContentFile init(CmsContentFile entity) {
        return entity;
    }

}