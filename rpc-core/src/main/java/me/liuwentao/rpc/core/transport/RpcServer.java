package me.liuwentao.rpc.core.transport;

import me.liuwentao.rpc.core.Serializer.CommonSerializer;
import me.liuwentao.rpc.core.config.RpcServiceConfig;

/**
 * Created by liuwentao on 2021/6/14 22:59
 */

public interface RpcServer {
    // 一个start方法
    void start();

    // v3.0中新增publishService方法，用于向nacos中发布一个服务
    // 接口中方法都是隐式public abstract的；属性都是隐式public static final的
//    <T> void publishService(T service, Class<T> serviceClass);

    // 指定RpcServer默认的序列化方式
    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;

    // v3.2版本的发布服务，第二个参数是String
    // v3.3版本将第一个参数由原来的服务实现类改为：服务实现类的封装类RpcServiceConfig(其实就是对服务实现类增加了group, version属性)
    void publishService(RpcServiceConfig rpcServiceConfig, String interfaceGroupVersionName);
}
