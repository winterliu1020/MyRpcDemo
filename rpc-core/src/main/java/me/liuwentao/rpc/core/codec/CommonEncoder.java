package me.liuwentao.rpc.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.liuwentao.rpc.common.Entity.RpcRequest;
import me.liuwentao.rpc.common.Enumeration.PackageType;
import me.liuwentao.rpc.common.Enumeration.SerializerCode;
import me.liuwentao.rpc.core.Serializer.CommonSerializer;
import me.liuwentao.rpc.core.Serializer.JsonSerializer;

/**
 * Created by liuwentao on 2021/6/15 10:14
 *
 * 通用编码拦截器，这个类继承了MessageToByteEncoder，这样管道中的数据就会在这个类中重写的encode方法进行处理
 */
public class CommonEncoder extends MessageToByteEncoder {
    // 自定义数据包中的魔数
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    private final CommonSerializer serializer;
    public CommonEncoder(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    // 在MessageToByteEncoder类中，o是你要发送的对象 这里其实就是你用channel.writeAndFlush(rpcRequest)写过来的rpcRequest对象，然后你需要把你要发送的对象的字节流通过byteBuf写出去
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        // encode其实就是将：魔数、包类型、序列化类型、数据长度、数据字节流依次以流的形式往后面写
        byteBuf.writeInt(MAGIC_NUMBER);
        if (o instanceof RpcRequest) {
            byteBuf.writeInt(PackageType.REQUEST_PACKAGE.getCode());
        } else {
            byteBuf.writeInt((PackageType.RESPONSE_PACKAGE.getCode()));
        }
        byteBuf.writeInt(serializer.getCode());
        byte[] bytes = serializer.serializer(o);
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }
}
