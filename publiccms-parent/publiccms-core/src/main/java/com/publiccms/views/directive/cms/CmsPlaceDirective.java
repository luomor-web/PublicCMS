package com.publiccms.views.directive.cms;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.common.tools.CmsUrlUtils;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.ExtendUtils;
import com.publiccms.entities.cms.CmsPlace;
import com.publiccms.entities.cms.CmsPlaceAttribute;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.component.site.FileUploadComponent;
import com.publiccms.logic.component.site.StatisticsComponent;
import com.publiccms.logic.service.cms.CmsPlaceAttributeService;
import com.publiccms.logic.service.cms.CmsPlaceService;

import freemarker.template.TemplateException;

/**
 *
 * place 推荐位查询指令
 * <p>参数列表
 * <ul>
 * <li><code>id</code>:推荐位id,结果返回<code>object</code>
 * {@link com.publiccms.entities.cms.CmsPlace}
 * <li><code>absoluteURL</code>:url处理为绝对路径 默认为<code> true</code>
 * <li><code>containsAttribute</code>:默认为<code>false</code>,为true时<code>object.attribute</code>为推荐位扩展数据<code>map</code>(字段编码,<code>value</code>)
 * <li><code>ids</code>:
 * 多个推荐位id,逗号或空格间隔,当id为空时生效,结果返回<code>map</code>(id,<code>object</code>)
 * </ul>
 * <p>使用示例
 * <p>
 * &lt;@cms.place id=1&gt;${object.title}&lt;/@cms.place&gt;
 * <p>
 * &lt;@cms.place ids='1,2,3'&gt;&lt;#list map as
 * k,v&gt;${k}:${v.title}&lt;#sep&gt;,&lt;/#list&gt;&lt;/@cms.place&gt;
 *
 * <pre>
 &lt;script&gt;
  $.getJSON('${site.dynamicPath}api/directive/cms/place?id=1&amp;appToken=接口访问授权Token', function(data){
    console.log(data.title);
  });
  &lt;/script&gt;
 * </pre>
 */
@Component
public class CmsPlaceDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        Long id = handler.getLong("id");
        boolean absoluteURL = handler.getBoolean("absoluteURL", true);
        boolean containsAttribute = handler.getBoolean("containsAttribute", false);
        SysSite site = getSite(handler);
        if (CommonUtils.notEmpty(id)) {
            CmsPlace entity = service.getEntity(id);
            if (null != entity && site.getId() == entity.getSiteId()) {
                if (absoluteURL) {
                    CmsUrlUtils.initPlaceUrl(site, entity);
                    fileUploadComponent.initPlaceCover(site, entity);
                }
                if (containsAttribute) {
                    entity.setAttribute(ExtendUtils.getAttributeMap(attributeService.getEntity(id)));
                }
                handler.put("object", entity).render();
            }
        } else {
            Long[] ids = handler.getLongArray("ids");
            if (CommonUtils.notEmpty(ids)) {
                List<CmsPlace> entityList = service.getEntitys(ids);
                Map<Long, CmsPlaceAttribute> attributeMap = containsAttribute
                        ? CommonUtils.listToMap(attributeService.getEntitys(ids), k -> k.getPlaceId())
                        : null;
                UnaryOperator<CmsPlace> valueMapper = e -> {
                    Integer clicks = statisticsComponent.getPlaceClicks(e.getId());
                    if (null != clicks) {
                        e.setClicks(e.getClicks() + clicks);
                    }
                    if (absoluteURL) {
                        CmsUrlUtils.initPlaceUrl(site, e);
                        fileUploadComponent.initPlaceCover(site, e);
                    }
                    if (containsAttribute) {
                        e.setAttribute(ExtendUtils.getAttributeMap(attributeMap.get(e.getId())));
                    }
                    return e;
                };
                Map<String, CmsPlace> map = CommonUtils.listToMapSorted(entityList, k -> k.getId().toString(), valueMapper, ids,
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
    private CmsPlaceService service;
    @Resource
    private CmsPlaceAttributeService attributeService;
    @Resource
    protected FileUploadComponent fileUploadComponent;
    @Resource
    private StatisticsComponent statisticsComponent;
}
