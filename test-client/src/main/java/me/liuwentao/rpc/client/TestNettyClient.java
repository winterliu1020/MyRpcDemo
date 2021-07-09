package me.liuwentao.rpc.client;

import me.liuwentao.rpc.api.HelloObject;
import me.liuwentao.rpc.api.HelloService;
import me.liuwentao.rpc.core.Serializer.CommonSerializer;
import me.liuwentao.rpc.core.transport.Netty.client.NettyClient;
import me.liuwentao.rpc.core.RpcClient;
import me.liuwentao.rpc.core.RpcClientProxy;
import me.liuwentao.rpc.core.Serializer.KryoSerializer;

/**
 * Created by liuwentao on 2021/6/16 15:20
 */
public class TestNettyClient {
    public static void main(String[] args) {
        RpcClient nettyClient = new NettyClient(CommonSerializer.KRYO_SERIALIZER);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(nettyClient);
        // 拿到某一个接口的代理
        HelloService helloServiceProxy = rpcClientProxy.getProxy(HelloService.class); // 这里其实拿到一个代理类helloServiceProxy
        // ，它会继承Proxy类，并且实现了HelloService接口
        System.out.println(helloServiceProxy.sayHello(new HelloObject(2, "hi")));
    }
}
