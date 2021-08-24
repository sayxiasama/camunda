package com.ago.camunda.enums;

public enum  ChooseTypeEnum {


    SINGLE,
    MULTI,
    OTHER
    ;

    ChooseTypeEnum() {
    }


    public static ChooseTypeEnum get(String key){

        for (ChooseTypeEnum chooseTypeEnum : ChooseTypeEnum.values()) {

            if(chooseTypeEnum.name().equalsIgnoreCase(key)){
                return chooseTypeEnum;
            }
        }

        return ChooseTypeEnum.OTHER;
    }
}
