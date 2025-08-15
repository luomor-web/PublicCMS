package com.publiccms.interceptor;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UrlPathHelper;

import com.publiccms.common.constants.CommonConstants;
import com.publiccms.common.constants.Constants;
import com.publiccms.common.tools.CmsFileUtils;
import com.publiccms.common.tools.CmsUrlUtils;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.ControllerUtils;
import com.publiccms.common.tools.RequestUtils;
import com.publiccms.entities.sys.SysDomain;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.logic.service.log.LogLoginService;
import com.publiccms.logic.service.sys.SysRoleAuthorizedService;
import com.publiccms.logic.service.sys.SysRoleService;

/**
 *
 * AdminContextInterceptor
 *
 */
public class AdminContextInterceptor extends WebContextInterceptor {
    private String adminContextPath;
    private String loginUrl;
    private String loginJsonUrl;
    private String unauthorizedUrl;
    private String[] needNotLoginUrls;
    private String[] needNotAuthorizedUrls;
    private UrlPathHelper urlPathHelper = UrlPathHelper.defaultInstance;

    @Resource
    private SysRoleAuthorizedService roleAuthorizedService;
    @Resource
    private SysRoleService sysRoleService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        SysDomain domain = siteComponent.getDomain(request.getServerName());
        SysSite site = siteComponent.getSite(domain, request.getServerName(), null);
        String ctxPath = urlPathHelper.getOriginatingContextPath(request);

        if (site.isMultiple()) {
            String currentSiteId = request.getParameter("currentSiteId");
            if (null != currentSiteId) {
                try {
                    RequestUtils.addCookie(ctxPath, request.getScheme(), response, CommonConstants.getCookiesSite(),
                            currentSiteId, Integer.MAX_VALUE, null);
                    response.sendRedirect(CommonUtils.joinString(ctxPath, adminContextPath, Constants.SEPARATOR));
                    return false;
                } catch (IOException e) {
                    return true;
                }
            }
            Cookie cookie = RequestUtils.getCookie(request.getCookies(), CommonConstants.getCookiesSite());
            if (null != cookie && CommonUtils.notEmpty(cookie.getValue())) {
                SysSite newSite = siteComponent.getSiteById(cookie.getValue());
                if (null != newSite && null != newSite.getParentId() && newSite.getParentId() == site.getId()) {
                    site = newSite;
                } else {
                    RequestUtils.cancleCookie(ctxPath, request.getScheme(), response, CommonConstants.getCookiesSite(), null);
                }
            }
        }

