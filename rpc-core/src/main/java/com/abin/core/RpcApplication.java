package com.abin.core;

import com.abin.core.config.RegistryConfig;
import com.abin.core.config.RpcConfig;
import com.abin.core.constant.RpcConstant;
import com.abin.core.registry.RegistryFactory;
import com.abin.core.uitils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcApplication {

    private static volatile RpcConfig rpcConfig;

    public static void init() {
        RpcConfig custom;
        try {
            custom = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            custom = new RpcConfig();
        }
        init(custom);
    }

    public static void init(RpcConfig customConfig) {
        rpcConfig = customConfig;
        log.info("s-rpc init, config = {}", customConfig.toString());
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        RegistryFactory.getInstance(registryConfig.getRegistry()).init(registryConfig);
        log.info("{} registry init, config = {}", registryConfig.getRegistry(), registryConfig);
    }

    public static RpcConfig getRpcConfig() {
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
