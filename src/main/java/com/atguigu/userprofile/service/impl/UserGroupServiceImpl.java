package com.atguigu.userprofile.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.userprofile.bean.TagCondition;
import com.atguigu.userprofile.bean.TagInfo;
import com.atguigu.userprofile.bean.UserGroup;
import com.atguigu.userprofile.constants.ConstCodes;
import com.atguigu.userprofile.mapper.UserGroupMapper;
import com.atguigu.userprofile.service.TagInfoService;
import com.atguigu.userprofile.service.UserGroupService;
import com.atguigu.userprofile.utils.RedisUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhangchen
 * @since 2021-05-04
 */
@Service
@Slf4j
@DS("mysql")
public class UserGroupServiceImpl extends ServiceImpl<UserGroupMapper, UserGroup> implements UserGroupService {

    @Autowired
    TagInfoService tagInfoService;

    @Override
    public void saveUserGroupInfo(UserGroup userGroup) {
         //1 把条件集合转储成json
        List<TagCondition> tagConditionList = userGroup.getTagConditions();
        String conditionJsonStr = JSON.toJSONString(tagConditionList);
        userGroup.setConditionJsonStr(conditionJsonStr);

        // 2 把条件中的中文提取出来
        String conditionComment = userGroup.conditionJsonToComment();
        userGroup.setConditionComment(conditionComment);
        // 3 创建日期
        userGroup.setCreateTime(new Date());



        // 4 保存到mysql中
        super.saveOrUpdate(userGroup);

    }

    /**
     * 写入clickhouse
     * 1 先清理已有的usergroup
     * 2 再新增usergroup
     * @param userGroup
     */
    @Override
    public void genUserGroup(UserGroup userGroup) {
        //执行sql
        String insertSelectSQL = getInsertSelectSQL(userGroup);

        //   清理已有的userGroup
        baseMapper.deleteUserGroup(userGroup.getId().toString());
       //
        super.baseMapper.insertBitmapSQL(insertSelectSQL);
        System.out.println(insertSelectSQL);
    }



    //负责生成insert select sql
    // 把insert select 拼接到 bitmapAnd()
    public  String getInsertSelectSQL(UserGroup userGroup){

        String bitmapAndSQL = getBitmapAndSQL(userGroup);
        String insertSQL="insert into  user_group select  '"+userGroup.getId()+"' ,"+bitmapAndSQL;

        return  insertSQL;
    }

    //负责生成bitmapAndSQL
    //把多个子查询用bitmapAnd函数拼接起来
    //bitmapAnd(
    //  bitmapAnd(
    //
    //   (subquery1)
    //   ,
    //   (subquery2)
    //  )
    //,
    //  (subquery3)
    //)
    public  String getBitmapAndSQL(UserGroup userGroup){
        List<TagCondition> tagConditionList = userGroup.getTagConditions();
        Map<String, TagInfo> tagInfoMapWithCode = tagInfoService.getTagInfoMapWithCode();
        String busiDate=userGroup.getBusiDate();
        StringBuilder  sqlBuilder=new StringBuilder();
        for (TagCondition tagCondition : tagConditionList) {
            //获得新的子查询sql
            String subQuerySQL = getConditionSubQuerySQL(tagCondition, tagInfoMapWithCode, busiDate);
            //判断 如果是builder中没有值 ，则不用拼接把  subquerySQL直接写入builder中
            //    如果builder有值，不是第一条   bitmapAnd(+ builder中的sql+ , +新的子查询 +)
            if(sqlBuilder.length()==0){
                sqlBuilder.append(subQuerySQL);
            }else{
                sqlBuilder.insert(0,"bitmapAnd(").append(","+subQuerySQL+")");
            }

        }
        System.out.println(sqlBuilder );
        return sqlBuilder.toString();


    }

