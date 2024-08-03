package com.abin.core.registry;

import com.abin.core.model.ServiceMetaInfo;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.List;

public class CaffeineServiceCache {

    private static final Cache<String, List<ServiceMetaInfo>> cache;

    static {
        cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .maximumSize(1000)
                .build();
    }

    public void write(String serviceKey, List<ServiceMetaInfo> serviceCache) {
        cache.put(serviceKey, serviceCache);
    }

    public List<ServiceMetaInfo> read(String serviceKey) {
        return cache.getIfPresent(serviceKey);
    }

    public void remove(String serviceKey) {
        cache.invalidate(serviceKey);
    }
}
