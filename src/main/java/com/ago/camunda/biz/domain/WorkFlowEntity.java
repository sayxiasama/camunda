package com.ago.camunda.biz.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkFlowEntity implements Serializable {


    private String name;

    private String tenantId;

    private List<CustomBPMNModelAttribute> fluents;


}
