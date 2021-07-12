package me.liuwentao.rpc.core.transport;import me.liuwentao.rpc.common.Enumeration.RpcError;import me.liuwentao.rpc.common.Exception.RpcException;import me.liuwentao.rpc.common.util.ReflectUtil;import me.liuwentao.rpc.core.Provider.ServiceProvider;import me.liuwentao.rpc.core.Registry.ServiceRegistry;import me.liuwentao.rpc.core.annotation.Service;import me.liuwentao.rpc.core.annotation.ServiceScan;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import java.net.InetSocketAddress;import java.util.Set;/** * Created by liuwentao on 2021/7/12 11:48 */public abstract class AbstractRpcServer implements RpcServer{    protected Logger logger = LoggerFactory.getLogger(AbstractRpcServer.class);    // RpcServer的四个属性    protected String host;    protected Integer port;    protected ServiceRegistry serviceRegistry;    protected ServiceProvider serviceProvider;    // 服务扫描    public void scanService() {        String mainClassName = ReflectUtil.getStackTrace(); // 或得主类名        Class<?> startClass = null;        try {            startClass = Class.forName(mainClassName);            if (!startClass.isAnnotationPresent(ServiceScan.class)) {                logger.error("启动类少了@ServiceScan注解");                throw new RpcException(RpcError.SERVICE_SCAN_PACKAGE_NOT_FOUND);            }        } catch (ClassNotFoundException e) {            logger.error("无法找到主类", e);            e.printStackTrace();        }        String basePackage = startClass.getAnnotation(ServiceScan.class).value(); // 获取主类上ServiceScan注解的value属性值        if ("".equals(basePackage)) {            logger.info("basePackage为空字符串");            basePackage = mainClassName.substring(0, mainClassName.lastIndexOf("."));        }        Set<Class<?>> classSet = ReflectUtil.getClass(basePackage);        for (Class<?> clazz : classSet) {            if (clazz.isAnnotationPresent(Service.class)) {                String serviceName = clazz.getAnnotation(Service.class).name();                Object obj;                try {                    obj = clazz.newInstance();                } catch (InstantiationException | IllegalAccessException e) {                    logger.error("创建" + clazz + "时发生错误");                    continue;                }                if ("".equals(serviceName)) {                    logger.info("serviceName为空字符串");                    Class<?>[] interfaces = clazz.getInterfaces();                    for (Class<?> oneInterface : interfaces) {                        publishService(obj, oneInterface.getCanonicalName()); // 服务具体实现类，该实现类上的接口                    }                } else {                    publishService(obj, serviceName);                }            }        }    }    @Override    public <T> void publishService(T obj, String interfaceName) {        serviceProvider.addServiceProvide(obj, interfaceName);        serviceRegistry.register(interfaceName, new InetSocketAddress(host, port));    }}