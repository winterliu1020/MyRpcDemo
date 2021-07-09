package me.liuwentao.rpc.core.transport.Netty.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Entity.RpcResponse;
import me.liuwentao.rpc.common.factory.ThreadPoolFactory;
import me.liuwentao.rpc.core.Provider.DefaultServiceProvider;
import me.liuwentao.rpc.core.Provider.ServiceProvider;
import me.liuwentao.rpc.core.handler.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * Created by liuwentao on 2021/6/15 16:49
 *
 * NettyServerHandler位于服务器端责任链的尾部，直接和RpcServer打交道，用于接收RpcRequest对象，然后进行反射执行，将结果封装到RpcResponse中发送给客户端
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    // 需要有两个属性：注册表对象、RequestHandler对象
    private static RequestHandler requestHandler;
    private static ServiceProvider serviceProvider;
    private static ExecutorService threadPoolExecutor;

    private static final String THREAD_NAME_PREFIX = "netty-server-handler";

    static {
        requestHandler = new RequestHandler();
        serviceProvider = new DefaultServiceProvider();
        // 用线程池工厂类创建一个线程池
        threadPoolExecutor = ThreadPoolFactory.createDefaultThreadPool(THREAD_NAME_PREFIX);
    }

    // 在v3.0中创建了一个线程池来处理客户端发过来的rpcRequest，每次调用channelRead0都往线程池中添加一个任务
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        threadPoolExecutor.execute(() -> {
            try {
                // 从rpcRequest对象中读取interfaceName
                logger.info("服务器接收到请求：{}", rpcRequest);
                if (rpcRequest.getHeartBeat()) {
                    logger.info("服务器端接收到心跳包..."); // 服务器端不需要处理心跳包
                    return;
                }
                Object result = requestHandler.handler(rpcRequest);
                // 异步通信
                ChannelFuture channelFuture = channelHandlerContext.writeAndFlush(RpcResponse.success(result,
                        rpcRequest.getRequestId()));

//                channelFuture.addListener(ChannelFutureListener.CLOSE); // 写完就关闭了channel。。。没有心跳
            } finally {
                ReferenceCountUtil.release(rpcRequest);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("处理过程调用时发生错误");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (IdleState.READER_IDLE.equals(((IdleStateEvent) evt).state())) {
                // 如果被触发了一个空闲事件，需要发一个心跳包
                logger.info("长时间未收到客户端请求，断开连接...");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
