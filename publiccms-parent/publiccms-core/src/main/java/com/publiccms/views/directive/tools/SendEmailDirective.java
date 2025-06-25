package com.publiccms.views.directive.tools;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.mail.MessagingException;

import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.FreeMarkerUtils;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.component.site.EmailComponent;
import com.publiccms.logic.component.site.SiteComponent;
import com.publiccms.logic.component.template.MetadataComponent;
import com.publiccms.logic.component.template.TemplateComponent;
import com.publiccms.views.pojo.entities.CmsPageData;
import com.publiccms.views.pojo.entities.CmsPageMetadata;

import freemarker.template.TemplateException;

/**
 * sendEmail 发送邮件指令
 * <p>参数列表
 * <ul>
 * <li><code>email</code>:多个邮件地址
 * <li><code>cc</code>:多个抄送地址
 * <li><code>bcc</code>:多个密送地址
 * <li><code>title</code>:标题
 * <li><code>templatePath</code>:内容模板路径
 * <li><code>content</code>:邮件内容,templatePath为空时有效
 * <li><code>fileNames</code>:多个附件名称
 * <li><code>filePaths</code>:多个文件路径
 * <li><code>parameters</code>:参数map
 * </ul>
 * <p>返回结果
 * <ul>
 * <li><code>result</code>:是否允许发送,【true,false】
 * </ul>
 * <p>使用示例
 * <p>
 * &lt;@tools.sendEmail email='master@puliccms.com' title='title'
 * content='content' parameters={"parameter1":"value1"}/&gt;
 *
 * <pre>
&lt;script&gt;
 $.getJSON('${site.dynamicPath}api/directive/tools/sendEmail?email=master@puliccms.com&amp;title=title&amp;content=content&amp;parameters.parameter1=value1&amp;appToken=接口访问授权Token', function(data){
   console.log(data.alias);
 });
 &lt;/script&gt;
 * </pre>
 *
 */
@Component
public class SendEmailDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        String[] email = handler.getStringArray("email");
        String[] cc = handler.getStringArray("cc");
        String[] bcc = handler.getStringArray("bcc");
        String title = handler.getString("title");
        String templatePath = handler.getString("templatePath");
        String[] fileNames = handler.getStringArray("fileNames");
        String[] filePaths = handler.getStringArray("filePaths");
        if (CommonUtils.notEmpty(email) && CommonUtils.notEmpty(title)) {
            File[] files = null;
            if (null != filePaths) {
                files = new File[filePaths.length];
                int i = 0;
                for (String filePath : filePaths) {
                    files[i] = new File(siteComponent.getWebFilePath(getSite(handler).getId(), filePath));
                    i++;
                }
            }
            SysSite site = getSite(handler);
            if (CommonUtils.notEmpty(templatePath)) {
                Map<String, Object> model = new HashMap<>();
                expose(handler, model);
                String filepath = siteComponent.getTemplateFilePath(site.getId(), templatePath);
                CmsPageMetadata metadata = metadataComponent.getTemplateMetadata(filepath);
                CmsPageData data = metadataComponent.getTemplateData(filepath);
                Map<String, String> parameters = handler.getMap("parameters");
                if (!parameters.isEmpty()) {
                    model.putAll(parameters);
                }
                model.put("metadata", metadata.getAsMap(data));
                String content = FreeMarkerUtils.generateStringByFile(
                        SiteComponent.getFullTemplatePath(site.getId(), templatePath), templateComponent.getWebConfiguration(),
                        model);
                try {
                    handler.put("result", emailComponent.sendHtml(site.getId(), email, cc, bcc, title, content, fileNames, files))
                            .render();
                } catch (MessagingException e) {
                    handler.print(e.getMessage());
                    handler.put("result", false).render();
                }
            } else {
                String content = handler.getString("content");
                if (CommonUtils.notEmpty(content)) {
                    try {
                        handler.put("result", emailComponent.send(site.getId(), email, cc, bcc, title, content, fileNames, files))
                                .render();
                    } catch (MessagingException e) {
                        handler.print(e.getMessage());
                        handler.put("result", false).render();
                    }
                }
            }

        }
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

    @Resource
    private EmailComponent emailComponent;
    @Resource
    private TemplateComponent templateComponent;
    @Resource
    private MetadataComponent metadataComponent;
}
