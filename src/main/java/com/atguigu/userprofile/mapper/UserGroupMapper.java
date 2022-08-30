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



}
