package com.dalomao.dubbo.registry;


import com.dalomao.dubbo.pojo.Registry;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class BaseRegistryDelegate {

    public static void registry(String ref, ApplicationContext application) {
        //根据spring上下文获取Registry Bean
        Registry registry = application.getBean(Registry.class);
        String protocol = registry.getProtocol();
        BaseRegistry registryBean = registry.getRegistryMap().get(protocol);
        registryBean.registry(ref, application);
    }

    public static List<String> getRegistry(String id, ApplicationContext application) {
        Registry registry = application.getBean(Registry.class);
        String protocol = registry.getProtocol();
        BaseRegistry registryBean = registry.getRegistryMap().get(protocol);
        return registryBean.getRegistry(id, application);
    }
}
