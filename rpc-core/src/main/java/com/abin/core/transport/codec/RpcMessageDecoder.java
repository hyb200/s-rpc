package com.abin.core.transport.codec;

import com.abin.core.model.RpcRequest;
import com.abin.core.model.RpcResponse;
import com.abin.core.protocol.ProtocolConstant;
import com.abin.core.protocol.ProtocolMessage;
import com.abin.core.protocol.enums.MessageSerializer;
import com.abin.core.protocol.enums.MessageType;
import com.abin.core.serializer.Serializer;
import com.abin.core.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder() {
        this(ProtocolConstant.MAX_FRAME_LENGTH, 13, 4, 0, 0);
    }

    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf byteBuf) {
            try {
                byte magic = byteBuf.readByte();
                if (magic != ProtocolConstant.PROTOCOL_MAGIC) {
                    throw new IllegalArgumentException("Unknown magic code: " + magic);
                }
                byte version = byteBuf.readByte();
                byte codec = byteBuf.readByte();
                byte type = byteBuf.readByte();

                ProtocolMessage.Header header = new ProtocolMessage.Header();
                header.setMagic(magic);
                header.setVersion(version);
                header.setCodec(codec);
                header.setType(type);
                header.setStatus(byteBuf.readByte());
                header.setRequestId(byteBuf.readLong());
                int bodyLength = byteBuf.readInt();
                byte[] body = new byte[bodyLength];
                byteBuf.readBytes(body);

                MessageSerializer serializerEnum = MessageSerializer.getEnumByKey(codec);
                Serializer serializer = SerializerFactory.getInstance(serializerEnum.getVal());

                ProtocolMessage<Object> protocolMessage = new ProtocolMessage<>();
                protocolMessage.setHeader(header);

                if (type == MessageType.REQUEST.getKey()) {
                    RpcRequest rpcRequest = serializer.deserialize(body, RpcRequest.class);
                    protocolMessage.setBody(rpcRequest);
                }
                if (type == MessageType.RESPONSE.getKey()) {
                    RpcResponse rpcResponse = serializer.deserialize(body, RpcResponse.class);
                    protocolMessage.setBody(rpcResponse);
                }

                return protocolMessage;
            } catch (Exception e) {
                log.error("Decode error.", e);
            } finally {
                byteBuf.release();
            }
        }
        return decode;
    }
}
