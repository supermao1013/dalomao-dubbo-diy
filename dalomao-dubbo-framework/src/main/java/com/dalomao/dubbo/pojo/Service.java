package com.dalomao.dubbo.pojo;


import com.dalomao.dubbo.redis.RedisApi;
import com.dalomao.dubbo.registry.BaseRegistryDelegate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Service实例
 */
public class Service implements InitializingBean,ApplicationContextAware {
    private String interfaceName;
    private String ref;
    private String protocol;

    private static ApplicationContext application;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * 实现InitializingBean的重写方法
     * 目的：在service实例化之后进行初始化操作
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        //委托模式
        BaseRegistryDelegate.registry(ref, application);
    }

    /**
     * 实现ApplicationContextAware的重写方法
     * 目的：为了获取spring上下文
     * @param applicationContext
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.application = applicationContext;
    }

    public static ApplicationContext getApplication() {
        return application;
    }
}
