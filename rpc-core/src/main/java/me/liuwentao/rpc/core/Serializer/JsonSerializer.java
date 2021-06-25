package me.liuwentao.rpc.core.Serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Enumeration.SerializerCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by liuwentao on 2021/6/15 11:23
 *
 * Json序列化类
 */
public class JsonSerializer implements CommonSerializer{
    private static final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serializer(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            logger.error("序列化时发生错误：{}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /*
    * clazz：这个字节数组所要写成的类型
    * */
    @Override
    public Object deserializer(byte[] bytes, Class<?> clazz) {
        try {
            // 注意这里clazz对应的类一定要有全参数构造方法，否则会反序列化失败
            Object object = objectMapper.readValue(bytes, clazz); // 注意这里object（也就是rpcRequest）对象中的各个参数对象的具体类型已经丢失了，反序列之后各个参数具体类型都变成了Object；所以需要根据传递过来的各个参数的类型重新将各个参数反序列化
            if (object instanceof RpcRequest) {
                object = handleRequest(object);
            }
            return object;
        } catch (IOException e) {
            logger.error("反序列化发生错误：{}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /*
    * 由于用JSON去序列化、反序列化Object数组（RpcRequest中parameters是Object数组类型），无法保证反序列化之后仍为实例类型，所以需要重新判断处理
    * */
    private Object handleRequest(Object object) {
        RpcRequest rpcRequest = (RpcRequest) object; //
        for (int i = 0; i < rpcRequest.getParamTypes().length; i++) {
            // 这里要用加通配符<?>,不然会警告
            Class<?> clazz = rpcRequest.getParamTypes()[i]; // rpcRequest中参数数组中第i个参数的类型；根据传过来的类型数组重新将每一个参数进行反序列化
            // 要判断转成的rpcRequest中的参数数组中的各个参数的类型是否和rpcRequest中参数类型数组一一对应
            if (!clazz.isAssignableFrom(rpcRequest.getParameters()[i].getClass())) {
                // 进入到这里就说明当前这个参数没有反序列化成功，需要重新对这个参数反序列化
                byte[] bytesParam = new byte[0];
                try {
                    bytesParam = objectMapper.writeValueAsBytes(rpcRequest.getParameters()[i]);
                } catch (JsonProcessingException e) {
                    logger.error("序列化中把值写成字节数组时发生错误：", e);
                }
                try {
                    rpcRequest.getParameters()[i] = objectMapper.readValue(bytesParam, clazz);
                } catch (IOException e) {
                    logger.error("序列化中读rpcRequest中数组的单个元素 成 对应对象 发生错误：", e);
                }
            }
        }
        return rpcRequest;
    }

//    private Object handleRequest(Object obj) throws IOException {
//        RpcRequest rpcRequest = (RpcRequest) obj;
//        for(int i = 0; i < rpcRequest.getParamTypes().length; i ++) {
//            Class<?> clazz = rpcRequest.getParamTypes()[i];
//            if(!clazz.isAssignableFrom(rpcRequest.getParameters()[i].getClass())) {
//                byte[] bytes = objectMapper.writeValueAsBytes(rpcRequest.getParameters()[i]);
//                rpcRequest.getParameters()[i] = objectMapper.readValue(bytes, clazz);
//            }
//        }
//        return rpcRequest;
//    }

    @Override
    public int getCode() {
        return SerializerCode.JSON.getCode();
    }
}
