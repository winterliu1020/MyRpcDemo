package me.liuwentao.rpc.core.hook;import me.liuwentao.rpc.common.factory.ThreadPoolFactory;import me.liuwentao.rpc.common.util.NacosUtil;import org.slf4j.Logger;import org.slf4j.LoggerFactory;/** * Created by liuwentao on 2021/7/7 16:25 */public class ShutdownHook {    private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);    private static final ShutdownHook shutdownHook = new ShutdownHook();    public static ShutdownHook getShutdownHook() {        return shutdownHook;    }    public void addClearAllHook() {        logger.info("清除远程注册中心上所有服务");        Runtime.getRuntime().addShutdownHook(new Thread(()->{            NacosUtil.clearRegistry();            ThreadPoolFactory.shutdownAll();        }));    }}