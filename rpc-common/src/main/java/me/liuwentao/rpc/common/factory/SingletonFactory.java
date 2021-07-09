package me.liuwentao.rpc.common.factory;import java.util.HashMap;/** * Created by liuwentao on 2021/7/6 11:50 */public class SingletonFactory {    // clazz对象，对应一个实例    private static HashMap<Class, Object> map = new HashMap<>();    private SingletonFactory(){    }    public static <T> T newInstance(Class<T> clazz) {        // 看下传过来的clazz是否在map中，在的话说明它已经实例化了，不需要在帮它创建实例了        Object instance = map.get(clazz);        synchronized (clazz) {            if (null == instance) {                try {                    instance = clazz.newInstance();                    map.put(clazz, instance);                } catch (InstantiationException | IllegalAccessException e) {                    throw new RuntimeException(e.getMessage(), e);                }            }        }        return clazz.cast(instance);    }}