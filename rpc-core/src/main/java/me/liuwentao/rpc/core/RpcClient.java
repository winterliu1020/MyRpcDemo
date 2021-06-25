package me.liuwentao.rpc.core;

import me.liuwentao.rpc.common.Entity.RpcRequest;

/**
 * Created by liuwentao on 2021/6/14 22:59
 */
public interface RpcClient {
    // sendRequest方法
    public Object sendRequest(RpcRequest rpcRequest);
}
