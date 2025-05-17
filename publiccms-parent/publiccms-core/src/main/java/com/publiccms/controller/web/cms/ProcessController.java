package com.publiccms.controller.web.cms;

// Generated 2023-8-16 by com.publiccms.common.generator.SourceGenerator

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import com.publiccms.common.annotation.Csrf;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.entities.sys.SysWorkflowProcessHistory;
import com.publiccms.logic.component.config.SafeConfigComponent;
import com.publiccms.logic.service.log.LogOperateService;
import com.publiccms.logic.service.sys.SysWorkflowProcessService;

/**
 *
 * ProcessController
 * 
 */
@Controller
@RequestMapping("workflowProcess")
public class ProcessController {
    @Resource
    protected SafeConfigComponent safeConfigComponent;

    /**
     * @param site
     * @param user
     * @param returnUrl 
     * @param entity
     * @param request
     * @return operate result
     */
    @RequestMapping("handle")
    @Csrf
    public String handle(@RequestAttribute SysSite site, @SessionAttribute SysUser user, String returnUrl,
            SysWorkflowProcessHistory entity, HttpServletRequest request) {
        returnUrl = safeConfigComponent.getSafeUrl(returnUrl, site, request.getContextPath());
        service.handleProcess(site, entity, user);
        return CommonUtils.joinString(UrlBasedViewResolver.REDIRECT_URL_PREFIX, returnUrl);
    }

    @Resource
    private SysWorkflowProcessService service;
    @Resource
    protected LogOperateService logOperateService;
}