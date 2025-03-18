package com.publiccms.views.method.tools;

import java.util.List;
import java.util.regex.Matcher;

import org.springframework.stereotype.Component;

import com.publiccms.common.base.BaseMethod;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.HtmlUtils;
import com.publiccms.views.pojo.diy.CmsLayout;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 *
 * getLayoutStyle 布局样式表
 * <p lang="zh">参数列表
 * <p lang="en">parameter list
 * <p lang="ja">パラメータリスト
 * <ol>
 * <li>包含&#47;&#42; selecter &#42;&#47;的样式表
 * <li>被替换的选择器
 * </ol>
 * <p>
 * 返回结果
 * <ul>
 * <li><code>style</code>:替换后的样式表
 * </ul>
 * <p lang="zh">使用示例
 * <p lang="en">usage example
 * <p lang="ja">使用例
 * <p>
 * ${getLayoutStyle('&#47;&#42; selecter &#42;&#47;','.diy-layout')}
 * <p>
 *
 * <pre>
&lt;script&gt;
$.getJSON('${site.dynamicPath}api/method/getLayoutStyle?appToken=接口访问授权Token&amp;parameters=&#47;&#42; selecter &#42;&#47;&amp;parameters=.diy-layout', function(data){
console.log(data);
});
&lt;/script&gt;
 * </pre>
 */
@Component
public class GetLayoutStyleMethod extends BaseMethod {

    @Override
    public Object execute(List<TemplateModel> arguments) throws TemplateModelException {
        if (arguments.size() >= 2) {
            String style = getString(0, arguments);
            String selector = getString(1, arguments);
            if (CommonUtils.notEmpty(selector) && CommonUtils.notEmpty(style)) {
                Matcher matcher = CmsLayout.SELECTOR_PATTERN.matcher(style);
                return matcher.replaceAll(HtmlUtils.removeHtmlTag(selector));
            }
        }
        return null;
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

    @Override
    public int minParametersNumber() {
        return 2;
    }

}
