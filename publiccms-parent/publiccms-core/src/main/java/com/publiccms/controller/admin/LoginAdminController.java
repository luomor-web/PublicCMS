package com.publiccms.controller.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import com.publiccms.common.annotation.Csrf;
import com.publiccms.common.constants.CommonConstants;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.ControllerUtils;
import com.publiccms.common.tools.ImageUtils;
import com.publiccms.common.tools.RequestUtils;
import com.publiccms.common.tools.UserPasswordUtils;
import com.publiccms.common.tools.VerificationUtils;
import com.publiccms.entities.log.LogLogin;
import com.publiccms.entities.log.LogOperate;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.entities.sys.SysUserSetting;
import com.publiccms.entities.sys.SysUserSettingId;
import com.publiccms.entities.sys.SysUserToken;
import com.publiccms.logic.component.cache.CacheComponent;
import com.publiccms.logic.component.config.ConfigDataComponent;
import com.publiccms.logic.component.config.SafeConfigComponent;
import com.publiccms.logic.component.site.LockComponent;
import com.publiccms.logic.component.site.SiteComponent;
import com.publiccms.logic.service.log.LogLoginService;
import com.publiccms.logic.service.log.LogOperateService;
import com.publiccms.logic.service.sys.SysUserService;
import com.publiccms.logic.service.sys.SysUserSettingService;
import com.publiccms.logic.service.sys.SysUserTokenService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 *
 * LoginAdminController
 *
 */
@Controller
public class LoginAdminController {
    @Resource
    protected LogOperateService logOperateService;
    @Resource
    private SysUserService service;
    @Resource
    private SysUserSettingService settingService;
    @Resource
    private SysUserTokenService sysUserTokenService;
    @Resource
    private LogLoginService logLoginService;
    @Resource
    private CacheComponent cacheComponent;
    @Resource
    private ConfigDataComponent configDataComponent;
    @Resource
    private LockComponent lockComponent;
    @Resource
    protected SiteComponent siteComponent;
    @Resource
    protected SafeConfigComponent safeConfigComponent;

