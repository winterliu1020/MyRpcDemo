package me.liuwentao.rpc.client;

import me.liuwentao.rpc.api.HelloObject;
import me.liuwentao.rpc.api.HelloService;
import me.liuwentao.rpc.core.RpcClientProxy;
import me.liuwentao.rpc.core.Socket.client.SocketClient;

/**
 * Created by liuwentao on 2021/6/13 12:13
 * 测试客户端 发起一个rpcRequest；相当于还是用的rpc-core这里面的框架代码，生成一个rpc-client对象 来发起请求
 */
public class TestSocketClient {
    public static void main(String[] args) {
        // 客户端用的是动态代理的方式; 客户端唯一的作用是：向目的ip, port发送rpcRequest
        SocketClient socketClient = new SocketClient("127.0.0.1", 8083);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(socketClient);
        // 客户端并没有HelloService这个接口对应的实现实例，所以不能直接调用实现实例的方法；所以客户端采用动态代理的方式
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class); // 拿到HelloService的代理类实例
        Object result = helloService.sayHello(new HelloObject(1, "hello world")); // 通过代理类去调用接口中某一个方法，都会被拦截到代理类的invoke()方法中；invoke()方法的返回值就是这个sayHello()的返回值
        System.out.println("客户端得到的来自服务器的响应结果：" + result);
    }
}