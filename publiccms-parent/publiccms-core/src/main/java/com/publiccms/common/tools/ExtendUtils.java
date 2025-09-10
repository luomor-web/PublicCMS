package com.publiccms.common.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import com.publiccms.common.api.Config;
import com.publiccms.common.constants.Constants;
import com.publiccms.entities.cms.CmsCategoryAttribute;
import com.publiccms.entities.cms.CmsContentAttribute;
import com.publiccms.entities.cms.CmsPlaceAttribute;
import com.publiccms.entities.sys.SysExtendField;
import com.publiccms.entities.sys.SysUserAttribute;
import com.publiccms.logic.component.config.ContentConfigComponent.KeywordsConfig;

/**
 *
 * ExtendUtils
 * 
 */
public class ExtendUtils {
    private ExtendUtils() {
    }

    public static final Pattern HTML_PATTERN = Pattern.compile(">([^<]+)</[^a|A]");

    /**
     * @param attribute
     * @return extent map
     */
    public static Map<String, String> getAttributeMap(@Nullable CmsCategoryAttribute attribute) {
        if (null == attribute) {
            return Collections.emptyMap();
        } else {
            Map<String, String> map = getExtendMap(attribute.getData());
            map.put("title", attribute.getTitle());
            map.put("keywords", attribute.getKeywords());
            map.put("description", attribute.getDescription());
            return map;
        }
    }

    /**
     * @param attribute
     * @param keywordsConfig
     * @return extent map
     */
    public static Map<String, String> getAttributeMap(@Nullable CmsContentAttribute attribute,
            @Nullable KeywordsConfig keywordsConfig) {
        if (null == attribute) {
            return Collections.emptyMap();
        } else {
            Map<String, String> map = getExtendMap(attribute.getData());
            map.put("text", replaceText(attribute.getText(), keywordsConfig));
            map.put("source", attribute.getSource());
            map.put("sourceUrl", attribute.getSourceUrl());
            map.put("wordCount", String.valueOf(attribute.getWordCount()));
            map.put("minPrice", String.valueOf(attribute.getMinPrice()));
            map.put("maxPrice", String.valueOf(attribute.getMaxPrice()));
            return map;
        }
    }

