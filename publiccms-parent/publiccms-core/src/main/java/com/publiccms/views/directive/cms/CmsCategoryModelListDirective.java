package com.publiccms.views.directive.cms;

// Generated 2015-5-10 17:54:56 by com.publiccms.common.generator.SourceGenerator

import java.io.IOException;
import java.util.List;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.entities.cms.CmsCategoryModel;
import com.publiccms.logic.service.cms.CmsCategoryModelService;

import freemarker.template.TemplateException;

/**
 *
 * categoryModelList 分类模型映射列表查询指令
 * <p lang="zh">参数列表
 * <p lang="en">parameter list
 * <p lang="ja">パラメータリスト
 * <ul>
 * <li><code>modelId</code>:内容模型id
 * <li><code>categoryId</code>:分类id
 * </ul>
 * <p lang="zh">返回结果
 * <p lang="en">return result
 * <p lang="ja">戻り値
 * <ul>
 * <li><code>list</code>:List类型 查询结果实体列表
 * {@link com.publiccms.entities.cms.CmsCategoryModel}
 * </ul>
 * <p lang="zh">使用示例
 * <p lang="en">usage example
 * <p lang="ja">使用例
 * <p>
 * &lt;@cms.categoryModelList modelId='article'&gt;&lt;#list list as
 * a&gt;${a.templatePath}&lt;#sep&gt;,&lt;/#list&gt;&lt;/@cms.categoryModelList&gt;
 *
 * <pre>
  &lt;script&gt;
   $.getJSON('${site.dynamicPath}api/directive/cms/categoryModelList?modelId=article', function(data){
     console.log(data[0].totalCount);
   });
   &lt;/script&gt;
 * </pre>
 */
@Component
public class CmsCategoryModelListDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        List<CmsCategoryModel> list = service.getList(getSite(handler).getId(), handler.getString("modelId"),
                handler.getInteger("categoryId"));
        handler.put("list", list).render();
    }

    @Resource
    private CmsCategoryModelService service;

}