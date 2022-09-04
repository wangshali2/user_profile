package com.atguigu.demo.customer.service.impl;

import com.atguigu.demo.customer.bean.Customer;
import com.atguigu.demo.customer.mapper.CustomerMapper;
import com.atguigu.demo.customer.service.CustomerService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("customer1")   //只要进程启动 ，会自动创建全局唯一的对象 ，会放在容器中
@DS("mysql0411")
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper,Customer> implements CustomerService  {

    @Autowired
    CustomerMapper customerMapper;


    @Override
    public void saveCustomer(Customer customer) {

        customer.setName("name0425");
         customerMapper.insertCustomer(customer);
        customer.setName("name0411");
        customerMapper.insert(customer);
        System.out.println("save customer:"+customer);
    }

    @Override
    public Customer getCustomer(String id) {
      //  Customer customer = customerMapper.selectCustomerById(id);
        Customer customer1 = customerMapper.selectById(id);
        return customer1;
    }

    @Override
    public List<Customer> getCustomerListByName(String name) {
      //  List<Customer> customerList = customerMapper.selectCustomerByName(name);

        List<Customer> customerList1 = customerMapper.selectList(new QueryWrapper<Customer>().like("name", name));
        return customerList1;
    }
}
