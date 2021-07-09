package me.liuwentao.rpc.core.Registry;

import java.net.InetSocketAddress;

/**
 * Created by liuwentao on 2021/6/27 19:54
 *
 * 服务注册、发现nacos；
 */
public interface ServiceRegistry {
    // 将一个服务注册到注册中心
    public void register(String serviceName, InetSocketAddress inetSocketAddress);
}
