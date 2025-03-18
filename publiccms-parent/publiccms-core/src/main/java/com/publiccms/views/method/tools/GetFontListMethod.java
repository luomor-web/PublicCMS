package com.publiccms.views.method.tools;

import java.awt.GraphicsEnvironment;
import java.util.List;

import org.springframework.stereotype.Component;

import com.publiccms.common.base.BaseMethod;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 *
 * getFontList 获取系统字体列表
 * <p lang="zh">返回结果
 * <p lang="en">return result
 * <p lang="ja">戻り値
 * <ul>
 * <li><code>font list</code>:字体名称列表
 * </ul>
 * <p lang="zh">使用示例
 * <p lang="en">usage example
 * <p lang="ja">使用例
 * <p>
 * &lt;#list getFontList() as font&gt;${font}&lt;/#list&gt;
 * <p>
 *
 * <pre>
&lt;script&gt;
$.getJSON('${site.dynamicPath}api/method/getFontList?appToken=接口访问授权Token', function(data){
console.log(data);
});
&lt;/script&gt;
 * </pre>
 */
@Component
public class GetFontListMethod extends BaseMethod {

    @Override
    public Object execute(List<TemplateModel> arguments) throws TemplateModelException {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

    @Override
    public int minParametersNumber() {
        return 0;
    }
}
