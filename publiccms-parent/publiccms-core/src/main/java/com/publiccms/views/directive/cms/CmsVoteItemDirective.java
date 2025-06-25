package com.publiccms.views.directive.cms;

// Generated 2020-3-26 11:46:48 by com.publiccms.common.generator.SourceGenerator

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.cms.CmsVoteItem;
import com.publiccms.logic.service.cms.CmsVoteItemService;

import freemarker.template.TemplateException;

/**
 *
 * voteItem 投票选项查询指令
 * <p>参数列表
 * <ul>
 * <li><code>id</code>:投票选项id,结果返回<code>object</code>
 * {@link com.publiccms.entities.cms.CmsVoteItem}
 * <li><code>ids</code>:
 * 多个投票选项id,逗号或空格间隔,当id为空时生效,结果返回<code>map</code>(id,<code>object</code>)
 * </ul>
 * <p>使用示例
 * <p>
 * &lt;@cms.voteItem id=1&gt;${object.title}&lt;/@cms.voteItem&gt;
 * <p>
 * &lt;@cms.voteItem ids='1,2,3'&gt;&lt;#list map as
 * k,v&gt;${k}:${v.title}&lt;#sep&gt;,&lt;/#list&gt;&lt;/@cms.voteItem&gt;
 *
 * <pre>
&lt;script&gt;
$.getJSON('${site.dynamicPath}api/directive/cms/voteItem?id=1', function(data){
  console.log(data.title);
});
&lt;/script&gt;
 * </pre>
 */
@Component
public class CmsVoteItemDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        Long id = handler.getLong("id");
        if (CommonUtils.notEmpty(id)) {
            CmsVoteItem entity = service.getEntity(id);
            if (null != entity) {
                handler.put("object", entity).render();
            }
        } else {
            Long[] ids = handler.getLongArray("ids");
            if (CommonUtils.notEmpty(ids)) {
                List<CmsVoteItem> entityList = service.getEntitys(ids);
                Map<String, CmsVoteItem> map = CommonUtils.listToMapSorted(entityList, k -> k.getId().toString(), ids);
                handler.put("map", map).render();
            }
        }
    }

    @Resource
    private CmsVoteItemService service;

}
