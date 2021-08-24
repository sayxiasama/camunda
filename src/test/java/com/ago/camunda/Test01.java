package com.ago.camunda;

import com.ago.camunda.biz.domain.WorkFlowProcessVariable;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.javax.el.ExpressionFactory;
import org.camunda.bpm.engine.impl.javax.el.ValueExpression;
import org.camunda.bpm.engine.impl.juel.ExpressionFactoryImpl;
import org.camunda.bpm.engine.impl.juel.SimpleContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.*;

@SpringBootTest
public class Test01 {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private TaskService taskService;
    @Resource
    private HistoryService historyService;

    @Autowired
    private ManagementService managementService;


    private static final String USER_ID = "39";

    private static final String TENANT_ID = "41";

    private static final List<String> MULTI_INSTANCE = Arrays.asList("Anna","Ame");

    private static final List<String> MULTI_INSTANCE_TWO = Arrays.asList("LGD","GH");

    /**
     * 部署流程,测试撤回
     */
    @Test
    public void repositoryDeploy(){
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("leave.bpmn")
                .name("测试撤回-1")
                .tenantId(TENANT_ID)
                .deploy();
        System.out.println("部署ID:"+deploy.getId());
        System.out.println("部署名称"+deploy.getName());

    }

    /**
     * 发布流程
     */
    @Test
    public void runtimeRelease(){

        VariableMapImpl variableMap = new VariableMapImpl();

        variableMap.put("userId",USER_ID);

        ProcessInstance pi = runtimeService.createProcessInstanceByKey("leave").processDefinitionTenantId(TENANT_ID).setVariables(variableMap).execute();
        System.out.println("流程实例ID:"+pi.getId());
        System.out.println("流程定义ID:"+pi.getProcessDefinitionId());


        BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(pi.getProcessDefinitionId());


        System.out.println(Bpmn.convertToString(bpmnModelInstance));
    }

    /**
     * 查询及完成任务
     */
    @Test
    public void taskQueryComplete(){
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(USER_ID)
                .tenantIdIn(TENANT_ID)
                .list();
        VariableMapImpl variableMap = new VariableMapImpl();

        variableMap.put("approvalList",MULTI_INSTANCE);
        variableMap.put("approval_list_two",MULTI_INSTANCE_TWO);
        for (Task task : list) {
            System.out.println("--------------------------------------------");
            System.out.println("任务ID:" + task.getId());
            System.out.println("任务名称:" + task.getName());
            System.out.println("任务创建时间:" + task.getCreateTime());
            System.out.println("任务委派人:" + task.getAssignee());
            System.out.println("流程实例ID:" + task.getProcessInstanceId());
            System.out.println("--------------------------------------------");
            taskService.complete(task.getId(),variableMap);
        }
    }





