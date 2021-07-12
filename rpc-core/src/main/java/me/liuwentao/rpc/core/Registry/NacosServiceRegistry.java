package me.liuwentao.rpc.core.Registry;

import me.liuwentao.rpc.common.util.NacosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;

/**
 * Created by liuwentao on 2021/6/27 19:57
 *
 * nacos服务注册中心
 */
public class NacosServiceRegistry implements ServiceRegistry{
    private static final Logger logger = LoggerFactory.getLogger(NacosServiceRegistry.class);

    // 参数：1. 调用的接口的类名， 2. 注册到的inetSocketAddress地址
    @Override
    public void register(String interfaceName, InetSocketAddress inetSocketAddress) {
        // 将一个服务进行注册；serviceName注册到inetSocketAddress这个地址
        NacosUtil.registryService(interfaceName, inetSocketAddress);
    }
}
