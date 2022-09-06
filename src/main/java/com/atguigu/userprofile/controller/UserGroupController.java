package com.atguigu.userprofile.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.userprofile.bean.TagCondition;
import com.atguigu.userprofile.bean.TaskInfo;
import com.atguigu.userprofile.bean.UserGroup;
import com.atguigu.userprofile.service.UserGroupService;
import com.atguigu.userprofile.utils.RedisUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhangchen
 * @since 2021-05-04
 */
@RestController
public class UserGroupController {

    @Autowired
    UserGroupService userGroupService;

    @RequestMapping("/user-group-list")
    @CrossOrigin
    public String  getUserGroupList(@RequestParam("pageNo")int pageNo , @RequestParam("pageSize") int pageSize){
        int startNo=(  pageNo-1)* pageSize;
        int endNo=startNo+pageSize;

        QueryWrapper<UserGroup> queryWrapper = new QueryWrapper<>();
        int count = userGroupService.count(queryWrapper);

        queryWrapper.orderByDesc("id").last(" limit " + startNo + "," + endNo);
        List<UserGroup> userGroupList = userGroupService.list(queryWrapper);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("detail",userGroupList);
        jsonObject.put("total",count);

        return  jsonObject.toJSONString();
    }


    /**
     * 分群的保存
     *
     * 1   要把分群的基本信息要保存起来
     *          mysql
     *
     * 2   根据分群基本信息的条件，在clickhouse中进行计算获得人群包（uid的集合)
     *         保存在clickhouse中
     *
     * 3   把clickhouse中的人群包数据转存在redis中，便于高频访问。
     */

    @PostMapping("/user-group")
    public  String createUserGroup(@RequestBody UserGroup userGroup ){

        System.out.println(userGroup);
      //  userGroupService.saveOrUpdate(userGroup);
//     * 1   要把分群的基本信息要保存起来  mysql
        userGroupService.saveUserGroupInfo(userGroup);

   //    2   根据分群基本信息的条件，在clickhouse中进行计算获得人群包（uid的集合)
   //         保存在clickhouse中
        userGroupService.genUserGroup(userGroup);
   //3   把clickhouse中的人群包数据转存在redis中，便于高频访问。
        Long userGroupNum = userGroupService.saveToRedis(userGroup);

    //4 把计算后的人群个数再次写入mysql
        userGroup.setUserGroupNum(userGroupNum);
        userGroupService.saveUserGroupInfo(userGroup);
        return "success";

    }

    @PostMapping("/user-group-evaluate")
    public  Long userGroupEvaluate(@RequestBody UserGroup userGroup){
            // 1  查询等多个bitmap 进行交集 ，得到bitmap的元素个数

          Long  userGroupNum=  userGroupService.getUserGroupNum(  userGroup);
          return  userGroupNum;
    }

    @PostMapping("/user-group-refresh/{id}")
    public  String userGroupRefresh(@PathVariable("id") String userGroupId,
                                    @RequestParam("busiDate") String busiDate ){
        //  1  活动的userGroup的基本信息  根据userGroupId查询UserGroup对象
         UserGroup userGroup= userGroupService.getUserGroupInfo(  userGroupId,busiDate);

        // 2 重新计算人群包 clickhouse
        userGroupService.genUserGroup(userGroup);  //先清理 在写入

        // 3 重新写入redis
        Long userGroupNum = userGroupService.saveToRedis(userGroup); //先清理 在写入

        //4 把计算后的人群个数再次写入mysql
        userGroup.setUserGroupNum(userGroupNum);
        userGroupService.saveUserGroupInfo(userGroup);
        return  "success";
    }



}

