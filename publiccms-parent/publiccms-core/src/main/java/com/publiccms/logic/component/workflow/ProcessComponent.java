package com.publiccms.logic.component.workflow;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publiccms.common.api.WorkflowHandler;
import com.publiccms.entities.sys.SysUser;
import com.publiccms.entities.sys.SysWorkflowProcess;
import com.publiccms.entities.sys.SysWorkflowProcessHistory;
import com.publiccms.logic.service.sys.SysWorkflowProcessHistoryService;

@Component
public class ProcessComponent {
    private Map<String, WorkflowHandler<?>> workflowHandlerMap = new HashMap<>();

    /**
     * @param <T>
     * @param entity
     * @param user
     * @param history
     */
    public <T> void process(SysWorkflowProcess entity, SysUser user, SysWorkflowProcessHistory history) {
        @SuppressWarnings("unchecked")
        WorkflowHandler<T> workflowHandler = (WorkflowHandler<T>) workflowHandlerMap.get(entity.getItemType());
        if (null != workflowHandler) {
            if (SysWorkflowProcessHistoryService.OPERATE_ACCEPT.equalsIgnoreCase(history.getOperate())) {
                workflowHandler.accept(entity, user, history, workflowHandler.getItemId(entity.getItemId()));
            } else if (SysWorkflowProcessHistoryService.OPERATE_REJECT.equalsIgnoreCase(history.getOperate())) {
                workflowHandler.reject(entity, user, history, workflowHandler.getItemId(entity.getItemId()));
            }

        }
    }

    /**
     * @param <T>
     * @param workflowHandlerList
     */
    @Autowired
    public <T> void setParameterTypeHandlerMap(List<WorkflowHandler<T>> workflowHandlerList) {
        this.workflowHandlerMap = new LinkedHashMap<>();
        for (WorkflowHandler<T> workflowHandler : workflowHandlerList) {
            this.workflowHandlerMap.put(workflowHandler.getItemType(), workflowHandler);
        }
    }
}
