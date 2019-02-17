package com.dalomao.dubbo.registry;


import com.alibaba.fastjson.JSONObject;
import com.dalomao.dubbo.pojo.Protocol;
import com.dalomao.dubbo.pojo.Registry;
import com.dalomao.dubbo.pojo.Service;
import com.dalomao.dubbo.redis.RedisApi;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis注册中心处理类
 */
public class RedisRegistry implements BaseRegistry {
    public boolean registry(String ref, ApplicationContext application) {
        Protocol protocol = application.getBean(Protocol.class);
        Map<String, Service> services = application.getBeansOfType(Service.class);
        Registry registry = application.getBean(Registry.class);

        //创建redis连接池
        RedisApi.createJedisPool(registry.getAddress());

        for (Map.Entry<String, Service> entry : services.entrySet()) {
            if (entry.getValue().getRef().equals(ref)) {
                //只注册相同的
                /**
                 * 注册格式：
                 * [
                 *     "testServiceImpl" : [{
                 *         "host:port" : {
                 *             "protocol" : {},
                 *             "service" : {}
                 *         }
                 *     },{
                 *         "host:port" : {
                 *             "protocol" : {},
                 *             "service" : {}
                 *         }
                 *     },{
                 *         "host:port" : {
                 *             "protocol" : {},
                 *             "service" : {}
                 *         }
                 *     }]
                 * ]
                 */
                JSONObject jo = new JSONObject();
                jo.put("protocol", JSONObject.toJSONString(protocol));
                jo.put("service", JSONObject.toJSONString(entry.getValue()));

                JSONObject ipport = new JSONObject();
                ipport.put(protocol.getHost() + ":" + protocol.getPort(), jo);

                this.lpush(ref, ipport);

                //启动时，发布消息，让消费端拿到本机的注册信息
                RedisApi.publish("channel" + ref, ipport.toJSONString());
            }
        }

        return false;
    }

    public List<String> getRegistry(String id, ApplicationContext application) {
        try {
            Registry registry = application.getBean(Registry.class);
            RedisApi.createJedisPool(registry.getAddress());
            if (RedisApi.exists(id)) {
                return RedisApi.lrange(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void lpush(String key, JSONObject ipport) {
        if (RedisApi.exists(key)) {
            //存在key的话，再判断是否存在ipport
            Set<String> keys = ipport.keySet();
            String ipportStr = "";
            for (String kk : keys) {
                ipportStr = kk;
            }

            List<String> registryInfo = RedisApi.lrange(key); //获取key的所有内容，list格式
            long index = 0;
            for (String node : registryInfo) {
                index++;
                JSONObject jo = JSONObject.parseObject(node);
                if (jo.containsKey(ipportStr)) {
                    //若已存在ipport，则修改
                    RedisApi.lset(key, index-1, ipport.toJSONString());
                    return;
                }
            }
            RedisApi.lpush(key, ipport.toJSONString());
        } else {
            //首次初始化
            RedisApi.lpush(key, ipport.toJSONString());
        }
    }
}
