package com.dalomao.dubbo.cluster;

import com.dalomao.dubbo.invoke.Invocation;
import com.dalomao.dubbo.invoke.Invoke;

/**
 * failfast --  若调用失节点败，则直接失败
 */
public class FailfastClusterInvoke implements Cluster {

    public String invoke(Invocation invocation) throws Exception {
        Invoke invoke = invocation.getInvoke();
        try {
            return invoke.invoke(invocation);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
