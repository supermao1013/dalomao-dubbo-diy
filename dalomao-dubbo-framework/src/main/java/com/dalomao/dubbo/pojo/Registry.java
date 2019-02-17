package com.dalomao.dubbo.pojo;


import com.dalomao.dubbo.registry.BaseRegistry;
import com.dalomao.dubbo.registry.RedisRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry实例
 */
public class Registry {
    private String protocol;
    private String address;

    private static Map<String, BaseRegistry> registryMap = new HashMap<String, BaseRegistry>();

    static {
        registryMap.put("redis", new RedisRegistry());
    }


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static Map<String, BaseRegistry> getRegistryMap() {
        return registryMap;
    }

    public static void setRegistryMap(Map<String, BaseRegistry> registryMap) {
        Registry.registryMap = registryMap;
    }
}
