package com.publiccms.controller.admin.sys;

import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.publiccms.common.annotation.Csrf;
import com.publiccms.common.constants.CommonConstants;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.ControllerUtils;
import com.publiccms.common.tools.JsonUtils;
import com.publiccms.common.tools.RequestUtils;
import com.publiccms.common.tools.UserPasswordUtils;
import com.publiccms.entities.log.LogOperate;
import com.publiccms.entities.sys.SysRoleUser;
import com.publiccms.entities.sys.SysRoleUserId;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.logic.component.site.SiteComponent;
import com.publiccms.logic.service.log.LogLoginService;
import com.publiccms.logic.service.log.LogOperateService;
import com.publiccms.logic.service.sys.SysRoleUserService;
import com.publiccms.logic.service.sys.SysUserService;
import com.publiccms.logic.service.sys.SysUserTokenService;

/**
 *
 * SysUserAdminController
 *
 */
@Controller
@RequestMapping("sysUser")
public class SysUserAdminController {
    @Resource
    private SysUserService service;
    @Resource
    private SysUserTokenService sysUserTokenService;
    @Resource
    private SysRoleUserService roleUserService;
    @Resource
    protected LogOperateService logOperateService;
    @Resource
    protected SiteComponent siteComponent;

    private String[] ignoreProperties = new String[] { "id", "registeredDate", "siteId", "salt", "password", "lastLoginDate",
            "lastLoginIp", "loginCount", "disabled" };

    /**
     * @param site
     * @param admin
     * @param entity
     * @param repassword
     * @param encoding
     * @param roleIds
     * @param request
     * @param model
     * @return view name
     */
    @RequestMapping("save")
    @Csrf
    public String save(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, SysUser entity, String repassword,
            String encoding, Integer[] roleIds, HttpServletRequest request, ModelMap model) {
        entity.setName(StringUtils.trim(entity.getName()));
        entity.setNickname(StringUtils.trim(entity.getNickname()));
        entity.setPassword(StringUtils.trim(entity.getPassword()));
        repassword = StringUtils.trim(repassword);
        if (ControllerUtils.errorNotEmpty("username", entity.getName(), model)
                || ControllerUtils.errorNotEmpty("nickname", entity.getNickname(), model)
                || ControllerUtils.errorNotUserName("username", entity.getName(), model)
                || ControllerUtils.errorNotNickname("nickname", entity.getNickname(), model)) {
            return CommonConstants.TEMPLATE_ERROR;
        }
        if (entity.isSuperuser()) {
            entity.setRoles(arrayToCommaDelimitedString(roleIds));
        } else {
            roleIds = null;
            entity.setRoles(null);
            if (SysUserService.CONTENT_PERMISSIONS_ALL == entity.getContentPermissions()) {
                entity.setContentPermissions(SysUserService.CONTENT_PERMISSIONS_DEPT);
            }
        }
        if (null != entity.getId()) {
            SysUser oldEntity = service.getEntity(entity.getId());
            if (null == oldEntity || ControllerUtils.errorNotEquals("siteId", site.getId(), oldEntity.getSiteId(), model)) {
                return CommonConstants.TEMPLATE_ERROR;
            }
            if ((!oldEntity.getName().equals(entity.getName())
                    && ControllerUtils.errorHasExist("username", service.findByName(site.getId(), entity.getName()), model))) {
                return CommonConstants.TEMPLATE_ERROR;
            }
            if (CommonUtils.notEmpty(entity.getPassword())) {
                if (ControllerUtils.errorNotEquals("repassword", entity.getPassword(), repassword, model)) {
                    return CommonConstants.TEMPLATE_ERROR;
                }
                service.updatePassword(entity.getId(),
                        UserPasswordUtils.passwordEncode(entity.getPassword(), UserPasswordUtils.getSalt(), null, encoding));
                sysUserTokenService.delete(entity.getId());
            }
            if (CommonUtils.empty(entity.getEmail()) || !entity.getEmail().equals(oldEntity.getEmail())) {
                entity.setEmailChecked(false);
            }
            entity.setUpdateDate(CommonUtils.getDate());
            entity = service.update(entity.getId(), entity, ignoreProperties);
            if (null != entity) {
                roleUserService.dealRoleUsers(entity.getId(), roleIds);
                logOperateService.save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(),
                        LogLoginService.CHANNEL_WEB_MANAGER, "update.user", RequestUtils.getIpAddress(request),
                        CommonUtils.getDate(), JsonUtils.getString(entity)));
            }
        } else {
            if (ControllerUtils.errorNotEmpty("password", entity.getPassword(), model)
                    || ControllerUtils.errorNotEquals("repassword", entity.getPassword(), repassword, model)
                    || ControllerUtils.errorHasExist("username", service.findByName(site.getId(), entity.getName()), model)) {
                return CommonConstants.TEMPLATE_ERROR;
            }
            entity.setSiteId(site.getId());
            entity.setPassword(
                    UserPasswordUtils.passwordEncode(entity.getPassword(), UserPasswordUtils.getSalt(), null, encoding));
            entity.setWeakPassword(true);
            service.save(entity);
            if (CommonUtils.notEmpty(roleIds)) {
                for (Integer roleId : roleIds) {
                    roleUserService.save(new SysRoleUser(new SysRoleUserId(roleId, entity.getId())));
                }
            }
            logOperateService
                    .save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(), LogLoginService.CHANNEL_WEB_MANAGER,
                            "save.user", RequestUtils.getIpAddress(request), CommonUtils.getDate(), JsonUtils.getString(entity)));
        }
        return CommonConstants.TEMPLATE_DONE;
    }

    /**
     * @param site
     * @param admin
     * @param nickname
     * @param cover
     * @param email
     * @param request
     * @return
     */
    @RequestMapping("update")
    @Csrf
    public String update(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, String nickname, String cover,
            String email, HttpServletRequest request) {
        SysUser entity = service.updateProfile(admin.getId(), nickname, cover, email);
        if (null != entity) {
            logOperateService.save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(),
                    LogLoginService.CHANNEL_WEB_MANAGER, "update.user", RequestUtils.getIpAddress(request), CommonUtils.getDate(),
                    JsonUtils.getString(entity)));
        }
        return CommonConstants.TEMPLATE_DONE;
    }

    /**
     * @param site
     * @param admin
     * @param ids
     * @param request
     * @return view name
     */
    @PostMapping("enable")
    @Csrf
    public String enable(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, Long[] ids,
            HttpServletRequest request) {
        if (CommonUtils.notEmpty(ids)) {
            service.updateStatus(site.getId(), ids, admin.getId(), false);
            logOperateService
                    .save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(), LogLoginService.CHANNEL_WEB_MANAGER,
                            "enable.user", RequestUtils.getIpAddress(request), CommonUtils.getDate(), JsonUtils.getString(ids)));
        }
        return CommonConstants.TEMPLATE_DONE;
    }

    /**
     * @param site
     * @param admin
     * @param ids
     * @param request
     * @return view name
     */
    @PostMapping("disable")
    @Csrf
    public String disable(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, Long[] ids,
            HttpServletRequest request) {
        if (CommonUtils.notEmpty(ids)) {
            service.updateStatus(site.getId(), ids, admin.getId(), true);
            logOperateService
                    .save(new LogOperate(site.getId(), admin.getId(), admin.getDeptId(), LogLoginService.CHANNEL_WEB_MANAGER,
                            "disable.user", RequestUtils.getIpAddress(request), CommonUtils.getDate(), JsonUtils.getString(ids)));
        }
        return CommonConstants.TEMPLATE_DONE;
    }
}
