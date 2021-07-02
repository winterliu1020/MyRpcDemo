package me.liuwentao.rpc.core.Registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import me.liuwentao.rpc.common.Enumeration.RpcError;
import me.liuwentao.rpc.common.Exception.RpcException;
import me.liuwentao.rpc.core.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by liuwentao on 2021/6/27 19:57
 *
 * nacos服务注册中心
 */
public class NacosServiceRegistry implements ServiceRegistry{
    private static final Logger logger = LoggerFactory.getLogger(NacosServiceRegistry.class);
    private static final String SERVER_ADDRESS = "127.0.0.1:8848"; // 注册中心的地址；也就是连接到nacos启动的服务
    private static final NamingService namingService;

    static {
        // 根据SERVER_ADDRESS得到一个namingService对象；注意这里是放在static块
        try {
            namingService = NamingFactory.createNamingService(SERVER_ADDRESS);
        } catch (NacosException e) {
            logger.error("连接到server的时候发生错误：", e);
            throw new RpcException(RpcError.FAILED_TO_CONNECTED_TO_SERVICE_REGISTRY);
        }
    }
    // 参数：1. 调用的接口的类名， 2. 注册到的inetSocketAddress地址
    @Override
    public void register(String interfaceName, InetSocketAddress inetSocketAddress) {
        // 将一个服务进行注册；serviceName注册到inetSocketAddress这个地址
        try {
            namingService.registerInstance(interfaceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
        } catch (NacosException e) {
            logger.error("注册服务失败", e);
            throw new RpcException(RpcError.FAILED_TO_REGISTER_SERVICE);
        }
    }

    @Override
    public InetSocketAddress lookupService(String interfaceName) {
        // 通过interfaceName拿到这个服务对应的host:port
        try { // 一个interfaceName对应一个host:port;
            List<Instance> list = namingService.getAllInstances(interfaceName);
            Instance instance = list.get(0); // 这里直接拿的是第一个，这里涉及到负载均衡策略；也就是说假设多台服务器都部署了实现这个接口的服务，那么我在这里就可以采用一定的负载算法；
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            logger.error("查找服务时发生失败", e);
        }
        return null;
    }
}
