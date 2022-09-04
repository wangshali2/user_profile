package com.atguigu.demo.customer.service;

import com.atguigu.demo.customer.bean.Customer;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface CustomerService extends IService<Customer> {



    public  void saveCustomer(Customer customer);

    public  Customer    getCustomer(String id);

    public List<Customer> getCustomerListByName(String name);
}
