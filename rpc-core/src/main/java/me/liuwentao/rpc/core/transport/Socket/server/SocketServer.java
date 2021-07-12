package me.liuwentao.rpc.core.transport.Socket.server;

import me.liuwentao.rpc.common.Enumeration.RpcError;
import me.liuwentao.rpc.common.Exception.RpcException;
import me.liuwentao.rpc.common.factory.ThreadPoolFactory;
import me.liuwentao.rpc.core.Provider.DefaultServiceProvider;
import me.liuwentao.rpc.core.Provider.ServiceProvider;
import me.liuwentao.rpc.core.Registry.NacosServiceRegistry;
import me.liuwentao.rpc.core.Registry.ServiceRegistry;
import me.liuwentao.rpc.core.Serializer.CommonSerializer;
import me.liuwentao.rpc.core.handler.RequestHandler;
import me.liuwentao.rpc.core.transport.AbstractRpcServer;
import me.liuwentao.rpc.core.transport.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Created by liuwentao on 2021/6/11 17:35
 */
public class SocketServer extends AbstractRpcServer {
    // Rpc的服务端，服务端用的是线程池，用socket监听客户端的rpcRequest，来一个请求就放到线程池中去；
    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);
    private RequestHandler requestHandler = new RequestHandler();

    private static final String THREAD_NAME_PREFIX = "socket-server-name";
    private final ExecutorService threadPoolExecutor;

    private CommonSerializer serializer;

    public SocketServer(String host, int port) {
        this(host, port, DEFAULT_SERIALIZER);
    }

    public SocketServer(String host, int port, Integer serializerCode) {
        this.host = host;
        this.port = port;
        // ServiceProvider、serviceRegistry
        serviceProvider = new DefaultServiceProvider();
        serviceRegistry = new NacosServiceRegistry();
        serializer = CommonSerializer.getByCode(serializerCode);

        // 用线程池工厂类创建一个线程池
        threadPoolExecutor = ThreadPoolFactory.createDefaultThreadPool(THREAD_NAME_PREFIX);

        scanService();
    }


    // 现在就没有register方法了，而是start(port) 这个rpcServer开启在哪个端口上
    @Override
    public void start() {
        // 接收port端口监听到的请求，然后用service去执行
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            logger.info("server端服务正在启动...");
            Socket client = null;
            while ((client = serverSocket.accept()) != null) {
                logger.info("当前服务器连接到的client端的ip:" + client.getInetAddress().getHostAddress());
                threadPoolExecutor.execute(new RequestHandlerThread(client, requestHandler, serviceRegistry,
                        serializer)); // 序列化只能决定以哪一种方式转成字节流，而和底层的通信方式无关，不管你用netty还是socket
            }
        } catch (IOException e) {
            logger.error("调用时发生错误：", e);
        }
    }

//    @Override
//    public <T> void publishService(T service, String interfaceName) { // 服务的具体实现类，服务对应接口的class对象
//        if(serializer == null) {
//            logger.error("未设置序列化器");
//            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
//        }
//        // 发布一项服务：注册到本地、添加到注册中心
//        serviceProvider.addServiceProvide(service, interfaceName);
//        serviceRegistry.register(interfaceName, new InetSocketAddress(host, port));
//    }
}
