package com.dalomao.dubbo.pojo;


import com.dalomao.dubbo.cluster.Cluster;
import com.dalomao.dubbo.cluster.FailfastClusterInvoke;
import com.dalomao.dubbo.cluster.FailoverClusterInvoke;
import com.dalomao.dubbo.cluster.FailsaveClusterInvoke;
import com.dalomao.dubbo.invoke.HttpInvoke;
import com.dalomao.dubbo.invoke.Invoke;
import com.dalomao.dubbo.invoke.NettyInvoke;
import com.dalomao.dubbo.invoke.RmiInvoke;
import com.dalomao.dubbo.loadbalance.LoadBalance;
import com.dalomao.dubbo.loadbalance.RandomLoadBalance;
import com.dalomao.dubbo.loadbalance.RoundRobinLoadBalance;
import com.dalomao.dubbo.proxy.advice.InvokeInvocationHandler;
import com.dalomao.dubbo.redis.RedisApi;
import com.dalomao.dubbo.redis.RedisServerRegistry;
import com.dalomao.dubbo.registry.BaseRegistryDelegate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reference实例
 */
public class Reference implements Serializable,FactoryBean,ApplicationContextAware,InitializingBean {
    private static final long serialVersionUID = 996667683897885968L;

    private String id;
    private String interfaceName;
    private String protocol;
    private String loadbalance;
    private String retries;
    private String cluster;

    private Invoke invoke;

    private ApplicationContext application;

    private static Map<String, Invoke> invokes = new HashMap<String, Invoke>();

    /**
     * 对应生产者提供的服务列表，可能一个，可能多个
     */
    private List<String> registryInfo = new ArrayList<String>();

    /**
     * 负载均衡算法
     */
    private static Map<String, LoadBalance> loadbalances = new HashMap<String, LoadBalance>();

    /**
     * 集群容错
     */
    private static Map<String, Cluster> clusters = new HashMap<String, Cluster>();

    static {
        invokes.put("http", new HttpInvoke());
        invokes.put("rmi", new RmiInvoke());
        invokes.put("netty", new NettyInvoke());

        loadbalances.put("random", new RandomLoadBalance());
        loadbalances.put("roundrob", new RoundRobinLoadBalance());

        clusters.put("failover", new FailoverClusterInvoke());
        clusters.put("failfast", new FailfastClusterInvoke());
        clusters.put("failsafe", new FailsaveClusterInvoke());
    }

    public Reference() {
        System.out.println("Reference 构造函数调用");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getLoadbalance() {
        return loadbalance;
    }

    public void setLoadbalance(String loadbalance) {
        this.loadbalance = loadbalance;
    }

    public synchronized List<String> getRegistryInfo() {
        return registryInfo;
    }

    public synchronized void setRegistryInfo(List<String> registryInfo) {
        this.registryInfo = registryInfo;
    }

    public static Map<String, LoadBalance> getLoadbalances() {
        return loadbalances;
    }

    public String getRetries() {
        return retries;
    }

    public void setRetries(String retries) {
        this.retries = retries;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public static Map<String, Cluster> getClusters() {
        return clusters;
    }

    public static void setClusters(Map<String, Cluster> clusters) {
        Reference.clusters = clusters;
    }

    public static void setLoadbalances(Map<String, LoadBalance> loadbalances) {
        Reference.loadbalances = loadbalances;
    }

    /**
     * 实现FactoryBean的方法，则spring初始化时候会调用getObject，即在ApplicationContext.getBean("xxx")时调用
     * 当然，以上的前提是getObjectType返回的类型一致
     * getObject这个方法的返回值，会交给spring容器进行管理
     * 在getObject这个方法里，返回的是interfaceName这个接口的代理
     * @return
     * @throws Exception
     */
    public Object getObject() throws Exception {
        System.out.println("返回interfaceName代理对象");

        if (protocol != null && !"".equals(protocol)) {
            //根据通信协议返回不同的代理对象
            invoke = invokes.get(protocol);
        } else {
            //若Reference标签中没有定义protocol协议，则需要获取Protocol标签中设置的协议
            //Protocol 这个实例是在spring容器中，通过类型获得
            Protocol protocol = application.getBean(Protocol.class);
            if (protocol != null) {
                invoke = invokes.get(protocol.getName());
            } else {
                //没有则给默认的http协议
                invoke = invokes.get("http");
            }
        }

        //返回代理对象InvokeInvocationHandler
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class<?>[] {Class.forName(interfaceName)},
                new InvokeInvocationHandler(invoke, this));
    }

    /**
     * 返回接口类型
     * 在调用ApplicationContext.getBean("xxx")时，会先去容器中判断所有bean的getObjectType是否符合
     * 若符合，则继续调用getObject方法，返回代理对象
     * @return
     */
    public Class<?> getObjectType() {
        if (interfaceName != null && !"".equals(interfaceName)) {
            try {
                return Class.forName(interfaceName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 是否单例
     * 代理实例是单例的
     * @return
     */
    public boolean isSingleton() {
        return true;
    }

    /**
     * 实现ApplicationContextAware，就可以拿到Spring上下文
     * @param applicationContext
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.application = applicationContext;
    }

    /**
     * 实现InitializingBean，在启动后初始化去注册中心拿取配置信息
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        List<String> registryInfo = BaseRegistryDelegate.getRegistry(id, application);
        System.out.println(registryInfo);
        this.setRegistryInfo(registryInfo);

        //订阅消息，当有新的服务端加入的时候，服务端会发布该消息
        RedisApi.subsribe("channel" + id, new RedisServerRegistry(id, application));
    }

}
