package com.abin.core.loadbalancer;

import com.abin.core.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

public interface LoadBalancer {

    ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceMetaInfos);
}
