package com.dalomao.dubbo.test;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MyTest {
    public static void main(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext("spring-dispatcher.xml");

        FrameworkService frameworkService = app.getBean(FrameworkService.class);
        frameworkService.eat("");
    }
}
