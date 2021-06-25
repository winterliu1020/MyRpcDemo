package me.liuwentao.rpc.server;

import me.liuwentao.rpc.core.Netty.server.NettyServer;
import me.liuwentao.rpc.core.Register.DefaultServiceRegister;
import me.liuwentao.rpc.core.Register.ServiceRegister;
import me.liuwentao.rpc.core.RpcServer;

/**
 * Created by liuwentao on 2021/6/16 15:24
 */
public class TestNettyServer {
    public static void main(String[] args) {
        RpcServer rpcServer = new NettyServer();
        ServiceRegister serviceRegister = new DefaultServiceRegister();
        // 服务器端注册一个服务
        serviceRegister.register(new HelloServiceImpl());
        rpcServer.start(8084);
    }

}
