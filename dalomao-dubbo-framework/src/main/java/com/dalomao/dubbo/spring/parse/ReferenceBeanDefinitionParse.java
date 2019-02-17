package com.dalomao.dubbo.spring.parse;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Reference标签解析类
 * 必须实现BeanDefinitionParser
 */
public class ReferenceBeanDefinitionParse implements BeanDefinitionParser{

    private Class<?> beanClass;

    public ReferenceBeanDefinitionParse(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        //Spring将beanClass实例化
        beanDefinition.setBeanClass(this.beanClass);
        beanDefinition.setLazyInit(false);
        String id = element.getAttribute("id");
        String interfaceName = element.getAttribute("interface");
        String protocol = element.getAttribute("protocol");
        String loadbalance = element.getAttribute("loadbalance");
        String retries = element.getAttribute("retries");
        String cluster = element.getAttribute("cluster");

        if (id == null || "".equals(id)) {
            throw new RuntimeException("Reference id 不能为空");
        }
        if (interfaceName == null || "".equals(interfaceName)) {
            throw new RuntimeException("Reference interface 不能为空");
        }
        if (protocol == null || "".equals(protocol)) {
            throw new RuntimeException("Reference protocol 不能为空");
        }
        if (loadbalance == null || "".equals(loadbalance)) {
            throw new RuntimeException("Reference loadbalance 不能为空");
        }

        //beanClass中必须有以下属性，不然会设置报错
        beanDefinition.getPropertyValues().addPropertyValue("id", id);
        beanDefinition.getPropertyValues().addPropertyValue("interfaceName", interfaceName);
        beanDefinition.getPropertyValues().addPropertyValue("protocol", protocol);
        beanDefinition.getPropertyValues().addPropertyValue("loadbalance", loadbalance);
        beanDefinition.getPropertyValues().addPropertyValue("retries", retries);
        beanDefinition.getPropertyValues().addPropertyValue("cluster", cluster);

        //注册bean，引用时需要使用类型匹配才可以引用
        parserContext.getRegistry().registerBeanDefinition("Reference" + id, beanDefinition);

        return beanDefinition;
    }
}
