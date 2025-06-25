package com.publiccms.views.directive.tools;

import java.io.IOException;

import javax.annotation.Resource;
import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.common.constants.CommonConstants;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.common.tools.LanguagesUtils;
import com.publiccms.logic.component.site.ScriptComponent;

import freemarker.template.TemplateException;

/**
 * executeScript 脚本执行指令
 * <p>
 * 参数列表
 * <ul>
 * <li><code>command</code>:命令【sync.bat,sync.sh,backupDB.bat,backupDB.sh】
 * <li><code>parameters</code>:参数数组
 * </ul>
 * <p>
 * 打印执行结果
 * <p>使用示例
 * <p>
 * &lt;@tools.executeScript command='backupdb.bat'/&gt;
 *
 * <pre>
&lt;script&gt;
 $.getJSON('${site.dynamicPath}api/directive/tools/executeScript?command=backupdb.bat&amp;appToken=接口访问授权Token', function(data){
   console.log(data.id);
 });
 &lt;/script&gt;
 * </pre>
 *
 */
@Component
public class ExecuteScriptDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        String command = handler.getString("command");
        String[] parameters = handler.getStringArray("parameters");
        if (siteComponent.isMaster(getSite(handler).getId())) {
            try {
                handler.print(scriptComponent.execute(command, parameters, handler.getLong("timeout", 1l)));
            } catch (IOException | InterruptedException e) {
                handler.print(e.getMessage());
                Thread.currentThread().interrupt();
            }
        } else {
            handler.print(
                    LanguagesUtils.getMessage(CommonConstants.applicationContext, handler.getLocale(), "verify.custom.noright"));
        }
        handler.render();
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

    @Resource
    protected ScriptComponent scriptComponent;
}
