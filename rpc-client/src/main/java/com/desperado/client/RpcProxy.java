package com.desperado.client;

import com.desperado.common.RpcRequest;
import com.desperado.common.RpcResponse;
import com.desperado.register.RpcDiscover;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

//动态代理类,用于获取到每个类的代理对象
// 对于被代理对象的所有的方法调用都会执行invoke方法
public class RpcProxy {

    //获取server地址
    private RpcDiscover rpcDiscover;

    public RpcDiscover getRpcDiscover() {
        return rpcDiscover;
    }

    public void setRpcDiscover(RpcDiscover rpcDiscover) {
        this.rpcDiscover = rpcDiscover;
    }

    public <T> T getInstance(Class<T> interfaceClass){
        T instance = (T)Proxy.newProxyInstance(interfaceClass.getClassLoader(),new Class<?>[]{interfaceClass}, new InvocationHandler(){

            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //创建请求对象
                RpcRequest rpcRequest = new RpcRequest();
                //获取类名
                String className = method.getDeclaringClass().getName();
                //获取参数类型
                Class<?>[] parameterTypes = method.getParameterTypes();
                rpcRequest.setRequestId(UUID.randomUUID().toString());
                rpcRequest.setClassName(className);
                rpcRequest.setMethodName(method.getName());
                rpcRequest.setParameters(args);
                rpcRequest.setParameterTypes(parameterTypes);
                RpcResponse rpcResponse = new RpcClient(rpcRequest, rpcDiscover).send();
                return rpcResponse.getResult();
            }
        });
        return instance;
    }
}
