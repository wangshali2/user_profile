package com.atguigu.demo.customer.service.impl;

import com.atguigu.demo.customer.bean.Customer;
import com.atguigu.demo.customer.service.CustomerService;
import org.springframework.stereotype.Service;

@Service("customer1")   //只要进程启动 ，会自动创建全局唯一的对象 ，会放在容器中
public class CustomerServiceImpl implements CustomerService {
    @Override
    public void saveCustomer(Customer customer) {
        System.out.println("save customer:"+customer);
    }
}
