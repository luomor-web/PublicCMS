package com.publiccms.views.directive.cms;

// Generated 2015-7-10 16:36:23 by com.publiccms.common.generator.SourceGenerator

import java.io.IOException;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.logic.service.cms.CmsTagTypeService;

import freemarker.template.TemplateException;

import javax.annotation.Resource;
import org.springframework.stereotype.Component;

import com.publiccms.common.handler.PageHandler;
import com.publiccms.common.handler.RenderHandler;

/**
 *
 * tagTypeList 标签类型列表查询指令
 * <p>参数列表
 * <ul>
 * <li><code>name</code>:标签名
 * <li><code>pageIndex</code>:页码
 * <li><code>pageSize</code>:每页条数
 * </ul>
 * <p>返回结果
 * <ul>
 * <li><code>page</code>:{@link com.publiccms.common.handler.PageHandler}
 * <li><code>page.list</code>:List类型 查询结果实体列表
 * {@link com.publiccms.entities.cms.CmsTagType}
 * </ul>
 * <p>使用示例
 * <p>
 * &lt;@cms.tagTypeList pageSize=10&gt;&lt;#list page.list as
 * a&gt;${a.name}&lt;#sep&gt;,&lt;/#list&gt;&lt;/@cms.tagTypeList&gt;
 *
 * <pre>
&lt;script&gt;
$.getJSON('${site.dynamicPath}api/directive/cms/tagTypeList?pageSize=10', function(data){
 console.log(data.page.totalCount);
});
&lt;/script&gt;
 * </pre>
 */
@Component
public class CmsTagTypeListDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        PageHandler page = service.getPage(getSite(handler).getId(), handler.getString("name"),
                handler.getInteger("pageIndex", 1), handler.getInteger("pageSize", 30));
        handler.put("page", page).render();
    }

    @Resource
    private CmsTagTypeService service;

}