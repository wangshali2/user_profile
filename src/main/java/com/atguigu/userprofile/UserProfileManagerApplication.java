package com.atguigu.userprofile;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//127.0.0.1
@SpringBootApplication
@EnableScheduling
@MapperScan("com.atguigu.userprofile.mapper")
public class UserProfileManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserProfileManagerApplication.class, args);
    }

}
