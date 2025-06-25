package com.publiccms.views.directive.cms;

// Generated 2020-7-1 21:06:19 by com.publiccms.common.generator.SourceGenerator

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.common.tools.CmsUrlUtils;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.cms.CmsSurveyQuestion;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.component.site.FileUploadComponent;
import com.publiccms.logic.service.cms.CmsSurveyQuestionService;

import freemarker.template.TemplateException;

/**
 *
 * surveyQuestion 调查问卷问题查询指令
 * <p>参数列表
 * <ul>
 * <li><code>id</code>:调查问卷问题id,结果返回<code>object</code>
 * {@link com.publiccms.entities.cms.CmsSurveyQuestion}
 * <li><code>ids</code>:
 * 多个调查问卷问题id,逗号或空格间隔,当id为空时生效,结果返回<code>map</code>(id,<code>object</code>)
 * <li><code>absoluteURL</code>:封面图处理为绝对路径 默认为<code>true</code>
 * <li><code>advanced</code>:开启高级选项, 默认为<code>false</code>
 * </ul>
 * <p>使用示例
 * <p>
 * &lt;@cms.surveyQuestion id=1&gt;${object.title}&lt;/@cms.surveyQuestion&gt;
 * <p>
 * &lt;@cms.surveyQuestion ids='1,2,3'&gt;&lt;#list map as
 * k,v&gt;${k}:${v.title}&lt;#sep&gt;,&lt;/#list&gt;&lt;/@cms.surveyQuestion&gt;
 *
 * <pre>
&lt;script&gt;
 $.getJSON('${site.dynamicPath}api/directive/cms/surveyQuestion?id=1&amp;appToken=接口访问授权Token', function(data){
   console.log(data.title);
 });
 &lt;/script&gt;
 * </pre>
 */
@Component
public class CmsSurveyQuestionDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        Long id = handler.getLong("id");
        boolean absoluteURL = handler.getBoolean("absoluteURL", true);
        boolean advanced = getAdvanced(handler);
        SysSite site = getSite(handler);
        if (CommonUtils.notEmpty(id)) {
            CmsSurveyQuestion entity = service.getEntity(id);
            if (null != entity) {
                if (!advanced) {
                    entity.setAnswer(null);
                }
                if (absoluteURL) {
                    entity.setCover(CmsUrlUtils.getUrl(site.getSitePath(), entity.getCover()));
                }
                handler.put("object", entity).render();
            }
        } else {
            Long[] ids = handler.getLongArray("ids");
            if (CommonUtils.notEmpty(ids)) {
                List<CmsSurveyQuestion> entityList = service.getEntitys(ids);
                UnaryOperator<CmsSurveyQuestion> valueMapper = e -> {
                    if (!advanced) {
                        e.setAnswer(null);
                    }
                    if (absoluteURL) {
                        e.setCover(CmsUrlUtils.getUrl(fileUploadComponent.getPrefix(site), e.getCover()));
                    }
                    return e;
                };
                Map<String, CmsSurveyQuestion> map = CommonUtils.listToMapSorted(entityList, k -> k.getId().toString(), valueMapper, ids, null);
                handler.put("map", map).render();
            }
        }
    }

    @Override
    public boolean supportAdvanced() {
        return true;
    }

    @Resource
    private CmsSurveyQuestionService service;
    @Resource
    protected FileUploadComponent fileUploadComponent;
}
