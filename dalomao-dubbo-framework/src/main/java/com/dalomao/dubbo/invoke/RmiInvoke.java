package com.dalomao.dubbo.invoke;

import com.alibaba.fastjson.JSONObject;
import com.dalomao.dubbo.loadbalance.LoadBalance;
import com.dalomao.dubbo.loadbalance.NodeInfo;
import com.dalomao.dubbo.pojo.Reference;
import com.dalomao.dubbo.remote.rmi.RmiRemote;
import com.dalomao.dubbo.remote.rmi.RmiUtil;
import com.dalomao.dubbo.rpc.HttpRequest;

import java.util.List;

/**
 * rmi调用过程
 */
public class RmiInvoke implements Invoke {
    public String invoke(Invocation invocation) throws Exception {
        String result = "";
        try {
            List<String> registryInfo = invocation.getReference().getRegistryInfo();
            //负载均衡算法
            String loadbalance = invocation.getReference().getLoadbalance();
            Reference reference = invocation.getReference();
            LoadBalance loadBalance = reference.getLoadbalances().get(reference.getLoadbalance());

            NodeInfo nodeInfo = loadBalance.doSelect(registryInfo);

            //我们调用远程的生产者是传输的json字符串
            //根据serviceid去对端生产者的spring容器中获取serviceid对应的实例
            //根据methodName和methodType获取实例的method对象
            //然后反射调用method方法
            JSONObject sendParam = new JSONObject();
            sendParam.put("methodName", invocation.getMethod().getName());//方法名
            sendParam.put("methodParams", invocation.getObjs());//方法参数
            sendParam.put("paramTypes", invocation.getMethod().getParameterTypes());//参数类型
            sendParam.put("serviceId", reference.getId());//服务ID

            //启动rmi客户端
            RmiUtil rmiUtil = new RmiUtil();
            RmiRemote rmiRemote = rmiUtil.startRmiClient(nodeInfo, "dalomao");
            return rmiRemote.invoke(sendParam.toJSONString());

        } catch (Exception e) {
            throw e;
        }
    }
}
