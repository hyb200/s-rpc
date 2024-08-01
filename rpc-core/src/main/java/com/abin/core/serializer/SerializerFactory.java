package com.abin.core.serializer;

import com.abin.core.spi.SpiLoader;

public class SerializerFactory {

    static {
        //  todo 懒汉单例加载
        SpiLoader.load(Serializer.class);
    }

    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    public static Serializer getInstance(String key) {
        return SpiLoader.getInstance(Serializer.class, key);
    }
}
