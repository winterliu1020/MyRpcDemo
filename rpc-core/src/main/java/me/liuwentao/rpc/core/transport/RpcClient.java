package me.liuwentao.rpc.core.transport;

import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Enumeration.SerializerCode;
import me.liuwentao.rpc.core.Serializer.CommonSerializer;

/**
 * Created by liuwentao on 2021/6/14 22:59
 */
public interface RpcClient {
    // sendRequest方法
    Object sendRequest(RpcRequest rpcRequest);

    // 设置client端的序列化方式
//    void setSerializer(CommonSerializer serializer);
    // v3.1改成使用默认序列化方式，也可以构造方法中传参数指定序列化方式
    int DEFAULT_SERIALIZER = SerializerCode.Kryo.getCode();
}
