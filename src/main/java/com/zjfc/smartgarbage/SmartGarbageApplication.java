package com.zjfc.smartgarbage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
public class SmartGarbageApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartGarbageApplication.class, args);
        System.out.println("==========================================");
        System.out.println("校园智能垃圾分类系统启动成功！");
        System.out.println("后端API地址: http://localhost:8080/api");
        System.out.println("垃圾分类API: http://localhost:8080/api/categories");
        System.out.println("==========================================");
    }
}