package com.publiccms.logic.component.site;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.publiccms.common.api.Cache;
import com.publiccms.common.cache.CacheEntity;
import com.publiccms.common.cache.CacheEntityFactory;
import com.publiccms.common.constants.Constants;
import com.publiccms.common.tools.CmsFileUtils;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.DateFormatUtils;
import com.publiccms.entities.sys.SysDomain;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.service.sys.SysDomainService;
import com.publiccms.logic.service.sys.SysSiteService;

import jakarta.annotation.Resource;

/**
 *
 * SiteComponent
 * 
 */
public class SiteComponent implements Cache {

    /**
     * 
     */
    public static final String TEMPLATE_PATH = "template";
    /**
     * 
     */
    public static final String BACKUP_PATH = "backup";
    /**
     * 
     */
    public static final String HISTORY_PATH = "history";
    /**
     * 
     */
    public static final String TASK_FILE_PATH = "task";
    /**
     * 
     */
    public static final String STATIC_FILE_PATH_WEB = "web";
    /**
     * 
     */
    public static final String STATIC_FILE_PATH_PRIVATE = "web/private";
    /**
     * 
     */
    public static final String SITE_FILE_PATH = "site";

    /**
     * 
     */
    public static final String SITE_PATH_PREFIX = "/site_";
    /**
     * 
     */
    public static final String MODEL_FILE = "model.data";
    /**
     * 
     */
    public static final String CATEGORY_TYPE_FILE = "categoryType.data";
    /**
     * 
     */
    public static final String CONFIG_FILE = "config.data";

    private CacheEntity<String, SysSite> siteCache;
    private CacheEntity<String, SysDomain> domainCache;
    private String rootPath;
    private String privateFilePath;
    private String webFilePath;
    private String taskTemplateFilePath;
    private String templateFilePath;

    private String webBackupFilePath;
    private String templateBackupFilePath;
    private String taskTemplateBackupFilePath;

    private String webHistoryFilePath;
    private String templateHistoryFilePath;
    private String taskTemplateHistoryFilePath;

    private short defaultSiteId;
    private boolean dictEnable = false;

    private Set<Short> masterSiteIdSet = new HashSet<>();
    @Resource
    private SysDomainService sysDomainService;
    @Resource
    private SysSiteService sysSiteService;

    /**
     * @param dictEnable
     *            the dictEnable to set
     */
    public void setDictEnable(boolean dictEnable) {
        this.dictEnable = dictEnable;
    }

    /**
     * @return the dictEnable
     */
    public boolean isDictEnable() {
        return dictEnable;
    }

    /**
     * @param siteId
     * @param path
     * @return full file name
     */
    public static String getFullTemplatePath(short siteId, String path) {
        return getFullFileName(siteId, path);
    }

    /**
     * @param siteId
     * @param path
     * @return full file name
     */
    public static String getFullFileName(short siteId, String path) {
        if (CommonUtils.empty(path)) {
            return CommonUtils.joinString(SITE_PATH_PREFIX, siteId);
        } else if (path.startsWith(Constants.SEPARATOR) || path.startsWith("\\")) {
            return CommonUtils.joinString(SITE_PATH_PREFIX, siteId, CmsFileUtils.getSafeFileName(path));
        } else {
            return CommonUtils.joinString(SITE_PATH_PREFIX, siteId, Constants.SEPARATOR, CmsFileUtils.getSafeFileName(path));
        }
    }

    /**
     * @param siteId
     * @param serverName
     * @param path
     * @return view name
     */
    public String getViewName(short siteId, String serverName, String path) {
        return getViewName(siteId, getDomain(serverName), path);
    }

    /**
     * @param siteId
     * @param sysDomain
     * @param path
     * @return view name
     */
    public String getViewName(short siteId, SysDomain sysDomain, String path) {
        if (CommonUtils.notEmpty(sysDomain.getPath())) {
            if (path.startsWith(Constants.SEPARATOR) || sysDomain.getPath().endsWith(Constants.SEPARATOR)) {
                if (path.startsWith(Constants.SEPARATOR) && sysDomain.getPath().endsWith(Constants.SEPARATOR)) {
                    path = CommonUtils.joinString(sysDomain.getPath(), path.substring(1));
                } else {
                    path = CommonUtils.joinString(sysDomain.getPath(), path);
                }
            } else {
                path = CommonUtils.joinString(sysDomain.getPath(), Constants.SEPARATOR, path);
            }
        }
        return getFullTemplatePath(siteId, path);
    }

