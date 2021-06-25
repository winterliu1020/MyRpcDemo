package me.liuwentao.rpc.common.Enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by liuwentao on 2021/6/15 15:06
 *
 * 字节流中标识包类型
 */
@AllArgsConstructor
@Getter
public enum PackageType {
    REQUEST_PACKAGE(0),
    RESPONSE_PACKAGE(1);
    private final Integer code;
}
