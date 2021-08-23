package com.ago.camunda.biz.controller;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
public class TestFileStream {

    private static final Logger logger = LoggerFactory.getLogger(TestFileStream.class);

    @Autowired
    private RepositoryService repositoryService;



    @PostMapping("/input-stream")
    public void test(MultipartFile file) throws IOException {

        logger.info("has been start input-stream");

        //DeploymentBuilder addInputStream(String resourceName, InputStream inputStream);
        String resourceName="leave.bpmn";//资源的名称必须是以bpmn或者bpmn20.xml结尾

        InputStream inputStream = file.getInputStream();

        String name = file.getName();

        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        Deployment deploy = deploymentBuilder.name("请假流程").source("流测试").tenantId("41")
                .addInputStream(resourceName,inputStream).deploy();

        System.out.println("deploymentBuilder"+deploymentBuilder);
        System.out.println("deploy"+deploy);
    }
}
