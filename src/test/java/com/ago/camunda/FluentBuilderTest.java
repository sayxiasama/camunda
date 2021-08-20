package com.ago.camunda;


import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
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

@SpringBootTest
public class FluentBuilderTest {


    @Autowired
    private ProcessEngine processEngine;


    @Test
    void fluentDesign() throws Exception {

        RepositoryService repositoryService = processEngine.getRepositoryService();

//        BpmnModelInstance done = Bpmn.createExecutableProcess()
//                .name("fluent deploy")
//                .startEvent("s")
//                .userTask("cs")
//                .name("抄送")
//                .camundaAssignee("${userId}")
//                .userTask("approval_1")
//                .multiInstance()
//                .camundaCollection("${approvalList}")
//                .camundaElementVariable("approvalOne")
//                .completionCondition("#{nrOfCompletedInstances == 1}")
//                .multiInstanceDone()
//
//                .exclusiveGateway("gateWay_approval1")
//                .name("approval?")
//                .condition("no","${!approval}")
//                .userTask("xxxx")
//                .camundaExecutionListenerExpression("approval_refuse","#{processFailDelegate}")
//                .connectTo("cs")
//                .moveToNode("gateWay_approval1")
//                .condition("yes","${approval}")
//                .camundaExecutionListenerExpression("approval_pass","#{approvalDelegate}")
//                .userTask("aaaa")
//                .endEvent("e")
//                .done();

        BpmnModelInstance done = Bpmn.createExecutableProcess()
                .name("test")
                .id("test")
                .startEvent("start")
                .userTask("people")
                .exclusiveGateway("question")
                .name("能成功吗？")
                .condition("no", "#{!answer}")
                .userTask("something")
                .connectTo("people")
                .moveToNode("question")
                .condition("yes", "#{answer}")
                .userTask()
                .endEvent()
                .done();

        Deployment deploy = repositoryService.createDeployment()
                .name("请假")
                .addModelInstance("done" + ".bpmn20.xml", done)
                .tenantId("41")
                .deploy();


        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();

        convert(processDefinition.getKey());

    }


    /**
     * 输出bpmn
     * @param definitionKey
     */
    void convert(String definitionKey){
        //创建流程引擎配置类
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //获取查询器
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        //设置查询条件
        processDefinitionQuery.processDefinitionKey(definitionKey);

        //执行查询操作，得到想要流程定义信息
        ProcessDefinition processDefinition = processDefinitionQuery.singleResult();

        //通过流程定义信息得到部署id
        String deploymentId = processDefinition.getDeploymentId();

        //实现读写bpmn文件信息
        InputStream bpmnIs = repositoryService.getResourceAsStream(deploymentId, processDefinition.getResourceName());

        //构建输出流
        FileOutputStream bpmnOs = null;
        try {
            bpmnOs = new FileOutputStream("C:\\Users\\fenmu\\Desktop\\test.bpmn20.xml");
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
