package com.abin.core.loadbalancer;

import com.abin.core.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PollingLoadBalancer implements LoadBalancer {

    private static final AtomicInteger count = new AtomicInteger();

    @Override
    public ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceMetaInfos) {
        if (serviceMetaInfos.isEmpty()) {
            return null;
        }
        int size = serviceMetaInfos.size();
        if (size == 1) {
            return serviceMetaInfos.get(0);
        }
        return serviceMetaInfos.get(count.getAndIncrement() % size);
    }
}
