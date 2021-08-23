package com.ago.camunda.biz.serivce.impl;

import com.ago.camunda.biz.serivce.WorkFlowService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class WorkFlowServiceImpl implements WorkFlowService {

    private static final Logger logger = LoggerFactory.getLogger(WorkFlowServiceImpl.class);

    private final RepositoryService repositoryService;

    private final RuntimeService runtimeService;

    public WorkFlowServiceImpl(RepositoryService repositoryService, RuntimeService runtimeService) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
    }

    // TODO 暂时提供一个以文件流形式部署的方法, 扩展请通过重载的方式;
    @Override
    public String deployInstance(InputStream is, String resourceName, String source, String name , String tenantId) {

        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        Deployment deploy = deploymentBuilder.name(name).source(source).tenantId(tenantId)
                .addInputStream(resourceName,is)
                .deploy();

        return Optional.ofNullable(deploy).isPresent() ? deploy.getId() : "";
    }

    @Override
    public void submit(String deployId , String tenantId ) {

        //根据部署id, 启动一个流程实例;
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployId).tenantIdIn(tenantId).latestVersion().singleResult();
        runtimeService.startProcessInstanceByKey(processDefinition.getKey(),new VariableMapImpl());


    }

    @Override
    public Map<String, Object> approval(Boolean result, String comment) {
        return null;
    }

    @Override
    public Map<String, Object> delGateTask(String taskId, String userId) {
        return null;
    }

    @Override
    public void revoke(String taskId) {

    }
}
