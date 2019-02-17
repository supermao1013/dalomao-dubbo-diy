package com.dalomao.dubbo.spring.parse;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Service标签解析类
 * 必须实现BeanDefinitionParser
 */
public class ServiceBeanDefinitionParse implements BeanDefinitionParser{

    private Class<?> beanClass;

    public ServiceBeanDefinitionParse(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        //Spring将beanClass实例化
        beanDefinition.setBeanClass(this.beanClass);
        beanDefinition.setLazyInit(false);
        String interfaceName = element.getAttribute("interface");
        String ref = element.getAttribute("ref");
        String protocol = element.getAttribute("protocol");

        if (interfaceName == null || "".equals(interfaceName)) {
            throw new RuntimeException("Service interface 不能为空");
        }
        if (ref == null || "".equals(ref)) {
            throw new RuntimeException("Service ref 不能为空");
        }

        //beanClass中必须有以下属性，不然会设置报错
        beanDefinition.getPropertyValues().addPropertyValue("interfaceName", interfaceName);
        beanDefinition.getPropertyValues().addPropertyValue("ref", ref);
        beanDefinition.getPropertyValues().addPropertyValue("protocol", protocol);

        //注册bean，引用时需要使用类型匹配才可以引用
        parserContext.getRegistry().registerBeanDefinition("Service" + ref + interfaceName, beanDefinition);

        return beanDefinition;
    }
}
