package com.atguigu.demo.customer.service.impl;

import com.atguigu.demo.customer.bean.Customer;
import com.atguigu.demo.customer.service.CustomerService;
import org.springframework.stereotype.Service;

@Service("customer2")
public class Customer2ServiceImpl implements CustomerService {
    @Override
    public void saveCustomer(Customer customer) {

            System.out.println("save customer22222222222:"+customer);

    }
}