        request.setAttribute(CommonConstants.getAttributeSite(), site);
        String path = CmsFileUtils.getSafeFileName(urlPathHelper.getLookupPathForRequest(request));
        if (adminContextPath.equals(path)) {
            try {
                response.sendRedirect(CommonUtils.joinString(ctxPath, adminContextPath, Constants.SEPARATOR));
                return false;
            } catch (IOException e) {
                return true;
            }
        } else if (verifyNeedLogin(path)) {
            HttpSession session = request.getSession();
            SysUser user = initUser(ControllerUtils.getAdminFromSession(session), LogLoginService.CHANNEL_WEB_MANAGER,
                    CommonConstants.getCookiesAdmin(), site, request, response);
            if (null == user) {
                ControllerUtils.clearAdminToSession(request.getContextPath(), request.getScheme(), session, response);
                try {
                    redirectLogin(ctxPath, path, request.getQueryString(), request.getHeader("X-Requested-With"), response);
                    return false;
                } catch (IllegalStateException | IOException e) {
                    return true;
                }
            }
            SysUser entity = sysUserService.getEntity(user.getId());
            if (null == entity || entity.isDisabled() || !entity.isSuperuser() || null == site || site.isDisabled()
                    || site.getId() != entity.getSiteId()) {
                ControllerUtils.clearAdminToSession(request.getContextPath(), request.getScheme(), session, response);
                try {
                    redirectLogin(ctxPath, path, request.getQueryString(), request.getHeader("X-Requested-With"), response);
                    return false;
                } catch (IllegalStateException | IOException e) {
                    return true;
                }
            } else if (verifyNeedAuthorized(path) && (!Constants.SEPARATOR.equals(path))) {
                int index = path.lastIndexOf(Constants.DOT);
                path = path.substring(path.startsWith(Constants.SEPARATOR) ? 1 : 0, index > -1 ? index : path.length());
                if (0 == roleAuthorizedService.count(entity.getRoles(), path) && !ownsAllRight(entity.getRoles())) {
                    try {
                        response.sendRedirect(CommonUtils.joinString(ctxPath, adminContextPath, unauthorizedUrl));
                        return false;
                    } catch (IOException e) {
                        return true;
                    }
                }

            }
            entity.setCover(CmsUrlUtils.getUrl(fileUploadComponent.getPrefix(site), entity.getCover()));
            entity.setPassword(null);
            ControllerUtils.setAdminToSession(session, entity);
        }
        return true;
    }

    private void redirectLogin(String ctxPath, String path, String queryString, String requestedWith,
            HttpServletResponse response) throws IOException {
        StringBuilder sb = new StringBuilder(ctxPath);
        sb.append(adminContextPath);
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            sb.append(loginJsonUrl);
        } else {
            sb.append(loginUrl).append("?returnUrl=");
            sb.append(RequestUtils.getEncodePath(CommonUtils.joinString(adminContextPath, path), queryString));
        }
        response.sendRedirect(sb.toString());
    }

    private boolean ownsAllRight(String roles) {
        String[] roleIdArray = StringUtils.split(roles, Constants.COMMA);
        if (null != roles && 0 < roleIdArray.length) {
            Integer[] roleIds = new Integer[roleIdArray.length];
            for (int i = 0; i < roleIdArray.length; i++) {
                roleIds[i] = Integer.parseInt(roleIdArray[i]);
            }
            return sysRoleService.ownsAllRight(roleIds);
        }
        return false;
    }

    private boolean verifyNeedLogin(String url) {
        if (null == loginUrl) {
            return false;
        } else if (null != needNotLoginUrls && null != url) {
            if (url.startsWith(loginUrl) || (null != loginJsonUrl && url.startsWith(loginJsonUrl))) {
                return false;
            }
            for (String needNotLoginUrl : needNotLoginUrls) {
                if (null != needNotLoginUrl && (url.startsWith(needNotLoginUrl))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean verifyNeedAuthorized(String url) {
        if (null == unauthorizedUrl) {
            return false;
        } else if (null != needNotAuthorizedUrls && null != url) {
            if (url.startsWith(unauthorizedUrl)) {
                return false;
            }
            for (String needNotAuthorizedUrl : needNotAuthorizedUrls) {
                if (null != needNotAuthorizedUrl && (url.startsWith(needNotAuthorizedUrl))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param adminContextPath
     *            the adminContextPath to set
     */
    public void setAdminContextPath(String adminContextPath) {
        this.adminContextPath = adminContextPath;
    }

    /**
     * @return the adminContextPath
     */
    public String getAdminContextPath() {
        return adminContextPath;
    }

    /**
     * @param loginUrl
     */
    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    /**
     * @param loginJsonUrl
     */
    public void setLoginJsonUrl(String loginJsonUrl) {
        this.loginJsonUrl = loginJsonUrl;
    }

    /**
     * @param unauthorizedUrl
     */
    public void setUnauthorizedUrl(String unauthorizedUrl) {
        this.unauthorizedUrl = unauthorizedUrl;
    }

    /**
     * @param needNotLoginUrls
     */
    public void setNeedNotLoginUrls(String[] needNotLoginUrls) {
        this.needNotLoginUrls = needNotLoginUrls;
    }

    /**
     * @param needNotAuthorizedUrls
     */
    public void setNeedNotAuthorizedUrls(String[] needNotAuthorizedUrls) {
        this.needNotAuthorizedUrls = needNotAuthorizedUrls;
    }
}
