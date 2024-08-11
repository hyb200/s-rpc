package com.abin.core.transport.client.handler;

import com.abin.core.factory.SingletonFactory;
import com.abin.core.model.RpcResponse;
import com.abin.core.protocol.ProtocolMessage;
import com.abin.core.protocol.enums.MessageType;
import com.abin.core.transport.client.UnprocessedRequests;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {

    private final UnprocessedRequests unprocessedRequests;

    public NettyRpcClientHandler() {
        unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            log.info("client receive msg: [{}]", msg);
            if (msg instanceof ProtocolMessage<?> response) {
                ProtocolMessage.Header header = response.getHeader();
                byte type = header.getType();
                if (type == MessageType.RESPONSE.getKey()) {
                    unprocessedRequests.complete((ProtocolMessage<RpcResponse>) response);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client caught exception.", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
