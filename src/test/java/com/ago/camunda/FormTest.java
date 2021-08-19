package com.ago.camunda;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FormTest {

    private static final Logger logger = LoggerFactory.getLogger(FormTest.class);

    @Autowired
    private ProcessEngine processEngine;

    @Test
    void formStart(){

        //deploy
        RepositoryService repositoryService = processEngine.getRepositoryService();

        Deployment deploy = repositoryService.createDeployment().name("自定义流程").addClasspathResource("leave.bpmn").tenantId("fm").deploy();

        logger.info("部署流程id : {} ", deploy.getId());
        logger.info("部署流程名称 : {} ", deploy.getName());
        logger.info("部署流程时间 : {} ", deploy.getDeploymentTime());
        logger.info("部署流程公司 : {} ", deploy.getTenantId());


//        Deployment deploy = repositoryService.createDeploymentQuery().deploymentName("自定义流程").tenantIdIn("fm").singleResult();


        //start instance
        RuntimeService runtimeService = processEngine.getRuntimeService();

        FormService formService = processEngine.getFormService();
        VariableMapImpl variableMap = new VariableMapImpl();

        variableMap.put("primaryId","0827");
        variableMap.put("userId","ago");
        variableMap.put("approval_1","lgd");
        variableMap.put("approval_2","gh");



        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();

        Assertions.assertNotNull(processDefinition);

        logger.info("流程定义id {} ",processDefinition.getId());
        logger.info("流程定义名称 {} ",processDefinition.getName());
        logger.info("流程定义key {} ",processDefinition.getKey());

        logger.info("runtime start");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinition.getKey(), variableMap);
        Assertions.assertNotNull(processInstance);

        ProcessInstance custom = formService.submitStartForm(processDefinition.getKey(), variableMap);

        System.out.println(custom.getId());
        System.out.println(custom.getBusinessKey());
        System.out.println(custom.getRootProcessInstanceId());
        System.out.println(custom.getProcessDefinitionId());
    }




    @ParameterizedTest
    @CsvSource({""})
    void complete(String taskId){

        TaskService taskService = processEngine.getTaskService();
        VariableMapImpl variableMap = new VariableMapImpl();
        variableMap.put("name","free");
        variableMap.put("age",18);
        variableMap.put("address","上海市");
        variableMap.put("startDate","2021-01-01");
        variableMap.put("endDate","2021-10-10");
        taskService.setVariables("apply_form",variableMap);
        taskService.complete(taskId);
    }


    @Test
    void query(){

        String deploy = "3d1b3102-ff2f-11eb-a1f6-c2d21dce0b69";

        ProcessDefinition processDefinition = processEngine.getRepositoryService().createProcessDefinitionQuery().deploymentId(deploy).singleResult();

        Assertions.assertNotNull(processDefinition);

        logger.info(" processDefinition Id : {}  - processDefinition Key : {}" ,processDefinition.getId(),processDefinition.getKey());

        System.out.println("111111");

    }
}
