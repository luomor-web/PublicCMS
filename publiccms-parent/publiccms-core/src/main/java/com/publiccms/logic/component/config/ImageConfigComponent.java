package com.publiccms.logic.component.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.publiccms.common.api.Config;
import com.publiccms.common.constants.CommonConstants;
import com.publiccms.common.constants.Constants;
import com.publiccms.common.tools.CommonUtils;
import com.publiccms.common.tools.LanguagesUtils;
import com.publiccms.entities.sys.SysExtendField;
import com.publiccms.entities.sys.SysSite;

/**
 *
 * ImageConfigComponent 图片配置组件
 *
 */
@Component
public class ImageConfigComponent implements Config {

    /**
     *
     */
    public static final String CONFIG_CODE = "image";

    /**
     *
     */
    public static final String CONFIG_CODE_DESCRIPTION = CommonUtils.joinString(CONFIGPREFIX, CONFIG_CODE);

    /**
     * max image width
     */
    public static final String CONFIG_MAX_IMAGE_WIDTH = "max_image_width";

    @Override
    public String getCode(short siteId, boolean showAll) {
        return CONFIG_CODE;
    }

    /**
     * @param locale
     * @return
     */
    @Override
    public String getCodeDescription(Locale locale) {
        return LanguagesUtils.getMessage(CommonConstants.applicationContext, locale, CONFIG_CODE_DESCRIPTION);
    }

    @Override
    public List<SysExtendField> getExtendFieldList(SysSite site, Locale locale) {
        List<SysExtendField> extendFieldList = new ArrayList<>();

        extendFieldList
                .add(new SysExtendField(CONFIG_MAX_IMAGE_WIDTH, INPUTTYPE_NUMBER, getMessage(locale, CommonUtils.joinString(CONFIG_CODE_DESCRIPTION, Constants.DOT, CONFIG_MAX_IMAGE_WIDTH)), null));

        return extendFieldList;
    }

    @Override
    public boolean exportable() {
        return true;
    }
}
