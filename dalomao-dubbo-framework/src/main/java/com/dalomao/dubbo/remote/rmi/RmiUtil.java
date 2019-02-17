package com.dalomao.dubbo.remote.rmi;

import com.dalomao.dubbo.loadbalance.NodeInfo;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * rmi通信工具，底层是通过socket，只能java间通讯
 */
public class RmiUtil {

    /**
     * 启动rmi服务端
     * @param host
     * @param port
     * @param id
     */
    public void startRmiServer(String host, String port, String id) {
        try {
            RmiRemote rmiRemote = new RmiRemoteImpl();
            LocateRegistry.createRegistry(Integer.valueOf(port));//固定写法
            Naming.bind("rmi://" + host + ":" + port + "/" + id, rmiRemote);//id可以随便
            System.out.println("rmi server start");
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动rmi客户端
     * @param nodeInfo
     * @param id
     * @return
     */
    public RmiRemote startRmiClient(NodeInfo nodeInfo, String id) {
        String host = nodeInfo.getHost();
        String port = nodeInfo.getPort();

        try {
            return (RmiRemote) Naming.lookup("rmi://" + host + ":" + port + "/" + id);
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return null;
    }
}
