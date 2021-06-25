package me.liuwentao.rpc.core.Netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Entity.RpcResponse;
import me.liuwentao.rpc.core.RpcClient;
import me.liuwentao.rpc.core.Serializer.JsonSerializer;
import me.liuwentao.rpc.core.codec.CommonDecoder;
import me.liuwentao.rpc.core.codec.CommonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liuwentao on 2021/6/14 23:52
 */
public class NettyClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    // 具体的client端去与IP，端口进行绑定
    private String host;
    private int port;
    // 用于启动client的bootstrap
    private static Bootstrap bootstrap;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
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
                        pipeline.addLast(new CommonDecoder()); // inbound
                        pipeline.addLast(new CommonEncoder(new JsonSerializer()));
                        pipeline.addLast(new NettyClientHandler()); // inbound;  都是inbound，前面以及decoder了，所以这里直接拿到rpcResponse
                    }
                });
    }
    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        // client端连接到服务器端
        try {
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            logger.info("客户端连接到服务器：{}:{}", host, port);
            Channel channel = channelFuture.channel();
            if (channel != null) {
                channel.writeAndFlush(rpcRequest).addListener(future -> {
                   if (future.isSuccess()) {
                       logger.info("客户端已经发送请求：{}", rpcRequest.toString());
                   } else {
                       logger.error("客户端发送请求失败：", future.cause());
                   }
                });
                channel.closeFuture().sync();
                // 如果这样写：AttributeKey<RpcResponse>会警告，因为RpcResponse是一个泛型类，「泛型不要使用原生类型」会导致丢失类型安全性；需要在<>中加上类型
                AttributeKey<RpcResponse<String>> attributeKey = AttributeKey.valueOf("rpcResponse");
                return channel.attr(attributeKey).get();
            }
        } catch (InterruptedException e) {
            logger.error("发送信息时发生错误：", e);
        }
        return null;
    }
}
