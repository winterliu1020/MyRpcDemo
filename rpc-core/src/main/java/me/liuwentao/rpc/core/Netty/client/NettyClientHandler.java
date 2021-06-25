package me.liuwentao.rpc.core.Netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import me.liuwentao.rpc.common.Entity.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liuwentao on 2021/6/16 14:40
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        try {
            // 负责将rpcResponse写到AttributeKey中，这样的话NettyClient类中可以拿到放的rpcResponse对象
            logger.info("客户端接收到响应信息：{}", rpcResponse);

            // 构造一个key值为rpcResponse的attr
            AttributeKey<RpcResponse> rpcResponseAttributeKey = AttributeKey.valueOf("rpcResponse");
            // 这个是绑定在channel上，所以在nettyClient上也是通过channel.attr去get
            channelHandlerContext.channel().attr(rpcResponseAttributeKey).set(rpcResponse); // 为某个AttributeKey设置具体的value值
            channelHandlerContext.channel().close();
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
}
