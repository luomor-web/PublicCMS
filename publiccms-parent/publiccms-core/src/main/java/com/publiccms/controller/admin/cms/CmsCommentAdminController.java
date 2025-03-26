package com.publiccms.controller.admin.cms;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

// Generated 2018-11-7 16:25:07 by com.publiccms.common.generator.SourceGenerator

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.publiccms.common.annotation.Csrf;
import com.publiccms.common.constants.CommonConstants;
import com.publiccms.common.constants.Constants;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.ControllerUtils;
import com.publiccms.common.tools.JsonUtils;
import com.publiccms.common.tools.RequestUtils;
import com.publiccms.entities.cms.CmsComment;
import com.publiccms.entities.cms.CmsContent;
import com.publiccms.entities.log.LogOperate;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.logic.component.config.ConfigDataComponent;
import com.publiccms.logic.component.config.SiteConfigComponent;
import com.publiccms.logic.component.site.SiteComponent;
import com.publiccms.logic.component.template.TemplateComponent;
import com.publiccms.logic.service.cms.CmsCommentService;
import com.publiccms.logic.service.cms.CmsContentService;
import com.publiccms.logic.service.log.LogLoginService;
import com.publiccms.logic.service.log.LogOperateService;

import freemarker.template.TemplateException;

/**
 *
 * CmsCommentAdminController
 * 
 */
@Controller
@RequestMapping("cmsComment")
public class CmsCommentAdminController {
    protected final Log log = LogFactory.getLog(getClass());
    @Resource
    protected LogOperateService logOperateService;
    @Resource
    protected SiteComponent siteComponent;
    @Resource
    private TemplateComponent templateComponent;
    @Resource
    private CmsContentService contentService;
    @Resource
    protected ConfigDataComponent configDataComponent;

    private String[] ignoreProperties = new String[] { "siteId", "userId", "createDate", "checkUserId", "checkDate", "contentId",
            "ip", "status", "replyId", "replyUserId", "replies", "scores", "disabled" };

