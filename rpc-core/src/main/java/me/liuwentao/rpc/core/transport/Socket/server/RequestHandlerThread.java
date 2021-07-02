package me.liuwentao.rpc.core.transport.Socket.server;

import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Entity.RpcResponse;
import me.liuwentao.rpc.core.Registry.ServiceRegistry;
import me.liuwentao.rpc.core.Serializer.CommonSerializer;
import me.liuwentao.rpc.core.transport.Socket.util.ObjectReader;
import me.liuwentao.rpc.core.transport.Socket.util.ObjectWriter;
import me.liuwentao.rpc.core.handler.RequestHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by liuwentao on 2021/6/14 12:41
 * 在这个线程中处理rpcRequest
 */
public class RequestHandlerThread implements Runnable{
    private Socket client;
    private RequestHandler requestHandler;
    private ServiceRegistry serviceRegistry;
    private CommonSerializer serializer;


    public RequestHandlerThread(Socket client, RequestHandler requestHandler, ServiceRegistry serviceRegistry,
                                CommonSerializer serializer) {
        this.client = client;
        this.requestHandler = requestHandler;
        this.serviceRegistry = serviceRegistry;
        this.serializer = serializer;
    }

    @Override
    public void run() {
        // client里面就有rpcRequest对象，然后具体的反射调用函数执行放到RequestHandler中
        // 这个是线程的run方法，再解耦合，调用RequestHandler类中的handler方法做具体的反射调用，handler方法的参数1：这个方法取出rpcRequest  参数2：从serviceRegister注册表中拿到具体的执行service;
        try (ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream())){

//            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject(); // 改掉，
            // 要用ObjectReader这个自己写的工具类来读，传输的数据包格式还是之前那样：魔数、packageType、dataType(rpcRequest/ rpcResponse)
            // 、序列化Code、dataLength、data
            RpcRequest rpcRequest = (RpcRequest) ObjectReader.readObject(objectInputStream);

            Object result = requestHandler.handler(rpcRequest);
            // 把在服务器端反射调用的执行结果写回客户端
            ObjectWriter.writeObject(objectOutputStream, RpcResponse.success(result, rpcRequest.getRequestId()), serializer); // 写入的流、把什么对象写入、对象转字节的序列化方式
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
