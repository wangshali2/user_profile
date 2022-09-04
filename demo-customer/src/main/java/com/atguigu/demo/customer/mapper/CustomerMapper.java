package com.atguigu.demo.customer.mapper;

import com.atguigu.demo.customer.bean.Customer;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
@DS("mysql0411")
public interface CustomerMapper extends BaseMapper<Customer> {

    // 对数据库customer表的 插删改查...

   //新增  1 注解  2 xml
    //  #{}  半自动化参数 ：  能够自动判断类型，来决定是否加单引，还可以解决sql包含的特殊字符的问题
    //  ${}  原生参数 ：  保持参数的原始状态，不会进行判断类型 也不会进行检查特殊字符  场景： 自己组合的sql
    @Insert("insert into  customer values(#{customer.id} ,#{customer.name} ,#{customer.age} )")
    @DS("mysql0425")
    public void insertCustomer(@Param("customer") Customer customer);



    @Select("select id,name,age from customer where id= #{id} ")
    public Customer  selectCustomerById(@Param("id") String id);


    @Select("select id ,name ,age from customer where name like '%${name}%'")
    public List<Customer> selectCustomerByName(@Param("name") String name);




}
