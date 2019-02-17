package com.dalomao.dubbo.registry;


import org.springframework.context.ApplicationContext;

import java.util.List;

public interface BaseRegistry {
    /**
     * 注册配置信息
     * @param param
     * @param application
     * @return
     */
    boolean registry(String param, ApplicationContext application);

    /**
     * 获取注册信息
     * @param id
     * @param application
     * @return
     */
    List<String> getRegistry(String id, ApplicationContext application);
}