    /**
     * @param serverName
     * @return domain
     */
    public SysDomain getDomain(String serverName) {
        SysDomain sysDomain = domainCache.get(serverName);
        if (null == sysDomain) {
            sysDomain = sysDomainService.getEntity(serverName);
            if (null == sysDomain) {
                int index;
                if (null != serverName && 0 < (index = serverName.indexOf(Constants.DOT))) {
                    String subname = serverName.substring(index + 1);
                    sysDomain = domainCache.get(subname);
                    if (null == sysDomain) {
                        sysDomain = sysDomainService.getEntity(subname);
                        if (null == sysDomain || !sysDomain.isWild()) {
                            sysDomain = new SysDomain();
                            sysDomain.setSiteId(defaultSiteId);
                        }
                    }
                } else {
                    sysDomain = new SysDomain();
                    sysDomain.setSiteId(defaultSiteId);
                }
            }
            domainCache.put(serverName, sysDomain);
        }
        return sysDomain;

    }

    /**
     * @param site
     * @param path
     * @return site
     */
    public String getPath(SysSite site, String path) {
        if (null != site.getParentId() && CommonUtils.notEmpty(site.getDirectory())) {
            int index = 0;
            if (path.startsWith(CommonUtils.joinString(Constants.SEPARATOR, site.getDirectory()))) {
                index = path.indexOf(Constants.SEPARATOR, 1);
            } else if (path.startsWith(site.getDirectory())) {
                index = path.indexOf(Constants.SEPARATOR);
            }
            if (0 < index) {
                return path.substring(index, path.length());
            }
        }
        return path;
    }

    /**
     * @param id
     * @return site
     */
    public SysSite getSiteById(Short id) {
        if (CommonUtils.notEmpty(id)) {
            String key = id.toString();
            SysSite site = siteCache.get(key);
            if (null == site) {
                try {
                    site = sysSiteService.getEntity(id);
                    siteCache.put(key, site);
                    return site;
                } catch (NumberFormatException e) {
                }
            } else {
                return site;
            }
        }
        return null;
    }

    /**
     * @param id
     * @return site
     */
    public SysSite getSiteById(String id) {
        if (CommonUtils.notEmpty(id)) {
            SysSite site = siteCache.get(id);
            if (null == site) {
                try {
                    site = sysSiteService.getEntity(Short.parseShort(id));
                    siteCache.put(id, site);
                    return site;
                } catch (NumberFormatException e) {
                }
            } else {
                return site;
            }
        }
        return null;
    }

    /**
     * @param domain
     * @param serverName
     * @param path
     * @return site
     */
    public SysSite getSite(SysDomain domain, String serverName, String path) {
        SysSite site = siteCache.get(serverName);
        if (null == site) {
            site = sysSiteService.getEntity(domain.getSiteId());
            siteCache.put(serverName, site);
        }
        if (site.isHasChild() && site.isMultiple() && CommonUtils.notEmpty(path)) {
            String directory = null;
            int index = 0;
            if (path.startsWith(Constants.SEPARATOR)) {
                index = path.indexOf(Constants.SEPARATOR, 1);
                if (0 < index) {
                    directory = path.substring(1, index);
                }
            } else {
                index = path.indexOf(Constants.SEPARATOR);
                if (0 < index) {
                    directory = path.substring(0, index);
                }
            }
            if (null != directory) {
                String cacheKey = CommonUtils.joinString(serverName, Constants.SEPARATOR, directory);
                SysSite newsite = siteCache.get(cacheKey);
                if (null == newsite) {
                    site = sysSiteService.getEntity(domain.getSiteId(), directory);
                }
                if (null != newsite) {
                    site = newsite;
                }
                siteCache.put(cacheKey, site);
            }
        }
        return site;
    }

