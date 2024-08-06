package com.abin.core.loadbalancer;

import com.abin.core.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class ConsistentHashLoadBalancer implements LoadBalancer{

    private final TreeMap<Integer, ServiceMetaInfo> virtualNodes = new TreeMap<>();

    private static final Integer VIRTUALNODES = 100;

    @Override
    public ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceMetaInfos) {
        if (serviceMetaInfos.isEmpty()) {
            return null;
        }

        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfos) {
            for (int i = 0; i < VIRTUALNODES; i++) {
                int hash = getHash(serviceMetaInfo + "#" + i);
                virtualNodes.put(hash, serviceMetaInfo);
            }
        }

        int hash = getHash(params);
        Map.Entry<Integer, ServiceMetaInfo> metaInfoEntry = virtualNodes.ceilingEntry(hash);
        if (Objects.isNull(metaInfoEntry)) {
            metaInfoEntry = virtualNodes.firstEntry();
        }

        return metaInfoEntry.getValue();
    }

    private int getHash(Object s) {
        return s.hashCode();
    }
}
