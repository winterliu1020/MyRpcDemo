package me.liuwentao.rpc.core.Provider;

import me.liuwentao.rpc.core.config.RpcServiceConfig;

/**
 * Created by liuwentao on 2021/6/13 23:44
 *
 * 在服务器端保存和提供服务实例对象
 */
public interface ServiceProvider {

    void addServiceProvide(RpcServiceConfig rpcServiceConfig, String interfaceGroupVersionName); // param1: 具体的服务实现类 param2: 你要给哪个接口进行注册

    Object getServiceProvider(String interfaceGroupVersionName);
}
