package com.abin;

import com.abin.core.RpcApplication;
import com.abin.core.config.RpcConfig;
import com.abin.core.model.ServiceMetaInfo;
import com.abin.core.registry.LocalRegistry;
import com.abin.core.registry.Registry;
import com.abin.core.registry.RegistryFactory;
import com.abin.core.transport.server.NettyTcpServer;
import com.abin.service.UserService;
import com.abin.service.UserServiceImpl;

public class ServiceProviderApplication {
    public static void main(String[] args) {
        RpcApplication.init();
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        ServiceMetaInfo metaInfo = ServiceMetaInfo.builder()
                .serviceName(UserService.class.getName())
                .host(rpcConfig.getHost())
                .port(rpcConfig.getPort())
                .build();

        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
        registry.register(metaInfo);
        new NettyTcpServer().init(Integer.parseInt(rpcConfig.getPort()));
    }
}
