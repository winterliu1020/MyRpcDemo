package me.liuwentao.rpc.server;

import me.liuwentao.rpc.api.HelloService;
import me.liuwentao.rpc.core.Server.RpcServer;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by liuwentao on 2021/6/13 12:03
 *
 * 这里是测试服务器端；rpc-core模块中的server相当于一个框架中的server端代码；而这里的TestServer相当于利用框架的代码执行rpc调用
 */
public class TestServer {
    public static void main(String[] args) {
        RpcServer rpcServer = new RpcServer();
        // 测试一下服务端的注册功能
        HelloService helloService = new HelloServiceImpl();
        // 相当于服务器端注册了一个服务，只要监听到了来自于8083的rpcRequest请求就用helloService去执行
        rpcServer.register(helloService, 8083);
    }
}