    /**
     * @param serverName
     * @param path
     * @return site
     */
    public SysSite getSite(String serverName, String path) {
        SysDomain domain = getDomain(serverName);
        return getSite(domain, serverName, path);
    }

    /**
     * @param siteId
     * @return whether the master site
     */
    public boolean isMaster(short siteId) {
        return null != masterSiteIdSet && masterSiteIdSet.contains(siteId);
    }

    /**
     * @param siteId
     * @param filepath
     * @return web file path
     */
    public String getWebFilePath(short siteId, String filepath) {
        return CommonUtils.joinString(webFilePath, getFullFileName(siteId, filepath));
    }

    /**
     * @param siteId
     * @param filepath
     * @return private file path
     */
    public String getPrivateFilePath(short siteId, String filepath) {
        return CommonUtils.joinString(privateFilePath, getFullFileName(siteId, filepath));
    }

    /**
     * @param siteId
     * @param filepath
     * @param newfile
     *            add new file name
     * @return web history file path
     */
    public String getWebHistoryFilePath(short siteId, String filepath, boolean newfile) {
        StringBuilder sb = new StringBuilder(webHistoryFilePath);
        sb.append(getFullFileName(siteId, filepath));
        sb.append(Constants.SEPARATOR);
        if (newfile) {
            sb.append(DateFormatUtils.getDateFormat(DateFormatUtils.FILE_NAME_FORMAT_STRING).format(CommonUtils.getDate()));
        }
        return sb.toString();
    }

    /**
     * @param siteId
     * @param filepath
     * @return web backup file path
     */
    public String getWebBackupFilePath(short siteId, String filepath) {
        return CommonUtils.joinString(webBackupFilePath, getFullFileName(siteId, filepath));
    }

    /**
     * @param path
     * @return site file path
     */
    public String getSiteFilePath(String path) {
        return CommonUtils.joinString(rootPath, BACKUP_PATH, Constants.SEPARATOR, SITE_FILE_PATH, Constants.SEPARATOR, path);
    }

    /**
     * @param siteId
     * @param templatePath
     * @return task template file path
     */
    public String getTaskTemplateFilePath(short siteId, String templatePath) {
        return CommonUtils.joinString(getTaskTemplateFilePath(), getFullTemplatePath(siteId, templatePath));
    }

    /**
     * @param siteId
     * @param templatePath
     * @param newfile
     *            add new file name
     * @return task template history file path
     */
    public String getTaskTemplateHistoryFilePath(short siteId, String templatePath, boolean newfile) {
        StringBuilder sb = new StringBuilder(taskTemplateHistoryFilePath);
        sb.append(getFullFileName(siteId, templatePath));
        sb.append(Constants.SEPARATOR);
        if (newfile) {
            sb.append(DateFormatUtils.getDateFormat(DateFormatUtils.FILE_NAME_FORMAT_STRING).format(CommonUtils.getDate()));
        }
        return sb.toString();
    }

    /**
     * @param siteId
     * @param templatePath
     * @return task template backup file path
     */
    public String getTaskTemplateBackupFilePath(short siteId, String templatePath) {
        return CommonUtils.joinString(taskTemplateBackupFilePath, getFullFileName(siteId, templatePath));
    }

    /**
     * @param siteId
     * @param templatePath
     * @return template file path
     */
    public String getTemplateFilePath(short siteId, String templatePath) {
        return CommonUtils.joinString(getTemplateFilePath(), getFullTemplatePath(siteId, templatePath));
    }

    /**
     * @param siteId
     * @param templatePath
     * @param newfile
     *            add new file name
     * @return template history file path
     */
    public String getTemplateHistoryFilePath(short siteId, String templatePath, boolean newfile) {
        StringBuilder sb = new StringBuilder(templateHistoryFilePath);
        sb.append(getFullFileName(siteId, templatePath));
        sb.append(Constants.SEPARATOR);
        if (newfile) {
            sb.append(DateFormatUtils.getDateFormat(DateFormatUtils.FILE_NAME_FORMAT_STRING).format(CommonUtils.getDate()));
        }
        return sb.toString();
    }

