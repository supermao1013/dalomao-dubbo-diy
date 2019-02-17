package com.dalomao.dubbo.redis;

import com.alibaba.fastjson.JSONObject;
import com.dalomao.dubbo.pojo.Reference;
import com.dalomao.dubbo.pojo.Service;
import org.springframework.context.ApplicationContext;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 
 * redis的发布与订阅，跟我们的activemq中的topic消息消费机制差不多
 * 是一个广播形式的消费消息
 */

public class RedisServerRegistry extends JedisPubSub {
    private String id;
    private ApplicationContext application;

    public RedisServerRegistry(String id, ApplicationContext application) {
        super();
        this.id = id;
        this.application = application;
    }
    
    /* 
     * @see 频道其实就是队列，当往里面发布消息的时候，这个方法就会触发
     */
    @Override
    public void onMessage(String channel, String message) {
        List<String> newRegistryInfo = new ArrayList<String>();
        JSONObject newJo = JSONObject.parseObject(message);
        String newHostPort = newJo.getJSONObject("protocol").getString("host") + ":" + newJo.getJSONObject("protocol").getString("port");

        //更新同一个reference
        Map<String, Reference> references = application.getBeansOfType(Reference.class);
        for (Map.Entry<String, Reference> entry : references.entrySet()) {
            if (id.equals(entry.getValue().getId())) {
                List<String> oldRegistryInfo = entry.getValue().getRegistryInfo();
                for (String old : oldRegistryInfo) {
                    JSONObject oldJo = JSONObject.parseObject(old);
                    JSONObject protocol = oldJo.getJSONObject("protocol");
                    String oldHostPort = oldJo.getJSONObject("protocol").getString("host") + ":" + oldJo.getJSONObject("protocol").getString("port");

                    if (oldHostPort.equals(newHostPort)) {
                        newRegistryInfo.add(message);
                    } else {
                        newRegistryInfo.add(old);
                    }
                }

                //更新
                entry.getValue().setRegistryInfo(newRegistryInfo);

                return;
            }
        }

    }
    
    @Override
    public void subscribe(String... channels) {
        // TODO Auto-generated method stub
        super.subscribe(channels);
    }
    
}
