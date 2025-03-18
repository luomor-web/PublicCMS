package com.publiccms.views.directive.visit;

// Generated 2021-1-14 22:44:12 by com.publiccms.common.generator.SourceGenerator

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import jakarta.annotation.Resource;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.visit.VisitUrl;
import com.publiccms.entities.visit.VisitUrlId;
import com.publiccms.logic.service.visit.VisitUrlService;

import freemarker.template.TemplateException;

/**
 *
 * visitUrl 访问网址报表查询指令
 * <p lang="zh">参数列表
 * <p lang="en">parameter list
 * <p lang="ja">パラメータリスト
 * <ul>
 * <li><code>visitDate</code>:访问日期,【2020-01-01】
 * <li><code>urlMd5</code>:访问网址md5
 * <li><code>urlSha</code>:访问网址sha,三个参数都不为空时,结果返回<code>object</code>
 * {@link com.publiccms.entities.visit.VisitUrl}
 * </ul>
 * <p lang="zh">使用示例
 * <p lang="en">usage example
 * <p lang="ja">使用例
 * <p>
 * &lt;@visit.url visitDate='2020-01-01' urlMd5='md5' urlSha='sha'&gt;${object.pv}&lt;/@visit.url&gt;
 * <pre>
 &lt;script&gt;
  $.getJSON('${site.dynamicPath}api/directive/visit/url?visitDate=2020-01-01&amp;urlMd5=MD5&amp;urlSha=sha&amp;appToken=接口访问授权Token', function(data){
    console.log(data.pv);
  });
  &lt;/script&gt;
 * </pre>
 */
@Component
public class VisitUrlDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        Date visitDate = handler.getDate("visitDate");
        String urlMd5 = handler.getString("urlMd5");
        String urlSha = handler.getString("urlSha");
        if (null != visitDate && CommonUtils.notEmpty(urlMd5) && CommonUtils.notEmpty(urlSha)) {
            visitDate = DateUtils.truncate(visitDate, Calendar.DATE);
            VisitUrl entity = service.getEntity(new VisitUrlId(getSite(handler).getId(), visitDate, urlMd5, urlSha));
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
    private VisitUrlService service;

}