    /**
     * @param siteId
     * @param templatePath
     * @return template backup file path
     */
    public String getTemplateBackupFilePath(short siteId, String templatePath) {
        return CommonUtils.joinString(templateBackupFilePath, getFullFileName(siteId, templatePath));
    }

    /**
     * @param siteId
     * @return model file path
     */
    public String getModelFilePath(short siteId) {
        return CommonUtils.joinString(getTemplateFilePath(), getFullTemplatePath(siteId, MODEL_FILE));
    }

    /**
     * @param siteId
     * @return category type file path
     */
    public String getCategoryTypeFilePath(short siteId) {
        return CommonUtils.joinString(getTemplateFilePath(), getFullTemplatePath(siteId, CATEGORY_TYPE_FILE));
    }

    /**
     * @param siteId
     * @return config file path
     */
    public String getConfigFilePath(short siteId) {
        return CommonUtils.joinString(getTemplateFilePath(), getFullTemplatePath(siteId, CONFIG_FILE));
    }

    /**
     * @param defaultSiteId
     */
    public void setDefaultSiteId(short defaultSiteId) {
        this.defaultSiteId = defaultSiteId;
    }

    /**
     * @param masterSiteIds
     */
    public void setMasterSiteIds(String masterSiteIds) {
        String[] masters = StringUtils.split(masterSiteIds, Constants.COMMA);
        for (String master : masters) {
            try {
                Short id = Short.parseShort(master);
                masterSiteIdSet.add(id);
            } catch (NumberFormatException e) {
            }
        }
    }

    /**
     * @param rootPath
     */
    public void setRootPath(String rootPath) {
        if (CommonUtils.notEmpty(rootPath)) {
            if (!(rootPath.endsWith(Constants.SEPARATOR) || rootPath.endsWith("\\"))) {
                rootPath = CommonUtils.joinString(rootPath, Constants.SEPARATOR);
            }
        }
        this.rootPath = rootPath;
        this.webFilePath = CommonUtils.joinString(rootPath, STATIC_FILE_PATH_WEB);
        this.privateFilePath = CommonUtils.joinString(rootPath, STATIC_FILE_PATH_PRIVATE);
        this.taskTemplateFilePath = CommonUtils.joinString(rootPath, TASK_FILE_PATH);
        this.templateFilePath = CommonUtils.joinString(rootPath, TEMPLATE_PATH);
        this.templateBackupFilePath = CommonUtils.joinString(rootPath, BACKUP_PATH, Constants.SEPARATOR, TEMPLATE_PATH);
        this.taskTemplateBackupFilePath = CommonUtils.joinString(rootPath, BACKUP_PATH, Constants.SEPARATOR, TASK_FILE_PATH);
        this.webBackupFilePath = CommonUtils.joinString(rootPath, BACKUP_PATH, Constants.SEPARATOR, STATIC_FILE_PATH_WEB);
        this.templateHistoryFilePath = CommonUtils.joinString(rootPath, HISTORY_PATH, Constants.SEPARATOR, TEMPLATE_PATH);
        this.taskTemplateHistoryFilePath = CommonUtils.joinString(rootPath, HISTORY_PATH, Constants.SEPARATOR, TASK_FILE_PATH);
        this.webHistoryFilePath = CommonUtils.joinString(rootPath, HISTORY_PATH, Constants.SEPARATOR, STATIC_FILE_PATH_WEB);
    }

    @Override
    public void clear() {
        siteCache.clear(false);
        domainCache.clear(false);
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
        domainCache = cacheEntityFactory.createCacheEntity("domain");
        siteCache = cacheEntityFactory.createCacheEntity("site");
    }

    /**
     * @return root path
     */
    public String getRootPath() {
        return rootPath;
    }

    /**
     * @return task template file path
     */
    public String getTaskTemplateFilePath() {
        return taskTemplateFilePath;
    }

    /**
     * @return web template file path
     */
    public String getTemplateFilePath() {
        return templateFilePath;
    }
}