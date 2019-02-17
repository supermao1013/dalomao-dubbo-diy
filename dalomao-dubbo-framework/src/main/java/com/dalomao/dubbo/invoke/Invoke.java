package com.dalomao.dubbo.invoke;

/**
 * 返回String，用json的方式进行通信
 */
public interface Invoke {
    String invoke(Invocation invocation) throws Exception;
}
