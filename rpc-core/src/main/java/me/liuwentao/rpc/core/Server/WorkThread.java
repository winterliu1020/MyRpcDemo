package me.liuwentao.rpc.core.Server;

import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Entity.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * Created by liuwentao on 2021/6/13 11:32
 *
 * 实现runnable接口，把服务端具体的执行逻辑放到这里
 */
public class WorkThread implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(WorkThread.class);
    private Socket client;
    private Object service;
    public WorkThread(Socket client, Object service) {
        this.client = client;
        this.service = service;
    }
    @Override
    public void run() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream())){
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject(); // 从监听到的socket中拿到了rpcRequest请求
            // 先解析rpcRequest，然后用service去反射执行具体方法 rpcRequest中有四部分：接口名字、调用接口中的哪个方法的名字、各参数实体、各参数实体的类型
            String interfaceName = rpcRequest.getInterfaceName();
            String methodName = rpcRequest.getMethodName();
            Object[] parameters = rpcRequest.getParameters();
            Class<?>[] parameterTypes = rpcRequest.getParamTypes();

            Method method = service.getClass().getMethod(methodName, parameterTypes);

            Object resultObject = method.invoke(service, parameters); // 这个相当于在服务器端执行完的结果，我们得返回给客户端，用ObjectOutPutStream
            objectOutputStream.writeObject(RpcResponse.success(resultObject));
            objectOutputStream.flush();
            logger.info("服务器端执行完毕...");
        } catch (IOException | ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
