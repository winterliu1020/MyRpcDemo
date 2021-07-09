package me.liuwentao.rpc.core.transport.Netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Entity.RpcResponse;
import me.liuwentao.rpc.common.factory.SingletonFactory;
import me.liuwentao.rpc.core.Serializer.CommonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by liuwentao on 2021/6/16 14:40
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse<?>> {
    private static Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    private final UnprocessedRequest unprocessedRequest;
    public NettyClientHandler() {
        unprocessedRequest = SingletonFactory.newInstance(UnprocessedRequest.class); // 通过单例工厂来获取UnprocessedRequest的单例
    }

    // 这里就可以直接拿到在Decode中以及解码并且反序列化，然后加入到list中的RpcResponse对象
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        try {
            logger.info("客户端接收到响应信息：{}", rpcResponse);

            unprocessedRequest.complete(rpcResponse);
//            channelHandlerContext.channel().close();
        } finally {
            ReferenceCountUtil.release(rpcResponse);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("过程调用时发生错误：");
        cause.printStackTrace();
        ctx.close();
    }

    // 心跳包
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 那我得发送一个心跳RpcRequest
            if (IdleState.WRITER_IDLE.equals(((IdleStateEvent) evt).state())) {
                logger.info("发送心跳包[{}]", ctx.channel().remoteAddress());
                RpcRequest rpcRequest = new RpcRequest();
                rpcRequest.setHeartBeat(true);
                Channel channel = ChannelProvider.get((InetSocketAddress) ctx.channel().remoteAddress(), CommonSerializer.getByCode(CommonSerializer.DEFAULT_SERIALIZER));
                channel.writeAndFlush(rpcRequest);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
