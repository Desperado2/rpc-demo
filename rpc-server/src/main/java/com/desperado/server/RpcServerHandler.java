package com.desperado.server;

import com.desperado.common.RpcRequest;
import com.desperado.common.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

public class RpcServerHandler extends ChannelInboundHandlerAdapter {
    private Map<String,Object> serviceBeanMap;

    public RpcServerHandler(Map<String, Object> serviceBeanMap) {
        this.serviceBeanMap = serviceBeanMap;

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("RpcServerHandler.channelRead");
        System.out.println(msg);
        RpcRequest rpcRequest = (RpcRequest) msg;
        RpcResponse rpcResponse = handler(rpcRequest);
        //告诉客户端，关闭socket
        ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE);
    }

    private RpcResponse handler(RpcRequest rpcRequest){
        //创建一个响应
        RpcResponse rpcResponse = new RpcResponse();
        //设置响应Id
        rpcResponse.setResponseId(UUID.randomUUID().toString());
        //设置请求id
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        try {
            //获取类名
            String className = rpcRequest.getClassName();
            //获取方法名
            String methodName = rpcRequest.getMethodName();
            //获取参数类型
            Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
            //获取参数
            Object[] parameters = rpcRequest.getParameters();
            //获取字节码
            Class<?> clz = Class.forName(className);
            //获取实现类
            Object serverBean = serviceBeanMap.get(className);
            if(serverBean == null){
                throw  new RuntimeException(className+"没有找到对应的serviceBean:"+className+":beanMap:"+serviceBeanMap);
            }
            //反射调用方法
            Method method = clz.getMethod(methodName, parameterTypes);
            if(method == null){
                throw  new RuntimeException(methodName+"没有找到对应的方法:"+methodName+":beanMap:"+serviceBeanMap);
            }
            Object result = method.invoke(serverBean, parameters);
            rpcResponse.setResult(result);
            rpcResponse.setSuccess(true);
        }catch (Exception e){
            rpcResponse.setSuccess(false);
            rpcResponse.setThrowable(e);
            e.printStackTrace();
        }
        return rpcResponse;
    }
}
