package com.publiccms.logic.service.visit;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

// Generated 2021-1-14 22:44:12 by com.publiccms.common.generator.SourceGenerator

import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.publiccms.common.base.BaseService;
import com.publiccms.common.handler.PageHandler;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.visit.VisitItem;
import com.publiccms.logic.dao.visit.VisitItemDao;

/**
 *
 * VisitItemService
 * 
 */
@Service
@Transactional
public class VisitItemService extends BaseService<VisitItem> {
    @Resource
    private VisitHistoryService visitHistoryService;

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
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public PageHandler getPage(short siteId, Date startVisitDate, Date endVisitDate, boolean dayAnalytics, String itemType,
            String itemId, Integer pageIndex, Integer pageSize) {
        PageHandler page = dao.getPage(siteId, startVisitDate, endVisitDate, dayAnalytics, itemType, itemId, pageIndex, pageSize);
        Date now = CommonUtils.getMinuteDate();
        if (dayAnalytics && null != page.getList() && (null == pageIndex || 1 == pageIndex)
                && (null == endVisitDate || DateUtils.isSameDay(now, endVisitDate))) {
            ((List<VisitItem>) page.getList()).addAll(0, visitHistoryService.getItemList(siteId, now, itemType, itemId, pageSize));
        }
        return page;
    }

    /**
     * @param begintime
     * @return number of data deleted
     */
    public int delete(Date begintime) {
        return dao.delete(begintime);
    }

    @Resource
    private VisitItemDao dao;

}