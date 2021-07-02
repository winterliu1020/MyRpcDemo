package me.liuwentao.rpc.common.Enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by liuwentao on 2021/6/15 15:03
 *
 * 在字节流中标识用哪一种（反）序列化方式
 */
@Getter
@AllArgsConstructor
public enum SerializerCode {
    Kryo(0),
    JSON(1);
    private final Integer code;
}
