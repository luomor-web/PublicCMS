package com.publiccms.views.directive.sys;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.entities.sys.SysRecord;
import com.publiccms.entities.sys.SysRecordId;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.service.sys.SysRecordService;

import freemarker.template.TemplateException;

/**
 *
 * sysRecord 自定义记录查询写入指令
 * <p lang="zh">
 * 参数列表
 * <p lang="en">
 * parameter list
 * <p lang="ja">
 * パラメータリスト
 * <ul>
 * <li><code>code</code>:记录编码,结果返回<code>object</code>
 * <li><code>data</code>:记录数据,不为空时记录该数据
 * {@link com.publiccms.entities.sys.SysRecord}
 * <li><code>codes</code>:多个记录编码,结果返回<code>map</code>(id,<code>object</code>)
 * </ul>
 * <p lang="zh">
 * 使用示例
 * <p lang="en">
 * usage example
 * <p lang="ja">
 * 使用例
 * <p>
 * &lt;@sys.record code='site' data='data'&gt;${object.data}&lt;/@sys.record&gt;
 *
 * <pre>
&lt;script&gt;
$.getJSON('${site.dynamicPath}api/directive/sys/record?code=site&amp;data=data&amp;appToken=接口访问授权Token', function(data){
 console.log(data.data);
});
&lt;/script&gt;
 * </pre>
 */
@Component
public class SysRecordDirective extends AbstractTemplateDirective {

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        String code = handler.getString("code");
        String[] codes = handler.getStringArray("codes");
        SysSite site = getSite(handler);
        if (CommonUtils.notEmpty(code)) {
            String data = handler.getString("data");
            SysRecord entity = service.getOrCreateOrUpdate(site.getId(), code, data);
            if (null != entity) {
                handler.put("object", entity).render();
            }
        } else if (CommonUtils.notEmpty(codes)) {
            SysRecordId[] ids = new SysRecordId[codes.length];
            int i = 0;
            for (String s : codes) {
                if (CommonUtils.notEmpty(s)) {
                    ids[i++] = new SysRecordId(site.getId(), s);
                }
            }
            Map<String, SysRecord> map = new LinkedHashMap<>();
            for (SysRecord entity : service.getEntitys(ids)) {
                map.put(entity.getId().getCode(), entity);
            }
            handler.put("map", map).render();
        }
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

    @Resource
    private SysRecordService service;

}
