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
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 7358081586427647868L; // 用jdk原生的序列化就需要实现Serializable接口；用Jackson来序列化就可以不用实现接口

    public RpcRequest(){}
    private String requestId; // 请求号

    // 一个rpc请求需要有：接口名字、方法名字、方法中的参数、方法中参数的类型
    private String interfaceName;
    private String methodName;
    private Object[] parameters; // 方法中传的对象实体
    private Class<?>[] paramTypes; // 方法中参数的类型; 也可以直接用String

    // 由于同一个接口可能有多种实现类，所以对服务进行分组，发布服务的时候增加group、version参数；version用于兼容版本
    private String group;
    private String version;

    // 是否是心跳包
    private Boolean heartBeat;
}
