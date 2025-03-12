package com.publiccms.controller.admin.sys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Generated 2023-8-16 by com.publiccms.common.generator.SourceGenerator

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.publiccms.common.annotation.Csrf;
import com.publiccms.common.constants.CommonConstants;
import com.publiccms.common.constants.Constants;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.JsonUtils;
import com.publiccms.common.tools.RequestUtils;
import com.publiccms.entities.log.LogOperate;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.entities.sys.SysWorkflow;
import com.publiccms.entities.sys.SysWorkflowStep;
import com.publiccms.logic.service.log.LogLoginService;
import com.publiccms.logic.service.log.LogOperateService;
import com.publiccms.logic.service.sys.SysWorkflowService;
import com.publiccms.logic.service.sys.SysWorkflowStepService;

/**
 *
 * SysWorkflowAdminController
 * 
 */
@Controller
@RequestMapping("sysWorkflow")
public class SysWorkflowAdminController {

    private String[] ignoreProperties = new String[] { "id", "siteId", "startStepId", "createDate" };
    private String[] stepIgnoreProperties = new String[] { "id", "workflowId" };
    @Resource
    private SysWorkflowStepService stepService;

    /**
     * @param site
     * @param admin
     * @param entity
     * @param stepdata
     * @param request
     * @param model
     * @return operate result
     */
    @RequestMapping("save")
    @Csrf
    public String save(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, SysWorkflow entity, String stepdata,
            HttpServletRequest request) {
        entity.setSiteId(site.getId());
        if (null != entity.getId()) {
            entity.setStartStepId(stepService.update(entity.getId(), getFlowStepList(stepdata), stepIgnoreProperties));
            entity = service.update(entity.getId(), entity, ignoreProperties);
            logOperateService.save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(),
                    LogLoginService.CHANNEL_WEB_MANAGER, "update.workflow", RequestUtils.getIpAddress(request),
                    CommonUtils.getDate(), JsonUtils.getString(entity)));
        } else {
            service.save(entity);
            Long startStepId = stepService.save(entity.getId(), getFlowStepList(stepdata));
            service.updateStartStepId(entity.getId(), startStepId);
            logOperateService.save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(),
                    LogLoginService.CHANNEL_WEB_MANAGER, "save.workflow", RequestUtils.getIpAddress(request),
                    CommonUtils.getDate(), JsonUtils.getString(entity)));
        }
        return CommonConstants.TEMPLATE_DONE;
    }

    /**
     * @param stepdata
     * @return step list
     */
    public List<SysWorkflowStep> getFlowStepList(String stepdata) {
        if (null != stepdata) {
            try {
                List<Map<String, Object>> datalist = Constants.objectMapper.readValue(stepdata,
                        Constants.objectMapper.getTypeFactory().constructCollectionLikeType(List.class, Map.class));
                if (null != datalist && 1 == datalist.size()) {
                    Map<String, Object> map = datalist.get(0);
                    if (null != map) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("items");
                        if (null != items) {
                            List<SysWorkflowStep> stepList = new ArrayList<>();
                            int i = 0;
                            for (Map<String, Object> data : items) {
                                SysWorkflowStep entity = new SysWorkflowStep();
                                if (null != data.get("id")) {
                                    entity.setId(Long.parseLong((String) data.get("id").toString()));
                                }
                                if (null != data.get("roleId")) {
                                    entity.setRoleId(Integer.parseInt((String) data.get("roleId")));
                                }
                                if (null != data.get("deptId")) {
                                    entity.setDeptId(Integer.parseInt((String) data.get("deptId")));
                                }
                                if (null != data.get("userId")) {
                                    entity.setUserId(Long.parseLong((String) data.get("userId")));
                                }
                                entity.setSort(i);
                                i++;
                                stepList.add(entity);
                            }
                            return stepList;
                        }
                    }
                }
                return null;
            } catch (JsonProcessingException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * @param ids
     * @param request
     * @param site
     * @param admin
     * @param model
     * @return operate result
     */
    @RequestMapping("disabled")
    @Csrf
    public String disabled(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, Integer[] ids,
            HttpServletRequest request) {
        if (CommonUtils.notEmpty(ids)) {
            service.delete(site.getId(), ids);
            logOperateService.save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(),
                    LogLoginService.CHANNEL_WEB_MANAGER, "disable.workflow", RequestUtils.getIpAddress(request),
                    CommonUtils.getDate(), StringUtils.join(ids, Constants.COMMA)));
        }
        return CommonConstants.TEMPLATE_DONE;
    }

    @Resource
    private SysWorkflowService service;
    @Resource
    protected LogOperateService logOperateService;
}