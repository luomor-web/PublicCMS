package com.publiccms.views.directive.cms;

// Generated 2015-5-10 17:54:56 by com.publiccms.common.generator.SourceGenerator

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.common.base.HighLighterQuery;
import com.publiccms.common.handler.PageHandler;
import com.publiccms.common.handler.RenderHandler;
import com.publiccms.common.tools.CmsUrlUtils;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.ExtendUtils;
import com.publiccms.common.tools.RequestUtils;
import com.publiccms.entities.cms.CmsContent;
import com.publiccms.entities.cms.CmsContentAttribute;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.component.config.ContentConfigComponent;
import com.publiccms.logic.component.config.ContentConfigComponent.KeywordsConfig;
import com.publiccms.logic.component.site.FileUploadComponent;
import com.publiccms.logic.component.site.StatisticsComponent;
import com.publiccms.logic.service.cms.CmsContentAttributeService;
import com.publiccms.logic.service.cms.CmsContentService;
import com.publiccms.views.pojo.entities.ClickStatistics;
import com.publiccms.views.pojo.query.CmsContentSearchQuery;

import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;

/**
 *
 * search 内容列表查询指令
 * <p>
 * 参数列表
 * <ul>
 * <li><code>word</code>:搜索词,多个搜索词时取并集结果
 * <li><code>exclude</code>:排除词汇
 * <li><code>tagIds</code>:多个标签id,多个标签时取并集结果
 * <li><code>userId</code>:用户id
 * <li><code>parentId</code>:父内容id
 * <li><code>categoryId</code>:分类id
 * <li><code>containChild</code>:包含子分类,当categoryId不为空时有效
 * <li><code>categoryIds</code>:多个分类id,当categoryId为空时有效
 * <li><code>modelIds</code>:多个模型id
 * <li><code>extendsValues</code>
 * 多个全文搜索字段値,格式：[字段编码]:字段値],例如:extendsValues='isbn:value1,unicode:value2'
 * <li><code>dictionaryValues</code>
 * 多个字典搜索字段値,只有数据字典父级値時包含所有子级结果,格式：[字段编码]_[字段値],例如:dictionaryValues='extend1_value1,extend1_value2'
 * <li><code>dictionaryUnion</code>
 * 取数据字典并集结果,dictionaryUnion不为空时有效,【true,false】,默认为交集结果
 * <li><code>highlight</code>:高亮关键词,【true,false】,默认为false,启用高亮后,
 * 标题、作者、编辑、描述字段应该加?no_esc以使高亮html生效,cms会自动对原值有进行html安全转义
 * <li><code>preTag</code>:高亮前缀,启用高亮时有效,默认为"&lt;B&gt;"
 * <li><code>postTag</code>:高亮后缀,启用高亮时有效,默认为"&lt;/B&gt;"
 * <li><code>projection</code>:投影结果,【true,false】,默认为false
 * <li><code>phrase</code>:精确搜索,【true,false】,默认为false
 * <li><code>fields</code>:搜索字段,【title:标题, author:作者, editor:编辑, description:描述,
 * text:正文,files:附件】
 * <li><code>containsAttribute</code>默认为<code>false</code>,http请求时为高级选项,为true时<code>content.attribute</code>为内容扩展数据<code>map</code>(字段编码,<code>value</code>)
 * <li><code>startPublishDate</code>:起始发布日期,【2000-01-01 23:59:59】,【2000-01-01】
 * <li><code>endPublishDate</code>:终止发布日期,【2000-01-01 23:59:59】,【2000-01-01】
 * <li><code>orderField</code>
 * 排序字段,【clicks:点击数倒序,score:分数倒序,publishDate:发布日期倒序,collections:收藏数倒叙,minPrice:最低价格,maxPrice:最高价格,extend.sort1-extend.sort10]:扩展字段排序标志】,默认相关度倒序
 * <li><code>pageIndex</code>:页码
 * <li><code>pageSize</code>:每页条数
 * <li><code>maxResults</code>:最大结果数
 * </ul>
 * <p>返回结果
 * <ul>
 * <li><code>page</code>:{@link com.publiccms.common.handler.PageHandler}
 * <li><code>page.list</code>:List类型 查询结果实体列表
 * {@link com.publiccms.entities.cms.CmsContent}
 * </ul>
 * 使用示例
 * <p>
 * &lt;@cms.search word='cms' pageSize=10&gt;&lt;#list page.list as
 * a&gt;${a.title}&lt;#sep&gt;,&lt;/#list&gt;&lt;/@cms.search&gt;
 *
 * <pre>
*  &lt;script&gt;
  $.getJSON('${site.dynamicPath}api/directive/cms/search?word=cms&amp;pageSize=10', function(data){
    console.log(data.page.totalCount);
  });
  &lt;/script&gt;
 * </pre>
 *
 */
