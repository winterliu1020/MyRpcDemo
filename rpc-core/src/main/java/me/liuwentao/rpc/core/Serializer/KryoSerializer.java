package me.liuwentao.rpc.core.Serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Entity.RpcResponse;
import me.liuwentao.rpc.common.Enumeration.SerializerCode;
import me.liuwentao.rpc.common.Exception.SerializeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by liuwentao on 2021/6/25 17:18
 */
public class KryoSerializer implements CommonSerializer {
    private static final Logger logger = LoggerFactory.getLogger(KryoSerializer.class);
    // 将kryo与线程绑定
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(()->{
        Kryo kryo = new Kryo();
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public byte[] serializer(Object object) {
        // 用kryo进行序列化
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); Output output = new Output(byteArrayOutputStream)){ // kryo中的Output是对java.io中的OutputStream类的扩展
             Kryo kryo = kryoThreadLocal.get();
             kryo.writeObject(output, object); // 对object对象进行序列化（kryo会保留对象类型）；其实这里把object对象的字节流写到了output这个数组中
             kryoThreadLocal.remove(); // 把这个线程绑定的kryo对象remove掉
             return output.toBytes();
        } catch (Exception e) {
            logger.error("序列化时发生错误：", e);
            throw new SerializeException("序列化时发生错误");
        }
    }

    @Override
    public Object deserializer(byte[] bytes, Class<?> clazz) {
        // 用kryo进行反序列化
        // try()中所有实现Closeable的声明都可以写在里面，比如可以写获取资源的操作 socket连接、file流，在try{}块退出的时候会自动帮我们释放资源
        // 注意点：你可以看到try()中两行代码并不会抛出IOException，但是为什么写在try()中会提示我们要catch IOException呢？因为会帮我们自动做byteArrayInputStream.close(); input.close();所以涉及到IOException
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes); Input input = new Input(byteArrayInputStream)){ // 1.7优化的try-with-resource
            Kryo kryo = kryoThreadLocal.get();
            Object object = kryo.readObject(input, clazz); // 反序列化得到的对象
            kryoThreadLocal.remove();
            return object;
        } catch (IOException e) {
            logger.error("反序列化失败：", e);
            throw new SerializeException("反序列化失败");
        }
    }

    @Override
    public int getCode() {
        return SerializerCode.Kryo.getCode();
    }
}
