package me.liuwentao.rpc.core;

import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Entity.RpcResponse;
import me.liuwentao.rpc.common.Enumeration.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by liuwentao on 2021/6/14 12:41
 */
public class RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    public Object handler(RpcRequest rpcRequest, Object service) {
        Object result = null;
        result = invokeTargetMethod(rpcRequest, service);
        logger.info("服务{}成功调用方法{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        return result;
    }

    // 反射执行
    public static Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Method method = null;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            return RpcResponse.fail(ResponseCode.NOT_FOUND_METHOD);
        }
        try {
            logger.info("反射执行的方法：{}", method.getName());
            logger.info("方法中各个参数：{}", rpcRequest.getParameters());
            return method.invoke(service, rpcRequest.getParameters());
        } catch (IllegalAccessException e) {
            logger.error("非法访问异常：", e); // 调用的方法是private的
        } catch (InvocationTargetException e) {
            logger.error("反射异常：", e); // 当被调用方法中异常没有捕获时，由此异常进行接收
        }
        return null;
    }
}
