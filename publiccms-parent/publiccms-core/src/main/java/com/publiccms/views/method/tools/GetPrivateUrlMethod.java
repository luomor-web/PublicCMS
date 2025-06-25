package com.publiccms.views.method.tools;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.publiccms.common.base.BaseMethod;
import com.publiccms.common.constants.CommonConstants;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.TemplateModelUtils;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.component.site.FileUploadComponent;

import freemarker.core.Environment;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 *
 * getPrivateUrl 获取私有文件绝对路径
 * <p>参数列表
 * <ol>
 * <li><code>url</code>,文件url
 * <li><code>expiryMinutes</code>,过期分钟数,可以为空
 * <li><code>string</code>,文件名,可以为空
 * </ol>
 * <p>返回结果
 * <ul>
 * <li><code>url</code>:绝对路径的url
 * </ul>
 * <p>使用示例
 * <p>
 * ${getPrivateUrl('index.html')}
 * <p>
 *
 * <pre>
&lt;script&gt;
$.getJSON('${site.dynamicPath}api/method/getPrivateUrl?appToken=接口访问授权Token&amp;parameters=index.html', function(data){
console.log(data);
});
&lt;/script&gt;
 * </pre>
 */
@Component
public class GetPrivateUrlMethod extends BaseMethod {
    @Resource
    private FileUploadComponent fileUploadComponent;

    @Override
    public Object execute(HttpServletRequest request, List<TemplateModel> arguments) throws TemplateModelException {
        SysSite site = (SysSite) request.getAttribute("site");
        return execute(site, arguments);
    }

    @Override
    public Object execute(List<TemplateModel> arguments) throws TemplateModelException {
        TemplateModel model = Environment.getCurrentEnvironment().getGlobalVariable(CommonConstants.getAttributeSite());
        SysSite site = null;
        if (null != model) {
            site = (SysSite) TemplateModelUtils.converBean(model);
        }
        return execute(site, arguments);
    }

    public Object execute(SysSite site, List<TemplateModel> arguments) throws TemplateModelException {
        String url = getString(0, arguments);
        Integer expiryMinutes = getInteger(1, arguments);
        String filename = getString(2, arguments);
        if (CommonUtils.notEmpty(url) && null != site) {
            return fileUploadComponent.getPrivateFileUrl(site, expiryMinutes, url, filename);
        }
        return url;
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

    @Override
    public int minParametersNumber() {
        return 1;
    }
}
