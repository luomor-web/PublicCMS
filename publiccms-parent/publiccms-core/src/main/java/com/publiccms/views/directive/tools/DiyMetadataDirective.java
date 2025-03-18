package com.publiccms.views.directive.tools;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.component.template.DiyComponent;

import freemarker.template.TemplateException;
import jakarta.annotation.Resource;

/**
 * diyMetadata diy元数据获取指令
 * <p lang="zh">参数列表
 * <p lang="en">parameter list
 * <p lang="ja">パラメータリスト
 * <ul>
 * <li><code>itemType</code>:元数据类型,【region,layout,module】
 * <li><code>itemId</code>:元数据id
 * </ul>
 * <p lang="zh">返回结果
 * <p lang="en">return result
 * <p lang="ja">戻り値
 * <ul>
 * <li><code>object</code>:diy元数据<code>region</code>:{@link com.publiccms.views.pojo.diy.CmsRegion},<code>layout</code>:{@link com.publiccms.views.pojo.diy.CmsLayout},<code>module</code>:{@link com.publiccms.views.pojo.diy.CmsModule}
 * </ul>
 * <p lang="zh">使用示例
 * <p lang="en">usage example
 * <p lang="ja">使用例
 * <p>
 * &lt;@tools.diyMetadata itemType='region' itemId='00000000-0000-0000-0000-000000000000'&gt;${object.name}&lt;/@tools.diyMetadata&gt;
 *
 * <pre>
&lt;script&gt;
 $.getJSON('${site.dynamicPath}api/directive/tools/diyMetadata?itemType=region&amp;itemId=00000000-0000-0000-0000-000000000000&amp;appToken=接口访问授权Token', function(data){
   console.log(data.name);
 });
 &lt;/script&gt;
 * </pre>
 *
 */
@Component
public class DiyMetadataDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        SysSite site = getSite(handler);
        String itemType = handler.getString("itemType");
        String itemId = handler.getString("itemId");
        if (CommonUtils.notEmpty(itemType) && CommonUtils.notEmpty(itemId)) {
            if ("region".equalsIgnoreCase(itemType)) {
                handler.put("object", diyComponent.getRegion(site, itemId)).render();
            } else if ("layout".equalsIgnoreCase(itemType)) {
                handler.put("object", diyComponent.getLayout(site, itemId)).render();
            } else if ("module".equalsIgnoreCase(itemType)) {
                handler.put("object", diyComponent.getModule(site, itemId)).render();
            }
        }
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

    @Resource
    private DiyComponent diyComponent;
}
