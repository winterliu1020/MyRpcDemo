package me.liuwentao.rpc.core.transport.Socket.client;

import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Entity.RpcResponse;
import me.liuwentao.rpc.common.Enumeration.RpcError;
import me.liuwentao.rpc.common.Exception.RpcException;
import me.liuwentao.rpc.common.util.RpcMessageChecker;
import me.liuwentao.rpc.core.Registry.NacosServiceRegistry;
import me.liuwentao.rpc.core.RpcClient;
import me.liuwentao.rpc.core.Serializer.CommonSerializer;
import me.liuwentao.rpc.core.transport.Socket.util.ObjectReader;
import me.liuwentao.rpc.core.transport.Socket.util.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by liuwentao on 2021/6/10 21:54
 *
 * v2.0中由具体的实现client去和host, port进行绑定；然后把client传给RpcClientProxy的构造函数进行代理
 * v3.0中需要指定client端用的序列化方式；同时将之前的「具体和某一个服务器host, port进行绑定」，改为从nacos那里获取服务地址；（注意：怎么变它都还是基于接口的，因为不基于接口我客户端怎么知道有什么方法呢）
 */
public class SocketClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);
//    private final String host;
//    private final Integer port;
    //
    private CommonSerializer serializer;
    public SocketClient() {

    }
    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }

        try {
            // v3.0中改为从nacos那里拿到对应服务的host, port
            NacosServiceRegistry nacosServiceRegistry = new NacosServiceRegistry();
            InetSocketAddress inetSocketAddress = nacosServiceRegistry.lookupService(rpcRequest.getInterfaceName());

            Socket socket = new Socket();
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            // 不直接用ObjectOutputStream、ObjectInputStream来读写流，改为用util包中的工具类来读写流
            // writer
            ObjectWriter.writeObject(objectOutputStream, rpcRequest, serializer);
            objectOutputStream.flush();

            // reader
            Object result = ObjectReader.readObject(objectInputStream);
            RpcResponse rpcResponse = (RpcResponse) result;
            RpcMessageChecker.check(rpcRequest, rpcResponse);
            return ((RpcResponse<?>) result).getData();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("sendRequest调用时发生错误：" + e);
            return null;
        }
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }
}
