package com.ago.camunda.delegate;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cmd.DeleteTaskCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskManager;

import java.util.Collection;

public class TaskCommand extends DeleteTaskCmd {


    public TaskCommand(String taskId, String deleteReason, boolean cascade) {
        super(taskId, deleteReason, cascade);
    }

    public TaskCommand(Collection<String> taskIds, String deleteReason, boolean cascade) {
        super(taskIds, deleteReason, cascade);
    }


    @Override
    protected void deleteTask(String taskId, CommandContext commandContext) {
        TaskManager taskManager = commandContext.getTaskManager();
        TaskEntity task = taskManager.findTaskById(taskId);
        if (task != null) {

//            if (task.getExecutionId() != null) {
//                throw new ProcessEngineException("The task cannot be deleted because is part of a running process");
//            }
//
//            if (task.getCaseExecutionId() != null) {
//                throw new ProcessEngineException("The task cannot be deleted because is part of a running case instance");
//            }

            this.checkDeleteTask(task, commandContext);
            String reason = this.deleteReason != null && this.deleteReason.length() != 0 ? this.deleteReason : "deleted";
            task.delete(reason, this.cascade);
        } else if (this.cascade) {
            Context.getCommandContext().getHistoricTaskInstanceManager().deleteHistoricTaskInstanceById(taskId);
        }
    }
}
