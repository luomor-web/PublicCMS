package com.publiccms.views.method.cms;

import java.util.Collections;
import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.publiccms.common.base.BaseMethod;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.ExtendUtils;
import com.publiccms.logic.service.cms.CmsPlaceAttributeService;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 *
 * getPlaceAttribute 获取推荐位数据扩展数据
 * <p lang="zh">参数列表
 * <p lang="en">parameter list
 * <p lang="ja">パラメータリスト
 * <ol>
 * <li>推荐位数据id
 * </ol>
 * <p lang="zh">返回结果
 * <p lang="en">return result
 * <p lang="ja">戻り値
 * <ul>
 * <li><code>attribute</code>:推荐位数据扩展数据(字段编码,<code>value</code>)
 * </ul>
 * <p lang="zh">使用示例
 * <p lang="en">usage example
 * <p lang="ja">使用例
 * <p>
 * &lt;#assign attribute=getContentAttribute(1)/&lt;
 * <p>
 * ${(attribute.description)!}
 * <p>
 *
 * <pre>
&lt;script&gt;
$.getJSON('${site.dynamicPath}api/method/getContentAttribute?appToken=接口访问授权Token&amp;parameters=1', function(data){
console.log(data.description);
});
&lt;/script&gt;
 * </pre>
 */
@Component
public class GetPlaceAttributeMethod extends BaseMethod {

    @Override
    public Object execute(List<TemplateModel> arguments) throws TemplateModelException {
        Long id = getLong(0, arguments);
        if (CommonUtils.notEmpty(id)) {
            return ExtendUtils.getAttributeMap(service.getEntity(id));
        }
        return Collections.emptyMap();
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

    @Override
    public int minParametersNumber() {
        return 1;
    }

    @Resource
    private CmsPlaceAttributeService service;

}
