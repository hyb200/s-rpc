package com.abin.core.proxy;

import cn.hutool.core.collection.CollUtil;
import com.abin.core.RpcApplication;
import com.abin.core.factory.SingletonFactory;
import com.abin.core.loadbalancer.LoadBalancer;
import com.abin.core.loadbalancer.LoadBalancerFactory;
import com.abin.core.model.RpcRequest;
import com.abin.core.model.RpcResponse;
import com.abin.core.model.ServiceMetaInfo;
import com.abin.core.protocol.ProtocolMessage;
import com.abin.core.registry.Registry;
import com.abin.core.registry.RegistryFactory;
import com.abin.core.transport.client.NettyTcpClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * JDK 动态代理
 */
@Slf4j
public class ServiceProxy implements InvocationHandler {

    private final NettyTcpClient client;

    public ServiceProxy() {
        client = SingletonFactory.getInstance(NettyTcpClient.class);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        ServiceMetaInfo metaInfo = ServiceMetaInfo.builder()
                .serviceName(method.getDeclaringClass().getName())
                .build();
        Registry registry = RegistryFactory.getInstance(RpcApplication.getRpcConfig().getRegistryConfig().getRegistry());
        List<ServiceMetaInfo> serviceMetaInfos = registry.serviceDiscovery(metaInfo.getServiceKey());
        if (CollUtil.isEmpty(serviceMetaInfos)) {
            log.error("No service address is available. ServiceKey: [{}]", metaInfo.getServiceKey());
            return null;
        }

        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(RpcApplication.getRpcConfig().getLoadBalancer());
        Map<String, Object> map = new HashMap<>();
        ServiceMetaInfo serviceMetaInfo = loadBalancer.select(map, serviceMetaInfos);

        ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = ((CompletableFuture<ProtocolMessage<RpcResponse>>) client.sendRpcRequest(
                rpcRequest,
                serviceMetaInfo))
                .get();
        return rpcResponseProtocolMessage.getBody().getData();
    }
}
