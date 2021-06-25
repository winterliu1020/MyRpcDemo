package me.liuwentao.rpc.core.Socket.client;

import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.core.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by liuwentao on 2021/6/10 21:54
 *
 * v2.0中由具体的实现client去和host, port进行绑定；然后把client传给RpcClientProxy的构造函数进行代理
 */
public class SocketClient implements RpcClient {
    Logger logger = LoggerFactory.getLogger(SocketClient.class);
    private final String host;
    private final Integer port;

    public SocketClient(String host, Integer port) {
        this.host = host;
        this.port = port;
    }
    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        try {
            Socket socket = new Socket(host, port);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            logger.error("调用时发生错误：" + e);
            return null;
        }
    }
}
