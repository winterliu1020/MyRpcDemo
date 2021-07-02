package me.liuwentao.rpc.core.transport.Netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Entity.RpcResponse;
import me.liuwentao.rpc.common.Enumeration.RpcError;
import me.liuwentao.rpc.common.Exception.RpcException;
import me.liuwentao.rpc.common.util.RpcMessageChecker;
import me.liuwentao.rpc.core.Registry.NacosServiceRegistry;
import me.liuwentao.rpc.core.Registry.ServiceRegistry;
import me.liuwentao.rpc.core.RpcClient;
import me.liuwentao.rpc.core.Serializer.CommonSerializer;
import me.liuwentao.rpc.core.Serializer.KryoSerializer;
import me.liuwentao.rpc.core.codec.CommonDecoder;
import me.liuwentao.rpc.core.codec.CommonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by liuwentao on 2021/6/14 23:52
 *
 * v3.0中需要指定client端用的序列化方式；同时具体和某一个服务器host,
 * port进行绑定，改为从nacos（ServiceRegistry）那里获取服务地址；（注意：怎么变它都还是基于接口的，因为不基于接口我客户端怎么知道有什么方法呢）
 */
public class NettyClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    // 具体的client端去与IP，端口进行绑定
//    private String host;
//    private int port;

    // 用于启动client的bootstrap
    private static Bootstrap bootstrap;

    // 注册中心对象
    private final ServiceRegistry serviceRegistry;

    // 序列化方式
    private CommonSerializer serializer;

    public NettyClient() {
        serviceRegistry = new NacosServiceRegistry();
    }

    // 完成客户端的配置
    static {
        bootstrap = new Bootstrap();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        // 加入handler的方法：根据业务顺序来加，比如先是编解码，你就把解码、编码混合加入，然后再是下一个业务；因为inbound是从前往后走的，outbound是从后往前走
                        pipeline.addLast(new CommonDecoder()); // inbound；client中进来的数据包，是根据数据流中的serializerCode再去拿到对应的序列化类
                        pipeline.addLast(new CommonEncoder(new KryoSerializer())); // 这里是对即将发出去的数据序列化，所以需要自己指定用哪种序列化类; 注意在服务器端需要和客户端选择的序列化类对应
                        pipeline.addLast(new NettyClientHandler()); // inbound;  都是inbound，前面以及decoder了，所以这里直接拿到rpcResponse
                    }
                });
    }

    // v3.0之前都是来一个rpcRequest就通过bootstrap与对应的服务器端host, port构建一个channel，但是之前都是与一个host，port进行通信；
    // 在v3.0版本中加入了nacos注册中心，也就是说注册中心中注册了很多的服务，每个服务对应一个host, port，那就需要改变这种固定的host,
    // port的模式了；而且如果两次sendRequest都是发往同一个host, port，之前的话会重复构建channel，v3.0版本中新建一个ChannelProvider类来提供对应InetSAddress(host,
    // port)、serializer的channel
    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        AtomicReference<Object> result = new AtomicReference<>(null);
        // v3.0改为从ChannelProvider中获取channel；每一个nettyClient中都包含了serviceRegistry，serializer这两个属性
        InetSocketAddress inetSocketAddress = serviceRegistry.lookupService(rpcRequest.getInterfaceName()); // 接口名
        Channel channel = ChannelProvider.get(inetSocketAddress, serializer); // 静态方法中调用静态成员，在多线程环境中会存在线程安全问题！所以我给get
        // ()方法加上synchronized保证多线程不能同时调用get方法，从而保证多线程环境下通过get方法拿到里面static类型channel值的唯一性

        // client端连接到服务器端
        try {
            logger.info("客户端连接到服务器：{}:{}", inetSocketAddress.getHostName(), inetSocketAddress.getPort());
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(rpcRequest).addListener(future -> { // channel.writeAndFlush(rpcRequest)会被通道中的OutBound类型handler捕捉到并处理，这里就会被CommonEncoder这个handler捕获到
                   if (future.isSuccess()) {
                       logger.info("客户端已经成功发送请求：{}", rpcRequest);
                   } else {
                       logger.error("客户端发送请求失败：", future.cause());
                   }
                });
                channel.closeFuture().sync();
                // 如果这样写：AttributeKey<RpcResponse>会警告，因为RpcResponse是一个泛型类，「泛型不要使用原生类型」会导致丢失类型安全性；需要在<>中加上类型
                AttributeKey<RpcResponse<?>> attributeKey =
                        AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
                // 获取nettyClient端的handler上附加的RpcResponse；
                RpcResponse<?> rpcResponse = channel.attr(attributeKey).get();

                RpcMessageChecker.check(rpcRequest, rpcResponse);
                result.set(rpcResponse.getData());
            } else {
                System.exit(0);
            }
        } catch (InterruptedException e) {
            logger.error("发送信息时发生错误：", e);
        }
        return result.get();
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }
}
