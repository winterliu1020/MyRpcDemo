package me.liuwentao.rpc.server;

import me.liuwentao.rpc.api.HelloService;
import me.liuwentao.rpc.core.RpcServer;
import me.liuwentao.rpc.core.Serializer.KryoSerializer;
import me.liuwentao.rpc.core.transport.Socket.server.SocketServer;

/**
 * Created by liuwentao on 2021/6/13 12:03
 *
 * 这里是测试服务器端；rpc-core模块中的server相当于一个框架中的server端代码；而这里的TestServer相当于利用框架的代码执行rpc调用
 */
public class TestSocketServer {
    public static void main(String[] args) {
        RpcServer socketServer = new SocketServer("127.0.0.1", 8086);
        // socketServer发布一个服务
        HelloService helloService = new HelloServiceImpl();
        socketServer.publishService(helloService, HelloService.class); // 服务具体实现类、接口class
        socketServer.start();
    }
}
