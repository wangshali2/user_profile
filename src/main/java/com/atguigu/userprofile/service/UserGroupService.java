package com.atguigu.userprofile.service;

import com.atguigu.userprofile.bean.UserGroup;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserGroupService  extends IService<UserGroup> {

    public void saveUserGroupInfo(UserGroup userGroup);

    public void genUserGroup(UserGroup userGroup);


    public Long saveToRedis(UserGroup userGroup);



    public Long getUserGroupNum( UserGroup userGroup);


    public UserGroup  getUserGroupInfo( String userGroupId,String busiDate);


}
