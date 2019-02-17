package com.dalomao.dubbo.cluster;

import com.dalomao.dubbo.invoke.Invocation;

/**
 * 集群容错
 * failover -- 若调用节点失败，则自动切换到其他节点
 * failfast --  若调用失节点败，则直接失败
 * failsafe -- 若调用节点失败则忽略，继续执行代码
 */
public interface Cluster {
    String invoke(Invocation invocation) throws Exception;
}
