package me.liuwentao.rpc.common.Enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by liuwentao on 2021/6/10 15:48
 */
@AllArgsConstructor
@Getter
public enum ResponseCode {
    SUCCESS(200, "成功调用"),
    FIELD(404, "失败调用"),
    NOT_FOUND_METHOD(500, "没找到API对应的方法"),
    NOT_FOUND_CLASS(500, "没找到API所在类");
    private final Integer code;
    private final String message;
}
