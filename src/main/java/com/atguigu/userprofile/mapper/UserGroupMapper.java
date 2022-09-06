package com.atguigu.userprofile.mapper;

import com.atguigu.userprofile.bean.UserGroup;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;

import javax.ws.rs.DELETE;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhangchen
 * @since 2021-05-04
 */


@Mapper
@DS("mysql")
public interface UserGroupMapper extends BaseMapper<UserGroup> {

    @Insert("${sql}")
    @DS("clickhouse")
    public  void  insertBitmapSQL(@Param("sql") String sql);

    //mybatis 每行数据会封装到list中每个元素   要进入list多个元素 ，要把clickhouse中的array炸开为多行
    @Select("select   arrayJoin( bitmapToArray(us)) from user_group where user_group_id=#{userGroupId}")
    @DS("clickhouse")
    public  String[] selectUidList(@Param("userGroupId") String userGroupId);

    @Select("${sql}")
    @DS("clickhouse")
    public  String[] selectUidListBySQL(@Param("sql") String sql);


    @Select("${sql}")
    @DS("clickhouse")
    public Long  selectUserGroupNum(@Param("sql") String sql);


    @Delete("  alter table user_group delete where user_group_id=#{userGroupId}")
    @DS("clickhouse")
    public  void  deleteUserGroup(@Param("userGroupId") String userGroupId);

}
