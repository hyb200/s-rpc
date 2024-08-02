package com.abin.core.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.abin.core.RpcApplication;
import com.abin.core.model.RpcRequest;
import com.abin.core.model.RpcResponse;
import com.abin.core.model.ServiceMetaInfo;
import com.abin.core.registry.Registry;
import com.abin.core.registry.RegistryFactory;
import com.abin.core.serializer.Serializer;
import com.abin.core.serializer.SerializerFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * JDK 动态代理
 */
@Slf4j
public class ServiceProxy implements InvocationHandler {

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
        //  todo 负载均衡
        ServiceMetaInfo serviceMetaInfo = serviceMetaInfos.get(0);

        Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        byte[] bytes = serializer.serialize(rpcRequest);
        try (HttpResponse httpResponse = HttpRequest.post(serviceMetaInfo.getServiceAddress()).body(bytes).execute()) {
            RpcResponse rpcResponse = serializer.deserialize(httpResponse.bodyBytes(), RpcResponse.class);
            return rpcResponse.getData();
        } catch (Exception e) {
            log.error("rpc call exception: {}", e.getMessage());
        }

        return null;
    }
}
