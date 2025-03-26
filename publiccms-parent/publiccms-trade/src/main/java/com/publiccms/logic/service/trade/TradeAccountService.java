package com.publiccms.logic.service.trade;

import java.math.BigDecimal;
import java.util.Date;

// Generated 2019-6-16 9:47:27 by com.publiccms.common.generator.SourceGenerator

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.publiccms.common.base.BaseService;
import com.publiccms.common.handler.PageHandler;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.entities.trade.TradeAccount;
import com.publiccms.entities.trade.TradeAccountHistory;
import com.publiccms.logic.dao.trade.TradeAccountDao;
import com.publiccms.logic.dao.trade.TradeAccountHistoryDao;
import com.publiccms.logic.service.sys.SysUserService;

/**
 *
 * TradeAccountService
 * 
 */
@Service
@Transactional
public class TradeAccountService extends BaseService<TradeAccount> {

    /**
     * 
     * @param siteId
     * @param orderField
     * @param orderType
     * @param pageIndex
     * @param pageSize
     * @return results page
     */
    @Transactional(readOnly = true)
    public PageHandler getPage(Short siteId, String orderField, String orderType, Integer pageIndex, Integer pageSize) {
        return dao.getPage(siteId, orderField, orderType, pageIndex, pageSize);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TradeAccount getOrCreate(short siteId, long accountId) {
        TradeAccount entity = getEntity(accountId);
        if (null == entity) {
            SysUser user = userService.getEntity(accountId);
            if (null != user && siteId == user.getSiteId()) {
                entity = new TradeAccount(accountId, siteId, BigDecimal.ZERO);
                save(entity);
            }
        } else if (siteId == entity.getSiteId()) {
            return entity;
        }
        return null;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TradeAccountHistory change(short siteId, String serialNumber, long accountId, Long userId, int status,
            BigDecimal change, String description) {
        if (null != change) {
            TradeAccount account = getOrCreate(siteId, accountId);
            if (null != account) {
                BigDecimal balance = change.add(account.getAmount());
                if (0 <= balance.compareTo(BigDecimal.ZERO)) {
                    Date now = CommonUtils.getDate();
                    TradeAccountHistory history = new TradeAccountHistory(siteId, serialNumber, accountId, userId, change,
                            account.getAmount(), balance, status, description, now);
                    historyDao.save(history);
                    account.setAmount(balance);
                    account.setUpdateDate(now);
                    return history;
                }
            }
        }
        return null;
    }

    @Resource
    private TradeAccountDao dao;
    @Resource
    private TradeAccountHistoryDao historyDao;
    @Resource
    private SysUserService userService;

}