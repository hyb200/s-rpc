package com.abin.core.config;

import lombok.Data;

@Data
public class RpcConfig {

    private String name = "s-rpc";

    private String version = "1.0";

    private Boolean mock = true;

    private String host;

    private String port;

    private String serializer;

}
