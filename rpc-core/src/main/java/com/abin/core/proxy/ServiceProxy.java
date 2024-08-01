package com.abin.core.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.abin.core.RpcApplication;
import com.abin.core.model.RpcRequest;
import com.abin.core.model.RpcResponse;
import com.abin.core.serializer.JdkSerializer;
import com.abin.core.serializer.Serializer;
import com.abin.core.serializer.SerializerFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * JDK 动态代理
 */
@Slf4j
public class ServiceProxy implements InvocationHandler {

    private final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        byte[] bytes = serializer.serialize(rpcRequest);
        try (HttpResponse httpResponse = HttpRequest.post("http://localhost:9394").body(bytes).execute()) {
            RpcResponse rpcResponse = serializer.deserialize(httpResponse.bodyBytes(), RpcResponse.class);
            return rpcResponse.getData();
        } catch (Exception e) {
            log.error("rpc call exception: {}", e.getMessage());
        }

        return null;
    }
}
