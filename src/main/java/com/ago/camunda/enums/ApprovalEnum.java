package com.ago.camunda.enums;

public enum ApprovalEnum {

    OR,
    AND,
    CROSS,
    OTHER
    ;

    ApprovalEnum() {
    }

    public static ApprovalEnum get(String name){

        for (ApprovalEnum value : ApprovalEnum.values()) {


            if(value.name().equalsIgnoreCase(name)){

                return value;
            }
        }

        return ApprovalEnum.OTHER;
    }
}
