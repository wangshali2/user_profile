package com.atguigu.demo.customer.controller;


import com.atguigu.demo.customer.bean.Customer;
import com.atguigu.demo.customer.service.CustomerService;
import com.atguigu.demo.customer.service.impl.CustomerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
public class CustomerController {

    @Autowired  //启动时，会根据接口的实现类 进行自动装配
    @Qualifier("customer2")
    CustomerService customerService;

    //常见的读取参数


    @RequestMapping(value = "/customer",method = RequestMethod.GET)    //请求路径和方法之间的映射
    public String getCustomer(@RequestParam("name") String name ,@RequestParam("age") Integer age){    //多个查询条件的参数
        System.out.println("hello world");
        return  "hello world name:"+name +"|| age:"+age ;
    }

//    @RequestMapping()
    @GetMapping("/customer/{id}.html")  //只支持读请求
    public String getCustomerByKey(@PathVariable("id") String customerId){    //非常关键的唯一性标识
        System.out.println("customerId:"+customerId);
        return  "customerId:"+customerId;
    }

    //提交写参数  一般是不会放在浏览器可见的地方提交的
    @PostMapping("/customer")
    public  String  saveCustomer(@RequestBody Customer customer){
        //System.out.println(customer);

        customerService.saveCustomer(customer);
        return "success";
    }
}
