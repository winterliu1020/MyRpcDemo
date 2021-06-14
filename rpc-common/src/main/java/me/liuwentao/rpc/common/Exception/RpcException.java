package me.liuwentao.rpc.common.Exception;

import me.liuwentao.rpc.common.Enumeration.RpcError;

/**
 * Created by liuwentao on 2021/6/14 00:16
 *
 * rpc调用异常
 */
public class RpcException extends RuntimeException{
    public RpcException(RpcError error) {
        super(error.getMessage());
    }
}
