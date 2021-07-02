package me.liuwentao.rpc.common.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by liuwentao on 2021/6/10 15:25
 */
@Data
@Builder
@AllArgsConstructor
public class RpcRequest implements Serializable { // 用jdk原生的序列化就需要实现Serializable接口；用Jackson来序列化就可以不用实现接口
    public RpcRequest(){}
    private String requestId; // 请求号

    // 一个rpc请求需要有：接口名字、方法名字、方法中的参数、方法中参数的类型
    private String interfaceName;
    private String methodName;
    private Object[] parameters; // 方法中传的对象实体
    private Class<?>[] paramTypes; // 方法中参数的类型; 也可以直接用String
}
