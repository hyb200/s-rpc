package com.abin.core.registry;

import cn.hutool.json.JSONUtil;
import com.abin.core.config.RegistryConfig;
import com.abin.core.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Slf4j
public class EtcdRegistry implements Registry {

    private static Client client;

    private static KV kvClient;

    private static final String ETCD_ROOT_PATH = "/rpc/";

    @Override
    public void init(RegistryConfig registryConfig) {
        Long timeout = registryConfig.getTimeout();
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(timeout))
                .build();
        kvClient = client.getKVClient();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) {
        Lease lease = client.getLeaseClient();

        long leaseId = 0L;
        try {
            leaseId = lease.grant(30).get().getID();
            String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();

            ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
            ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

//            PutOption option = PutOption.builder().withLeaseId(leaseId).build();
//            kvClient.put(key, value, option).get();
            kvClient.put(key, value).get();
        } catch (Exception e) {
            log.error("server register failed: {}", e.getMessage(), e);
        }
    }

    @Override
    public void logout(ServiceMetaInfo serviceMetaInfo) {
        String registryKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(registryKey, StandardCharsets.UTF_8));
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        String prefix = ETCD_ROOT_PATH + serviceKey + "/";

        try {
            GetOption option = GetOption.builder()
                    .isPrefix(true)
                    .build();
            List<KeyValue> kvs = kvClient.get(ByteSequence.from(prefix, StandardCharsets.UTF_8), option).get().getKvs();
            return kvs.stream().map(kv -> {
                String str = kv.getValue().toString(StandardCharsets.UTF_8);
                return JSONUtil.toBean(str, ServiceMetaInfo.class);
            }).toList();
        } catch (Exception e) {
            log.error("server discovery [{}] failed: {}", serviceKey, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void destory() {
        log.info("etcd registry closed.");
        if (Objects.nonNull(kvClient)) kvClient.close();
        if (Objects.nonNull(client)) client.close();
    }

    @Override
    public void heartBeat() {

    }

    @Override
    public void watch(String serviceNodeKey) {

    }
}
