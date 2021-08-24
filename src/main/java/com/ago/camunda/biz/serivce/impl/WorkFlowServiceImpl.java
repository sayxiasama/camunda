package com.ago.camunda.biz.serivce.impl;

import com.ago.camunda.biz.domain.CustomBPMNModelAttribute;
import com.ago.camunda.biz.domain.WorkFlowEntity;
import com.ago.camunda.biz.domain.WorkFlowProcessVariable;
import com.ago.camunda.biz.serivce.WorkFlowService;
import com.ago.camunda.enums.ApprovalEnum;
import com.ago.camunda.enums.ChooseTypeEnum;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.StartEventBuilder;
import org.camunda.bpm.model.bpmn.builder.UserTaskBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class WorkFlowServiceImpl implements WorkFlowService {

    private static final Logger logger = LoggerFactory.getLogger(WorkFlowServiceImpl.class);

    private static final String DEFAULT_KEY_FORMAT = "%s的%s审批";

    private static final String DEPLOY_PREFIX = ".bpmn20.xml";


    private final RepositoryService repositoryService;

    private final RuntimeService runtimeService;


    public WorkFlowServiceImpl(RepositoryService repositoryService, RuntimeService runtimeService) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
    }


    // TODO 暂时提供一个以文件流形式部署的方法, 扩展请通过重载的方式;
    @Override
    public String deployInstance(InputStream is, String resourceName, String source, String name , String tenantId) {

    Deployment deploy = repositoryService.createDeployment().name(name).source(source).tenantId(tenantId)
            .addInputStream(resourceName,is)
            .deploy();

    return Optional.ofNullable(deploy).isPresent() ? deploy.getId() : "";
    }

    @Override
    public String deployInstance(WorkFlowEntity workFlowEntity){

        StartEventBuilder seb = Bpmn.createExecutableProcess().name(workFlowEntity.getName()).startEvent();

        if(CollectionUtils.isEmpty(workFlowEntity.getFluents())){
            throw new IllegalArgumentException("流程节点不能为空;");
        }

        UserTaskBuilder utb = seb.userTask();

        for (CustomBPMNModelAttribute fluent : workFlowEntity.getFluents()) {

             if(fluent.hasViewNode){
                 utb = utb.name(fluent.getName());
                 //TODO 抄送逻辑
                 continue;
             }
            utb = utb.userTask().name(fluent.getName());
            switch (ChooseTypeEnum.get(fluent.getChooseType())) {

                case SINGLE:
                    utb = utb.camundaAssignee("${userId}").userTask();
                    break;
                case MULTI:
                    multiProcess(fluent,utb);
                    break;
                default:
                    throw new IllegalArgumentException("流程节点选人方式不能为空!");
            }
        }

        BpmnModelInstance bmi;

        if(Optional.ofNullable(utb).isPresent()){
            bmi = utb.endEvent().done();
        }else{
            throw new IllegalArgumentException("流程节点不能为空;");
        }

        Deployment deploy = repositoryService.createDeployment().addModelInstance(workFlowEntity.getName()+DEPLOY_PREFIX, bmi)
                .name(workFlowEntity.getName())
                .tenantId(workFlowEntity.getTenantId())
                .deploy();

        return Optional.ofNullable(deploy).isPresent()? deploy.getId():"";
    }

    private void multiProcess(CustomBPMNModelAttribute fluent, UserTaskBuilder utb) {
        //是否存在指定的审批方式;
        switch (ApprovalEnum.get(fluent.getApprovalType())){

            case AND:
                utb = utb.multiInstance()
                        .parallel()
                        .camundaCollection("approvals")
                        .camundaElementVariable("approval")
                        .completionCondition("#{nrOfCompletedInstances/nrOfInstances == 1}")
                        .multiInstanceDone();
                break;
            case CROSS:
                utb = utb.multiInstance()
                        .sequential()
                        .camundaCollection("approvals")
                        .camundaElementVariable("approval")
                        .completionCondition("#{nrOfCompletedInstances == 1}")
                        .multiInstanceDone();
                break;
            default:
                utb = utb.multiInstance()
                        .parallel()
                        .camundaCollection("approvals")
                        .camundaElementVariable("approval")
                        .completionCondition("#{nrOfCompletedInstances == 1}")
                        .multiInstanceDone();
                break;
        }
    }


    @Override
    public void submit(WorkFlowProcessVariable workFlowProcessVariable) {


        //根据部署id, 启动一个流程实例;
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(workFlowProcessVariable.getDeployId()).tenantIdIn(workFlowProcessVariable.getTenantId()).singleResult();

        runtimeService.createProcessInstanceByKey(processDefinition.getKey())
                .processDefinitionTenantId(workFlowProcessVariable.getTenantId())
                .businessKey(workFlowProcessVariable.getBusinessKey())
                .setVariables(workFlowProcessVariable.toMap())
                .execute();


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

    private String getNextNode(String procInstanceId){

        //1.根据流程Id获取当前任务

        //2.根据当前任务获取当前流程定义,根据流程定义获取所有节点

        //3.根据任务获取当前执行id,执行实力以及当前流程节点id

        //4.循环活动列表

        //5.判断当前所处节点,

        return "";
    }
}
