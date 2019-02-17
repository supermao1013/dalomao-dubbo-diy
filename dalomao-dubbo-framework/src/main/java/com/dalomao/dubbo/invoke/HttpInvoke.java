package com.dalomao.dubbo.invoke;

import com.alibaba.fastjson.JSONObject;
import com.dalomao.dubbo.loadbalance.LoadBalance;
import com.dalomao.dubbo.loadbalance.NodeInfo;
import com.dalomao.dubbo.pojo.Reference;
import com.dalomao.dubbo.rpc.HttpRequest;

import java.util.List;

/**
 * http调用过程
 */
public class HttpInvoke implements Invoke {
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
            //然后根据methodName和methodType获取实例的method对象
            //然后反射调用method方法
            JSONObject sendParam = new JSONObject();
            sendParam.put("methodName", invocation.getMethod().getName());//方法名
            sendParam.put("methodParams", invocation.getObjs());//方法参数
            sendParam.put("paramTypes", invocation.getMethod().getParameterTypes());//参数类型
            sendParam.put("serviceId", reference.getId());//服务ID

            //http://127.0.0.1:8023/xxx/xxx
            String url = "http://" + nodeInfo.getHost() + ":" + nodeInfo.getPort() + nodeInfo.getContextpath();

            //http调用远程服务，远程服务端使用servlet接收http请求
            result = HttpRequest.sendPost(url, sendParam.toJSONString());
        } catch (Exception e) {
            throw e;
        }

        return result;
    }
}
