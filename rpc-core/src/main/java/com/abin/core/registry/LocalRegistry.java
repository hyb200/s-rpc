package com.abin.core.registry;

import java.util.concurrent.ConcurrentHashMap;

public class LocalRegistry {

    /**
     * 存放服务信息
     */
    private static final ConcurrentHashMap<String, Class<?>> SERVICE_MAP = new ConcurrentHashMap<>();

    /**
     * 注册服务
     *
     * @param serviceName   服务名称
     * @param implClass  实现类
     */
    public static void register(String serviceName, Class<?> implClass) {
        SERVICE_MAP.put(serviceName, implClass);
    }

    /**
     * 获取服务
     *
     * @param serviceName   服务名称
     * @return
     */
    public static Class<?> get(String serviceName) {
        return SERVICE_MAP.get(serviceName);
    }

    /**
     * 移除服务
     *
     * @param serviceName   服务名称
     */
    public static void remove(String serviceName) {
        SERVICE_MAP.remove(serviceName);
    }
}
