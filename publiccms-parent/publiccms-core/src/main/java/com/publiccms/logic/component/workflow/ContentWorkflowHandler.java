package com.publiccms.logic.component.workflow;

import javax.annotation.Resource;

import com.publiccms.common.api.Config;
import com.publiccms.common.base.AbstractLongWorkflowHandler;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.entities.sys.SysWorkflowProcess;
import com.publiccms.entities.sys.SysWorkflowProcessHistory;
import com.publiccms.logic.service.cms.CmsContentService;

public class ContentWorkflowHandler extends AbstractLongWorkflowHandler {

    @Override
    public String getItemType() {
        return Config.INPUTTYPE_CONTENT;
    }

    @Override
    public void accept(SysWorkflowProcess entity, SysUser user, SysWorkflowProcessHistory history, Long itemId) {
        service.check(entity.getSiteId(), user, itemId, false);
    }

    @Override
    public void reject(SysWorkflowProcess entity, SysUser user, SysWorkflowProcessHistory history, Long itemId) {
        service.reject(entity.getSiteId(), user, itemId);
    }

    @Resource
    private CmsContentService service;
}
