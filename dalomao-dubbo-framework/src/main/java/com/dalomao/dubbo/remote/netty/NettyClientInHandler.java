package com.dalomao.dubbo.remote.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Netty客户端处理类
 */
public class NettyClientInHandler extends ChannelInboundHandlerAdapter {
    private StringBuffer message;//接收服务端传过来的消息
    private String sendMsg;//发送给服务端的消息
    public NettyClientInHandler(StringBuffer message, String sendMsg) {
        this.sendMsg = sendMsg;
        this.message = message;
    }

    /**
     * 当客户端成功连接到netty服务端时，会调用该方法，即通道激活了
     * 在这个方法里面完成消息房发送
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //往netty服务端发送消息
        System.out.println("--------------channelActive--------------------");
        ByteBuf encoded = ctx.alloc().buffer(4 * this.sendMsg.length());
        encoded.writeBytes(this.sendMsg.getBytes());
        ctx.write(encoded);
        ctx.flush();
    }

    /**
     * 当netty服务端有消息过来会触发该方法
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //接收到服务端发送过来的消息
        System.out.println("--------------channelRead--------------------");
        ByteBuf result = (ByteBuf) msg;
        byte[] resultByte = new byte[result.readableBytes()];
        result.readBytes(resultByte);
        System.out.println("server response msg:" + new String(resultByte));

        this.message.append(new String(resultByte));
        result.release();
    }
}
