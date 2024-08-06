package com.abin.core.transport.client;

import cn.hutool.core.util.IdUtil;
import com.abin.core.RpcApplication;
import com.abin.core.factory.SingletonFactory;
import com.abin.core.model.RpcRequest;
import com.abin.core.model.RpcResponse;
import com.abin.core.model.ServiceMetaInfo;
import com.abin.core.protocol.ProtocolConstant;
import com.abin.core.protocol.ProtocolMessage;
import com.abin.core.protocol.enums.MessageSerializer;
import com.abin.core.protocol.enums.MessageType;
import com.abin.core.transport.client.handler.NettyRpcClientHandler;
import com.abin.core.transport.codec.RpcMessageDecoder;
import com.abin.core.transport.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyTcpClient {

    private final Bootstrap bootstrap;

    private final EventLoopGroup eventLoopGroup;

    private final ChannelKeeper channelKeeper;

    private final UnprocessedRequests unprocessedRequests;


    public NettyTcpClient() {
        eventLoopGroup = new NioEventLoopGroup();

        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        pipeline.addLast(new RpcMessageEncoder());
                        pipeline.addLast(new RpcMessageDecoder());
                        pipeline.addLast(new NettyRpcClientHandler());
                    }
                });
        channelKeeper = SingletonFactory.getInstance(ChannelKeeper.class);
        unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    public Object sendRpcRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo) {
        // build return value
        CompletableFuture<ProtocolMessage<RpcResponse>> resultFuture = new CompletableFuture<>();
        String host = serviceMetaInfo.getHost();
        String port = serviceMetaInfo.getPort();

        // get server address related channel
        Channel channel = getChannel(new InetSocketAddress(host, Integer.parseInt(port)));
        if (channel.isActive()) {
            long reqID = IdUtil.getSnowflakeNextId();
            unprocessedRequests.put(reqID, resultFuture);
            ProtocolMessage.Header header = new ProtocolMessage.Header();
            header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
            header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
            header.setCodec((byte) MessageSerializer.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
            header.setType((byte) MessageType.REQUEST.getKey());
            header.setRequestId(reqID);

            ProtocolMessage<RpcRequest> protocolMessage = ProtocolMessage.<RpcRequest>builder()
                    .header(header)
                    .body(rpcRequest)
                    .build();

            channel.writeAndFlush(protocolMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", protocolMessage);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }
        return resultFuture;
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelKeeper.get(inetSocketAddress);
        if (channel == null) {
            channel = connect(inetSocketAddress);
            channelKeeper.set(inetSocketAddress, channel);
        }
        return channel;
    }

    @SneakyThrows
    public Channel connect(InetSocketAddress socketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(socketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("The client has connected [{}] successful!", socketAddress);
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
    }
}
