package com.publiccms.controller.admin.sys;

// Generated 2023-8-16 by com.publiccms.common.generator.SourceGenerator

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.publiccms.common.annotation.Csrf;
import com.publiccms.common.constants.CommonConstants;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.entities.sys.SysWorkflowProcessHistory;
import com.publiccms.logic.service.log.LogOperateService;
import com.publiccms.logic.service.sys.SysWorkflowProcessService;

/**
 *
 * SysProcessAdminController
 * 
 */
@Controller
@RequestMapping("sysWorkflowProcess")
public class SysProcessAdminController {

    /**
     * @param site
     * @param admin
     * @param entity
     * @param request
     * @param model
     * @return operate result
     */
    @RequestMapping("handle")
    @Csrf
    public String handle(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, SysWorkflowProcessHistory entity) {
        service.handleProcess(site, entity, admin);
        return CommonConstants.TEMPLATE_DONE;
    }

    @Resource
    private SysWorkflowProcessService service;
    @Resource
    protected LogOperateService logOperateService;
}