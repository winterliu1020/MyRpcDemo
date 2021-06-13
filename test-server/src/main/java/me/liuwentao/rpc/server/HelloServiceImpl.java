package me.liuwentao.rpc.server;

import me.liuwentao.rpc.api.HelloObject;
import me.liuwentao.rpc.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liuwentao on 2021/6/10 15:08
 *
 * 测试用提供端，也就是服务器端；用于去实现通用接口
 */
public class HelloServiceImpl implements HelloService {
    // 把当前类作为参数，输出日志的时候会打印是当前类的日志信息
    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);
    @Override
    public String sayHello(HelloObject helloObject) {
        logger.info("接收到{}", helloObject.getMessage());
        return "这是调用的返回值：" + helloObject.getId();
    }
}
