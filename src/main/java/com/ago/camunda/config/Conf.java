package com.ago.camunda.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class Conf implements BeanPostProcessor {


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        if(bean instanceof ProcessEngineConfigurationImpl){

            ProcessEngineConfigurationImpl pec = (ProcessEngineConfigurationImpl) bean;

            pec.setDbIdentityUsed(false);

            System.out.println(pec.isDbIdentityUsed());
        }

        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {


        if(bean instanceof ProcessEngineConfigurationImpl){

            ProcessEngineConfigurationImpl pec = (ProcessEngineConfigurationImpl) bean;

            System.out.println(pec.isDbIdentityUsed());
        }

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
