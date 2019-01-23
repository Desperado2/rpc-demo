package com.desperado.server;

import com.desperado.common.RpcDecoder;
import com.desperado.common.RpcEncoder;
import com.desperado.common.RpcRequest;
import com.desperado.common.RpcResponse;
import com.desperado.register.RpcRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Map;

//RPC服务端启动,实现Spring的感知接口
public class RpcServer implements ApplicationContextAware,InitializingBean {

    //用于保存所有提供服务的方法, 其中key为类的全路径名, value是所有的实现类
    private final Map<String,Object> serviceBeanMap=new HashMap<String,Object>();
    //rpcRegistry 用于注册相关的地址信息
    private RpcRegistry rpcRegistry;
    //服务器地址
    private String serverAddress;

    public RpcRegistry getRpcRegistry() {
        return rpcRegistry;
    }

    public void setRpcRegistry(RpcRegistry rpcRegistry) {
        this.rpcRegistry = rpcRegistry;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    //spring容器启动完成后会执行此方法
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //获取所有标注了注解的对象
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if(MapUtils.isNotEmpty(serviceBeanMap)){
            for (Object object : serviceBeanMap.values()){
                //获取到类的路径名称
                String name = object.getClass().getAnnotation(RpcService.class).value().getName();
                //保存到serviceBeanMap
                this.serviceBeanMap.put(name,object);
            }

        }
        System.out.println("服务器: "+serverAddress +" 提供的服务列表: "+ toString() );
    }

    /**
     * 初始化完成后执行
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        //创建服务器的通信对象
        ServerBootstrap server = new ServerBootstrap();
        //创建异步通讯的事件组，用于建立TCP连接
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        //创建异步通信的事件组。用于处理通道(channel)的I/O事件
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //设置server相关参数
            server.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)  //异步启动 serverSocket
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 初始化通道信息
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new RpcDecoder(RpcRequest.class)) // 解码请求参数
                                .addLast(new RpcEncoder(RpcResponse.class)) // 编码响应消息
                                .addLast(new RpcServerHandler(serviceBeanMap)); // 请求处理
                        }
                    }).option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);

            //获取主机地址,端口
            String host = serverAddress.split(":")[0];
            int port = Integer.parseInt(serverAddress.split(":")[1]);
            //开启异步通信服务
            ChannelFuture channelFuture = server.bind(host, port).sync();
            System.out.println("服务器启动成功:"+channelFuture.channel().localAddress());
            rpcRegistry.createNode(serverAddress);
            System.out.println("向zk注册服务地址信息");
            //等待通信完成
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //关闭socket
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public String toString() {
        String servers = "";
        for (String serverName : serviceBeanMap.keySet()){
            servers += serverName+",";
        }
        return "RpcServer{" +
                servers +
                '}';
    }
}
