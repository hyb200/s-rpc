package com.abin.core.server.handler;

import com.abin.core.model.RpcRequest;
import com.abin.core.model.RpcResponse;
import com.abin.core.registry.LocalRegistry;
import com.abin.core.serializer.JdkSerializer;
import com.abin.core.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    private static final Serializer SERIALIZER = new JdkSerializer();

    private static void doResponse(ChannelHandlerContext ctx, RpcResponse rpcResponse) {
        try {
            byte[] serialize = SERIALIZER.serialize(rpcResponse);
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(serialize));
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } catch (IOException e) {
            log.error("序列化失败：{}", e.getMessage());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof FullHttpRequest request) {
                ByteBuf byteBuf = request.content();
                byte[] bytes = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(bytes);

                RpcRequest rpcRequest = null;
                try {
                    rpcRequest = SERIALIZER.deserialize(bytes, RpcRequest.class);
                } catch (IOException e) {
                    log.error("反序列化失败：{}", e.getMessage());
                }

                if (Objects.isNull(rpcRequest)) {
                    log.error("rpcRequest is null");
                    return;
                }
                RpcResponse rpcResponse = new RpcResponse();
                try {
                    Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                    Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                    Object result = method.invoke(implClass.getDeclaredConstructor().newInstance(), rpcRequest.getArgs());
                    log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getServiceName(), rpcRequest.getMethodName());
                    rpcResponse.setData(result);
                    rpcResponse.setDataType(method.getReturnType());
                    rpcResponse.setMessage("ok");
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    log.error("service call exception: {}", e.getMessage());
                    rpcResponse.setMessage(e.getMessage());
                    rpcResponse.setException(e);
                }
                doResponse(ctx, rpcResponse);
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
