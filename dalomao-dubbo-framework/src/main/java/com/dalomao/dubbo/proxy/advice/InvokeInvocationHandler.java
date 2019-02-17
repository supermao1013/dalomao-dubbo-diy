package com.dalomao.dubbo.proxy.advice;


import com.dalomao.dubbo.cluster.Cluster;
import com.dalomao.dubbo.invoke.Invocation;
import com.dalomao.dubbo.invoke.Invoke;
import com.dalomao.dubbo.pojo.Reference;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 这个是一个advice，在这个advice里进行rpc远程调用
 * http、rmi、netty
 */
public class InvokeInvocationHandler implements InvocationHandler {

    private Invoke invoke;

    private Reference reference;

    public InvokeInvocationHandler(Invoke invoke, Reference reference) {
        this.invoke = invoke;
        this.reference = reference;
    }

    /**
     * 在这个invoke里面最终要调用多个远程的provider
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("已经获取到代理实例，已经调用到InvokeInvocationHandler.invoke");

        Invocation invocation = new Invocation();
        invocation.setMethod(method);
        invocation.setObjs(args);
        invocation.setReference(this.reference);
//        String result = this.invoke.invoke(invocation);
        invocation.setInvoke(this.invoke);
        Cluster cluster = reference.getClusters().get(this.reference.getCluster());
        String result = cluster.invoke(invocation);

        return result;
    }
}
