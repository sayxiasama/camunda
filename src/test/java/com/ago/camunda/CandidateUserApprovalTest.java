package com.ago.camunda;

import com.ago.camunda.biz.ActSettingMapper;
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

    @Autowired
    private ActSettingMapper actSettingMapper;


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

        logger.info("实例id : {} ", execute.getId());

        logger.info("实例key : {} ", execute.getBusinessKey());

        logger.info("根实例id : {} ", execute.getRootProcessInstanceId());

        logger.info("实例定义id : {} ", execute.getProcessDefinitionId());

        logger.info("实例id : {} " , execute.getProcessInstanceId());
    }



    private Deployment getDeployment(RepositoryService repositoryService) {

        Deployment deploy = repositoryService.createDeployment().name("请假流程").addClasspathResource("leave.bpmn").tenantId("41").deploy();



        return deploy;
    }


    @Test
    public void query(){


        actSettingMapper.insertModel("b50769df-00c2-11ec-bc6e-c2d21dce0b69");


        Map<String, Object> query = actSettingMapper.query();

        System.out.println(query);
    }
}
