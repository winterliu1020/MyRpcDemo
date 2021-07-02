package me.liuwentao.rpc.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Entity.RpcResponse;
import me.liuwentao.rpc.common.Enumeration.PackageType;
import me.liuwentao.rpc.common.Enumeration.RpcError;
import me.liuwentao.rpc.common.Exception.RpcException;
import me.liuwentao.rpc.core.Serializer.CommonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by liuwentao on 2021/6/15 10:15
 *
 * 通用解码拦截器；将管道中的字节流读取出来，这里读取出来之后需要校验自定义数据包中的各项数据，如果没问题才把数据包中真正的数据块字节流反序列化成对象
 *
 * 解码器不需要与反序列化类绑定，因为是从管道流中拿到序列化code，根据code获取对应的反序列化类
 */
public class CommonDecoder extends ReplayingDecoder {
    private static final int MAGIC_NUMBER = 0xCAFEBABE;
    Logger logger = LoggerFactory.getLogger(CommonDecoder.class);

    // 这里的byteBuf就是别的地方（客户端/服务器端）写过来的字节流，所以这是一个inBound事件；list是一个结果列表
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 依次对魔数、packageType进行校验
        int magic_num = byteBuf.readInt();
        if (magic_num != MAGIC_NUMBER) {
            logger.error("未知协议包");
            throw new RpcException(RpcError.PROTOCOL_NOT_FOUND);
        }
        int packageCode = byteBuf.readInt();
        Class<?> packageClass;
        if (packageCode == PackageType.REQUEST_PACKAGE.getCode()) {
            packageClass = RpcRequest.class;
        } else if (packageCode == PackageType.RESPONSE_PACKAGE.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            logger.error("未知数据包类型");
            throw new RpcException(RpcError.PACKAGE_TYPE_NOT_FOUND);
        }
        // 通过serializerCode拿到对应的序列化类；而不是传某个序列化类对象
        int serializerCode = byteBuf.readInt();
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if (serializer == null) {
            logger.error("未知的序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        // 用序列化类进行对读取到的字节流进行反序列化
        int length = byteBuf.readInt();
        byte[] bytes = new byte[length]; // 通过length字段读取数据包的长度，防止粘包
        byteBuf.readBytes(bytes); // 声明一个length长度的字节数组，然后把字节流中的字节填充进去
        Object object = serializer.deserializer(bytes, packageClass); // 也就是一个RpcResponse/RpcRequest对象
        list.add(object); // 会传给下一个handler
    }
}