@Component
public class CmsSearchDirective extends AbstractTemplateDirective {
    @Resource
    private CmsContentAttributeService attributeService;
    @Resource
    private StatisticsComponent statisticsComponent;
    @Resource
    protected ContentConfigComponent contentConfigComponent;
    @Resource
    protected FileUploadComponent fileUploadComponent;

    @Override
    public void execute(RenderHandler handler) throws IOException, TemplateException {
        handler.put("page", query(handler, handler.getBoolean("factSearch", false))).render();
    }

    public PageHandler query(RenderHandler handler, boolean factSearch) throws TemplateModelException {
        String word = handler.getString("word");
        Long[] tagIds = handler.getLongArray("tagIds");
        if (null == tagIds) {
            tagIds = handler.getLongArray("tagId");
        }
        SysSite site = getSite(handler);
        if (CommonUtils.notEmpty(word)) {
            word = CommonUtils.keep(word, 100, null);
            String ip = RequestUtils.getIpAddress(handler.getRequest());
            statisticsComponent.search(site.getId(), word, ip);
        }
        if (CommonUtils.notEmpty(tagIds)) {
            for (Long tagId : tagIds) {
                statisticsComponent.searchTag(tagId);
            }
        }
        Integer pageIndex = handler.getInteger("pageIndex", 1);
        Integer pageSize = handler.getInteger("pageSize", handler.getInteger("count", 30));
        Date currentDate = CommonUtils.getMinuteDate();
        HighLighterQuery highLighterQuery = new HighLighterQuery(handler.getBoolean("highlight", false));
        if (highLighterQuery.isHighlight()) {
            highLighterQuery.setPreTag(handler.getString("preTag"));
            highLighterQuery.setPostTag(handler.getString("postTag"));
        }
        boolean containsAttribute = handler.getBoolean("containsAttribute", false);
        containsAttribute = handler.inHttp() ? getAdvanced(handler) && containsAttribute : containsAttribute;
        try {
            CmsContentSearchQuery query = new CmsContentSearchQuery(site.getId(), handler.getBoolean("projection", false),
                    handler.getBoolean("phrase", false), highLighterQuery, word, handler.getString("exclude"),
                    handler.getStringArray("fields"), tagIds, handler.getLong("userId"), handler.getLong("parentId"),
                    handler.getInteger("categoryId"), handler.getIntegerArray("categoryIds"), handler.getStringArray("modelIds"),
                    handler.getStringArray("extendsValues"), handler.getStringArray("dictionaryValues"),
                    handler.getBoolean("dictionaryUnion"), handler.getDate("startPublishDate"),
                    handler.getDate("endPublishDate", currentDate), currentDate);
            PageHandler page = null;
            if (factSearch) {
                page = service.facetQuery(query, handler.getBoolean("containChild"), handler.getString("orderField"),
                        handler.getString("orderType"), pageIndex, pageSize, handler.getInteger("maxResults"));
            } else {
                page = service.query(query, handler.getBoolean("containChild"), handler.getString("orderField"),
                        handler.getString("orderType"), pageIndex, pageSize, handler.getInteger("maxResults"));
            }

            @SuppressWarnings("unchecked")
            List<CmsContent> list = (List<CmsContent>) page.getList();
            if (null != list) {
                Consumer<CmsContent> consumer = null;
                if (containsAttribute) {
                    Long[] ids = list.stream().map(CmsContent::getId).toArray(Long[]::new);
                    List<CmsContentAttribute> attributeList = attributeService.getEntitys(ids);
                    KeywordsConfig config = contentConfigComponent.getKeywordsConfig(site.getId());
                    Map<Object, CmsContentAttribute> attributeMap = CommonUtils.listToMap(attributeList, k -> k.getContentId());
                    consumer = e -> {
                        ClickStatistics statistics = statisticsComponent.getContentStatistics(e.getId());
                        if (null != statistics) {
                            e.setClicks(e.getClicks() + statistics.getClicks());
                        }
                        CmsUrlUtils.initContentUrl(site, e);
                        fileUploadComponent.initContentCover(site, e);
                        e.setAttribute(ExtendUtils.getAttributeMap(attributeMap.get(e.getId()), config));
                    };
                } else {
                    consumer = e -> {
                        ClickStatistics statistics = statisticsComponent.getContentStatistics(e.getId());
                        if (null != statistics) {
                            e.setClicks(e.getClicks() + statistics.getClicks());
                        }
                        CmsUrlUtils.initContentUrl(site, e);
                        fileUploadComponent.initContentCover(site, e);
                    };
                }
                list.forEach(consumer);
            }
            return page;
        } catch (Exception e) {
            log.error(e.getMessage());
            PageHandler page = new PageHandler(pageIndex, pageSize);
            page.setList(Collections.emptyList());
            return page;
        }
    }

    @Override
    public boolean supportAdvanced() {
        return true;
    }

    @Resource
    private CmsContentService service;
}