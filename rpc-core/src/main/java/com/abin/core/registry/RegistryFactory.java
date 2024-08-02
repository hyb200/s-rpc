package com.abin.core.registry;

import com.abin.core.spi.SpiLoader;

public class RegistryFactory {

    static {
        //  todo 懒汉单例加载
        SpiLoader.load(Registry.class);
    }

    private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();

    public static Registry getInstance(String key) {
        return SpiLoader.getInstance(Registry.class, key);
    }
}
