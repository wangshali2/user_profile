package com.atguigu.demo.customer.service.impl;

import com.atguigu.demo.customer.bean.Customer;
import com.atguigu.demo.customer.mapper.CustomerMapper;
import com.atguigu.demo.customer.service.CustomerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("customer2")
public class Customer2ServiceImpl extends ServiceImpl<CustomerMapper,Customer> implements CustomerService {
    @Override
    public void saveCustomer(Customer customer) {

            System.out.println("save customer22222222222:"+customer);

    }

    @Override
    public Customer getCustomer(String id) {
        return null;
    }

    @Override
    public List<Customer> getCustomerListByName(String name) {
        return null;
    }
}
