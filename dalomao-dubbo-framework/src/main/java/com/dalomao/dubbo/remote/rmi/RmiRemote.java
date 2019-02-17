package com.dalomao.dubbo.remote.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 使用rmi功能必须继承Remote（规范）
 */
public interface RmiRemote extends Remote {

    /**
     * 必须抛出异常RemoteException（规范）
     * @param param
     * @return
     * @throws RemoteException
     */
    public String invoke(String param) throws RemoteException;
}
