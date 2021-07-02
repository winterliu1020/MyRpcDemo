package me.liuwentao.rpc.common.Enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by liuwentao on 2021/6/14 10:40
 */
@AllArgsConstructor
@Getter
public enum RpcError {
    SERVICE_NOT_IMPLEMENT_ANY_INTERFACE("服务没有实现任何接口"),
    SERVICE_NOT_FOUND("注册表中没有发现当前服务"),
    PROTOCOL_NOT_FOUND("未知协议数据包"),
    PACKAGE_TYPE_NOT_FOUND("未知数据包类型"),
    SERIALIZER_NOT_FOUND("未知的序列化器"),

    FAILED_TO_CONNECTED_TO_SERVICE_REGISTRY("连接到服务注册地址失败"),
    FAILED_TO_REGISTER_SERVICE("注册服务失败"),
    CLIENT_CONNECT_SERVER_FAILURE("client连接server端失败"),
    SERVICE_INVOCATION_FAILURE("服务调用失败"),
    RESPONSE_NOT_MATCH("响应结果不匹配");
    private final String message;
}