    /**
     * @param site
     * @param username
     * @param password
     * @param returnUrl
     * @param encoding
     * @param captcha
     *            验证码
     * @param request
     * @param response
     * @param model
     * @return view name
     */
    @PostMapping("login")
    public String login(@RequestAttribute SysSite site, String username, String password, String returnUrl, String encoding,
            String captcha, HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        username = StringUtils.trim(username);
        password = StringUtils.trim(password);
        if (ControllerUtils.errorNotEmpty("username", username, model)
                || ControllerUtils.errorNotLongThen("password", password, UserPasswordUtils.PASSWORD_MAX_LENGTH, model)) {
            model.addAttribute("username", username);
            model.addAttribute("returnUrl", returnUrl);
            return "login";
        }

        String ip = RequestUtils.getIpAddress(request);
        SysUser user = service.findByName(site.getId(), username);
        if (CommonUtils.notEmpty(captcha)
                || safeConfigComponent.enableCaptcha(site.getId(), SafeConfigComponent.CAPTCHA_MODULE_MANAGEMENT_SYSTEM)) {
            String sessionCaptcha = (String) request.getSession().getAttribute("captcha");
            request.getSession().removeAttribute("captcha");
            if (ControllerUtils.errorCustom("captcha.error", null == sessionCaptcha || !sessionCaptcha.equalsIgnoreCase(captcha),
                    model)) {
                if (null != user) {
                    lockComponent.lock(site.getId(), LockComponent.ITEM_TYPE_LOGIN, String.valueOf(user.getId()), null, true);
                }
                lockComponent.lock(site.getId(), LockComponent.ITEM_TYPE_IP_LOGIN, ip, null, true);
                logLoginService.save(new LogLogin(site.getId(), username, null == user ? null : user.getId(), ip,
                        LogLoginService.CHANNEL_WEB_MANAGER, LogLoginService.TYPE_PASSWORD, false, CommonUtils.getDate(),
                        password));
                return "login";
            }
        }

        boolean locked = lockComponent.isLocked(site.getId(), LockComponent.ITEM_TYPE_IP_LOGIN, ip, null);
        if (ControllerUtils.errorCustom("locked.ip", locked && ControllerUtils.ipNotEquals(ip, user), model)
                || ControllerUtils.errorNotEquals("password", user, model)) {
            model.addAttribute("username", username);
            model.addAttribute("returnUrl", returnUrl);
            lockComponent.lock(site.getId(), LockComponent.ITEM_TYPE_IP_LOGIN, ip, null, true);
            logLoginService.save(new LogLogin(site.getId(), username, null, ip, LogLoginService.CHANNEL_WEB_MANAGER,
                    LogLoginService.TYPE_PASSWORD, false, CommonUtils.getDate(), password));
            return "redirect:login";
        }
        locked = lockComponent.isLocked(site.getId(), LockComponent.ITEM_TYPE_LOGIN, String.valueOf(user.getId()), null);
        if (ControllerUtils.errorCustom("locked.user", locked, model)
                || ControllerUtils.errorNotEquals("password",
                        UserPasswordUtils.passwordEncode(password, null, user.getPassword(), encoding), user.getPassword(), model)
                || verifyNotAdmin(user, model) || verifyNotEnablie(user, model)) {
            model.addAttribute("username", username);
            model.addAttribute("returnUrl", returnUrl);
            Long userId = user.getId();
            lockComponent.lock(site.getId(), LockComponent.ITEM_TYPE_LOGIN, String.valueOf(user.getId()), null, true);
            lockComponent.lock(site.getId(), LockComponent.ITEM_TYPE_IP_LOGIN, ip, null, true);
            logLoginService.save(new LogLogin(site.getId(), username, userId, ip, LogLoginService.CHANNEL_WEB_MANAGER,
                    LogLoginService.TYPE_PASSWORD, false, CommonUtils.getDate(), password));
            return "redirect:login";
        }

        lockComponent.unLock(site.getId(), LockComponent.ITEM_TYPE_IP_LOGIN, ip, user.getId());
        lockComponent.unLock(site.getId(), LockComponent.ITEM_TYPE_LOGIN, String.valueOf(user.getId()), null);
        if (UserPasswordUtils.needUpdate(user.getPassword())) {
            service.updatePassword(user.getId(),
                    UserPasswordUtils.passwordEncode(password, UserPasswordUtils.getSalt(), null, encoding));
        }
        SysUserSetting userSetting = settingService
                .getEntity(new SysUserSettingId(user.getId(), SysUserSettingService.OPTSECRET_SETTINGS_CODE));
        if (safeConfigComponent.enableOtpLogin(site.getId()) || null != userSetting) {
            ControllerUtils.setOtpAdminToSession(request.getSession(), user);
            model.addAttribute("returnUrl", returnUrl);
            return "redirect:otp/login";
        } else {
            service.updateLoginStatus(user.getId(), ip);
            String authToken = UUID.randomUUID().toString();
            Date now = CommonUtils.getDate();
            Map<String, String> safeConfig = configDataComponent.getConfigData(site.getId(), SafeConfigComponent.CONFIG_CODE);
            int expiryMinutes = ConfigDataComponent.getInt(safeConfig.get(SafeConfigComponent.CONFIG_EXPIRY_MINUTES_MANAGER),
                    SafeConfigComponent.DEFAULT_EXPIRY_MINUTES);
            addLoginStatus(user, authToken, request, response, expiryMinutes);

            sysUserTokenService.save(new SysUserToken(authToken, site.getId(), user.getId(), LogLoginService.CHANNEL_WEB_MANAGER,
                    now, DateUtils.addMinutes(now, expiryMinutes), ip));
            logLoginService.save(new LogLogin(site.getId(), username, user.getId(), ip, LogLoginService.CHANNEL_WEB_MANAGER,
                    LogLoginService.TYPE_PASSWORD, true, CommonUtils.getDate(), null));
            String safeReturnUrl = safeConfig.get(SafeConfigComponent.CONFIG_RETURN_URL);
            if (SafeConfigComponent.isUnSafeUrl(returnUrl, site, safeReturnUrl, request.getContextPath())) {
                returnUrl = CommonConstants.getDefaultPage();
            }
            return CommonUtils.joinString(UrlBasedViewResolver.REDIRECT_URL_PREFIX, returnUrl);
        }
    }

    public static void addLoginStatus(SysUser user, String authToken, HttpServletRequest request, HttpServletResponse response,
            int expiryMinutes) {
        user.setPassword(null);
        ControllerUtils.setAdminToSession(request.getSession(), user);
        String cookie = CommonUtils.joinString(user.getId(), CommonConstants.getCookiesUserSplit(), authToken);
        RequestUtils.addCookie(request.getContextPath(), request.getScheme(), response, CommonConstants.getCookiesAdmin(), cookie,
                expiryMinutes * 60, null);
    }

