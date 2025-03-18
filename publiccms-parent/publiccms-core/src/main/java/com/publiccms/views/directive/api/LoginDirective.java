package com.publiccms.views.directive.api;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractAppDirective;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.ControllerUtils;
import com.publiccms.common.tools.RequestUtils;
import com.publiccms.common.tools.UserPasswordUtils;
import com.publiccms.entities.log.LogLogin;
import com.publiccms.entities.sys.SysApp;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.entities.sys.SysUserToken;
import com.publiccms.logic.component.config.ConfigDataComponent;
import com.publiccms.logic.component.config.SafeConfigComponent;
import com.publiccms.logic.component.site.LockComponent;
import com.publiccms.logic.service.log.LogLoginService;
import com.publiccms.logic.service.sys.SysUserService;
import com.publiccms.logic.service.sys.SysUserTokenService;

import freemarker.template.TemplateException;
import jakarta.annotation.Resource;

/**
 *
 * login 登录接口
 * <p lang="zh">参数列表
 * <p lang="en">parameter list
 * <p lang="ja">パラメータリスト
 * <ul>
 * <li><code>username</code>:设备唯一id
 * <li><code>password</code>:用户名
 * <li><code>encoding</code>:密码加密方式
 * <li><code>channel</code>:登录渠道
 * </ul>
 * <p lang="zh">返回结果
 * <p lang="en">return result
 * <p lang="ja">戻り値
 * <ul>
 * <li><code>result</code>:登录结果,【true,false】
 * <li><code>authToken</code>:用户登录授权
 * <li><code>expiryDate</code>:过期日期
 * <li><code>user</code>:用户信息 {@link com.publiccms.entities.sys.SysUser}
 * </ul>
 * <p lang="zh">使用示例
 * <p lang="en">usage example
 * <p lang="ja">使用例
 * <p>
 *
 * <pre>
&lt;script&gt;
$.getJSON('${site.dynamicPath}api/login?username=admin&amp;password=sha512encodingpassword&amp;encoding=sha512&amp;channel=web', function(data){
    console.log(result+","+authToken+","+user.nickname+","+expiryDate);
});
&lt;/script&gt;
 * </pre>
 */
@Component
public class LoginDirective extends AbstractAppDirective {

    @Resource
    private SysUserTokenService sysUserTokenService;
    @Resource
    private LogLoginService logLoginService;
    @Resource
    private ConfigDataComponent configDataComponent;
    @Resource
    private LockComponent lockComponent;

    @Override
    public void execute(RenderHandler handler, SysApp app, SysUser user) throws IOException, TemplateException {
        String username = StringUtils.trim(handler.getString("username"));
        String password = StringUtils.trim(handler.getString("password"));
        String encoding = StringUtils.trim(handler.getString("encoding"));
        String channel = handler.getString("channel", LogLoginService.CHANNEL_WEB);
        boolean result = false;
        if (CommonUtils.notEmpty(username) && CommonUtils.notEmpty(password)
                && password.length() <= UserPasswordUtils.PASSWORD_MAX_LENGTH) {
            SysSite site = getSite(handler);
            if (ControllerUtils.notEMail(username)) {
                user = service.findByName(site.getId(), username);
            } else {
                user = service.findByEmail(site.getId(), username);
            }
            String ip = RequestUtils.getIpAddress(handler.getRequest());
            boolean locked = lockComponent.isLocked(site.getId(), LockComponent.ITEM_TYPE_IP_LOGIN, ip, null);
            if (null != user && (!locked || !ControllerUtils.ipNotEquals(ip, user)) && !user.isDisabled() && user.getPassword()
                    .equals(UserPasswordUtils.passwordEncode(password, null, user.getPassword(), encoding))) {
                lockComponent.unLock(site.getId(), LockComponent.ITEM_TYPE_IP_LOGIN, ip, user.getId());
                lockComponent.unLock(site.getId(), LockComponent.ITEM_TYPE_LOGIN, String.valueOf(user.getId()), null);
                if (UserPasswordUtils.needUpdate(user.getPassword())) {
                    service.updatePassword(user.getId(),
                            UserPasswordUtils.passwordEncode(password, UserPasswordUtils.getSalt(), null, encoding));
                }
                service.updateLoginStatus(user.getId(), ip);
                Date now = CommonUtils.getDate();
                Map<String, String> config = configDataComponent.getConfigData(site.getId(), SafeConfigComponent.CONFIG_CODE);
                int expiryMinutes = ConfigDataComponent.getInt(config.get(SafeConfigComponent.CONFIG_EXPIRY_MINUTES_WEB),
                        SafeConfigComponent.DEFAULT_EXPIRY_MINUTES);
                Date expiryDate = DateUtils.addMinutes(now, expiryMinutes);
                SysUserToken userToken = new SysUserToken(UUID.randomUUID().toString(), site.getId(), user.getId(), channel, now,
                        expiryDate, ip);
                sysUserTokenService.save(userToken);
                logLoginService
                        .save(new LogLogin(site.getId(), username, user.getId(), ip, channel, true, CommonUtils.getDate(), null));
                user.setPassword(null);
                result = true;
                handler.put("authToken", userToken.getAuthToken()).put("expiryDate", userToken.getExpiryDate()).put("user", user);
            } else {
                if (null != user) {
                    lockComponent.lock(site.getId(), LockComponent.ITEM_TYPE_LOGIN, String.valueOf(user.getId()), null, true);
                }
                lockComponent.lock(site.getId(), LockComponent.ITEM_TYPE_IP_LOGIN, ip, null, true);
                LogLogin log = new LogLogin();
                log.setSiteId(site.getId());
                log.setName(username);
                log.setErrorPassword(password);
                log.setIp(ip);
                log.setChannel(channel);
                logLoginService.save(log);
            }
        }
        handler.put("result", result).render();
    }

    @Resource
    private SysUserService service;

    @Override
    public boolean needUserToken() {
        return false;
    }

    @Override
    public boolean needAppToken() {
        return false;
    }
}