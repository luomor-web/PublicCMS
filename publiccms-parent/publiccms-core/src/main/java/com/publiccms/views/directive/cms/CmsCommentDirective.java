package com.publiccms.views.directive.cms;

// Generated 2018-11-7 16:25:07 by com.publiccms.common.generator.SourceGenerator

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.ExtendUtils;
import com.publiccms.entities.cms.CmsComment;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.component.config.ContentConfigComponent;
import com.publiccms.logic.component.config.ContentConfigComponent.KeywordsConfig;
import com.publiccms.logic.service.cms.CmsCommentService;

import freemarker.template.TemplateException;

/**
 *
 * comment 评论查询指令
 * <p>参数列表
 * <ul>
 * <li><code>replaceSensitive</code>:替换敏感词, 默认为<code>true</code>
 * <li><code>id</code>:评论id,结果返回<code>object</code>
 * {@link com.publiccms.entities.cms.CmsComment}
 * <li><code>ids</code>:
 * 多个评论id,逗号或空格间隔,当id为空时生效,结果返回<code>map</code>(id,<code>object</code>)
 * </ul>
 * <p>使用示例
 * <p>
 * &lt;@cms.comment id=1&gt;${object.text}&lt;/@cms.comment&gt;
 * <p>
 * &lt;@cms.comment ids=1,2,3&gt;&lt;#list map as
 * k,v&gt;${k}:${v.text}&lt;#sep&gt;,&lt;/#list&gt;&lt;/@cms.comment&gt;
 *
 * <pre>
  &lt;script&gt;
   $.getJSON('${site.dynamicPath}api/directive/cms/comment?id=1&amp;appToken=接口访问授权Token', function(data){
     console.log(data.text);
   });
   &lt;/script&gt;
 * </pre>
 */
@Component
public class CmsCommentDirective extends AbstractTemplateDirective {
    @Resource
    protected ContentConfigComponent contentConfigComponent;

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        Long id = handler.getLong("id");
        boolean replaceSensitive = handler.getBoolean("replaceSensitive", true);
        SysSite site = getSite(handler);
        if (CommonUtils.notEmpty(id)) {
            CmsComment entity = service.getEntity(id);
            if (null != entity && site.getId() == entity.getSiteId()) {
                if (replaceSensitive) {
                    KeywordsConfig config = contentConfigComponent.getKeywordsConfig(site.getId());
                    entity.setText(ExtendUtils.replaceSensitive(entity.getText(), config));
                }
                handler.put("object", entity).render();
            }
        } else {
            Long[] ids = handler.getLongArray("ids");
            if (CommonUtils.notEmpty(ids)) {
                List<CmsComment> entityList = service.getEntitys(ids);
                UnaryOperator<CmsComment> valueMapper = null;
                if (replaceSensitive) {
                    KeywordsConfig config = contentConfigComponent.getKeywordsConfig(site.getId());
                    valueMapper = e -> {
                        e.setText(ExtendUtils.replaceSensitive(e.getText(), config));
                        return e;
                    };
                }
                Map<String, CmsComment> map = CommonUtils.listToMapSorted(entityList, k -> k.getId().toString(), valueMapper, ids,
                        entity -> site.getId() == entity.getSiteId());
                handler.put("map", map).render();
            }
        }
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

    @Resource
    private CmsCommentService service;

}
