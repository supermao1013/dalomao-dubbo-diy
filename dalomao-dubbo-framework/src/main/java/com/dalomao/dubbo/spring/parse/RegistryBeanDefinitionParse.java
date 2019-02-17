package com.dalomao.dubbo.spring.parse;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Registry标签解析类
 * 必须实现BeanDefinitionParser
 */
public class RegistryBeanDefinitionParse implements BeanDefinitionParser{

    private Class<?> beanClass;

    public RegistryBeanDefinitionParse(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        //Spring将beanClass实例化
        beanDefinition.setBeanClass(this.beanClass);
        beanDefinition.setLazyInit(false);
        String protocol = element.getAttribute("protocol");
        String address = element.getAttribute("address");

        if (protocol == null || "".equals(protocol)) {
            throw new RuntimeException("Registry protocol 不能为空");
        }
        if (address == null || "".equals(address)) {
            throw new RuntimeException("Registry address 不能为空");
        }

        //设置bean中的成员变量：beanClass中必须有以下属性，不然会设置报错
        beanDefinition.getPropertyValues().addPropertyValue("protocol", protocol);
        beanDefinition.getPropertyValues().addPropertyValue("address", address);

        //注册bean，引用时需要使用类型匹配才可以引用
        parserContext.getRegistry().registerBeanDefinition("Registry" + address, beanDefinition);

        return beanDefinition;
    }
}
