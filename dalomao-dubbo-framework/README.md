# 手写dubbo简易框架
手写简易dubbo框架，声明一点：该框架只是用来学习用，加深对dubbo框架的理解

## 涉及知识点
* 与spring整合、spring相关接口、自定义标签
* 涉及模式：策略、委托、观察、代理
* 注册中心redis的服务注册、服务获取
* 消费端获取服务的动态代理，拿到的是代理实例
* 消费端如何调用服务端、底层通信协议：http、rmi、netty
* 负载均衡、集群容错、节点自动加入、节点故障剔除

## 框架编写流程
### 和spring整合
当spring容器启动时会去加载所有jar包中/META-INF中的解析类，这里就是利用这个特性和spring进行整合的，而这个标签解析类就是整个dubbo初始化的入口。
* 1.自定义Spring标签：在resources/META-INF下定义几个标签文件，用于spring启动时自动加载对应的标签解析类
* 2.标签解析类：DubboNamespaceHandler。该类是解析dubbo涉及的相关标签的总入口，如：registry、protocol、reference、service，在该入口中分别对这四种标签进行解析，每个标签有对应各自的解析类，如下
    > * RegistryBeanDefinitionParse：registry标签的解析类
    > * ProtocolBeanDefinitionParse：protocol标签的解析类
    > * ReferenceBeanDefinitionParse：reference标签的解析类
    > * ServiceBeanDefinitionParse：service标签的解析类

### 服务端-发布服务到注册中心
本demo使用redis作为注册中心，当解析每一条Service标签时，都会被实例化为bean。然后在实例化之后，初始化之前，将其注册到redis中。考虑到集群的问题，同一个服务可能会有多个不同的机器注册，因此必须定义好redis的存储格式。
注册格式：
```json
[
	"testServiceImpl" : [{
		"host:port" : {
			"protocol" : {},
			"service" : {}
		}
	},{
		"host:port" : {
			"protocol" : {},
			"service" : {}
		}
	},{
		"host:port" : {
			"protocol" : {},
			"service" : {}
		}
	}]
]
```
注意：这里的key为Service标签中定义的属性id，而消费端也是通过该id去redis注册中心中获取

### 消费端-从注册中心获取服务
同样的，在消费端解析每一个Reference标签时，实例化之后，初始化之前，都会使用id去redis注册中心中查找对应的服务。
注意：这里可能会拿到多个服务列表，因为有可能是集群部署
    
### 消费端-代理类注入
当消费者使用reference标签订阅服务时，且在代码中使用@Autowried注入该bean的时候，其实注入的是一个代理类。因为在消费端的spring容器中，并没有真正的该服务对应的bean。

而且在使用reference标签过程中，可以指定protocol底层通信协议，如果没有指定则使用protocol标签中指定的协议。

以上两点通过如下进行实现：
* 解析Reference标签时，让Reference实现FactoryBean并重写getObjectType()和getObject()方法，这样在使用@Autowried注入时会去spring容器中通过getObjectType()方法寻找类型一致的bean，然后再调用getObject()获取实例
* getObject()：在这个方法中返回代理类，通过jdk自带的Proxy创建代理类
* 那么，具体返回什么样的代理类呢？根据reference标签中的属性protocol，若没指定则使用protocol标签中指定的协议，若都没有指定则默认使用http协议。而返回的代理类以为协议的不同而不同
* 该框架中支持http、rmi、netty通信协议，对应的代理类分别为HttpInvoke、RmiInvoke、NettyInvoke

### 底层通信协议
消费端在@Autowried注入一个代理类时，该代理类就已经是指定了通信协议的代理类。当调用了服务的某个方法时，具体代理类就会通过底层通信协议将相关信息发送给服务端。

该demo支持三种协议：http、rmi、netty。
* http
    
    当reference中的protocol定义为http时，会使用http调用服务端，传输参数：服务ID、方法名、参数值、参数值类型
    
    然后服务端怎么接收呢？定义一个DispatcherServlet类继承HttpServlet，通过该servlet来接收消费端发送过来的数据。
    
    这种http调用的方式，需要消费端是web工程项目，然后在web.xml中引入DispatcherServlet，这也是http方式调用不便之处
* rmi
    
    当reference中的protocol定义为http时，会使用rmi调用服务端，传输参数：服务ID、方法名、参数值、参数值类型
    
    服务端启动rmi监听消费端发送过来的数据
    
    rmi底层是基于socket通信的，只限于java间通信，不跨语言
    
* netty
    
    当reference中的protocol定义为netty时，会使用netty调用服务端，传输参数：服务ID、方法名、参数值、参数值类型
    
    服务端在启动时，会启动netty服务监听消费端发送过来的数据
    
    netty底层是基于NIO通信的，跨平台
    
### 负载均衡
本demo提供了随机、轮询两种负载均衡策略，比较简单，对应的代码为RandomLoadBalance、RoundRobinLoadBalance
    
### 集群容错
为了程序的健壮性，需要引入容错机制。消费端通过reference标签中的cluster和retries属性配置容错规则。  
* cluster：指定容错策略，目前提供三种策略，failover（若调用失败则切换到其他节点）、failfast（若调用失败则直接失败）、failsafe（若调用失败则忽略）
* retries：当cluster指定为failover时的重试次数

### 节点自动加入
采用redis发布订阅功能

当消费端启动时，在初始化每一个reference标签时，订阅一个redis通道，该通道是用来接收服务端发布的服务

当服务端启动时，在初始化每一个service标签时，发布一个redis消息，让消费端接受到该服务，从而更新消费端的服务列表

### 节点故障剔除
本demo并没有实现，可以大致说下思路：
* 思路1：消费端在调用服务端应用时，如果调用失败，或者失败达到一定次数，那么通知redis去剔除该节点的服务
* 思路2：服务端发布服务的时候，往redis写一个过期时间，并每个一段时间去刷新。服务端每隔一段时间去服务中心获取服务列表。
