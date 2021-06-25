package me.liuwentao.rpc.core.Netty.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Entity.RpcResponse;
import me.liuwentao.rpc.core.Register.DefaultServiceRegister;
import me.liuwentao.rpc.core.Register.ServiceRegister;
import me.liuwentao.rpc.core.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liuwentao on 2021/6/15 16:49
 *
 * NettyServerHandler位于服务器端责任链的尾部，直接和RpcServer打交道，用于接收RpcRequest对象，然后进行反射执行，将结果封装到RpcResponse中发送给客户端
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    // 需要有两个属性：注册表对象、RequestHandler对象
    private static RequestHandler requestHandler;
    private static ServiceRegister serviceRegister;

    static {
        requestHandler = new RequestHandler();
        serviceRegister = new DefaultServiceRegister();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        try {
            // 从rpcRequest对象中读取interfaceName
            logger.info("服务器接收到请求：{}", rpcRequest);
            String interfaceName = rpcRequest.getInterfaceName();
            Object service = serviceRegister.getService(interfaceName);
            logger.info("处理该请求的service：{}", service.getClass().getName());
            Object result = requestHandler.handler(rpcRequest, service);
            // 异步执行
            ChannelFuture channelFuture = channelHandlerContext.writeAndFlush(RpcResponse.success(result));
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        } finally {
            ReferenceCountUtil.release(rpcRequest);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("处理过程调用时发生错误");
        cause.printStackTrace();
        ctx.close();
    }
}
