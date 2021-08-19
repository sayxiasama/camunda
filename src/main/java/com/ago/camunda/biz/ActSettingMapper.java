package com.ago.camunda.biz;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface ActSettingMapper {




    @Insert(value = " insert into act_z_setting (deploy_id,node_id,approval_type,pick_type,scope,createdTime)" +
            " values \n"+
            " (#{deployId},1,1,1,1)"
    )
    void insertModel(String deployId);



    @Select(value = "select * from act_z_setting")
    Map<String,Object> query();
}
