package com.dalomao.dubbo.loadbalance;

import java.util.List;

/**
 *
 */
public interface LoadBalance {
    NodeInfo doSelect(List<String> registryInfo);
}
