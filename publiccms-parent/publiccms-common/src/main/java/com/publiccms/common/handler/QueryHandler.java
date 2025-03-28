package com.publiccms.common.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.query.Query;

import com.publiccms.common.constants.Constants;
import com.publiccms.common.tools.CommonUtils;

/**
 *
 * QueryHandler
 * 
 */
public class QueryHandler {

    /**
     * 
     */
    public static final String COUNT_SQL = "select count(*) ";
    /**
     * 
     */
    public static final String KEYWORD_FROM = " from ";
    /**
     * 
     */
    public static final String KEYWORD_ORDER = " order by ";
    /**
     * 
     */
    public static final String KEYWORD_GROUP = " group by ";

    boolean whereFlag = true;
    boolean orderFlag = true;
    boolean groupFlag = true;
    private StringBuilder sqlBuilder;
    private Map<String, Object> map;
    private Map<String, Object[]> arrayMap;
    private Map<String, Collection<?>> collectionMap;
    private Integer firstResult;
    private Integer maxResults;
    private Boolean cacheable;

    /**
     * @param sql
     */
    public QueryHandler(String sql) {
        this.sqlBuilder = new StringBuilder(Constants.BLANK_SPACE);
        sqlBuilder.append(sql);
    }

    /**
     * 
     */
    public QueryHandler() {
        this.sqlBuilder = new StringBuilder();
    }

    /**
     * @param condition
     * @return query handler
     */
    public QueryHandler condition(String condition) {
        if (whereFlag) {
            whereFlag = false;
            sqlBuilder.append(" where ");
        } else {
            sqlBuilder.append(" and ");
        }
        sqlBuilder.append(condition);
        return this;
    }

    /**
     * @param sqlString
     * @return query handler
     */
    public QueryHandler order(String sqlString) {
        if (orderFlag) {
            orderFlag = false;
            append(KEYWORD_ORDER);
        } else {
            sqlBuilder.append(Constants.COMMA_DELIMITED);
        }
        sqlBuilder.append(sqlString);
        return this;
    }

    /**
     * @param sqlString
     * @return query handler
     */
    public QueryHandler group(String sqlString) {
        if (groupFlag) {
            groupFlag = false;
            sqlBuilder.append(KEYWORD_GROUP);
        } else {
            sqlBuilder.append(Constants.COMMA_DELIMITED);
        }
        sqlBuilder.append(sqlString);
        return this;
    }

    /**
     * @param sqlString
     * @return query handler
     */
    public QueryHandler appendWithoutSpace(String sqlString) {
        sqlBuilder.append(sqlString);
        return this;
    }

    /**
     * @param sqlString
     * @return query handler
     */
    public QueryHandler append(String sqlString) {
        sqlBuilder.append(Constants.BLANK_SPACE);
        sqlBuilder.append(sqlString);
        return this;
    }

    /**
     * @param firstResult
     * @return query handler
     */
    public QueryHandler setFirstResult(Integer firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    /**
     * @param maxResults
     * @return query handler
     */
    public QueryHandler setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * @param cacheable
     * @return query handler
     */
    public QueryHandler setCacheable(Boolean cacheable) {
        this.cacheable = cacheable;
        return this;
    }

    /**
     * @param key
     * @param value
     * @return query handler
     */
    public QueryHandler setParameter(String key, Object value) {
        if (null == map) {
            map = new HashMap<>();
        }
        map.put(key, value);
        return this;
    }

    /**
     * @param key
     * @param value
     * @return query handler
     */
    public QueryHandler setParameter(String key, Object[] value) {
        if (null == arrayMap) {
            arrayMap = new HashMap<>();
        }
        arrayMap.put(key, value);
        return this;
    }

    /**
     * @param key
     * @param value
     * @return query handler
     */
    public QueryHandler setParameter(String key, Collection<?> value) {
        if (null == collectionMap) {
            collectionMap = new HashMap<>();
        }
        collectionMap.put(key, value);
        return this;
    }

    public <T> Query<T> initQuery(Query<T> query) {
        return initQuery(query, true);
    }

    public <T> Query<T> initQuery(Query<T> query, boolean pageable) {
        if (null != map) {
            for (Entry<String, Object> entry : map.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
        }
        if (null != arrayMap) {
            for (Entry<String, Object[]> entry : arrayMap.entrySet()) {
                query.setParameterList(entry.getKey(), entry.getValue());
            }
        }
        if (null != collectionMap) {
            for (Entry<String, Collection<?>> entrySet : collectionMap.entrySet()) {
                query.setParameterList(entrySet.getKey(), entrySet.getValue());
            }
        }
        if (pageable) {
            if (null != firstResult) {
                query.setFirstResult(firstResult);
            }
            if (null != maxResults) {
                query.setMaxResults(maxResults);
            }
        }
        query.setCacheable(null == cacheable || cacheable);
        return query;
    }

    public String getSql() {
        return sqlBuilder.toString();
    }

    public String getCountSql() {
        String sql = getSql();
        sql = sql.substring(sql.toLowerCase().indexOf(KEYWORD_FROM));
        int orderIndex = sql.toLowerCase().indexOf(KEYWORD_ORDER);
        if (-1 != orderIndex) {
            sql = sql.substring(0, orderIndex);
        }
        return CommonUtils.joinString(COUNT_SQL, sql);
    }

}
