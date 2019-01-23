package com.desperado.register;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//向注册中心设置地址信息，服务端使用
public class RpcRegistry {
    public static final  Logger LOGGER = LoggerFactory.getLogger(RpcRegistry.class);
    //zk地址信息
    private String registerAddress;
    //zk客户端
    private ZooKeeper zooKeeper;

    public String getRegisterAddress() {
        return registerAddress;
    }

    public void setRegisterAddress(String registerAddress) {
        this.registerAddress = registerAddress;
    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public void setZooKeeper(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    //创建一个客户端程序
    public void createNode(String data) throws Exception{
        zooKeeper = new ZooKeeper(registerAddress, Constant.SESSION_TIMEOUT, new Watcher() {
            public void process(WatchedEvent watchedEvent) {

            }
        });
        if(zooKeeper != null){
           try {
               //判断注册地址是否存在
               Stat stat = zooKeeper.exists(Constant.REGISTER_PATH, false);
               if(stat == null){
                   //如果不存在，创建一个持久的节点目录
                   zooKeeper.create(Constant.REGISTER_PATH,null,ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
               }
               //创建一个临时的序列节点，保存数据信息
               zooKeeper.create(Constant.DATE_PATH,data.getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
           }catch (Exception e){
               LOGGER.error("",e);
               e.printStackTrace();
           }
        }else{
            LOGGER.debug("zk connect is null");
        }
    }

    //测试
    public static void main(String[] args) throws Exception{
        RpcRegistry rpcRegistry = new RpcRegistry();
        rpcRegistry.setRegisterAddress("127.0.0.1:2181");
        rpcRegistry.createNode("testdata");
        //让程序等待输入。程序一直运行中
        System.in.read();
    }
}
