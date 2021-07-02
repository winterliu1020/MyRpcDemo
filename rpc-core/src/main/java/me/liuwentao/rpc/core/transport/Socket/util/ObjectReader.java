package me.liuwentao.rpc.core.transport.Socket.util;import me.liuwentao.rpc.common.Entity.RpcRequest;import me.liuwentao.rpc.common.Entity.RpcResponse;import me.liuwentao.rpc.common.Enumeration.PackageType;import me.liuwentao.rpc.common.Enumeration.RpcError;import me.liuwentao.rpc.common.Exception.RpcException;import me.liuwentao.rpc.core.Serializer.CommonSerializer;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import java.io.IOException;import java.io.InputStream;/** * Created by liuwentao on 2021/7/1 20:26 */public class ObjectReader {    private static final Logger logger = LoggerFactory.getLogger(ObjectReader.class);    private static final int MAGIC_NUMBER = 0xCAFEBABE;    //    public static Object readObject(InputStream inputStream) throws IOException {        // 也要按照约定的协议包格式读数据流        // 魔数        byte[] numberBytes = new byte[4];        inputStream.read(numberBytes);        int magicNumber = bytesToInt(numberBytes);        if (magicNumber != MAGIC_NUMBER) {            logger.error("无法识别的协议包{}", magicNumber);            throw new RpcException(RpcError.PROTOCOL_NOT_FOUND);        }        // 数据类型        inputStream.read(numberBytes);        int dataType = bytesToInt(numberBytes);        Class<?> packageType; // 记录数据包类型等会用于反序列化        if (dataType == PackageType.REQUEST_PACKAGE.getCode()) { // 如果是一个请求包            packageType = RpcRequest.class;        } else if (dataType == PackageType.RESPONSE_PACKAGE.getCode()){            packageType = RpcResponse.class;        } else {            // 未知数据包            logger.error("未知数据包:{}", dataType);            throw new RpcException(RpcError.PACKAGE_TYPE_NOT_FOUND);        }        // serializerCode        inputStream.read(numberBytes);        int serializerCode = bytesToInt(numberBytes);        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);        if (serializer == null) {            logger.error("未知的序列化方式,serializerCode:{}", serializerCode);            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);        }        // 数据字节长度        inputStream.read(numberBytes);        int dataLength = bytesToInt(numberBytes);        // 数据字节数组        byte[] data = new byte[dataLength];        inputStream.read(data);        // 反序列化        Object object = serializer.deserializer(data, packageType);        return object;    }    public static int bytesToInt(byte[] src) {        // 四个字节的数组转成int类型        int value;        value = ((src[0] & 0xFF)<<24) // 0x十六进制 FF就是八个1，也就是一个字节八位上全是1                |((src[1] & 0xFF)<<16)                |((src[2] & 0xFF)<<8)                |(src[3] & 0xFF);        return value;    }}