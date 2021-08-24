package com.ago.camunda.biz.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class WorkFlowProcessVariable implements Serializable {


    private String deployId;

    private String userId;

    private String businessKey;

    private String tenantId;

    private String firstLevelApprovals;

    private String secondLevelApprovals;

    private String threeLevelApprovals;

    private String fourLevelApprovals;

    private String fiveLevelApprovals;

    private Map<String,Object> customVariables;



    /**
     * ignore empty convert
     * @return Map<String,Object>
     */
    public Map<String,Object> toMap(){

        return JSON.parseObject(JSON.toJSONString(this), new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * full convert
     * @return Map<String,Object>
     * @throws IllegalAccessException
     */
    public Map<String,Object> fullToMap() throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);

            map.put(field.getName(), field.get(this));
        }
        return map;
    }
}
