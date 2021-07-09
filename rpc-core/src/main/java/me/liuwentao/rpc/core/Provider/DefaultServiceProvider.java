package me.liuwentao.rpc.core.Provider;

import me.liuwentao.rpc.common.Enumeration.RpcError;
import me.liuwentao.rpc.common.Exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liuwentao on 2021/6/13 23:48
 *
 * 默认的服务注册表
 */
public class DefaultServiceProvider implements ServiceProvider {
    // 将这个接口的完整类名作为服务名；
    // 向 接口 注册 服务；也就是调用这个接口上所有的方法都是通过这个service去执行
    // 说明：v2.0版本中将这两个属性变成static，也就是说全局只有一份注册表，这样RpcServer类也不用和ServiceRegister对象进行绑定了
    private static final ConcurrentHashMap<String, Object> serviceMap = new ConcurrentHashMap<>(); // 接口名，service；因为同一个service可能实现了很多接口
    private static final Set<String> registerService = ConcurrentHashMap.newKeySet(); // 放所有服务实现类的名字；一个线程安全的set

    private static Logger logger = LoggerFactory.getLogger(ServiceProvider.class);

    @Override
    public synchronized <T> void addServiceProvide(T service) { // 这个service是具体的服务实现类，eg:HelloServiceImpl
        // 将service上的所有接口、service注册到注册表
        // 1. 如果已经在注册表，直接return
        String serviceName = service.getClass().getCanonicalName();
        if (registerService.contains(serviceName)) return;
        // 2. 否则的话就把service这个类上所有的接口、service注册到注册表map
        registerService.add(serviceName);
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if (interfaces.length == 0) {
            throw(new RpcException(RpcError.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE));
        }
        for (Class<?> clazz : interfaces) {
            serviceMap.put(clazz.getCanonicalName(), service);
        }
        logger.info("向接口{}注册服务{}", interfaces, serviceName);
    }

    // 通过interfaceName获取注册表中的service实例；因为在serviceMap中是：interfaceName对应一个具体的实现对象service；
    // 说明：一个接口只能对应一个service对象，而一个service对象可以用于实现多个接口
    @Override
    public synchronized Object getServiceProvider(String interfaceName) { // 这里传入你需要调用的接口名，会返回这个接口对应的服务实现类
        Object service = serviceMap.get(interfaceName);
        if (service == null) {
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        }
        return service;
    }
}
