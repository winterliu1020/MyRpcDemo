package me.liuwentao.rpc.core.Client;

import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Entity.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by liuwentao on 2021/6/10 16:07
 *
 * 客户端实现：动态代理
 * 客户端并没有API接口的实现类，那客户端怎么去调用API接口对应的实现方法呢？
 */
public class RpcClientProxy implements InvocationHandler {
    private String host;
    private Integer port;

    public RpcClientProxy(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    // 这里用jdk代理模式，极大的简化了代码量（如果不用代理模式，你还得自己手动去new一个rpcRequest对象，而且你调用不同的rcp请求还得传递不同的rpc参数）：
    // 这样你只需要在客户端传服务接口的class对象，Proxy.newProxyInstance()会帮你生成一个实现这个接口的子类，而这个子类调用接口中的方法又会通过发送rpcRequest去调用在服务器上的真正的实例方法
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder().interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .build();
        RpcClient rpcClient = new RpcClient();
        return ((RpcResponse)rpcClient.sendRequest(rpcRequest, host, port)).getData();
    }
}