    /**
     * @param site
     * @param admin
     * @param entity
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("save")
    @Csrf
    public String save(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, CmsComment entity,
            HttpServletRequest request, ModelMap model) {

        String ip = RequestUtils.getIpAddress(request);
        entity.setIp(ip);
        if (null != entity.getId()) {
            CmsComment oldEntity = service.getEntity(entity.getId());
            if (null == oldEntity || ControllerUtils.errorNotEquals("siteId", site.getId(), oldEntity.getSiteId(), model)) {
                return CommonConstants.TEMPLATE_ERROR;
            }
            entity.setUpdateDate(CommonUtils.getDate());
            entity = service.update(entity.getId(), entity, ignoreProperties);
            logOperateService.save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(),
                    LogLoginService.CHANNEL_WEB_MANAGER, "update.cmsComment", RequestUtils.getIpAddress(request),
                    CommonUtils.getDate(), JsonUtils.getString(entity)));
        } else {
            Date now = CommonUtils.getDate();
            entity.setSiteId(site.getId());
            entity.setUserId(admin.getId());
            entity.setStatus(CmsCommentService.STATUS_NORMAL);
            entity.setCheckUserId(admin.getId());
            entity.setCheckDate(now);
            if (null != entity.getReplyId()) {
                CmsComment reply = service.updateReplies(site.getId(), entity.getReplyId(), 1);
                if (null == reply) {
                    entity.setReplyId(null);
                } else {
                    entity.setContentId(reply.getContentId());
                    if (null == entity.getReplyUserId()) {
                        entity.setReplyUserId(reply.getUserId());
                    }
                }
            }
            if (null != entity.getReplyUserId() && entity.getReplyUserId().equals(admin.getId())) {
                entity.setReplyUserId(null);
            }
            service.save(entity);
            logOperateService
                    .save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(), LogLoginService.CHANNEL_WEB_MANAGER,
                            "save.cmsComment", RequestUtils.getIpAddress(request), now, JsonUtils.getString(entity)));
        }
        Map<String, String> config = configDataComponent.getConfigData(site.getId(), SiteConfigComponent.CONFIG_CODE);
        boolean needStatic = ConfigDataComponent.getBoolean(config.get(SiteConfigComponent.CONFIG_STATIC_AFTER_COMMENT), false);
        if (needStatic && CmsCommentService.STATUS_NORMAL == entity.getStatus()) {
            CmsContent content = contentService.getEntity(entity.getContentId());
            if (null != content && !content.isDisabled()) {
                try {
                    templateComponent.createContentFile(site, content, null, null);
                } catch (IOException | TemplateException e) {
                    model.addAttribute(CommonConstants.ERROR, e.getMessage());
                    log.error(e.getMessage(), e);
                    return CommonConstants.TEMPLATE_ERROR;
                }
            }
        }
        return CommonConstants.TEMPLATE_DONE;
    }

    /**
     * @param site
     * @param admin
     * @param ids
     * @param request
     * @param model
     * @return view name
     */
    @RequestMapping("check")
    @Csrf
    public String check(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, Long[] ids, HttpServletRequest request,
            ModelMap model) {
        if (CommonUtils.notEmpty(ids)) {
            Set<CmsContent> contentSet = service.check(site.getId(), ids, admin.getId());
            Map<String, String> config = configDataComponent.getConfigData(site.getId(), SiteConfigComponent.CONFIG_CODE);
            boolean needStatic = ConfigDataComponent.getBoolean(config.get(SiteConfigComponent.CONFIG_STATIC_AFTER_COMMENT), false);
            if (needStatic) {
                try {
                    for (CmsContent content : contentSet) {
                        templateComponent.createContentFile(site, content, null, null);
                    }
                } catch (IOException | TemplateException e) {
                    model.addAttribute(CommonConstants.ERROR, e.getMessage());
                    log.error(e.getMessage(), e);
                    return CommonConstants.TEMPLATE_ERROR;
                }
            }
            logOperateService.save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(),
                    LogLoginService.CHANNEL_WEB_MANAGER, "check.cmsComment", RequestUtils.getIpAddress(request),
                    CommonUtils.getDate(), StringUtils.join(ids, Constants.COMMA)));
        }
        return CommonConstants.TEMPLATE_DONE;
    }

    /**
     * @param site
     * @param admin
     * @param ids
     * @param request
     * @param model
     * @return view name
     */
    @RequestMapping("uncheck")
    @Csrf
    public String uncheck(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, Long[] ids, HttpServletRequest request,
            ModelMap model) {
        if (CommonUtils.notEmpty(ids)) {
            Set<CmsContent> contentSet = service.uncheck(site.getId(), ids);
            Map<String, String> config = configDataComponent.getConfigData(site.getId(), SiteConfigComponent.CONFIG_CODE);
            boolean needStatic = ConfigDataComponent.getBoolean(config.get(SiteConfigComponent.CONFIG_STATIC_AFTER_COMMENT), false);
            if (needStatic) {
                try {
                    for (CmsContent content : contentSet) {
                        templateComponent.createContentFile(site, content, null, null);// 静态化
                    }
                } catch (IOException | TemplateException e) {
                    model.addAttribute(CommonConstants.ERROR, e.getMessage());
                    log.error(e.getMessage(), e);
                    return CommonConstants.TEMPLATE_ERROR;
                }
            }
            logOperateService.save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(),
                    LogLoginService.CHANNEL_WEB_MANAGER, "uncheck.cmsComment", RequestUtils.getIpAddress(request),
                    CommonUtils.getDate(), StringUtils.join(ids, Constants.COMMA)));
        }
        return CommonConstants.TEMPLATE_DONE;
    }

    /**
     * @param site
     * @param admin
     * @param ids
     * @param request
     * @param model
     * @return operate result
     */
    @RequestMapping("delete")
    @Csrf
    public String delete(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, Long[] ids, HttpServletRequest request,
            ModelMap model) {
        if (CommonUtils.notEmpty(ids)) {
            Set<CmsContent> contentSet = service.delete(site.getId(), ids);
            Map<String, String> config = configDataComponent.getConfigData(site.getId(), SiteConfigComponent.CONFIG_CODE);
            boolean needStatic = ConfigDataComponent.getBoolean(config.get(SiteConfigComponent.CONFIG_STATIC_AFTER_COMMENT), false);
            if (needStatic) {
                try {
                    for (CmsContent content : contentSet) {
                        templateComponent.createContentFile(site, content, null, null);// 静态化
                    }
                } catch (IOException | TemplateException e) {
                    model.addAttribute(CommonConstants.ERROR, e.getMessage());
                    log.error(e.getMessage(), e);
                    return CommonConstants.TEMPLATE_ERROR;
                }
            }
            logOperateService.save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(),
                    LogLoginService.CHANNEL_WEB_MANAGER, "delete.cmsComment", RequestUtils.getIpAddress(request),
                    CommonUtils.getDate(), StringUtils.join(ids, Constants.COMMA)));
        }
        return CommonConstants.TEMPLATE_DONE;
    }

    @Resource
    private CmsCommentService service;
}