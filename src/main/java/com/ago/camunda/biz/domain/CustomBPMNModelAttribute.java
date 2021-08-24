package com.ago.camunda.biz.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomBPMNModelAttribute implements Serializable {

    /**
     * 节点名称
     */
    private String name;

    /**
     * 审批人, 多个审批使用逗号隔开
     */
    private String approvals;

    /**
     *  抄送人, 多个抄送人使用逗号隔开
     */
    private String views;

    /**
     * 审批类型, OR 或签 AND 会签 CROSS 串签(顺序签)
     */
    private String approvalType;

    /**
     * 审批人为空时的处理逻辑;
     */
    private String emptyLeaderApprovalWay;

    /**
     * 选人方式
     */
    private String chooseType;

    /**
     * 是否抄送节点
     */
    public Boolean hasViewNode;

}
