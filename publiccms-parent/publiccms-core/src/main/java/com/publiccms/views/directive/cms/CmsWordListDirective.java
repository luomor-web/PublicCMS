package com.publiccms.views.directive.cms;

// Generated 2016-3-22 11:21:35 by com.publiccms.common.generator.SourceGenerator

import java.io.IOException;

import jakarta.annotation.Resource;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.logic.service.cms.CmsWordService;

import freemarker.template.TemplateException;

import org.springframework.stereotype.Component;

import com.publiccms.common.handler.PageHandler;
import com.publiccms.common.handler.RenderHandler;

/**
 *
 * wordList 用户投票列表查询指令
 * <p lang="zh">参数列表
 * <p lang="en">parameter list
 * <p lang="ja">パラメータリスト
 * <ul>
 * <li><code>startCreateDate</code>:起始创建日期,【2020-01-01 23:59:59】,【2020-01-01】
 * <li><code>endCreateDate</code>:终止创建日期,【2020-01-01 23:59:59】,【2020-01-01】
 * <li><code>advanced</code>:开启高级选项, 默认为<code>false</code>
 * <li><code>hidden</code>:高级选项:禁用状态,默认为<code>false</code>
 * <li><code>name</code>:高级选项:名称
 * <li><code>orderField</code>
 * 高级选项:排序,【searchCount:搜索次数,createDate:创建日期,id:id】默认为searchCount按orderType排序
 * <li><code>orderType</code>:排序类型,【asc:正序,desc:倒序】,默认为创建日期倒序
 * <li><code>pageIndex</code>:页码
 * <li><code>pageSize</code>:每页条数
 * </ul>
 * <p lang="zh">返回结果
 * <p lang="en">return result
 * <p lang="ja">戻り値
 * <ul>
 * <li><code>page</code>:{@link com.publiccms.common.handler.PageHandler}
 * <li><code>page.list</code>:List类型 查询结果实体列表
 * {@link com.publiccms.entities.cms.CmsWord}
 * </ul>
 * <p lang="zh">使用示例
 * <p lang="en">usage example
 * <p lang="ja">使用例
 * <p>
 * &lt;@cms.wordList userId=1 pageSize=10&gt;&lt;#list page.list as
 * a&gt;${a.ip}&lt;#sep&gt;,&lt;/#list&gt;&lt;/@cms.wordList&gt;
 *
 * <pre>
&lt;script&gt;
$.getJSON('${site.dynamicPath}api/directive/cms/wordList?userId=1&amp;pageSize=10', function(data){    
console.log(data.page.totalCount);
});
&lt;/script&gt;
 * </pre>
 */
@Component
public class CmsWordListDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        Boolean hidden = false;
        String orderField = "searchCount";
        String name = null;
        if (getAdvanced(handler)) {
            hidden = handler.getBoolean("hidden");
            orderField = handler.getString("orderField");
            name = handler.getString("name");
        }
        PageHandler page = service.getPage(getSite(handler).getId(), hidden, handler.getDate("startCreateDate"),
                handler.getDate("endCreateDate"), name, orderField, handler.getString("orderType"),
                handler.getInteger("pageIndex", 1), handler.getInteger("pageSize", handler.getInteger("count", 30)));
        handler.put("page", page).render();
    }

    @Override
    public boolean supportAdvanced() {
        return true;
    }

    @Resource
    private CmsWordService service;

}