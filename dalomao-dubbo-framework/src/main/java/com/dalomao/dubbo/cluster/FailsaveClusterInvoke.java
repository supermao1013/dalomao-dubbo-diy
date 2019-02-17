package com.dalomao.dubbo.cluster;

import com.dalomao.dubbo.invoke.Invocation;
import com.dalomao.dubbo.invoke.Invoke;

/**
 * failsafe -- 若调用节点失败则忽略，继续执行代码
 */
public class FailsaveClusterInvoke implements Cluster {

    public String invoke(Invocation invocation) throws Exception {
        Invoke invoke = invocation.getInvoke();
        try {
            return invoke.invoke(invocation);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "忽略异常";
    }
}