    private static String replaceEachOnce(final String text, final String[] searchList, final String[] replacementList,
            AtomicInteger counter) {
        if (StringUtils.isEmpty(text) || ArrayUtils.isEmpty(searchList) || ArrayUtils.isEmpty(replacementList)
                || null != counter && counter.get() <= 0) {
            return text;
        }

        final int searchLength = searchList.length;
        final int replacementLength = replacementList.length;
        if (searchLength != replacementLength) {
            return text;
        }

        final boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];
        int textIndex = -1;
        int replaceIndex = -1;
        int tempIndex = -1;
        for (int i = 0; i < searchLength; i++) {
            if (noMoreMatchesForReplIndex[i] || searchList[i] == null || searchList[i].isEmpty() || replacementList[i] == null) {
                continue;
            }
            tempIndex = Strings.CI.indexOf(text, searchList[i]);
            if (tempIndex == -1) {
                noMoreMatchesForReplIndex[i] = true;
            } else if (textIndex == -1 || tempIndex < textIndex) {
                textIndex = tempIndex;
                replaceIndex = i;
            }
        }
        if (textIndex == -1) {
            return text;
        }
        int start = 0;
        int increase = 0;
        for (int i = 0; i < searchList.length; i++) {
            if (searchList[i] == null || searchList[i].isEmpty() || replacementList[i] == null) {
                continue;
            }
            final int greater = replacementList[i].length() - searchList[i].length();
            if (greater > 0) {
                increase += greater;
            }
        }
        increase = Math.min(increase, text.length() / 5);
        final StringBuilder buf = new StringBuilder(text.length() + increase);
        while (textIndex != -1 && (null == counter || counter.getAndDecrement() > 0)) {
            for (int i = start; i < textIndex; i++) {
                buf.append(text.charAt(i));
            }
            buf.append(replacementList[replaceIndex]);
            replacementList[replaceIndex] = null;
            start = textIndex + searchList[replaceIndex].length();
            textIndex = -1;
            replaceIndex = -1;
            for (int i = 0; i < searchLength; i++) {
                if (noMoreMatchesForReplIndex[i] || searchList[i] == null || searchList[i].isEmpty()
                        || replacementList[i] == null) {
                    continue;
                }
                tempIndex = Strings.CI.indexOf(text, searchList[i], start);
                if (tempIndex == -1) {
                    noMoreMatchesForReplIndex[i] = true;
                } else if (textIndex == -1 || tempIndex < textIndex) {
                    textIndex = tempIndex;
                    replaceIndex = i;
                }
            }
        }
        final int textLength = text.length();
        for (int i = start; i < textLength; i++) {
            buf.append(text.charAt(i));
        }
        return buf.toString();
    }

    /**
     * @param text
     * @param keywordsConfig
     * @return
     */
    public static String replaceSensitive(@Nullable String text, @Nullable KeywordsConfig keywordsConfig) {
        if (null != keywordsConfig && CommonUtils.notEmpty(text) && CommonUtils.notEmpty(keywordsConfig.getSensitiveWords())
                && CommonUtils.notEmpty(keywordsConfig.getReplaceWords())) {
            return StringUtils.replaceEach(text, keywordsConfig.getSensitiveWords(), keywordsConfig.getReplaceWords());
        } else {
            return text;
        }
    }

    /**
     * @param html
     * @param keywordsConfig
     * @return
     */
    public static String replaceText(@Nullable String html, @Nullable KeywordsConfig keywordsConfig) {
        if (null != keywordsConfig && CommonUtils.notEmpty(html)) {
            if (CommonUtils.notEmpty(keywordsConfig.getWords()) && CommonUtils.notEmpty(keywordsConfig.getWordWithUrls())) {
                Matcher matcher = HTML_PATTERN.matcher(html);
                StringBuilder sb = new StringBuilder();
                int end = 0;
                AtomicInteger counter = new AtomicInteger(keywordsConfig.getMax());
                String[] replacementList = null == keywordsConfig.getWordWithUrls() ? null
                        : Arrays.copyOf(keywordsConfig.getWordWithUrls(), keywordsConfig.getWordWithUrls().length);
                while (matcher.find() && counter.get() > 0) {
                    String temp = matcher.group();
                    sb.append(html.substring(end, matcher.start())).append(">");
                    sb.append(replaceEachOnce(matcher.group(1), keywordsConfig.getWords(), replacementList, counter));
                    sb.append(temp.substring(temp.length() - 3, temp.length()));
                    end = matcher.end();
                }
                if (end < html.length()) {
                    sb.append(html.substring(end, html.length()));
                }
                return replaceSensitive(sb.toString(), keywordsConfig);
            } else {
                return replaceSensitive(html, keywordsConfig);
            }
        } else {
            return html;
        }
    }

    /**
     * @param attribute
     * @return extent map
     */
    public static Map<String, String> getAttributeMap(@Nullable CmsPlaceAttribute attribute) {
        if (null == attribute) {
            return Collections.emptyMap();
        } else {
            return getExtendMap(attribute.getData());
        }
    }

    /**
     * @param attribute
     * @return extent map
     */
    public static Map<String, String> getAttributeMap(@Nullable SysUserAttribute attribute) {
        if (null == attribute) {
            return Collections.emptyMap();
        } else {
            return getExtendMap(attribute.getData());
        }
    }

    /**
     * @param data
     * @return extent map
     */
    public static Map<String, String> getExtendMap(@Nullable String data) {
        if (CommonUtils.notEmpty(data)) {
            try {
                return Constants.objectMapper.readValue(data, Constants.objectMapper.getTypeFactory()
                        .constructMapType(LinkedHashMap.class, String.class, String.class));
            } catch (IOException | ClassCastException e) {
                return new LinkedHashMap<>();
            }
        }
        return new LinkedHashMap<>();
    }

    /**
     * @param map
     * @param sitePath
     * @param extendFieldListArrays
     * @return extend string
     */
    @SafeVarargs
    public static String getExtendString(Map<String, String> map, @Nullable String sitePath,
            List<SysExtendField>... extendFieldListArrays) {
        return getExtendString(map, sitePath, null, extendFieldListArrays);
    }

    /**
     * @param map
     * @param sitePath
     * @param notSafeKeys
     * @param searchableConsumer
     * @param extendFieldList
     */
    public static void decodeField(Map<String, String> map, @Nullable String sitePath,
            @Nullable List<SysExtendField> extendFieldList) {
        Set<String> notSafeKeys = new HashSet<>();
        notSafeKeys.addAll(map.keySet());
        decodeField(map, sitePath, notSafeKeys, null, extendFieldList);
    }

    /**
     * @param map
     * @param sitePath
     * @param notSafeKeys
     * @param searchableConsumer
     * @param extendFieldList
     */
    private static void decodeField(Map<String, String> map, String sitePath, Set<String> notSafeKeys,
            BiConsumer<SysExtendField, String> searchableConsumer, List<SysExtendField> extendFieldList) {
        if (CommonUtils.notEmpty(extendFieldList)) {
            for (SysExtendField extend : extendFieldList) {
                notSafeKeys.remove(extend.getId().getCode());
                String value = map.get(extend.getId().getCode());
                if (null == value) {
                    if (null != extend.getDefaultValue()) {
                        map.put(extend.getId().getCode(), value);
                    }
                } else if (null != extend.getMaxlength()) {
                    if (ArrayUtils.contains(Config.INPUT_TYPE_EDITORS, extend.getInputType())) {
                        value = HtmlUtils.cleanUnsafeHtml(
                                HtmlUtils.keep(new String(VerificationUtils.base64Decode(value), StandardCharsets.UTF_8),
                                        extend.getMaxlength()),
                                sitePath);
                    } else {
                        value = CommonUtils.keep(value, extend.getMaxlength(), null);
                    }
                    map.put(extend.getId().getCode(), value);
                } else {
                    if (ArrayUtils.contains(Config.INPUT_TYPE_EDITORS, extend.getInputType())) {
                        value = HtmlUtils.cleanUnsafeHtml(
                                new String(VerificationUtils.base64Decode(value), StandardCharsets.UTF_8), sitePath);
                        map.put(extend.getId().getCode(), value);
                    }
                }
                if (extend.isSearchable() && null != searchableConsumer && null != value) {
                    searchableConsumer.accept(extend, value);
                }
            }
        }
    }

    /**
     * @param map
     * @param sitePath
     * @param searchableConsumer
     * @param extendFieldListArrays
     * @return extend string
     */
    @SafeVarargs
    public static String getExtendString(@Nullable Map<String, String> map, @Nullable String sitePath,
            @Nullable BiConsumer<SysExtendField, String> searchableConsumer,
            @Nullable List<SysExtendField>... extendFieldListArrays) {
        if (CommonUtils.notEmpty(extendFieldListArrays) && null != map) {
            Set<String> notSafeKeys = new HashSet<>();
            notSafeKeys.addAll(map.keySet());
            for (List<SysExtendField> extendFieldList : extendFieldListArrays) {
                decodeField(map, sitePath, notSafeKeys, searchableConsumer, extendFieldList);
            }
            if (!notSafeKeys.isEmpty()) {
                for (String key : notSafeKeys) {
                    map.remove(key);
                }
            }
            try {
                return Constants.objectMapper.writeValueAsString(map);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }
    
    public static String getExtendString(@Nullable Map<String, String> map) {
        if (null != map) {
            try {
                return Constants.objectMapper.writeValueAsString(map);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }
}