package com.ago.camunda;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class HistoryTest {

    @Autowired
    private ProcessEngine processEngine;


    @Test
    public void queryHistoricActivitiInstance() {
        String processInstanceId = "405860d9-ffd6-11eb-bec8-c2d21dce0b69";
        List<HistoricActivityInstance> list = processEngine.getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        if (list != null && list.size() > 0) {
            for (HistoricActivityInstance hai : list) {
                System.out.println(hai.getId());
                System.out.println("步骤ID：" + hai.getActivityId());
                System.out.println("步骤名称：" + hai.getActivityName());
                System.out.println("执行人：" + hai.getAssignee());
                System.out.println("====================================");
            }
        }
    }

    /**
     * 某一次流程的执行经历的多少任务
     */
    @Test
    public void queryHistoricTask() {
        String processInstanceId = "405860d9-ffd6-11eb-bec8-c2d21dce0b69";
        List<HistoricTaskInstance> list = processEngine.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        if (list != null && list.size() > 0) {
            for (HistoricTaskInstance hti : list) {
                System.out.println("taskId:" + hti.getId() + "，");
                System.out.println("name:" + hti.getName() + "，");
                System.out.println("pdId:" + hti.getProcessDefinitionId() + "，");
                System.out.println("assignee:" + hti.getAssignee() + "，");
                System.out.println("====================================");
            }
        }
    }

    @Test
    public void queryHistoricVariables() {
        String processInstanceId = "405860d9-ffd6-11eb-bec8-c2d21dce0b69";
        List<HistoricVariableInstance> list = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        if(list != null && list.size()>0){
            for(HistoricVariableInstance hvi : list){
                System.out.print("piId:"+hvi.getProcessInstanceId()+"，");
                System.out.print("variablesName:"+hvi.getName()+"，");
                System.out.println("variablesValue:"+hvi.getValue()+";");
            }
        }
    }

    @Test
    public void testUser(){

        IdentityService identityService = processEngine.getIdentityService();

        List<String> userInfoKeys = identityService.getUserInfoKeys("39");

        System.out.println(userInfoKeys);

    }


}