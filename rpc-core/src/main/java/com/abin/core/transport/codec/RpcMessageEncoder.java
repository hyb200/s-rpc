package com.abin.core.transport.codec;

import com.abin.core.model.RpcRequest;
import com.abin.core.protocol.ProtocolMessage;
import com.abin.core.protocol.enums.MessageSerializer;
import com.abin.core.serializer.Serializer;
import com.abin.core.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 *   0        1        2        3        4        5         ...       13                17
 *   +--------+--------+--------+--------+--------+--------------------+----------------+
 *   | magic  |version | codec  |  type  | status |    requestId       |   bodyLength   |
 *   +--------+--------+--------+--------+--------+--------------------+----------------+
 *   |                                                                                  |
 *   |                                   body                                           |
 *   |                                                                                  |
 *   +----------------------------------------------------------------------------------+
 * 1B magic（魔数）
 * 1B version（版本）
 * 1B codec（序列化类型）
 * 1B type（消息类型）
 * 1B status（状态）
 * 8B requestId（请求ID）
 * 4B bodyLenght（消息长度）
 * body（数据）
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<ProtocolMessage<RpcRequest>> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ProtocolMessage msg, ByteBuf out) throws Exception {
        try {
            ProtocolMessage.Header header = msg.getHeader();

            out.writeByte(header.getMagic());
            out.writeByte(header.getVersion());
            out.writeByte(header.getCodec());
            out.writeByte(header.getType());
            out.writeByte(header.getStatus());
            out.writeLong(header.getRequestId());

            MessageSerializer serializerEnum = MessageSerializer.getEnumByKey(header.getCodec());
            if (Objects.isNull(serializerEnum)) {
                log.error("failed to encode message, can not find the serializer");
            }

            Serializer serializer = SerializerFactory.getInstance(serializerEnum.getVal());
            byte[] bytes = serializer.serialize(msg.getBody());

            out.writeInt(bytes.length);
            out.writeBytes(bytes);

        } catch (Exception e) {
            log.error("Encode request error!", e);
        }

    }
}
