package com.abin.core.config;

import com.abin.core.constant.LoadBalancerConstant;
import lombok.Data;

@Data
public class RpcConfig {

    private String name = "s-rpc";

    private String version = "1.0";

    private Boolean mock = false;

    private String host;

    private String port;

    private String serializer = "jdk";

    private RegistryConfig registryConfig = new RegistryConfig();

    private String loadBalancer = LoadBalancerConstant.RANDOM;

    private String retryStrategy = "no";
}
