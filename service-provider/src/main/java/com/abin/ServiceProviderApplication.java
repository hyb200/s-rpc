package com.abin;

import com.abin.core.registry.LocalRegistry;
import com.abin.core.server.NettyTcpServer;
import com.abin.service.UserService;
import com.abin.service.UserServiceImpl;

public class ServiceProviderApplication {
    public static void main(String[] args) {
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        new NettyTcpServer().init(9394);
    }
}
