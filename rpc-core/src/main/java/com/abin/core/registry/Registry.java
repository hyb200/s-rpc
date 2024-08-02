package com.abin.core.registry;

import com.abin.core.config.RegistryConfig;
import com.abin.core.model.ServiceMetaInfo;

import java.util.List;

public interface Registry {

    void init(RegistryConfig registryConfig);

    /**
     * 注册服务（服务端）
     */
    void register(ServiceMetaInfo serviceMetaInfo);

    /**
     * 注销服务（服务端）
     */
    void logout(ServiceMetaInfo serviceMetaInfo);

    /**
     * 发现服务（获取某服务的所有节点，消费端）
     */
    List<ServiceMetaInfo> serviceDiscovery(String serviceKey);

    /**
     * 服务销毁
     */
    void destory();

    /**
     * 心跳检测（服务端）
     */
    void heartBeat();

    /**
     * 监听（消费端）
     */
    void watch(String serviceNodeKey);
}
