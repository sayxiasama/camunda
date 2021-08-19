package com.ago.camunda;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.impl.TenantQueryImpl;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.persistence.entity.TenantEntity;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.RestartProcessInstanceBuilder;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
public class CamundaTest {

    private static final Logger logger = LoggerFactory.getLogger(CamundaTest.class);

//    @Autowired
//    private RuntimeService runtimeService;
//
//    @Autowired
//    private RepositoryService repositoryService;
//
//    @Autowired
//    private TaskService taskService;

    @Autowired
    private ProcessEngine processEngine;

    @Test
    public void start(){

        Assert.assertNotNull(processEngine);

        RepositoryService repositoryService = processEngine.getRepositoryService();

        RuntimeService runtimeService = processEngine.getRuntimeService();

        Deployment deploy = repositoryService.createDeployment().name("请假流程").addClasspathResource("leave.bpmn").tenantId("41").deploy();

        VariableMap variableMap = new VariableMapImpl();

        variableMap.put("primaryId","0827");
        variableMap.put("userId","ago");
        variableMap.put("approval_1","lgd");
        variableMap.put("approval_2","gh");

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();

        Assertions.assertNotNull(processDefinition);

        logger.info(" processDefinition Id : {}  - processDefinition Key : {}" ,processDefinition.getId(),processDefinition.getKey());

        ProcessInstance leave = runtimeService.createProcessInstanceByKey(processDefinition.getKey()).processDefinitionTenantId("41").businessKey("Ele_leave").setVariables(variableMap).execute();

        System.out.println(leave.getId());
        System.out.println(leave.getBusinessKey());
        System.out.println(leave.getRootProcessInstanceId());
        System.out.println(leave.getProcessDefinitionId());
    }

    @Test
    void query(){

        TaskService taskService = processEngine.getTaskService();

        taskService.createTaskQuery().taskAssignee("ago").list().forEach(task -> {
//        taskService.createTaskQuery().list().forEach(task ->{
            System.out.println("代办任务ID:"+task.getId());

            System.out.println("代办任务name:"+task.getName());

            System.out.println("代办任务创建时间:"+task.getCreateTime());

            System.out.println("代办任务办理人:"+task.getAssignee());

            System.out.println("流程实例ID:"+task.getProcessInstanceId());

            System.out.println("执行对象ID:"+task.getExecutionId());

        });
    }

    /**
     * 非人工干预流程完成测试
     * @param taskId
     */
    @ParameterizedTest
    @CsvSource({"6b87d288-0011-11ec-bb3d-c2d21dce0b69"})
    void complete(String taskId){
        TaskService taskService = processEngine.getTaskService();
        IdentityService identityService = processEngine.getIdentityService();
        Task t = taskService.createTaskQuery().taskId(taskId).singleResult();

        Authentication authentication = new Authentication("39", null,Arrays.asList("41","32","11"));

        identityService.setAuthentication(authentication);

        Authentication currentAuthentication = identityService.getCurrentAuthentication();

        logger.info("角色 : {}",currentAuthentication.getGroupIds());

        logger.info("用户 : {}",currentAuthentication.getUserId());

        logger.info("租户 : {}",currentAuthentication.getTenantIds());

        Comment comment = taskService.createComment(taskId, t.getProcessInstanceId(), "测试自定义用户集成");

        System.out.println(comment.getId());

        taskService.complete(taskId);

//
//        identityService.createTenantQuery().tenantId("41").list().forEach(x ->{
//
//            System.out.println(x.getId());
//            System.out.println(x.getName());
//
//        });


    }

    /**
     * 人工干预任务完成测试
     * @param taskId
     */
    @ParameterizedTest
    @CsvSource({"9db4b187-ff0b-11eb-8903-c2d21dce0b69"})
    void competePerson(String taskId){
        TaskService taskService = processEngine.getTaskService();
        taskService.setVariable(taskId,"approval",true);
        taskService.complete(taskId);
    }


    @ParameterizedTest
    @CsvSource({"9db4b187-ff0b-11eb-8903-c2d21dce0b69"})
    void queryTaskDetail(String taskId){

        TaskService taskService = processEngine.getTaskService();

         taskService.getVariables(taskId).forEach((key, value) -> {

             System.out.println(key +":"+ value);

         });
    }

    @Test
    void historyQuery(){

        HistoryService historyService = processEngine.getHistoryService();

        historyService.createHistoricTaskInstanceQuery().taskAssignee("ago").finished().list().forEach(x -> {

            System.out.println(x.getAssignee());

            System.out.println(x.getName());

            System.out.println(x.getStartTime());

            System.out.println(x.getEndTime());

        });
    }


    @Test
    void followUpTask(){

        TaskService taskService = processEngine.getTaskService();

    }
}
