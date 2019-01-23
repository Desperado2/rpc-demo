package com.desperado.register;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//从注册中心获取服务端的地址信息，客户端使用
public class RpcDiscover {
    public static final Logger LOGGER = LoggerFactory.getLogger(RpcDiscover.class);
    //服务端地址，zk的地址
    private String registerAddress;
    //获取到的所有提供服务的服务器的列表
    private volatile List<String> dataList = new ArrayList<String>();
    //初始化zk客户端
    private ZooKeeper zooKeeper = null;

    public String getRegisterAddress() {
        return registerAddress;
    }

    public void setRegisterAddress(String registerAddress) {
        this.registerAddress = registerAddress;
    }

    public RpcDiscover(String registerAddress) throws Exception{
        this.registerAddress = registerAddress;
        zooKeeper = new ZooKeeper(registerAddress, Constant.SESSION_TIMEOUT, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged){
                    //监听zk服务器列表变化
                    watchNode();
                }
            }
        });
        watchNode();
    }

    //从dataList随机获取一个可用的地址的服务端的可用的地址信息
    public String discover(){
        int size = dataList.size();
        if(size >0){
            int index = new Random().nextInt(size);
            return dataList.get(index);
        } throw new RuntimeException("没有找到对应服务器");
    }

    //监听服务端的列表变化
    private void watchNode(){
        try {
            //获取子节点信息
            List<String> children = zooKeeper.getChildren(Constant.REGISTER_PATH, true);
            List<String> dataList = new ArrayList<String>();
            for (String child :children){
                byte[] data = zooKeeper.getData(Constant.REGISTER_PATH + "/" + child, false, null);
                dataList.add(new String(data));
            }
            this.dataList = dataList;
        }catch (Exception e){
            LOGGER.error("",e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception{
        //打印列表
        System.out.println(new RpcDiscover("127.0.0.1:2181").discover());
        System.in.read();
    }
}
