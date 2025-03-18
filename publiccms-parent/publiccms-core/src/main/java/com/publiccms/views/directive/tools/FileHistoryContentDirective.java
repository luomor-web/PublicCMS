package com.publiccms.views.directive.tools;

// Generated 2022-5-10 by com.publiccms.common.generator.SourceGenerator

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.common.tools.CmsFileUtils;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.sys.SysSite;

import freemarker.template.TemplateException;

/**
 * fileHistoryContent 文件修改历史内容获取指令
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
 * <li><code>object</code>:文件内容文本
 * </ul>
 * <p lang="zh">使用示例
 * <p lang="en">usage example
 * <p lang="ja">使用例
 * <p>
 * &lt;@tools.fileHistoryContent type='file'
 * path='index.html/2020-01-01_01-01-000000'&gt;${object}&lt;/@tools.fileHistoryContent&gt;
 *
 * <pre>
&lt;script&gt;
 $.getJSON('${site.dynamicPath}api/directive/tools/fileHistoryContent?type=file&amp;path=index.html/2020-01-01_01-01-000000&amp;appToken=接口访问授权Token', function(data){
   console.log(data);
 });
 &lt;/script&gt;
 * </pre>
 *
 */
@Component
public class FileHistoryContentDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        String type = handler.getString("type");
        String path = handler.getString("path");
        if (CommonUtils.notEmpty(path)) {
            SysSite site = getSite(handler);
            String realpath;

            if (CommonUtils.notEmpty(type)) {
                switch (type) {
                case "file":
                    realpath = siteComponent.getWebHistoryFilePath(site.getId(), path, false);
                    break;
                case "task":
                    realpath = siteComponent.getTaskTemplateHistoryFilePath(site.getId(), path, false);
                    break;
                case "template":
                default:
                    realpath = siteComponent.getTemplateHistoryFilePath(site.getId(), path, false);
                }
            } else {
                realpath = siteComponent.getTemplateHistoryFilePath(site.getId(), path, false);
            }
            handler.put("object", CmsFileUtils.getFileContent(realpath)).render();
        }
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

}
