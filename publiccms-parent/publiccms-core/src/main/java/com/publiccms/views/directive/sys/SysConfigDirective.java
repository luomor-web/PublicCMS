package com.publiccms.views.directive.sys;

import java.io.IOException;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.logic.component.config.ConfigComponent;
import com.publiccms.views.pojo.entities.ConfigInfo;

import freemarker.template.TemplateException;

/**
 *
 * sysConfig 配置查询指令
 * <p lang="zh">参数列表
 * <p lang="en">parameter list
 * <p lang="ja">パラメータリスト
 * <ul>
 * <li><code>code</code>:配置编码,结果返回<code>object</code>
 * {@link com.publiccms.views.pojo.entities.ConfigInfo}
 * </ul>
 * <p lang="zh">使用示例
 * <p lang="en">usage example
 * <p lang="ja">使用例
 * <p>
 * &lt;@sys.config code='site'&gt;${object.description}&lt;/@sys.config&gt;
 *
 * <pre>
&lt;script&gt;
$.getJSON('${site.dynamicPath}api/directive/sys/config?code=site&amp;appToken=接口访问授权Token', function(data){
  console.log(data.description);
});
&lt;/script&gt;
 * </pre>
 */
@Component
public class SysConfigDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        String code = handler.getString("code");
        if (CommonUtils.notEmpty(code)) {
            ConfigInfo entity = configComponent.getConfig(getSite(handler).getId(), code, handler.getLocale());
            if (null != entity) {
                handler.put("object", entity).render();
            }
        }
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

    @Resource
    private ConfigComponent configComponent;
}