package me.liuwentao.rpc.server;

import me.liuwentao.rpc.api.HelloService;
import me.liuwentao.rpc.core.Serializer.CommonSerializer;
import me.liuwentao.rpc.core.annotation.ServiceScan;
import me.liuwentao.rpc.core.transport.Netty.server.NettyServer;
import me.liuwentao.rpc.core.transport.RpcServer;

/**
 * Created by liuwentao on 2021/6/16 15:24
 */
@ServiceScan
public class TestNettyServer {
    public static void main(String[] args) {
        RpcServer rpcServer = new NettyServer("127.0.0.1", 8085); // 这是服务器端的地址及监听端口;
        // 在NettyServer的构造方法中，会自动帮我们产生一个注册中心，和一个ServiceProvider类对象用于在服务器端本地注册表中存储真正的实现实例对象

        // 然后你就只需要publishService即可，具体的工作都由publishService类来完成：添加到本地注册表、添加到注册中心
//        HelloService helloService = new HelloServiceImpl();
//        rpcServer.publishService(helloService, HelloService.class);

        rpcServer.start();
    }

}
