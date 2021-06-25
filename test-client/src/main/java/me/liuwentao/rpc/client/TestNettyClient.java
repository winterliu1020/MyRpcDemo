package me.liuwentao.rpc.client;

import me.liuwentao.rpc.api.HelloObject;
import me.liuwentao.rpc.api.HelloService;
import me.liuwentao.rpc.core.Netty.client.NettyClient;
import me.liuwentao.rpc.core.RpcClient;
import me.liuwentao.rpc.core.RpcClientProxy;

/**
 * Created by liuwentao on 2021/6/16 15:20
 */
public class TestNettyClient {
    public static void main(String[] args) {
        RpcClient nettyClient = new NettyClient("127.0.0.1", 8084);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(nettyClient);
        // 拿到某一个接口的代理
        Object result = rpcClientProxy.getProxy(HelloService.class).sayHello(new HelloObject(2, "hi"));
        System.out.println(result);
    }
}
