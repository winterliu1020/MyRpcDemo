package me.liuwentao.rpc.server;

import me.liuwentao.rpc.api.HelloService;
import me.liuwentao.rpc.core.Register.DefaultServiceRegister;
import me.liuwentao.rpc.core.Register.ServiceRegister;
import me.liuwentao.rpc.core.Server.RpcServer;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by liuwentao on 2021/6/13 12:03
 *
 * 这里是测试服务器端；rpc-core模块中的server相当于一个框架中的server端代码；而这里的TestServer相当于利用框架的代码执行rpc调用
 */
public class TestServer {
    public static void main(String[] args) {
        ServiceRegister serviceRegister = new DefaultServiceRegister();
        // 测试一下服务端的注册功能
        HelloService helloService = new HelloServiceImpl();
        serviceRegister.register(helloService);
        // 给RpcServer绑定一个注册表
        RpcServer rpcServer = new RpcServer(serviceRegister);
        rpcServer.start(8083); // 开启服务
    }
}
