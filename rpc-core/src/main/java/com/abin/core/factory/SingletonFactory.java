package com.abin.core.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonFactory {

    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    private static final Object lock = new Object();

    private SingletonFactory() {
    }

    public static <T> T getInstance(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException();
        }
        String key = clazz.toString();
        if (OBJECT_MAP.containsKey(key)) {
            return clazz.cast(OBJECT_MAP.get(key));
        } else {
            synchronized (lock) {
                if (!OBJECT_MAP.containsKey(key)) {
                    try {
                        T instance = clazz.getDeclaredConstructor().newInstance();
                        OBJECT_MAP.put(key, instance);
                        return instance;
                    } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                             NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    return clazz.cast(OBJECT_MAP.get(key));
                }
            }
        }
    }
}
