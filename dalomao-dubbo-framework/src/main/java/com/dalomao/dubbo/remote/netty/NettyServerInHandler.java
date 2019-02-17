package com.dalomao.dubbo.remote.netty;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dalomao.dubbo.pojo.Service;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Netty服务端处理类
 */
public class NettyServerInHandler extends ChannelInboundHandlerAdapter {
    /**
     * netty客户端有消息传送过来时，会调用此方法
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //从buffer缓冲区拿信息
        ByteBuf result = (ByteBuf)msg;
        byte[] resultByte = new byte[result.readableBytes()];
        result.readBytes(resultByte);

        String resultStr = new String(resultByte);
        System.out.println(resultStr);

        result.release();//拿完信息释放缓存

        String response = this.invokeService(resultStr);

        //将返回信息set到buffer缓冲区
        ByteBuf encoded = ctx.alloc().buffer(4 * response.length());
        encoded.writeBytes(response.getBytes());
        ctx.writeAndFlush(encoded);
        ctx.close();
    }

    private String invokeService(String param) {
        try {
            JSONObject requestParam = JSONObject.parseObject(param);
            String serviceId = requestParam.getString("serviceId");
            String methodName = requestParam.getString("methodName");
            JSONArray paramTypes = requestParam.getJSONArray("paramTypes");
            //对应方法参数
            JSONArray methodParamJa = requestParam.getJSONArray("methodParams");
            Object[] objs = null;
            if (methodParamJa != null) {
                objs = new Object[methodParamJa.size()];
                int i = 0;
                for (Object o : methodParamJa) {
                    objs[i++] = o;
                }
            }

            //拿到spring上下文
            ApplicationContext application = Service.getApplication();
            //服务层的实例
            Object serviceBean = application.getBean(serviceId);
            //获取实例的方法，并考虑到方法的重载情况
            Method method = getMethod(serviceBean, methodName, paramTypes);
            if (method != null) {
                Object result = method.invoke(serviceBean, objs);
                //返回
                return result.toString();
            } else {
                return "-------------no such method-------------";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Method getMethod(Object bean, String methodName, JSONArray paramTypes) {
        //获取该bean的所有方法
        Method[] methods = bean.getClass().getMethods();

        //获取方法名匹配的所有方法（重载方法）
        List<Method> matchingMethodList = new ArrayList<Method>();
        for (Method method : methods) {
            if (method.getName().trim().equals(methodName)) {
                matchingMethodList.add(method);
            }
        }

        if (matchingMethodList.size() == 1) {
            return matchingMethodList.get(0);
        }

        //存在多个重载方法，则根据参数类型筛选出最符合的那个方法
        for (Method method : matchingMethodList) {
            boolean isSameType = true;
            Class<?>[] types = method.getParameterTypes();
            //参数类型个数不匹配
            if (types.length != paramTypes.size()) {
                continue;
            }

            //匹配参数类型
            for (int i=0; i<types.length; i++) {
                if (!types[i].toString().contains(paramTypes.getString(i))) {
                    isSameType = false;
                    break;
                }
            }

            if (isSameType) {
                return method;
            }
        }
        return null;
    }
}
