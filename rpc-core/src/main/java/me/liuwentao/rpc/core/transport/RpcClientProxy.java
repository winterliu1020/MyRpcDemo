package me.liuwentao.rpc.core.transport;

import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Entity.RpcResponse;
import me.liuwentao.rpc.common.util.RpcMessageChecker;
import me.liuwentao.rpc.core.transport.Netty.client.NettyClient;
import me.liuwentao.rpc.core.transport.Socket.client.SocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Created by liuwentao on 2021/6/10 16:07
 *
 * 客户端实现：动态代理
 * 客户端并没有API接口的实现类，那客户端怎么去调用API接口对应的实现方法呢？
 */
public class RpcClientProxy implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);
    private RpcClient rpcClient;

    // 版本2.0中将RpcClientProxy类进行抽象，它可以为不同的client实现类进行代理；所以构造方法中你需要传一个client对象，从此由client去与host, port进行绑定；
    public RpcClientProxy(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    // 这里用jdk代理模式，极大的简化了代码量（如果不用代理模式，你还得自己手动去new一个rpcRequest对象，而且你调用不同的rcp请求还得传递不同的rpc参数）：
    // 这样你只需要在客户端传服务接口的class对象，Proxy.newProxyInstance()会帮你生成一个实现这个接口的子类，而这个子类调用接口中的方法又会通过发送rpcRequest去调用在服务器上的真正的实例方法
    // 这里用@Builder注解 构造者模式，传入属性值生成一个RpcRequest对象
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { // 这个method是接口class对象中的method对象
        logger.info("调用方法：{}#{}", method.getDeclaringClass().getName(), method.getName());
        RpcRequest rpcRequest = RpcRequest.builder().requestId(UUID.randomUUID().toString())
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .heartBeat(false)
                .build();
        logger.info("构造的rpcRequest中interfaceName:{}", method.getDeclaringClass().getCanonicalName());
        // 在代理proxy类中，要区分NettyClient和SocketClient，因为这两个执行sendRequest的返回值是不一样的
        RpcResponse<?> response = null;
        if (rpcClient instanceof NettyClient) {
            CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>)rpcClient.sendRequest(rpcRequest);
            response = completableFuture.get();
        }
        if (rpcClient instanceof SocketClient) {
            logger.info("发起一个socket RpcRequest请求");
            response = (RpcResponse<?>) rpcClient.sendRequest(rpcRequest);
        }
        RpcMessageChecker.check(rpcRequest, response);
        logger.info("反射调用执行结果    " + response.getData().toString());
        return response.getData();
    }
}
