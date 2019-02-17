package com.dalomao.dubbo.pojo;


import com.dalomao.dubbo.remote.netty.NettyUtil;
import com.dalomao.dubbo.remote.rmi.RmiUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Protocol实例
 */
public class Protocol implements InitializingBean,ApplicationListener<ContextRefreshedEvent> {
    private String name;
    private String host;
    private String port;
    private String contextpath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getContextpath() {
        return contextpath;
    }

    public void setContextpath(String contextpath) {
        this.contextpath = contextpath;
    }

    /**
     * 实现InitializingBean类
     * 目的：spring启动成功后进行初始化动作
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        if (this.name.equalsIgnoreCase("rmi")) {
            //若协议使用rmi，则必须启动rmi服务端
            RmiUtil rmi = new RmiUtil();
            rmi.startRmiServer(this.host, this.port, "dalomao");
        }

        /* 不能在初始化时启动netty，会导致netty阻塞从而引起整个spring为完全启动成功
            因此将netty启动放到spring完全启动之后进行
        if (this.name.equalsIgnoreCase("netty")) {
            try {
                NettyUtil.startServer(this.port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }   */
    }

    /**
     * 极其重要！！！
     * 实现ApplicationListener<ContextRefreshedEvent>
     * 监听ContextRefreshedEvent事件，该事件表示spring完全启动成功后的事件
     * 等spring完全启动成功后会调用该方法，在该方法中完成netty的启动
     *
     * @param event
     */
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //只监听ContextRefreshedEvent事件，若是其他事件，表示spring还未完全启动成功，此时直接返回
        if (!ContextRefreshedEvent.class.getName().equals(event.getClass().getName())) {
            return;
        }

        if (!this.name.equalsIgnoreCase("netty")) {
            return;
        }

        //重开一个线程用来启动netty服务端，否则还是会阻塞在接收消息的那行代码里
        new Thread(new Runnable() {
            public void run() {
                try {
                    NettyUtil.startServer(port);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
