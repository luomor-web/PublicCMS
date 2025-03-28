package com.publiccms.logic.service.visit;

// Generated 2021-1-14 22:43:59 by com.publiccms.common.generator.SourceGenerator
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.publiccms.common.base.BaseService;
import com.publiccms.common.handler.PageHandler;
import com.publiccms.entities.visit.VisitHistory;
import com.publiccms.entities.visit.VisitDay;
import com.publiccms.entities.visit.VisitItem;
import com.publiccms.entities.visit.VisitSession;
import com.publiccms.entities.visit.VisitUrl;
import com.publiccms.logic.dao.visit.VisitHistoryDao;

/**
 *
 * VisitHistoryService
 * 
 */
@Service
@Transactional
public class VisitHistoryService extends BaseService<VisitHistory> {

    /**
     * @param siteId
     * @param sessionId
     * @param ip
     * @param url
     * @param userId
     * @param startCreateDate
     * @param endCreateDate
     * @param orderType
     * @param pageIndex
     * @param pageSize
     * @return results page
     */
    @Transactional(readOnly = true)
    public PageHandler getPage(Short siteId, String sessionId, String ip, String url, Long userId, Date startCreateDate,
            Date endCreateDate, String orderType, Integer pageIndex, Integer pageSize) {
        return dao.getPage(siteId, sessionId, ip, url, userId, startCreateDate, endCreateDate, orderType, pageIndex, pageSize);
    }

    /**
     * @param siteId
     * @param startCreateDate
     * @param endCreateDate
     * @param maxResults 
     * @return results page
     */
    @Transactional(readOnly = true)
    public List<VisitSession> getSessionList(Short siteId, Date startCreateDate, Date endCreateDate, Integer maxResults) {
        return dao.getSessionList(siteId, startCreateDate, endCreateDate, maxResults);
    }

    /**
     * @param siteId
     * @param visitDate
     * @param visitHour
     * @param maxResults 
     * @return results page
     */
    @Transactional(readOnly = true)
    public List<VisitDay> getHourList(Short siteId, Date visitDate, Byte visitHour, Integer maxResults) {
        return dao.getHourList(siteId, visitDate, visitHour, maxResults);
    }

    /**
     * @param siteId
     * @param visitDate
     * @param itemType
     * @param itemId
     * @param maxResults 
     * @return results page
     */
    @Transactional(readOnly = true)
    public List<VisitItem> getItemList(Short siteId, Date visitDate, String itemType, String itemId, Integer maxResults) {
        return dao.getItemList(siteId, visitDate, itemType, itemId, maxResults);
    }

    /**
     * @param siteId
     * @param url
     * @param visitDate
     * @param maxResults 
     * @return results page
     */
    @Transactional(readOnly = true)
    public List<VisitUrl> getUrlList(Short siteId, String url, Date visitDate, Integer maxResults) {
        return dao.getUrlList(siteId, url, visitDate, maxResults);
    }

    /**
     * @param begintime
     * @return number of data deleted
     */
    public int delete(Date begintime) {
        return dao.delete(begintime);
    }

    /**
     * @param blockingQueue
     */
    public void save(BlockingQueue<VisitHistory> blockingQueue) {
        VisitHistory entity = null;
        while (null != (entity = blockingQueue.poll())) {
            save(entity);
        }
    }

    @Resource
    private VisitHistoryDao dao;

}