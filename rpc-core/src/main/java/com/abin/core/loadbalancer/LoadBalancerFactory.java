package com.abin.core.loadbalancer;

import com.abin.core.spi.SpiLoader;

public class LoadBalancerFactory {

    static {
        //  todo 懒汉单例加载
        SpiLoader.load(LoadBalancer.class);
    }

    private static final LoadBalancer DEFAULT_LOADBALANCER = new PollingLoadBalancer();

    public static LoadBalancer getInstance(String key) {
        return SpiLoader.getInstance(LoadBalancer.class, key);
    }
}
