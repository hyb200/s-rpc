package com.abin.core.transport.server.handler;

import com.abin.core.model.RpcRequest;
import com.abin.core.model.RpcResponse;
import com.abin.core.protocol.ProtocolMessage;
import com.abin.core.protocol.enums.MessageType;
import com.abin.core.registry.LocalRegistry;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    private static void doResponse(ChannelHandlerContext ctx, ProtocolMessage.Header header, RpcResponse rpcResponse) {
        ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
        header.setType((byte) MessageType.RESPONSE.getKey());
        ctx.writeAndFlush(rpcResponseProtocolMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof ProtocolMessage request) {
                log.info("server receive msg: [{}] ", msg);
                RpcRequest rpcRequest = (RpcRequest) request.getBody();
                RpcResponse rpcResponse = new RpcResponse();
                try {
                    Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                    Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                    Object result = method.invoke(implClass.getDeclaredConstructor().newInstance(), rpcRequest.getArgs());
                    log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getServiceName(), rpcRequest.getMethodName());
                    rpcResponse.setData(result);
                    rpcResponse.setDataType(method.getReturnType());
                    rpcResponse.setMessage("ok");
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                         InvocationTargetException e) {
                    log.error("service call exception: {}", e.getMessage());
                    rpcResponse.setMessage(e.getMessage());
                    rpcResponse.setException(e);
                }
                doResponse(ctx, request.getHeader(), rpcResponse);
            }
        } finally {
            //  注意释放 Bytebuf，防止内存泄漏
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("An error occurred! ", cause);
        ctx.close();
    }
}
