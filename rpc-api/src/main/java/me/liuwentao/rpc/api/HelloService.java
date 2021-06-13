package me.liuwentao.rpc.api;

/**
 * Created by liuwentao on 2021/6/10 12:00
 * rpc-api放的是一些客户端和服务器端都会用的接口
 */
public interface HelloService {
    // 接口里面的方法
    String sayHello(HelloObject helloObject);
}
