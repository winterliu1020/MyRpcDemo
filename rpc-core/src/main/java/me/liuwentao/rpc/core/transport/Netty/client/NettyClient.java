package me.liuwentao.rpc.core.transport.Netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Entity.RpcResponse;
import me.liuwentao.rpc.common.Enumeration.RpcError;
import me.liuwentao.rpc.common.Exception.RpcException;
import me.liuwentao.rpc.common.factory.SingletonFactory;
import me.liuwentao.rpc.core.Registry.NacosServiceDiscovery;
import me.liuwentao.rpc.core.Registry.ServiceDiscovery;
import me.liuwentao.rpc.core.transport.RpcClient;
import me.liuwentao.rpc.core.Serializer.CommonSerializer;
import me.liuwentao.rpc.core.loadBalance.LoadBalance;
import me.liuwentao.rpc.core.loadBalance.RandomLoadBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Created by liuwentao on 2021/6/14 23:52
 *
 * v3.0中需要指定client端用的序列化方式；同时具体和某一个服务器host,
 * port进行绑定，改为从nacos（ServiceRegistry）那里获取服务地址；（注意：怎么变它都还是基于接口的，因为不基于接口我客户端怎么知道有什么方法呢）
 */
public class NettyClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    // 用于启动client的bootstrap
    private static Bootstrap bootstrap;

    // 序列化方式
    private final CommonSerializer serializer;
    // 未处理请求
    private final UnprocessedRequest unprocessedRequest;
    // 服务发现
    private final ServiceDiscovery serviceDiscovery;

    // 默认序列化方式
    public NettyClient() {
        this(DEFAULT_SERIALIZER, new RandomLoadBalance());
    }

    public NettyClient(LoadBalance loadBalance) {
        this(DEFAULT_SERIALIZER, loadBalance);
    }

    public NettyClient(Integer serializer) {
        this(serializer, new RandomLoadBalance());
    }

    // 客户端并没有loadBalance属性，loadBalance属性是存在于ServiceDiscovery中的，服务发现类对象去根据loadBalance策略从list中去select一个inetSocketAddress
    // 客户端的构造方法中需要指定：1. 序列化方式

    public NettyClient(int DEFAULT_SERIALIZER, LoadBalance loadBalance) {
        this.serializer = CommonSerializer.getByCode(DEFAULT_SERIALIZER);
        this.unprocessedRequest = SingletonFactory.newInstance(UnprocessedRequest.class); // 获取对应类的单例
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalance);
    }

    // v3.0之前都是来一个rpcRequest就通过bootstrap与对应的服务器端host, port构建一个channel，但是之前都是与一个host，port进行通信；
    // 在v3.0版本中加入了nacos注册中心，也就是说注册中心中注册了很多的服务，每个服务对应一个host, port，那就需要改变这种固定的host,
    // port的模式了；而且如果两次sendRequest都是发往同一个host, port，之前的话会重复构建channel，v3.0版本中新建一个ChannelProvider类来提供对应InetSAddress(host,
    // port)、serializer的channel
    @Override
    public CompletableFuture<RpcResponse> sendRequest(RpcRequest rpcRequest) {
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        // v3.0版本中使用了AtomicReference，保证result在多线程下的安全性
//        AtomicReference<Object> result = new AtomicReference<>(null);

        // v3.1版本中使用unprocessedRequests这个类来让每一个request对应一个CompletableFuture<RpcResponse>，通过这种方式来保证request对应result的线程安全性
        CompletableFuture<RpcResponse> completableFuture = new CompletableFuture<>();


        try {
            // client端连接到服务器端
            // v3.0改为从ChannelProvider中获取channel；每一个nettyClient中都包含了serviceRegistry，serializer这两个属性；v3
            // .1对远程注册中心的接口进行抽象，服务发现、服务注册分别放到两个接口中，但是这两个接口中的实现类都用NacosUtil中的静态方法以及同一个NamingService
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName()); // 接口名
            Channel channel = ChannelProvider.get(inetSocketAddress, serializer); // 静态方法中调用静态成员，在多线程环境中会存在线程安全问题！所以我给get
            // ()方法加上synchronized保证多线程不能同时调用get方法，从而保证多线程环境下通过get方法拿到里面static类型channel值的唯一性

            // 来了一个rpcRequest，先put到unprocessedRequests对象中
            unprocessedRequest.put(rpcRequest.getRequestId(), completableFuture);

            logger.info("客户端连接到服务器：{}:{}", inetSocketAddress.getHostName(), inetSocketAddress.getPort());
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener)future -> { // channel.writeAndFlush(rpcRequest)会被通道中的OutBound类型handler捕捉到并处理，这里就会被CommonEncoder这个handler捕获到
                    if (future.isSuccess()) {
                        logger.info("客户端已经成功发送请求：{}", rpcRequest);
//                        future.channel().close();
                    } else {
                        future.channel().close();
                        logger.error("客户端发送请求失败：", future.cause());
                        completableFuture.completeExceptionally(future.cause());
                    }
                });
            }
        } catch (InterruptedException e) {
            unprocessedRequest.remove(rpcRequest.getRequestId());
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }

        return completableFuture;
    }
}
