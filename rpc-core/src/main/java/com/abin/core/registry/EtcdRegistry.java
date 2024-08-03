package com.abin.core.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.abin.core.config.RegistryConfig;
import com.abin.core.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EtcdRegistry implements Registry {

    private static Client client;

    private static KV kvClient;

    private static final String ETCD_ROOT_PATH = "/rpc/";

    private static final Set<String> localRegisterNodeKeySet = new HashSet<>();

    private static final Set<String> watchingServiceNodeSet = new HashSet<>();

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private final CaffeineServiceCache cache = new CaffeineServiceCache();

    @Override
    public void init(RegistryConfig registryConfig) {
        Long timeout = registryConfig.getTimeout();
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(timeout))
                .build();
        kvClient = client.getKVClient();
        scheduledExecutorService.scheduleAtFixedRate(this::heartBeat, 0L, 20L, TimeUnit.SECONDS);
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) {
        Lease lease = client.getLeaseClient();

        long leaseId;
        try {
            leaseId = lease.grant(30).get().getID();
            String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();

            ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
            ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

            PutOption option = PutOption.builder().withLeaseId(leaseId).build();
            kvClient.put(key, value, option).get();
            localRegisterNodeKeySet.add(registerKey);
        } catch (Exception e) {
            log.error("server register failed: {}", e.getMessage(), e);
        }
    }

    @Override
    public void logout(ServiceMetaInfo serviceMetaInfo) {
        String registryKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(registryKey, StandardCharsets.UTF_8));
        localRegisterNodeKeySet.remove(registryKey);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {

        List<ServiceMetaInfo> serviceMetaInfos = cache.read(serviceKey);
        if (CollUtil.isNotEmpty(serviceMetaInfos)) {
            return serviceMetaInfos;
        }

        String prefix = ETCD_ROOT_PATH + serviceKey + "/";

        try {
            GetOption option = GetOption.builder()
                    .isPrefix(true)
                    .build();
            List<KeyValue> kvs = kvClient.get(ByteSequence.from(prefix, StandardCharsets.UTF_8), option).get().getKvs();
            List<ServiceMetaInfo> metaInfos = kvs.stream().map(kv -> {
                watch(kv.getKey().toString(StandardCharsets.UTF_8));
                String str = kv.getValue().toString(StandardCharsets.UTF_8);
                return JSONUtil.toBean(str, ServiceMetaInfo.class);
            }).toList();
            cache.write(serviceKey, metaInfos);
            return metaInfos;
        } catch (Exception e) {
            log.error("server discovery [{}] failed: {}", serviceKey, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void destory() {
        log.info("current node offline");
        for (String key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                log.error("{} failed to offline", key, e);
            }
        }
        if (Objects.nonNull(kvClient)) kvClient.close();
        if (Objects.nonNull(client)) client.close();
        scheduledExecutorService.shutdown();
    }

    @Override
    public void heartBeat() {
        for (String key : localRegisterNodeKeySet) {
            try {
                List<KeyValue> kvs = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8)).get().getKvs();
                if (CollUtil.isEmpty(kvs)) {
                    continue;
                }
                KeyValue keyValue = kvs.get(0);
                String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                ServiceMetaInfo bean = JSONUtil.toBean(value, ServiceMetaInfo.class);
                register(bean);
            } catch (Exception e) {
                log.error("failed to renew: {}", key, e);
            }
        }
    }

    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        boolean isNew = watchingServiceNodeSet.add(serviceNodeKey);
        if (isNew) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), o -> {
                for (WatchEvent event : o.getEvents()) {
                    if (event.getEventType().equals(WatchEvent.EventType.DELETE)) {
                        cache.remove(serviceNodeKey);
                    }
                }
            });
        }
    }
}
