package com.desperado.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 对传递的消息进行解码, 接受到的数据是字节数组,需要把数组转换为对应的请求/响应消息对象
 */
public class RpcDecoder extends ByteToMessageDecoder {
    //传递的数据的对象类型
    private Class<?> genericClass;
    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int size = in.readableBytes();
        if(size <4){
            //保证所有的数据都接收完成
            return;
        }
        byte[] bytes = new byte[size];
        //读取接收到的数据
        in.readBytes(bytes);
        //反序列化对象
        Object obj = SerializationUtil.deserialize(bytes, genericClass);
        //输出对象
        out.add(obj);
        //刷新缓存
        ctx.flush();
    }
}
