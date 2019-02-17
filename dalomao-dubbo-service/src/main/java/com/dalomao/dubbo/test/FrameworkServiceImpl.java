package com.dalomao.dubbo.test;


public class FrameworkServiceImpl implements FrameworkService {
    public String eat(String param) {
        String resp = "service-->FrameworkServiceImpl-->eat-->param:" + param;
        System.out.println(resp);
        return resp;
    }
}
