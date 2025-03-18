package com.publiccms.views.directive.cms;

// Generated 2015-5-10 17:54:56 by com.publiccms.common.generator.SourceGenerator

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.cms.CmsCategoryModel;
import com.publiccms.entities.cms.CmsCategoryModelId;
import com.publiccms.logic.service.cms.CmsCategoryModelService;

import freemarker.template.TemplateException;

/**
 *
 * categoryModel 分类内容模型映射查询指令
 * <p lang="zh">参数列表
 * <p lang="en">parameter list
 * <p lang="ja">パラメータリスト
 * <ul>
 * <li><code>categoryId</code>:分类id,结果返回<code>object</code>
 * {@link com.publiccms.entities.cms.CmsCategoryModel}
 * <li><code>modelId</code>:内容模型id,当id为空时生效,结果返回<code>object</code>
 * <li><code>modelIds</code>
 * 多个内容模型id,当categoryId存在,且modelId为空时生效,结果返回<code>map</code>(内容模型id,<code>object</code>)
 * <li><code>categoryIds</code>
 * 多个分类id,当categoryId不存在,且modelId存在时生效,结果返回<code>map</code>(分类id,<code>object</code>)
 * </ul>
 * <p lang="zh">使用示例
 * <p lang="en">usage example
 * <p lang="ja">使用例
 * <p>
 * &lt;@cms.categoryModel categoryId=1
 * modelId='article'&gt;${object.templatePath}&lt;/@cms.categoryModel&gt;
 * <p>
 * &lt;@cms.categoryModel categoryIds=1,2,3 modelId='article'&gt;&lt;#list map
 * as
 * k,v&gt;${k}:${v.templatePath}&lt;#sep&gt;,&lt;/#list&gt;&lt;/@cms.categoryModel&gt;
 *
 * <pre>
  &lt;script&gt;
   $.getJSON('${site.dynamicPath}api/directive/cms/categoryModel?categoryId=1&amp;modelId=article', function(data){
     console.log(data.templatePath);
   });
   &lt;/script&gt;
 * </pre>
 */
@Component
public class CmsCategoryModelDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        Integer categoryId = handler.getInteger("categoryId");
        String modelId = handler.getString("modelId");
        if (null != categoryId) {
            if (CommonUtils.notEmpty(modelId)) {
                CmsCategoryModelId id = new CmsCategoryModelId(categoryId, modelId);
                CmsCategoryModel entity = service.getEntity(id);
                if (null != entity) {
                    handler.put("object", entity).render();
                }
            } else {
                String[] modelIds = handler.getStringArray("modelIds");
                if (CommonUtils.notEmpty(modelIds)) {
                    CmsCategoryModelId[] entityIds = new CmsCategoryModelId[modelIds.length];
                    for (int i = 0; i < modelIds.length; i++) {
                        entityIds[i] = new CmsCategoryModelId(categoryId, modelIds[i]);
                    }
                    List<CmsCategoryModel> entityList = service.getEntitys(entityIds);
                    Map<String, CmsCategoryModel> map = CommonUtils.listToMapSorted(entityList, k -> k.getId().getModelId(), null, modelIds, null);
                    handler.put("map", map).render();
                }
            }
        } else if (CommonUtils.notEmpty(modelId)) {
            Integer[] categoryIds = handler.getIntegerArray("categoryIds");
            if (CommonUtils.notEmpty(categoryIds)) {
                CmsCategoryModelId[] entityIds = new CmsCategoryModelId[categoryIds.length];
                for (int i = 0; i < categoryIds.length; i++) {
                    entityIds[i] = new CmsCategoryModelId(categoryIds[i], modelId);
                }
                List<CmsCategoryModel> entityList = service.getEntitys(entityIds);
                Map<String, CmsCategoryModel> map = CommonUtils.listToMapSorted(entityList, k -> String.valueOf(k.getId().getCategoryId()), categoryIds);
                handler.put("map", map).render();
            }
        }
    }

    @Resource
    private CmsCategoryModelService service;

}