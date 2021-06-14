package me.liuwentao.rpc.core.Server;

import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.core.Register.ServiceRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.jvm.hotspot.utilities.WorkerThread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by liuwentao on 2021/6/11 17:35
 */
public class RpcServer {
    // Rpc的服务端，服务端用的是线程池，用socket监听客户端的rpcRequest，来一个请求就放到线程池中去；
    final private ThreadPoolExecutor threadPoolExecutor;
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);
    ServiceRegister serviceRegister;
    private RequestHandler requestHandler = new RequestHandler();

    // 你要传一个register给RpcServer进行初始化
    public RpcServer(ServiceRegister serviceRegister) {
        this.threadPoolExecutor = new ThreadPoolExecutor(5,
                10,
                1,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(5));
        this.serviceRegister = serviceRegister; // serviceRegister里面是带了已经注册了的服务的
    }


    // 现在就没有register方法了，而是start(port) 这个rpcServer开启在哪个端口上
    public void start(int port) {
        // 接收port端口监听到的请求，然后用service去执行
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            logger.info("server端服务正在启动...");
            Socket client = null;
            while ((client = serverSocket.accept()) != null) {
                logger.info("当前服务器连接到的client端的ip:" + client.getInetAddress().getHostAddress());
                threadPoolExecutor.execute(new RequestHandlerThread(client, requestHandler, serviceRegister));
            }
        } catch (IOException e) {
            logger.error("调用时发生错误：", e);
        }
    }

}
