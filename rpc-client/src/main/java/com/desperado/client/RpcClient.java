package com.desperado.client;

import com.desperado.common.RpcDecoder;
import com.desperado.common.RpcEncoder;
import com.desperado.common.RpcRequest;
import com.desperado.common.RpcResponse;
import com.desperado.register.RpcDiscover;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

//RPC通信客户端,往服务端发送请求,并且接受服务端的响应
//RPC通信客户端,启动RPC通信服务,创建TCP连接,发送请求,接受响应
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {
    // 消息响应对象
    private RpcResponse rpcResponse;
    //消息请求对象
    private RpcRequest rpcRequest;
    //同步锁 资源对象
    private Object object = new Object();
    //获取服务地址列表
    private RpcDiscover rpcDiscover;

    public RpcClient(RpcRequest rpcRequest, RpcDiscover rpcDiscover){
        this.rpcRequest = rpcRequest;
        this.rpcDiscover = rpcDiscover;
    }

    public RpcResponse getRpcResponse() {
        return rpcResponse;
    }

    public void setRpcResponse(RpcResponse rpcResponse) {
        this.rpcResponse = rpcResponse;
    }

    public RpcRequest getRpcRequest() {
        return rpcRequest;
    }

    public void setRpcRequest(RpcRequest rpcRequest) {
        this.rpcRequest = rpcRequest;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        this.rpcResponse=msg;//响应消息
        synchronized (object){
            //刷新缓存
            ctx.flush();
            //唤醒等待
            object.notifyAll();
        }
    }

    public RpcResponse send() throws Exception{
        //创建一个socket对象
        Bootstrap client = new Bootstrap();
        //创建一个事件组，负责通道(channel)的I/O处理
        NioEventLoopGroup loopGroup = new NioEventLoopGroup();
        try {
            //设置参数
            client.group(loopGroup).channel(NioSocketChannel.class) // 异步通信
                .handler(new ChannelInitializer<SocketChannel>() {  //
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new RpcEncoder(RpcRequest.class)) //编码
                            .addLast(new RpcDecoder(RpcResponse.class)) //解码
                            .addLast(RpcClient.this);//发送请求对象

                    }
                }).option(ChannelOption.SO_KEEPALIVE,true);
            //获取一个服务器的地址
            String discover = rpcDiscover.discover();
            //获取地址  端口
            String host = discover.split(":")[0];
            int port = Integer.parseInt(discover.split(":")[1]);
            ChannelFuture channelFuture = client.connect(host, port).sync();
            System.out.println("客户端准备发送数据:"+rpcRequest);
            channelFuture.channel().writeAndFlush(rpcRequest).sync();
            synchronized (object){
                //线程等待，等待客户端响应
                object.wait();
            }
            if(rpcResponse != null){
                //等待服务端关闭socket
                channelFuture.channel().closeFuture().sync();
            }
            return rpcResponse;
        }finally {
            loopGroup.shutdownGracefully();//优雅关闭socket
        }
    }

    /**
     * 异常处理
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
