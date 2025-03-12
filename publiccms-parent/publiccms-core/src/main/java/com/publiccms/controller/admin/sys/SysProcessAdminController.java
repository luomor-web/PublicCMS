package com.publiccms.controller.admin.sys;

// Generated 2023-8-16 by com.publiccms.common.generator.SourceGenerator

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
import com.publiccms.logic.service.sys.SysWorkflowProcessHistoryService;
import com.publiccms.logic.service.sys.SysWorkflowProcessService;
import com.publiccms.logic.service.sys.SysWorkflowStepService;

/**
 *
 * SysProcessAdminController
 * 
 */
@Controller
@RequestMapping("sysWorkflowProcess")
public class SysProcessAdminController {
    @Resource
    private SysWorkflowStepService stepService;
    @Resource
    private SysWorkflowProcessHistoryService historyService;

    /**
     * @param site
     * @param admin
     * @param entity
     * @param request
     * @param model
     * @return operate result
     */
    @RequestMapping("save")
    @Csrf
    public String save(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, SysWorkflowProcessHistory entity,
            HttpServletRequest request) {
        entity.setUserId(admin.getId());
        return CommonConstants.TEMPLATE_DONE;
    }

    @Resource
    private SysWorkflowProcessService service;
    @Resource
    protected LogOperateService logOperateService;
}