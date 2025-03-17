package com.publiccms.common.api;

import com.publiccms.entities.sys.SysUser;
import com.publiccms.entities.sys.SysWorkflowProcess;
import com.publiccms.entities.sys.SysWorkflowProcessHistory;

public interface WorkflowHandler<T> {
    public String getItemType();

    public T getItemId(String value);

    public void accept(SysWorkflowProcess entity, SysUser user, SysWorkflowProcessHistory history, T itemId);

    public void reject(SysWorkflowProcess entity, SysUser user, SysWorkflowProcessHistory history, T itemId);
}
