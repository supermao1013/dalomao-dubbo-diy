package com.dalomao.dubbo.test;


public class TestServiceImpl implements TestService {
    public String eat(String param) {
        System.out.println("吃饭");
        return null;
    }
}
