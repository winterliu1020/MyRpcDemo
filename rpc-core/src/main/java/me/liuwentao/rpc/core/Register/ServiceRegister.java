package me.liuwentao.rpc.core.Register;

/**
 * Created by liuwentao on 2021/6/13 23:44
 */
public interface ServiceRegister {
    // 一个注册方法、一个getService()
    <T> void register(T service);
    Object getService(String serviceName);
}
