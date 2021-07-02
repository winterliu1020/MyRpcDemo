package me.liuwentao.rpc.core.Serializer;

/**
 * Created by liuwentao on 2021/6/15 10:52
 * 统一的序列化接口，其它的具体的序列化、反序列化实现类都是实现这个接口中的方法
 */
public interface CommonSerializer {
    // 序列化：将对象转成字节数组
    byte[] serializer(Object object);

    // 反序列化：将字节数组写成指定的类对象
    Object deserializer(byte[] bytes, Class<?> clazz);

    int getCode(); // 获取某个具体序列化实现类的唯一标识

    // 这里还是回到接口（其实是约定一种协议）的设计思想来：接口中的属性应该是不可变的，接口符合开闭原则，这里的getByCode()方法是static方法，接口中允许将相关的方法以static的形式内聚在接口中，类似于类中的静态方法；
    // 但是接口中的static方法和抽象类中static方法又有些不同，抽象类中可以有可变的属性、构造方法；
    // 通过标识code拿到某个序列化实现类
    static CommonSerializer getByCode(int code) {
        switch (code){
            case 0:
                return new KryoSerializer();
            case 1:
                return new JsonSerializer();
            default:
                return null;

        }
    }
}
