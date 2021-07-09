package me.liuwentao.rpc.core.transport.Socket.client;

import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Entity.RpcResponse;
import me.liuwentao.rpc.common.Enumeration.RpcError;
import me.liuwentao.rpc.common.Exception.RpcException;
import me.liuwentao.rpc.common.util.RpcMessageChecker;
import me.liuwentao.rpc.core.Registry.NacosServiceDiscovery;
import me.liuwentao.rpc.core.Registry.NacosServiceRegistry;
import me.liuwentao.rpc.core.Registry.ServiceDiscovery;
import me.liuwentao.rpc.core.RpcClient;
import me.liuwentao.rpc.core.Serializer.CommonSerializer;
import me.liuwentao.rpc.core.loadBalance.LoadBalance;
import me.liuwentao.rpc.core.loadBalance.RandomLoadBalance;
import me.liuwentao.rpc.core.transport.Socket.util.ObjectReader;
import me.liuwentao.rpc.core.transport.Socket.util.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by liuwentao on 2021/6/10 21:54
 *
 * v2.0中由具体的实现client去和host, port进行绑定；然后把client传给RpcClientProxy的构造函数进行代理
 * v3.0中需要指定client端用的序列化方式；同时将之前的「具体和某一个服务器host, port进行绑定」，改为从nacos那里获取服务地址；（注意：怎么变它都还是基于接口的，因为不基于接口我客户端怎么知道有什么方法呢）
 * v3.1中初始化一个客户端时需要指定使用的序列化方式、负载均衡方式，不指定的话采用默认的
 */
public class SocketClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private final CommonSerializer serializer;
    private final ServiceDiscovery serviceDiscovery;

    public SocketClient() {
        this(DEFAULT_SERIALIZER, new RandomLoadBalance());
    }
    public SocketClient(int serializer) {
        this(serializer, new RandomLoadBalance());
    }
    public SocketClient(LoadBalance loadBalance) {
        this(DEFAULT_SERIALIZER, loadBalance);
    }
    public SocketClient(int serializer, LoadBalance loadBalance) {
        this.serializer = CommonSerializer.getByCode(serializer);
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalance);
    }

    // 规定：sendRequest返回的是RpcResponse
    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }

        // v3.0中改为从nacos那里拿到对应服务的host, port
        // v3.1抽象出serviceDiscovery
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        logger.info("获取到一个可用的服务器端地址{}:{}", inetSocketAddress.getHostName(), inetSocketAddress.getPort());
        Socket socket = new Socket();
        try {
            socket.connect(inetSocketAddress);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("socket连接错误");
        }
        logger.info("客户端是否连接成功：{}", socket.isConnected());

        OutputStream outputStream = null;
        try {
            outputStream = socket.getOutputStream();
            logger.info("outputStream 获取成功");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("获取outPutStream错误");
        }

        InputStream InputStream = null;
        try {
            InputStream = socket.getInputStream();
            logger.info("inputStream 获取成功");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("获取inPutStream错误");
        }

        // 不直接用ObjectOutputStream、ObjectInputStream来读写流，改为用util包中的工具类来读写流
        // writer
        logger.info("客户端开始写数据");
        try {
            ObjectWriter.writeObject(outputStream, rpcRequest, serializer);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("客户端写数据发生异常");
        }


        // reader
        Object result = null;
        try {
            result = ObjectReader.readObject(InputStream);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("客户端读数据发生异常");
        }
        RpcResponse rpcResponse = (RpcResponse) result;
        RpcMessageChecker.check(rpcRequest, rpcResponse);
        return rpcResponse;
    }
}
