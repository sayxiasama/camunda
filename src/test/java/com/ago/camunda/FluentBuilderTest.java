package com.ago.camunda;


import com.ago.camunda.biz.domain.CustomBPMNModelAttribute;
import com.ago.camunda.biz.domain.WorkFlowEntity;
import com.ago.camunda.biz.serivce.WorkFlowService;
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

@SpringBootTest
public class FluentBuilderTest {


    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private WorkFlowService workFlowService;


    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Test
    void fluentDesign() throws Exception {

        /**
         * nrOfInstances：实例总数
         * nrOfActiveInstances：当前活动的，比如，还没完成的，实例数量。 对于顺序执行的多实例，值一直为1。
         * nrOfCompletedInstances：已经完成实例的数目。
         */
        RepositoryService repositoryService = processEngine.getRepositoryService();

        BpmnModelInstance done = Bpmn.createExecutableProcess()
                .executable()
                .name("test")
                .startEvent("start")
                .camundaInitiator("${userId}")
                .userTask()
                .name("apply")
                .camundaAssignee("${userId}")
                .userTask()
                .name("一级审批")
                .multiInstance()
                .camundaCollection("firstLevelApprovals")
                .camundaElementVariable("approvalOne")
                .completionCondition("#{nrOfCompletedInstances == 1}")
                .multiInstanceDone()
                .userTask()
                .name("二级审批")
                .endEvent()
                .done();

        Deployment deploy = repositoryService.createDeployment()
                .name("123473895")
                .addModelInstance("done" + ".bpmn20.xml", done)
                .tenantId("41")
                .deploy();


        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();

        convert(processDefinition.getKey(),deploy.getId());
    }



    @Test
    void testFluent(){


        WorkFlowEntity workFlowEntity = new WorkFlowEntity();

        workFlowEntity.setName("测试fluent_construct_Bpmn");
        workFlowEntity.setTenantId("41");

        ArrayList<CustomBPMNModelAttribute> attributes = Lists.newArrayList();


        attributes.add(CustomBPMNModelAttribute.builder()
                .name("抄送")
                .views("1,2,3")
                .hasViewNode(true)
                .build());
        attributes.add(CustomBPMNModelAttribute.builder()
                .name("一级审批")
                .approvals("zhangsan,lisi,wangwu")
                .approvalType("OR")
                .emptyLeaderApprovalWay(null)
                .chooseType("MULTI")
                .hasViewNode(false)
                .build());
        attributes.add(CustomBPMNModelAttribute.builder()
                .name("二级审批")
                .approvals("zhangsan,lisi,wangwu")
                .approvalType("AND")
                .emptyLeaderApprovalWay(null)
                .chooseType("MULTI")
                .hasViewNode(false)
                .build());

        workFlowEntity.setFluents(attributes);

        String s = workFlowService.deployInstance(workFlowEntity);

        System.out.println(String.format("部署流程ID %s" , s));

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(s).singleResult();

        convert(processDefinition.getKey(),s);

    }



    /**
     * 输出bpmn
     * @param definitionKey
     */
    void convert(String definitionKey,String deployId){
        //创建流程引擎配置类
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //获取查询器
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(definitionKey)
                .deploymentId(deployId).tenantIdIn("41").singleResult();

        //通过流程定义信息得到部署id
        String deploymentId = processDefinition.getDeploymentId();

        //实现读写bpmn文件信息
        InputStream bpmnIs = repositoryService.getResourceAsStream(deploymentId, processDefinition.getResourceName());

        //构建输出流
        FileOutputStream bpmnOs = null;
        try {
            bpmnOs = new FileOutputStream("C:\\Users\\admin\\Desktop\\test.bpmn20.xml");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //输入流，输出流的转换
        try {
            IOUtils.copy(bpmnIs,bpmnOs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
