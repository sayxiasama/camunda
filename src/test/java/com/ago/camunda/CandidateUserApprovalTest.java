package com.ago.camunda;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class CandidateUserApprovalTest {

    private static final Logger logger = LoggerFactory.getLogger(CandidateUserApprovalTest.class);

    private static final String TENANT_ONE = "41";

    @Autowired
    private ProcessEngine processEngine;



    @ParameterizedTest
    @CsvSource({"1111"})
    void start(String deployId){

        RepositoryService repositoryService = processEngine.getRepositoryService();

        Assert.assertNotNull(processEngine);

        Deployment deploy = getDeployment(repositoryService);

        RuntimeService runtimeService = processEngine.getRuntimeService();

        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploy.getId()).tenantIdIn(TENANT_ONE).singleResult();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).tenantIdIn(TENANT_ONE).singleResult();

        logger.info(processDefinition.getId());
        logger.info(processDefinition.getKey());
        logger.info(processDefinition.getName());
        logger.info(processDefinition.getCategory());

        VariableMapImpl variableMap = new VariableMapImpl();

        variableMap.put("userId","ago");

        ProcessInstance execute = runtimeService.createProcessInstanceByKey(processDefinition.getKey()).businessKey("ago-leave").processDefinitionTenantId(TENANT_ONE).setVariables(variableMap).execute();

        logger.info("??????id : {} ", execute.getId());

        logger.info("??????key : {} ", execute.getBusinessKey());

        logger.info("?????????id : {} ", execute.getRootProcessInstanceId());

        logger.info("????????????id : {} ", execute.getProcessDefinitionId());

        logger.info("??????id : {} " , execute.getProcessInstanceId());
    }



    private Deployment getDeployment(RepositoryService repositoryService) {

        Deployment deploy = repositoryService.createDeployment().name("????????????").addClasspathResource("leave.bpmn").tenantId("41").deploy();



        return deploy;
    }
}
