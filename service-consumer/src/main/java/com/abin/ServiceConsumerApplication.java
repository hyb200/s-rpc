package com.abin;

import com.abin.core.proxy.ServiceProxyFactory;
import com.abin.service.UserService;

public class ServiceConsumerApplication {
    public static void main(String[] args) {
        UserService userService = ServiceProxyFactory.serviceProxy(UserService.class);
        String rpcResp = userService.getUserInfo("ikun", 18);
        System.out.println(rpcResp);
    }
}
