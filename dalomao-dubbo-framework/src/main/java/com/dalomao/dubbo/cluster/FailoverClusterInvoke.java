package com.dalomao.dubbo.cluster;

import com.dalomao.dubbo.invoke.Invocation;

/**
 * failover -- 若调用节点失败，则自动切换到其他节点
 */
public class FailoverClusterInvoke implements Cluster {

    public String invoke(Invocation invocation) throws Exception {
        String retries = invocation.getReference().getRetries();
        Integer retriesInt = Integer.parseInt(retries);
        for (int i=0; i<retriesInt; i++) {
            try {
                return invocation.getInvoke().invoke(invocation);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }

        //调用都失败了直接抛异常
        throw new RuntimeException("retries " + retries + " all failed");
    }
}
