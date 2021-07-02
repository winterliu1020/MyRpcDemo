package me.liuwentao.rpc.common.Entity;

import lombok.Data;
import me.liuwentao.rpc.common.Enumeration.ResponseCode;

import java.io.Serializable;

/**
 * Created by liuwentao on 2021/6/10 15:37
 *
 * client发起rpc请求之后服务端的响应
 */
@Data
public class RpcResponse<T> implements Serializable {
    // 响应对应的请求号
    private String requestId;

    // 需要包括：状态码、附加信息message、响应数据data
    private Integer statusCode;
    private String message;
    private T data;

    // 这里多写两个静态方法用于快速生成两个response对象
    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> rpcResponse = new RpcResponse<>();
        rpcResponse.setRequestId(requestId);
        rpcResponse.setStatusCode(ResponseCode.SUCCESS.getCode());
        rpcResponse.setMessage(ResponseCode.SUCCESS.getMessage());
        rpcResponse.setData(data);
        return rpcResponse;
    }

    // 参数：传一个responseCode对象过来
    public static <T> RpcResponse<T> fail(ResponseCode responseCode) {
        RpcResponse<T> rpcResponse = new RpcResponse<>();
        rpcResponse.setStatusCode(responseCode.getCode());
        rpcResponse.setMessage(responseCode.getMessage());
        return rpcResponse;
    }
}
