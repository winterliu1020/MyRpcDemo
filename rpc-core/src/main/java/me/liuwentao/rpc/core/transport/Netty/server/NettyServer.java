package me.liuwentao.rpc.core.transport.Netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import me.liuwentao.rpc.common.Enumeration.RpcError;
import me.liuwentao.rpc.common.Exception.RpcException;
import me.liuwentao.rpc.core.Provider.DefaultServiceProvider;
import me.liuwentao.rpc.core.Provider.ServiceProvider;
import me.liuwentao.rpc.core.Registry.NacosServiceRegistry;
import me.liuwentao.rpc.core.RpcServer;
import me.liuwentao.rpc.core.Serializer.CommonSerializer;
import me.liuwentao.rpc.core.codec.CommonDecoder;
import me.liuwentao.rpc.core.codec.CommonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by liuwentao on 2021/6/14 23:52
 */
public class NettyServer implements RpcServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private final String host;
    private final int port;
    // 一个服务注册类
    private final NacosServiceRegistry nacosServiceRegistry;
    private final ServiceProvider serviceProvider;

    // nettyServer对象中还得指定序列化方式
    private CommonSerializer serializer;

    // 在nettyServer对象中，得有服务端的host, port，除此之外，还得有一个注册中心对象nacosServiceRegistry，以及一个真正提供服务的serviceProvider对象
    public NettyServer(String host, int port) {
        this.host = host;
        this.port = port;
        nacosServiceRegistry = new NacosServiceRegistry();
        serviceProvider = new DefaultServiceProvider(); // 这个对象中才真正存储服务器端注册服务的实现类实例
    }

    // 启动服务器端（你应该先调用publishService方法来发布你的服务，然后再调用start启动服务器端）
    @Override
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline channelPipeline = socketChannel.pipeline();
                            channelPipeline.addLast(new CommonEncoder(serializer)); // 服务器端给发送出去的数据选择序列化类
                            channelPipeline.addLast(new CommonDecoder());
                            channelPipeline.addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("启动服务器时有错误发生：", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    // 在服务器端发布一个服务; parameters: 1.service 具体服务实现类  2.这个接口的class对象
    @Override
    public <T> void publishService(Object service, Class<T> interfaceClass) {
        if (serializer == null) {
            logger.error("还没有设置序列化方式");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        serviceProvider.addServiceProvide(service); // 1. 需要将具体服务实现实例添加到服务器端本地的注册表，也就是那个ConcurrentHashMap中

        // 参数1：接口的类名
        nacosServiceRegistry.register(interfaceClass.getCanonicalName(), new InetSocketAddress(host, port)); // 2.
        // 同时需要将服务发布到nacosServiceRegistry注册中心；后面客户端都是从ServiceRegistry注册中心里面用接口名来获取某个服务所在的ip, port
    }

    // 通过方法调用的方式给服务器端设定序列化方式
    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }
}