    //单独一个条件 生成一个子查询sql
    ////select groupBitmapMergeState(us) from user_tag_value_string  where  tag_code='tg_person_base_agegroup'
    //            and  tag_value in ('70后','80后','90后') and  dt='2020-06-14'
    //
    //// 1  表名 ： 要根据标签值的类型确定  标签值类型 用tagcode 去查询mysql 的标签定义  tag_info
    //// 2  tag_code : 转小写
    //// 3  operator ： 把页面传过来的英文 进行转义
    //// 4  tag_value :    tagValues   1 多个 进行拼接 用逗号分割 2 依据标签值类型 来决定是否加单引 3 根据 in 或者 not in 来决定是否加小括号
    //// 5  dt   : busidate
    public String  getConditionSubQuerySQL(TagCondition tagCondition ,Map<String,TagInfo> tagInfoMap,String busiDate){
        String tagCode = tagCondition.getTagCode();

        //1 确定表面
        TagInfo tagInfo = tagInfoMap.get(tagCode);
        String tagValueType = tagInfo.getTagValueType();
        String tableName="";
        if(tagValueType.equals(ConstCodes.TAG_VALUE_TYPE_LONG)){
            tableName="user_tag_value_long";
        }else if(tagValueType.equals(ConstCodes.TAG_VALUE_TYPE_DECIMAL)){
            tableName="user_tag_value_decimal";
        }else if(tagValueType.equals(ConstCodes.TAG_VALUE_TYPE_STRING)){
            tableName="user_tag_value_string";
        }else if(tagValueType.equals(ConstCodes.TAG_VALUE_TYPE_DATE)){
            tableName="user_tag_value_date";
        }
        //// 2  tag_code : 转小写
        tagCode=tagCode.toLowerCase();
        //3  operator ： 把页面传过来的英文 进行转义 操作符
        String operator=getConditionOperator(tagCondition.getOperator());
        //// 4  tag_value :    tagValues   1 多个 进行拼接 用逗号分割 2 依据标签值类型 来决定是否加单引 3 根据 in 或者 not in 来决定是否加小括号

        String tagValueSQL = "";

        if(tagValueType.equals(ConstCodes.TAG_VALUE_TYPE_STRING)||tagValueType.equals(ConstCodes.TAG_VALUE_TYPE_DATE)){
            tagValueSQL = "'"+StringUtils.join(tagCondition.getTagValues(), "','")+"'";
        }else{
            tagValueSQL = StringUtils.join(tagCondition.getTagValues(), ",");
        }

        if(tagCondition.getOperator().equals("in")||tagCondition.getOperator().equals("nin")){
            tagValueSQL= "("+tagValueSQL+")";
        }

        // 5  dt   : busidate
        String dt=busiDate;


        //6 组合子查询
        String subquerySQL="(select groupBitmapMergeState(us) " +
                " from   " +tableName+
                " where  tag_code='"+tagCode+"'\n" +
                "  and  tag_value  "+operator+" " +tagValueSQL+
                " and  dt='"+dt+"' ) ";

        System.out.println(subquerySQL);
        return subquerySQL;
    }



    private  String getConditionOperator(String operator){
        switch (operator){
            case "eq":
                return "=";
            case "lte":
                return "<=";
            case "gte":
                return ">=";
            case "lt":
                return "<";
            case "gt":
                return ">";
            case "neq":
                return "<>";
            case "in":
                return "in";
            case "nin":
                return "not in";
        }
        throw  new RuntimeException("操作符不正确");
    }


    @Override
    public Long saveToRedis(UserGroup userGroup) {
        // 1  得到分群的计算结果   查询clickhouse
        // 此处依赖clickhouse的写入结果，更新时会有查询出旧数据的情况
        // String[] uidArr=   super.baseMapper.selectUidList(userGroup.getId().toString());
        // 1  重新计算 得到uidArr 不依赖clickhouse写入结果  直接计算
        String bitmapAndSQL = getBitmapAndSQL(userGroup);
        String uidArrSQL="select  arrayJoin(bitmapToArray("+bitmapAndSQL+"))";

        String[] uidArr = baseMapper.selectUidListBySQL(uidArrSQL);

        // 2  把计算结果（人群包) 保存到 redis中
        //  type ?  string(1对1 还是1对多 )  list(有没有重复,非幂等)   set (判存场景，去重，幂等)    zset(要不要排序) hash（单值 ，还是键值对）
        //  key ?     user_group:101      value ?    uids             field (hash)/score (zset) 无
        //  写api  ?  sadd        读api?  smembers 全读    sismember 判存   scard  统计个数
        //  过期 时间?  临时数据  缓存    此场景不是缓存 也不是临时数据 不设过期时间

        Jedis jedis = RedisUtil.getJedis();
        String userGroupKey="user_group:"+userGroup.getId();
        // 清理已有的usergroup
        jedis.del(userGroupKey);


      //  String[] uidArr = uidList.toArray(new String[]{});
        jedis.sadd(userGroupKey,uidArr);


        jedis.close();

        return uidArr.length+0L;

    }

    @Override
    public Long getUserGroupNum(UserGroup userGroup) {
       // 1  组合sql   获得bitmapAndSQL  拼接 select bitmapCardinality( bitmapAndSQL ) ;
        String bitmapAndSQL = getBitmapAndSQL(userGroup);
        String userGroupNumSQL=" select bitmapCardinality( "+bitmapAndSQL+" )";

        // 2  执行该sql 获得Long结果
        Long userGroupNum = baseMapper.selectUserGroupNum(userGroupNumSQL);

        return userGroupNum;
    }

    /**
     * 根据主键查询usergroup基本信息
     * @param userGroupId
     * @param busiDate
     * @return
     */
    @Override
    public UserGroup getUserGroupInfo(String userGroupId, String busiDate) {
        UserGroup userGroup = getById(userGroupId);
        String conditionJsonStr = userGroup.getConditionJsonStr();
        List<TagCondition> tagConditionList = JSON.parseArray(conditionJsonStr, TagCondition.class);
        userGroup.setTagConditions(tagConditionList);
        userGroup.setBusiDate(busiDate);
        return userGroup;
    }


}
