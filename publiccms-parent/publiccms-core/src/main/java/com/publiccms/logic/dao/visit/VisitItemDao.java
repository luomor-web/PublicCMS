package com.publiccms.logic.dao.visit;

import java.util.Date;

// Generated 2021-1-14 22:44:12 by com.publiccms.common.generator.SourceGenerator

import org.springframework.stereotype.Repository;

import com.publiccms.common.base.BaseDao;
import com.publiccms.common.handler.PageHandler;
import com.publiccms.common.handler.QueryHandler;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.visit.VisitItem;

/**
 *
 * VisitItemDao
 *
 */
@Repository
public class VisitItemDao extends BaseDao<VisitItem> {

    /**
     * @param siteId
     * @param startVisitDate
     * @param endVisitDate
     * @param dayAnalytics 
     * @param itemType
     * @param itemId
     * @param pageIndex
     * @param pageSize
     * @return results page
     */
    public PageHandler getPage(short siteId, Date startVisitDate, Date endVisitDate, boolean dayAnalytics, String itemType, String itemId,
            Integer pageIndex, Integer pageSize) {
        QueryHandler queryHandler ;
        if(dayAnalytics) {
            queryHandler = getQueryHandler("from VisitItem bean");
        }else {
            queryHandler = getQueryHandler("select new VisitItem(bean.id.siteId,bean.id.itemType,bean.id.itemId,sum(bean.pv),sum(bean.uv),sum(bean.ipviews)) from VisitItem bean");
        }
        queryHandler.condition("bean.id.siteId = :siteId").setParameter("siteId", siteId);
        if (null != startVisitDate) {
            queryHandler.condition("bean.id.visitDate > :startVisitDate").setParameter("startVisitDate", startVisitDate);
        }
        if (null != endVisitDate) {
            queryHandler.condition("bean.id.visitDate <= :endVisitDate").setParameter("endVisitDate", endVisitDate);
        }
        if (CommonUtils.notEmpty(itemType)) {
            queryHandler.condition("bean.id.itemType <= :itemType").setParameter("itemType", itemType);
        }
        if (CommonUtils.notEmpty(itemId)) {
            queryHandler.condition("bean.id.itemId <= :itemId").setParameter("itemId", itemId);
        }
        if(dayAnalytics) {
            queryHandler.order("bean.id.visitDate").append(ORDERTYPE_DESC);
            queryHandler.order("bean.pv").append(ORDERTYPE_DESC);
        }else {
            queryHandler.group("bean.id.siteId").group("bean.id.itemType").group("bean.id.itemId");
            queryHandler.order("sum(bean.pv)").append(ORDERTYPE_DESC);
        }
        return getPage(queryHandler, pageIndex, pageSize);
    }

    /**
     * @param begintime
     * @return number of data deleted
     */
    public int delete(Date begintime) {
        if (null != begintime) {
            QueryHandler queryHandler = getQueryHandler("delete from VisitItem bean");
            queryHandler.condition("bean.id.visitDate <= :visitDate").setParameter("visitDate", begintime);
            return delete(queryHandler);
        }
        return 0;
    }

    @Override
    protected VisitItem init(VisitItem entity) {
        return entity;
    }

}