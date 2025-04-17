package com.publiccms.logic.component.config;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.typography.font.sfntly.Font;
import com.publiccms.common.api.Config;
import com.publiccms.common.api.SiteCache;
import com.publiccms.common.cache.CacheEntity;
import com.publiccms.common.cache.CacheEntityFactory;
import com.publiccms.common.constants.Constants;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.JsonUtils;
import com.publiccms.entities.sys.SysExtendField;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.component.site.SiteComponent;
import com.publiccms.views.pojo.model.SimpleAiMessage;

import jakarta.annotation.Resource;

/**
 *
 * SimpleAiConfigComponent 文本配置组件
 *
 */
@Component
public class SimpleAiConfigComponent implements Config, SiteCache {

    /**
     * config code
     */
    public static final String CONFIG_CODE = "ai";

    /**
     * config description code
     */
    public static final String CONFIG_CODE_DESCRIPTION = CommonUtils.joinString(CONFIGPREFIX, CONFIG_CODE);
    /**
     * url
     */
    public static final String CONFIG_URL = "url";
    /**
     * token
     */
    public static final String CONFIG_TOKEN = "token";
    /**
     * model
     */
    public static final String CONFIG_MODEL = "model";

    private CacheEntity<Short, Font> cache;
    @Resource
    protected ConfigDataComponent configDataComponent;
    @Resource
    protected SiteComponent siteComponent;

    /**
     * @param siteId
     * @param showAll
     * @return config code or null
     */
    @Override
    public String getCode(short siteId, boolean showAll) {
        return CONFIG_CODE;
    }

    /**
     * @param locale
     * @return
     */
    @Override
    public String getCodeDescription(Locale locale) {
        return getMessage(locale, CONFIG_CODE_DESCRIPTION);
    }

    @Override
    public List<SysExtendField> getExtendFieldList(SysSite site, Locale locale) {
        List<SysExtendField> extendFieldList = new ArrayList<>();
        extendFieldList.add(new SysExtendField(CONFIG_URL, INPUTTYPE_TEXT, false,
                getMessage(locale, CommonUtils.joinString(CONFIG_CODE_DESCRIPTION, Constants.DOT, INPUTTYPE_TEXT)), null, null));
        extendFieldList.add(new SysExtendField(CONFIG_TOKEN, INPUTTYPE_TEXT, false,
                getMessage(locale, CommonUtils.joinString(CONFIG_CODE_DESCRIPTION, Constants.DOT, INPUTTYPE_TEXT)), null, null));
        extendFieldList.add(new SysExtendField(CONFIG_MODEL, INPUTTYPE_TEXT, false,
                getMessage(locale, CommonUtils.joinString(CONFIG_CODE_DESCRIPTION, Constants.DOT, INPUTTYPE_TEXT)), null, null));
        return extendFieldList;
    }

    public HttpRequest getHttpRequest(short siteId, List<SimpleAiMessage> messageList) {
        Map<String, String> configData = configDataComponent.getConfigData(siteId, CONFIG_CODE);
        if (CommonUtils.notEmpty(messageList) && CommonUtils.notEmpty(configData.get(CONFIG_URL))
                && CommonUtils.notEmpty(configData.get(CONFIG_TOKEN)) && CommonUtils.notEmpty(configData.get(CONFIG_MODEL))) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(configData.get(CONFIG_URL)))
                    .header("Authorization", CommonUtils.joinString("Bearer ", configData.get(CONFIG_TOKEN)))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            CommonUtils.joinString("{\"model\":\"deepseek-r1\",\"messages\":[", JsonUtils.getString(messageList),
                                    "],\"stream\":true,\"stream_options\": {\"include_usage\": true}}")))
                    .build();
            return request;
        }
        return null;
    }

    @Override
    public void clear(short siteId) {
        cache.remove(siteId);
    }

    @Override
    public void clear() {
        cache.clear(false);
    }

    /**
     * @param cacheEntityFactory
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    @Resource
    public void initCache(CacheEntityFactory cacheEntityFactory)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        cache = cacheEntityFactory.createCacheEntity(CONFIG_CODE, CacheEntityFactory.MEMORY_CACHE_ENTITY);
    }

    @Override
    public boolean exportable() {
        return true;
    }
}
