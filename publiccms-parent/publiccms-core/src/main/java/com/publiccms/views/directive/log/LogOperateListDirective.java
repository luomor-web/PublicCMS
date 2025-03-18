package com.publiccms.views.directive.log;

// Generated 2015-5-12 12:57:43 by com.publiccms.common.generator.SourceGenerator

import java.io.IOException;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.logic.service.log.LogOperateService;

import freemarker.template.TemplateException;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import com.publiccms.common.handler.PageHandler;
import com.publiccms.common.handler.RenderHandler;

/**
 *
 * logOperateList 操作日志列表查询指令
 * <p lang="zh">参数列表
 * <p lang="en">parameter list
 * <p lang="ja">パラメータリスト
 * <ul>
 * <li><code>channel</code>:渠道
 * <li><code>operate</code>:操作编码
 * <li><code>userId</code>:用户ID
 * <li><code>startCreateDate</code>:起始创建日期,【2020-01-01 23:59:59】,【2020-01-01】
 * <li><code>endCreateDate</code>:终止创建日期,【2020-01-01 23:59:59】,【2020-01-01】
 * <li><code>content</code>:操作内容
 * <li><code>name</code>:用户名
 * <li><code>ip</code>:IP
 * <li><code>orderType</code>:排序类型,【asc:正序,desc:倒序】,默认为倒序
 * <li><code>pageIndex</code>:页码
 * <li><code>pageSize</code>:每页条数
 * </ul>
 * <p lang="zh">返回结果
 * <p lang="en">return result
 * <p lang="ja">戻り値
 * <ul>
 * <li><code>page</code>:{@link com.publiccms.common.handler.PageHandler}
 * <li><code>page.list</code>:List类型 查询结果实体列表
 * {@link com.publiccms.entities.log.LogOperate}
 * </ul>
 * <p lang="zh">使用示例
 * <p lang="en">usage example
 * <p lang="ja">使用例
 * <p>
 * &lt;@log.operateList pageSize=10&gt;&lt;#list page.list as
 * a&gt;${a.name}&lt;#sep&gt;,&lt;/#list&gt;&lt;/@log.operateList&gt;
 *
 * <pre>
  &lt;script&gt;
   $.getJSON('${site.dynamicPath}api/directive/log/OperateList?pageSize=10&amp;appToken=接口访问授权Token', function(data){
     console.log(data.page.totalCount);
   });
   &lt;/script&gt;
 * </pre>
 */
@Component
public class LogOperateListDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        PageHandler page = service.getPage(getSite(handler).getId(), handler.getString("channel"), handler.getString("operate"),
                handler.getLong("userId"), handler.getDate("startCreateDate"), handler.getDate("endCreateDate"),
                handler.getString("content"), handler.getString("ip"), handler.getString("orderType"),
                handler.getInteger("pageIndex", 1), handler.getInteger("pageSize", 30));
        handler.put("page", page).render();
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

    @Resource
    private LogOperateService service;

}