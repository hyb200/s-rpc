package com.abin.core;

import com.abin.core.config.RpcConfig;
import com.abin.core.uitils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcApplication {

    private static volatile RpcConfig rpcConfig;

    public static void init() {
        RpcConfig custom;
        try {
            custom = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
        } catch (Exception e) {
            custom = new RpcConfig();
        }
        init(custom);
    }

    public static void init(RpcConfig customConfig) {
        rpcConfig = customConfig;
        log.info("s-rpc init, config = {}", customConfig.toString());
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
