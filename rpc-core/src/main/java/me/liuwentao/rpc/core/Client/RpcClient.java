package me.liuwentao.rpc.core.Client;

import me.liuwentao.rpc.common.Entity.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by liuwentao on 2021/6/10 21:54
 */
public class RpcClient {

    Logger logger = LoggerFactory.getLogger(RpcClient.class);
    public Object sendRequest(RpcRequest rpcRequest, String host, Integer port) {
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
