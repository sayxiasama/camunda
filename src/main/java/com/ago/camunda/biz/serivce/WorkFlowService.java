package com.ago.camunda.biz.serivce;

import org.camunda.bpm.engine.repository.Deployment;

import java.io.InputStream;
import java.util.Map;

public interface WorkFlowService {

    // 部署流程定义.

    /**
     * 流方式部署:
     * @param is 输入流
     * @param resourceName 资源名称
     * @param source 资源信息
     * @param tenantId 租户id
     * @return 部署id
     */
    String deployInstance(InputStream is , String resourceName , String source , String name , String tenantId);

    // 提交(开启一个流程实例).
    void submit(String deployId ,String tenantId);

    // 审批.
    Map<String,Object> approval(Boolean result , String comment);

    // 委托(转交)。
    Map<String,Object> delGateTask(String taskId , String userId);

    // 撤回/修改.
    void revoke(String taskId);

    // 催办(待定 流程内实现 or 业务实现)

    // 启用停用(待定 流程内实现 or 业务实现).


    //查询待办任务;

    //查询历史已完成任务;


}
