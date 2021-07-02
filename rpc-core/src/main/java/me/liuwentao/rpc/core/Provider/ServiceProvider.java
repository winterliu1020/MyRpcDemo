package me.liuwentao.rpc.core.Provider;

/**
 * Created by liuwentao on 2021/6/13 23:44
 *
 * 在服务器端保存和提供服务实例对象
 */
public interface ServiceProvider {

    <T> void addServiceProvide(T service);

    Object getServiceProvider(String serviceName);
}