    /**
     * @param site
     * @param username
     * @param password
     * @param encoding
     * @param captcha
     * @param request
     * @param response
     * @param model
     * @return view name
     */
    @PostMapping("loginDialog")
    public String loginDialog(@RequestAttribute SysSite site, String username, String password, String encoding, String captcha,
            HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        if ("login".equals(login(site, username, password, null, encoding, captcha, request, response, model))) {
            return CommonConstants.TEMPLATE_ERROR;
        }
        return CommonConstants.TEMPLATE_DONE;
    }

    /**
     * @param site
     * @param admin
     * @param oldpassword
     * @param password
     * @param repassword
     * @param encoding
     * @param request
     * @param response
     * @param model
     * @return view name
     */
    @PostMapping("changePassword")
    @Csrf
    public String changeMyselfPassword(@RequestAttribute SysSite site, @SessionAttribute SysUser admin, String oldpassword,
            String password, String repassword, String encoding, HttpServletRequest request, HttpServletResponse response,
            ModelMap model) {
        SysUser user = service.getEntity(admin.getId());
        String encodedOldPassword = UserPasswordUtils.passwordEncode(oldpassword, null, user.getPassword(), encoding);
        if (null != user.getPassword()
                && ControllerUtils.errorNotEquals("password", user.getPassword(), encodedOldPassword, model)) {
            return CommonConstants.TEMPLATE_ERROR;
        } else if (ControllerUtils.errorNotEmpty("password", password, model)
                || ControllerUtils.errorNotEquals("repassword", password, repassword, model)) {
            return CommonConstants.TEMPLATE_ERROR;
        } else {
            ControllerUtils.clearAdminToSession(request.getContextPath(), request.getScheme(), request.getSession(), response);
            model.addAttribute(CommonConstants.MESSAGE, "message.needReLogin");
        }
        service.updatePassword(user.getId(),
                UserPasswordUtils.passwordEncode(password, UserPasswordUtils.getSalt(), null, encoding));
        if (user.isWeakPassword()) {
            service.updateWeekPassword(user.getId(), false);
        }
        sysUserTokenService.delete(user.getId());
        logOperateService.save(new LogOperate(site.getId(), user.getId(), user.getDeptId(), LogLoginService.CHANNEL_WEB_MANAGER,
                "changepassword", RequestUtils.getIpAddress(request), CommonUtils.getDate(), encodedOldPassword));
        return "common/ajaxTimeout";
    }

    /**
     * @param admin
     * @param userId
     * @param request
     * @param response
     * @return view name
     */
    @GetMapping(value = "logout")
    public String logout(@SessionAttribute SysUser admin, Long userId, HttpServletRequest request, HttpServletResponse response) {
        if (null != userId && null != admin && userId.equals(admin.getId())) {
            Cookie userCookie = RequestUtils.getCookie(request.getCookies(), CommonConstants.getCookiesAdmin());
            if (null != userCookie && CommonUtils.notEmpty(userCookie.getValue())) {
                String value = userCookie.getValue();
                if (null != value) {
                    String[] userData = value.split(CommonConstants.getCookiesUserSplit());
                    if (userData.length > 1) {
                        sysUserTokenService.delete(userData[1]);
                    }
                }
            }
            ControllerUtils.clearAdminToSession(request.getContextPath(), request.getScheme(), request.getSession(), response);
        }
        return CommonUtils.joinString(UrlBasedViewResolver.REDIRECT_URL_PREFIX, CommonConstants.getDefaultPage());
    }

    /**
     * @param session
     * @return response entity
     */
    @RequestMapping(value = "getCaptchaImage")
    public ResponseEntity<StreamingResponseBody> getCaptchaImage(HttpSession session) {
        String captcha = VerificationUtils.getRandomString("ABCDEFGHJKMNPQRSTUVWXYZ23456789", 4);
        session.setAttribute("captcha", captcha);
        StreamingResponseBody body = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                ImageUtils.drawImage(120, 30, captcha, outputStream);
            }
        };
        return ResponseEntity.ok().body(body);
    }

    /**
     * @return view name
     */
    @RequestMapping(value = "clearCache")
    public String clearCache() {
        cacheComponent.clear();
        return CommonConstants.TEMPLATE_DONE;
    }

    public static boolean verifyNotAdmin(SysUser user, ModelMap model) {
        if (!user.isDisabled() && !user.isSuperuser()) {
            model.addAttribute(CommonConstants.ERROR, "verify.user.notAdmin");
            return true;
        }
        return false;
    }

    public static boolean verifyNotEnablie(SysUser user, ModelMap model) {
        if (user.isDisabled()) {
            model.addAttribute(CommonConstants.ERROR, "verify.user.notEnablie");
            return true;
        }
        return false;
    }
}