    /**
     * 退回起点
     */
    @Test
    public void taskQueryComplete2(){

        String assignee = "39";
        String taskId = "393a99c3-0191-11ec-9374-c2d21dce0b69";
        // 取得已提交的任务
//        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();
         HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).taskAssignee(assignee).singleResult();
        // HistoricTaskInstance historicTaskInstance =
        // historyService.createHistoricTaskInstanceQuery().taskCandidateUser(assignee).singleResult();
        System.out.println(historicTaskInstance.getId());
        // 取得流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(historicTaskInstance.getProcessInstanceId()).singleResult();
        System.out.println(processInstance.getId());
        Map<String, Object> variables = runtimeService.getVariables(historicTaskInstance.getExecutionId());
        System.out.println(variables);
        // 取得流程定义
        ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity) repositoryService
                .getProcessDefinition(historicTaskInstance.getProcessDefinitionId());
        System.out.println(definitionEntity);
        // 取得上一步活动
        ActivityImpl hisActivity = definitionEntity.findActivity(historicTaskInstance.getTaskDefinitionKey());
        System.out.println(hisActivity);
        // 取得当前活动
        List<PvmTransition> currTransitionList = hisActivity.getOutgoingTransitions();
        System.out.println(currTransitionList);
    }


    @ParameterizedTest
    @CsvSource({"a86e3d7b-0193-11ec-94db-c2d21dce0b69,Ame"})
    void revoke(String taskId , String assignee){

        Task currentTask = extracted(taskId, assignee);

        //目标节点key
        String destTaskKey = "apply";
        Map<String, Object> variables;

        ExecutionEntity entity = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(currentTask.getExecutionId()).singleResult();

        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(entity.getProcessDefinitionId());


        variables = taskService.getVariables(currentTask.getId());

        //当前活动环节
        ActivityImpl currActivityImpl = definition.findActivity(entity.getActivityId());

        //目标活动节点
        ActivityImpl nextActivityImpl = definition.findActivity(destTaskKey);
        if (currActivityImpl != null) {
            //所有的出口集合
            List<PvmTransition> pvmTransitions = currActivityImpl.getOutgoingTransitions();
            List<PvmTransition> oriPvmTransitions = new ArrayList<>(pvmTransitions);

            //清除所有出口
            pvmTransitions.clear();

            //建立新的出口
            List<TransitionImpl> transitionImpls = new ArrayList<>();
            TransitionImpl tImpl = currActivityImpl.createOutgoingTransition();
            tImpl.setDestination(nextActivityImpl);
            transitionImpls.add(tImpl);

            List<Task> list = taskService.createTaskQuery().tenantIdIn(TENANT_ID).processInstanceId(entity.getProcessInstanceId())
                    .taskDefinitionKey(entity.getActivityId()).list();
            Integer multiCount= 0;
            for (Task task : list) {
                if(multiCount.compareTo(1) >= 0){

                }else{
                    taskService.complete(task.getId(), variables);
                    historyService.deleteHistoricTaskInstance(task.getId());
                }
                multiCount++;
            }


            for (TransitionImpl transitionImpl : transitionImpls) {
                currActivityImpl.getOutgoingTransitions().remove(transitionImpl);
            }

            pvmTransitions.addAll(oriPvmTransitions);
        }


    }

    private Task extracted(String taskId , String assignee) {
        return taskService.createTaskQuery().taskId(taskId).taskAssignee(assignee).tenantIdIn(TENANT_ID).singleResult();
    }

    @Test
    public void refuseToFirst(){
        String processInstanceId="";
        String message="xxx";
        Task task = taskService.createTaskQuery()
                .taskAssignee(USER_ID) //当前登录用户的id
                .processInstanceId(processInstanceId)
                .singleResult();
        ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);
        List<HistoricActivityInstance> resultList = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .activityType("userTask")
                .finished()
                .orderByHistoricActivityInstanceEndTime()
                .asc()
                .list();
        //得到第一个任务节点的id
        HistoricActivityInstance historicActivityInstance = resultList.get(0);
        String toActId = historicActivityInstance.getActivityId();
        String assignee = historicActivityInstance.getAssignee();
        //设置流程中的可变参数
        Map<String, Object> taskVariable = new HashMap<>(2);
        taskVariable.put("user", assignee);
        taskVariable.put("formName", "项目建设");
        taskService.createComment(task.getId(), processInstanceId, "驳回原因:" + message);
        runtimeService.createProcessInstanceModification(processInstanceId)
                .cancelActivityInstance(getInstanceIdForActivity(tree, task.getTaskDefinitionKey()))//关闭相关任务
                .startBeforeActivity(toActId)//启动目标活动节点
                .setVariables(taskVariable)//流程的可变参数赋值
                .execute();
    }
    private String getInstanceIdForActivity(ActivityInstance activityInstance, String activityId) {
        ActivityInstance instance = getChildInstanceForActivity(activityInstance, activityId);
        if (instance != null) {
            return instance.getId();
        }
        return null;
    }

    private ActivityInstance getChildInstanceForActivity(ActivityInstance activityInstance, String activityId) {
        if (activityId.equals(activityInstance.getActivityId())) {
            return activityInstance;
        }
        for (ActivityInstance childInstance : activityInstance.getChildActivityInstances()) {
            ActivityInstance instance = getChildInstanceForActivity(childInstance, activityId);
            if (instance != null) {
                return instance;
            }
        }
        return null;
    }



    @Test
    public void objToMap() throws IllegalAccessException {

        WorkFlowProcessVariable processVariable = new WorkFlowProcessVariable();

        processVariable.setBusinessKey("111");
        processVariable.setDeployId("33");
        Map<String, Object> stringObjectMap = processVariable.toMap();


        Map<String, Object> full = processVariable.fullToMap();

        System.out.println(stringObjectMap);

        System.out.println(full);
    }
}
