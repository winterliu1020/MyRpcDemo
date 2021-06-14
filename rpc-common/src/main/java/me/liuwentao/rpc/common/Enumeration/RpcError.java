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
    SERVICE_NOT_FOUND("注册表中没有发现当前服务");
    private final String message;
}