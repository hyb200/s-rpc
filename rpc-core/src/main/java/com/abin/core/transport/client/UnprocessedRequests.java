package com.abin.core.transport.client;

import com.abin.core.model.RpcResponse;
import com.abin.core.protocol.ProtocolMessage;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UnprocessedRequests {

    private static final Map<Long, CompletableFuture<ProtocolMessage<RpcResponse>>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    public void put(Long reqId, CompletableFuture<ProtocolMessage<RpcResponse>> future) {
        UNPROCESSED_RESPONSE_FUTURES.put(reqId, future);
    }

    public void complete(ProtocolMessage<RpcResponse> result) {
        CompletableFuture<ProtocolMessage<RpcResponse>> future = UNPROCESSED_RESPONSE_FUTURES.remove(result.getHeader().getRequestId());
        if (future != null) {
            future.complete(result);
        } else {
            throw new IllegalStateException();
        }
    }
}
