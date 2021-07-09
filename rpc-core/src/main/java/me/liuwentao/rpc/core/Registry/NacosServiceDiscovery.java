package me.liuwentao.rpc.core.Registry;import com.alibaba.nacos.api.exception.NacosException;import com.alibaba.nacos.api.naming.pojo.Instance;import me.liuwentao.rpc.common.util.NacosUtil;import me.liuwentao.rpc.core.loadBalance.LoadBalance;import me.liuwentao.rpc.core.loadBalance.RandomLoadBalance;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import java.net.InetSocketAddress;import java.util.List;/** * Created by liuwentao on 2021/7/5 11:10 * * 发现服务，涉及到负载均衡 * 服务发现类构造方法中需要指定负载均衡的策略 */public class NacosServiceDiscovery implements ServiceDiscovery{    private static final Logger logger = LoggerFactory.getLogger(NacosServiceDiscovery.class);    private final LoadBalance loadBalance;    public NacosServiceDiscovery(LoadBalance loadBalance) {        this.loadBalance = loadBalance;    }    @Override    public InetSocketAddress lookupService(String interfaceName) {        // 通过interfaceName拿到这个服务对应的host:port        // 一个interfaceName对应一个host:port;        List<Instance> list = NacosUtil.getAllInstance(interfaceName);        Instance instance = loadBalance.select(list); // 根据设置的负载均衡策略来对所有实现该服务的服务端进行选择        // 这里涉及到负载均衡策略；也就是说假设多台服务器都部署了实现这个接口的服务，那么我在这里就可以采用一定的负载算法；        return new InetSocketAddress(instance.getIp(), instance.getPort());    }}