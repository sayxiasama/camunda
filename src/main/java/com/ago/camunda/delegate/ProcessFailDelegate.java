package com.ago.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class ProcessFailDelegate implements JavaDelegate {


    @Override
    public void execute(DelegateExecution delegateExecution) {

        System.out.println("拒绝审批 - ");

        // do anything

    }
}
