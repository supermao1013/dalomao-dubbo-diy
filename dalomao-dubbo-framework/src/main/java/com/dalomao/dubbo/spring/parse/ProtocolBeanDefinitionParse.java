package com.dalomao.dubbo.spring.parse;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Protocol标签解析类
 * 必须实现BeanDefinitionParser
 */
public class ProtocolBeanDefinitionParse implements BeanDefinitionParser{

    private Class<?> beanClass;

    public ProtocolBeanDefinitionParse(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        //Spring将beanClass实例化
        beanDefinition.setBeanClass(this.beanClass);
        beanDefinition.setLazyInit(false);
        String name = element.getAttribute("name");
        String host = element.getAttribute("host");
        String port = element.getAttribute("port");
        String contextpath = element.getAttribute("contextpath");

        if (name == null || "".equals(name)) {
            throw new RuntimeException("Protocol name 不能为空");
        }
        if (host == null || "".equals(host)) {
            throw new RuntimeException("Protocol host 不能为空");
        }
        if (port == null || "".equals(port)) {
            throw new RuntimeException("Protocol port 不能为空");
        }

        //beanClass中必须有以下属性，不然会设置报错
        beanDefinition.getPropertyValues().addPropertyValue("name", name);
        beanDefinition.getPropertyValues().addPropertyValue("host", host);
        beanDefinition.getPropertyValues().addPropertyValue("port", port);
        beanDefinition.getPropertyValues().addPropertyValue("contextpath", contextpath);

        //注册bean，引用时需要使用类型匹配才可以引用
        parserContext.getRegistry().registerBeanDefinition("Protocol" + host + port, beanDefinition);

        return beanDefinition;
    }
}
