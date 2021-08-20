package com.ago.camunda;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@SpringBootTest
public class CountersignTest {

    private static final Logger logger = LoggerFactory.getLogger(CountersignTest.class);

    @Autowired
    private ProcessEngine processEngine;


    private static final String TENANT_ONE = "41";

    private static final String TENANT_TWO = "42";



    @Test
    void countSign(){

        //静态部署
        RepositoryService repositoryService = processEngine.getRepositoryService();

        Deployment deploy = repositoryService.createDeployment().addClasspathResource("leave.bpmn").name("会签测试").tenantId(TENANT_ONE).deploy();

        logger.info("流程部署Id : {} ", deploy.getId());
        logger.info("流程部署名称 : {} ", deploy.getName());
        logger.info("流程部署公司 : {} ", deploy.getTenantId());


        //流程启动

        RuntimeService runtimeService = processEngine.getRuntimeService();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).tenantIdIn(TENANT_ONE).singleResult();

        VariableMapImpl variableMap = new VariableMapImpl();

        variableMap.put("primaryId","0827");
        variableMap.put("userId","ago");
        variableMap.put("approvalOne","lgd");
        variableMap.put("approvalTwo","gh");

        variableMap.put("tenant_id",TENANT_ONE);
        variableMap.put("approvalList", Arrays.asList("Anna","Ame"));
        variableMap.put("approval_list_two",Arrays.asList("lgd","gh"));

        ProcessInstance processInstance = runtimeService.createProcessInstanceByKey(processDefinition.getKey()).processDefinitionTenantId(TENANT_ONE).setVariables(variableMap).execute();

        logger.info("流程实例id ： {} " , processInstance.getId());

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
    @CsvSource({"f2492983-015d-11ec-832c-c2d21dce0b69"})
    void complete(String taskId){
        TaskService taskService = processEngine.getTaskService();
        VariableMapImpl variableMap = new VariableMapImpl();
        variableMap.put("formId","9527");
        processEngine.getTaskService().complete(taskId , variableMap);
    }

    /**
     * 人工干预任务完成测试
     * @param taskId
     */
    @ParameterizedTest
    @CsvSource({"bc111b33-0094-11ec-a280-c2d21dce0b69"})
    void competePerson(String taskId){
        TaskService taskService = processEngine.getTaskService();

        IdentityService identityService = processEngine.getIdentityService();

        identityService.setAuthentication(new Authentication("39",null, Collections.singletonList(TENANT_ONE)));

        Task t = taskService.createTaskQuery().taskId(taskId).singleResult();

        taskService.createComment(taskId,t.getProcessInstanceId(),"同意同意同意");

        taskService.setVariable(taskId,"approval",true);

        taskService.complete(taskId);
    }

    @ParameterizedTest
    @CsvSource({"652d3dd8-ffd6-11eb-8e61-c2d21dce0b69,委托工具人1号,将任务委托给工具人1号,39"})
    void delegateTask(String taskId,String delegateUser,String comment){
        TaskService taskService = processEngine.getTaskService();

        Task t = taskService.createTaskQuery().taskId(taskId).singleResult();

        IdentityService identityService = processEngine.getIdentityService();

        identityService.setAuthenticatedUserId("39");

        taskService.createComment(taskId,t.getProcessInstanceId(),comment);
        taskService.delegateTask(taskId,delegateUser);

        taskService.createTaskQuery().taskAssignee(delegateUser).list().forEach(task -> {
        System.out.println("代办任务ID:"+task.getId());

        System.out.println("代办任务name:"+task.getName());

        System.out.println("代办任务创建时间:"+task.getCreateTime());

        System.out.println("代办任务办理人:"+task.getAssignee());

        System.out.println("流程实例ID:"+task.getProcessInstanceId());

        System.out.println("执行对象ID:"+task.getExecutionId());

        });
    }

    @ParameterizedTest
    @CsvSource({"652d3dd8-ffd6-11eb-8e61-c2d21dce0b69"})
    void resolveTask(String taskId){
        VariableMapImpl variables = new VariableMapImpl();
        variables.put("approval",true);
        TaskService taskService = processEngine.getTaskService();

        taskService.createTaskQuery().taskAssignee("多级委托测试1号").list().forEach(task -> {

            if (Optional.ofNullable(task.getOwner()).isPresent()) {
                DelegationState delegationState = task.getDelegationState();
                if (("RESOLVED").equalsIgnoreCase(delegationState.name())) {
                    System.out.println("此委托任务已是完结状态");
                } else if (delegationState.toString().equals("PENDING")) {
                    //如果是委托任务需要做处理
                    taskService.resolveTask(taskId);
                    taskService.complete(taskId, variables);
                } else {
                    System.out.println("此任务不是委托任务");
                }
            }

        });

    }

    @ParameterizedTest
    @CsvSource({"652d3dd8-ffd6-11eb-8e61-c2d21dce0b69,多级委托测试1号"})
    void multiDelegate(String taskId,String multiDelegateTwo){
        VariableMapImpl variables = new VariableMapImpl();
        variables.put("approval",true);
        TaskService taskService = processEngine.getTaskService();

        taskService.createTaskQuery().taskAssignee("委托工具人1号").list().forEach(task -> {

            if (Optional.ofNullable(task.getOwner()).isPresent()) {
                DelegationState delegationState = task.getDelegationState();
                if (("RESOLVED").equalsIgnoreCase(delegationState.name())) {
                    logger.info("  task has been resolved");
                    System.out.println("");
                } else if (delegationState.toString().equals("PENDING")) {
                    logger.info(" resolve task ");
                    //如果是委托任务需要做处理
                    taskService.delegateTask(taskId,multiDelegateTwo);
//                    taskService.resolveTask(taskId);
//                    taskService.complete(taskId,variables);
                } else {
                    logger.info(" current task is not delegate Task");
                }
            }

        });
    }



    @Test
    void queryHis(){

        HistoryService historyService = processEngine.getHistoryService();

        historyService.createHistoricProcessInstanceQuery().processInstanceId("405860d9-ffd6-11eb-bec8-c2d21dce0b69").list().forEach(s -> {
            logger.info("id : {}",s.getId());
            logger.info("name : {}", s.getProcessDefinitionName());

        });


    }

}
