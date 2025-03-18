package com.publiccms.views.directive.tools;

// Generated 2015-5-10 17:54:56 by com.publiccms.common.generator.SourceGenerator

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.common.tools.CmsFileUtils;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.sys.SysSite;

import freemarker.template.TemplateException;

/**
 * fileExists 文件是否存在指令
 * <p lang="zh">参数列表
 * <p lang="en">parameter list
 * <p lang="ja">パラメータリスト
 * <ul>
 * <li><code>type</code>:文件类型【file,task,template】,默认template
 * <li><code>path</code>:文件路径
 * </ul>
 * <p lang="zh">返回结果
 * <p lang="en">return result
 * <p lang="ja">戻り値
 * <ul>
 * <li><code>object</code>:boolean类型文件是否存在
 * </ul>
 * <p lang="zh">使用示例
 * <p lang="en">usage example
 * <p lang="ja">使用例
 * <p>
 * &lt;@tools.fileExists type='file' path='/'&gt;&lt;#list list as
 * a&gt;${a}&lt;#sep&gt;,&lt;/#list&gt;&lt;/@tools.fileExists&gt;
 *
 * <pre>
&lt;script&gt;
 $.getJSON('${site.dynamicPath}api/directive/tools/fileExists?type=file&amp;path=/&amp;appToken=接口访问授权Token', function(data){
   console.log(data);
 });
 &lt;/script&gt;
 * </pre>
 *
 */
@Component
public class FileExistDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        String type = handler.getString("type");
        String path = handler.getString("path");
        SysSite site = getSite(handler);
        String realpath;
        if (CommonUtils.notEmpty(type)) {
            switch (type) {
            case "file":
                realpath = siteComponent.getWebFilePath(site.getId(), path);
                break;
            case "task":
                realpath = siteComponent.getTaskTemplateFilePath(site.getId(), path);
                break;
            case "template":
            default:
                realpath = siteComponent.getTemplateFilePath(site.getId(), path);
            }
        } else {
            realpath = siteComponent.getTemplateFilePath(site.getId(), path);
        }
        handler.put("object", CommonUtils.notEmpty(path) && CmsFileUtils.isFile(realpath)).render();
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

}