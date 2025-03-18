package com.publiccms.views.directive.tools;

// Generated 2015-5-10 17:54:56 by com.publiccms.common.generator.SourceGenerator

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.common.constants.Constants;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.common.tools.CmsFileUtils;
import com.publiccms.common.tools.CmsFileUtils.FileSearchResult;
import com.publiccms.common.tools.CommonUtils;

import freemarker.template.TemplateException;

/**
 * templateSearchList 模板文件搜索列表指令
 * <p lang="zh">参数列表
 * <p lang="en">parameter list
 * <p lang="ja">パラメータリスト
 * <ul>
 * <li><code>path</code>:文件路径
 * <li><code>word</code>:搜索词
 * </ul>
 * <p lang="zh">返回结果
 * <p lang="en">return result
 * <p lang="ja">戻り値
 * <ul>
 * <li><code>list</code>:文件列表
 * {@link com.publiccms.common.tools.CmsFileUtils$FileSearchResult}
 * </ul>
 * <p lang="zh">使用示例
 * <p lang="en">usage example
 * <p lang="ja">使用例
 * <p>
 * &lt;@tools.templateSearchList path='/' word='script'&gt;&lt;#list list as
 * a&gt;${a.path}&lt;#sep&gt;,&lt;/#list&gt;&lt;/@tools.templateSearchList&gt;
 *
 * <pre>
&lt;script&gt;
 $.getJSON('${site.dynamicPath}api/directive/tools/templateSearchList?path=/&amp;word=script&amp;appToken=接口访问授权Token', function(data){
   console.log(data);
 });
 &lt;/script&gt;
 * </pre>
 *
 */
@Component
public class TemplateSearchListDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        String path = handler.getString("path", Constants.SEPARATOR);
        String word = handler.getString("word");
        if (CommonUtils.notEmpty(word) ) {
            List<FileSearchResult> list = CmsFileUtils
                    .searchFileList(siteComponent.getTemplateFilePath(getSite(handler).getId(), path), path, word);

            String safeWord = HtmlUtils.htmlEscape(word, StandardCharsets.UTF_8.name());
            StringBuilder sb = new StringBuilder("<b>");
            sb.append(safeWord).append("</b>");
            String resultWord = sb.toString();

            list.forEach(file -> {
                for (int i = 0; i < file.getMatchList().size(); i++) {
                    String safeLine = HtmlUtils.htmlEscape(file.getMatchList().get(i), StandardCharsets.UTF_8.name());
                    file.getMatchList().set(i, StringUtils.replace(safeLine, safeWord, resultWord));
                }
            });
            handler.put("list", list).render();
        }
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

}