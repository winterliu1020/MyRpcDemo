package me.liuwentao.rpc.core.Server;

import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Entity.RpcResponse;
import me.liuwentao.rpc.core.Register.ServiceRegister;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

/**
 * Created by liuwentao on 2021/6/14 12:41
 * 在这个线程中处理rpcRequest
 */
public class RequestHandlerThread implements Runnable{
    private Socket client;
    private RequestHandler requestHandler;
    private ServiceRegister serviceRegister;


    public RequestHandlerThread(Socket client, RequestHandler requestHandler, ServiceRegister serviceRegister) {
        this.client = client;
        this.requestHandler = requestHandler;
        this.serviceRegister = serviceRegister;
    }

    @Override
    public void run() {
        // client里面就有rpcRequest对象，然后具体的反射调用函数执行放到RequestHandler中
        // 这个是线程的run方法，再解耦合，调用RequestHandler类中的handler方法做具体的反射调用，handler方法的参数1：这个方法取出rpcRequest  参数2：从serviceRegister注册表中拿到具体的执行service;
        try (ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream())){
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            // 从rpcRequest中拿service对象
            String serviceName = rpcRequest.getInterfaceName();
            Object service = serviceRegister.getService(serviceName);
            Object result = requestHandler.handler(rpcRequest, service);
            // 把在服务器端反射调用的执行结果写会客户端
            objectOutputStream.writeObject(RpcResponse.success(result));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
