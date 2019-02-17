package com.dalomao.dubbo.remote.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 底层通信是基于NIO通讯，即无阻塞IO+SOCKET通信
 */
public class NettyUtil {
    /**
     * 启动netty服务，用于服务端
     * @param port
     * @throws Exception
     */
    public static void startServer(String port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();//boss线程，负责调度
        EventLoopGroup workerGroup = new NioEventLoopGroup();//工作线程，负责工作

        try {
            //固定写法
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new NettyServerInHandler());
                }
            }).option(ChannelOption.SO_BACKLOG, 128);

            ChannelFuture f = b.bind(Integer.parseInt(port)).sync();//绑定同步通道
            System.out.println("------netty server start success------------");
            f.channel().closeFuture().sync();//在这句话同步阻塞等待，直到接收到客户端过来的消息，会进入到NettyServerInHandler.channelRead方法
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //释放线程
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 发送消息
     * @param host
     * @param port
     * @param sendMsg
     * @return
     */
    public static String sendMsg(String host, String port, final String sendMsg) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();//工作线程，负责工作
        final StringBuffer resultMsg = new StringBuffer();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new NettyClientInHandler(resultMsg, sendMsg));
                }
            });

            //这个是连接服务端，一直在等待着服务端的返回消息，返回的信息封装到future，可以监控线程的返回
            ChannelFuture f = b.connect(host, Integer.parseInt(port))
                    .channel()
                    .closeFuture()
                    .await();
            return resultMsg.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
        return null;
    }
}
