package com.dalomao.dubbo.spring.parse;


import com.dalomao.dubbo.pojo.Protocol;
import com.dalomao.dubbo.pojo.Reference;
import com.dalomao.dubbo.pojo.Registry;
import com.dalomao.dubbo.pojo.Service;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * xsd文件标签类解析
 * spring启动时调用解析的一个总入口，必须继承NamespaceHandlerSupport
 * 在这里分别对涉及到的所有标签进行解析
 */
public class DubboNamespaceHandler extends NamespaceHandlerSupport {
    public void init() {
        //spring会将标签自动注册为bean
        this.registerBeanDefinitionParser("registry", new RegistryBeanDefinitionParse(Registry.class));
        this.registerBeanDefinitionParser("protocol", new ProtocolBeanDefinitionParse(Protocol.class));
        this.registerBeanDefinitionParser("service", new ServiceBeanDefinitionParse(Service.class));
        this.registerBeanDefinitionParser("reference", new ReferenceBeanDefinitionParse(Reference.class));
    }
}
