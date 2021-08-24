package com.ago.camunda.business;

import com.ago.camunda.biz.domain.WorkFlowProcessVariable;
import com.ago.camunda.biz.serivce.WorkFlowService;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.bpmn.behavior.ParallelMultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.javax.el.ExpressionFactory;
import org.camunda.bpm.engine.impl.javax.el.ValueExpression;
import org.camunda.bpm.engine.impl.juel.ExpressionFactoryImpl;
import org.camunda.bpm.engine.impl.juel.SimpleContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SpringBootTest
public class DeployTest {


    @Autowired
    private WorkFlowService workFlowService;

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private TaskService taskService;
    @Resource
    private HistoryService historyService;


    private static final String USER_ID = "39";

    private static final String TENANT_ID = "41";

    private static final List<String> MULTI_INSTANCE = Arrays.asList("Anna","Ame");

    private static final List<String> MULTI_INSTANCE_TWO = Arrays.asList("LGD","GH");


    @Test
    public void testProcess() throws Exception {


        FileInputStream fis = new FileInputStream("C:\\Users\\admin\\Desktop\\leave.bpmn");


        String s = workFlowService.deployInstance(fis, "leave.bpmn", "auto", "自动审核", TENANT_ID);


        WorkFlowProcessVariable build = WorkFlowProcessVariable.builder()
                .businessKey("1")
                .userId(USER_ID)
                .customVariables(new VariableMapImpl())
                .deployId(s)
                .tenantId(TENANT_ID)
                .build();

        workFlowService.submit(build);

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(s).tenantIdIn(TENANT_ID).singleResult();

        List<Task> list = taskService.createTaskQuery().taskAssignee(USER_ID).tenantIdIn(TENANT_ID).processDefinitionKey(processDefinition.getKey()).list();

        for (Task task : list) {

            Set<Expression> nextTaskGroup = getNextTaskGroup(task.getId());

            for (Expression expression : nextTaskGroup) {

                System.out.println(expression);
            }

        }


    }

    @Test
    /**
     * 获取下一个用户任务用户组信息
     * @param String taskId     任务Id信息
     * @return  下一个用户任务用户组信息
     * @throws Exception
     */
    public Set<Expression> getNextTaskGroup(String taskId) throws Exception {

        ProcessDefinitionEntity processDefinitionEntity = null;

        String id = null;

        TaskDefinition task = null;

        //获取流程实例Id信息
        String processInstanceId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();

        //获取流程发布Id信息
        String definitionId = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getProcessDefinitionId();

        processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(definitionId);

        ExecutionEntity execution = (ExecutionEntity) runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        //当前流程节点Id信息
        String activitiId = execution.getActivityId();

        //获取流程所有节点信息
        List<ActivityImpl> activitiList = processDefinitionEntity.getActivities();

        //遍历所有节点信息
        for(ActivityImpl activityImpl : activitiList){
            id = activityImpl.getId();

            // 找到当前节点信息
            if (activitiId.equals(id)) {

                //获取下一个节点信息
                task = nextTaskDefinition(activityImpl, activityImpl.getId(), null, processInstanceId);

                break;
            }
        }

        assert task != null;

        return task.getCandidateGroupIdExpressions();
    }

    /**
     * 下一个任务节点信息,
     *
     * 如果下一个节点为用户任务则直接返回,
     *
     * 如果下一个节点为排他网关, 获取排他网关Id信息, 根据排他网关Id信息和execution获取流程实例排他网关Id为key的变量值,
     * 根据变量值分别执行排他网关后线路中的el表达式, 并找到el表达式通过的线路后的用户任务信息
     * @param  activityImpl     流程节点信息
     * @param  activityId             当前流程节点Id信息
     * @param  elString               排他网关顺序流线段判断条件, 例如排他网关顺序留线段判断条件为${money>1000}, 若满足流程启动时设置variables中的money>1000, 则流程流向该顺序流信息
     * @param  processInstanceId      流程实例Id信息
     * @return
     */
    private TaskDefinition nextTaskDefinition(ActivityImpl activityImpl, String activityId, String elString, String processInstanceId){

        PvmActivity ac = null;

        Object s = null;

        //如果遍历节点为用户任务并且节点不是当前节点信息
        if("userTask".equals(activityImpl.getProperty("type")) && !activityId.equals(activityImpl.getId())){
            //获取该节点下一个节点信息
            TaskDefinition taskDefinition = ((UserTaskActivityBehavior)activityImpl.getActivityBehavior()).getTaskDefinition();
            return taskDefinition;
        }else{
            //获取节点所有流向线路信息
            List<PvmTransition> outTransitions = activityImpl.getOutgoingTransitions();
            List<PvmTransition> outTransitionsTemp = null;
            for(PvmTransition tr : outTransitions){
                ac = tr.getDestination(); //获取线路的终点节点
                if("userTask".equals(ac.getProperty("type"))){
                    return ((UserTaskActivityBehavior)(ac).getActivityBehavior()).getTaskDefinition();
                }else if("multiInstanceBody".equals(ac.getProperty("type"))){

                    ParallelMultiInstanceActivityBehavior parallelMultiInstanceActivityBehavior =(ParallelMultiInstanceActivityBehavior)ac.getActivityBehavior();

                    Expression collectionExpression = parallelMultiInstanceActivityBehavior.getCollectionExpression();

                }
            }
            return null;
        }
    }


    /**
     * 查询流程启动时设置排他网关判断条件信息
     * @param  gatewayId          排他网关Id信息, 流程启动时设置网关路线判断条件key为网关Id信息
     * @param  processInstanceId  流程实例Id信息
     * @return
     */
    public String getGatewayCondition(String gatewayId, String processInstanceId) {
        Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).singleResult();
        return runtimeService.getVariable(execution.getId(), gatewayId).toString();
    }

    /**
     * 根据key和value判断el表达式是否通过信息
     * @param  key    el表达式key信息
     * @param  el     el表达式信息
     * @param  value  el表达式传入值信息
     * @return
     */
    public boolean isCondition(String key, String el, String value) {
        ExpressionFactory factory = new ExpressionFactoryImpl();
        SimpleContext context = new SimpleContext();
        context.setVariable(key, factory.createValueExpression(value, String.class));
        ValueExpression e = factory.createValueExpression(context, el, boolean.class);
        return (Boolean) e.getValue(context);
    }
}
