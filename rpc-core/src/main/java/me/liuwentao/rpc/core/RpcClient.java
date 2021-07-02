package me.liuwentao.rpc.core;

import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.core.Serializer.CommonSerializer;

/**
 * Created by liuwentao on 2021/6/14 22:59
 */
public interface RpcClient {
    // sendRequest方法
    Object sendRequest(RpcRequest rpcRequest);

    // 设置client端的序列化方式
    void setSerializer(CommonSerializer serializer);
}
