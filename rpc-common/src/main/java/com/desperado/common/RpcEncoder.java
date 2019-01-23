package com.desperado.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 对传递的信息进行编码，因为是请求/响应对象的传递，先编码为字节数据，发送到
 * 服务端再解码
 */
public class RpcEncoder extends MessageToByteEncoder {
    //传递的数据的对象类型
    private Class genericClass;
    public RpcEncoder(Class genericClass){
        this.genericClass = genericClass;
    }
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if(genericClass.isInstance(msg)){
            //序列化为字节数组
            byte[] bytes = SerializationUtil.serialize(msg);
            //把数据写入下一个通道或者发送到服务端
            out.writeBytes(bytes);
        }
    }
}
