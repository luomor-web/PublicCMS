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
import com.publiccms.entities.visit.VisitDay;
import com.publiccms.entities.visit.VisitDayId;
import com.publiccms.logic.service.visit.VisitDayService;

import freemarker.template.TemplateException;

/**
 *
 * visitDay 访问日报表查询指令
 * <p lang="zh">参数列表
 * <p lang="en">parameter list
 * <p lang="ja">パラメータリスト
 * <ul>
 * <li><code>visitDate</code>:访问日期,【2020-01-01】
 * <li><code>visitHour</code>:访问小时,【-1-23】,-1表示整天数据,两个参数都不为空时,结果返回<code>object</code>
 * {@link com.publiccms.entities.visit.VisitDay}
 * </ul>
 * <p lang="zh">使用示例
 * <p lang="en">usage example
 * <p lang="ja">使用例
 * <p>
 * &lt;@visit.day visitDate='2020-01-01'
 * visitHour=9&gt;${object.pv}&lt;/@visit.day&gt;
 *
 * <pre>
 &lt;script&gt;
  $.getJSON('${site.dynamicPath}api/directive/visit/day?visitDate=2020-01-01&amp;visitHour=9&amp;appToken=接口访问授权Token', function(data){
    console.log(data.pv);
  });
  &lt;/script&gt;
 * </pre>
 */
@Component
public class VisitDayDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        Date visitDate = handler.getDate("visitDate");
        Byte visitHour = handler.getByte("visitHour");
        if (null != visitDate && null != visitHour) {
            visitDate = DateUtils.truncate(visitDate, Calendar.DATE);
            VisitDay entity = service.getEntity(new VisitDayId(getSite(handler).getId(), visitDate, visitHour));
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
    private VisitDayService service;

}
