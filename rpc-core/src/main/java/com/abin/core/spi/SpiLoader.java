package com.abin.core.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SpiLoader {

    private static final String SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    private static final String CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    private static final String[] SCAN_PATH = new String[]{SYSTEM_SPI_DIR, CUSTOM_SPI_DIR};

    private static final Map<String, Map<String, Class<?>>> loadMap = new ConcurrentHashMap<>();

    private static final Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    public static Map<String, Class<?>> load(Class<?> clazz) {
        log.info("spi load: {}", clazz.getName());

        Map<String, Class<?>> map = new HashMap<>();
        for (String scanDir : SCAN_PATH) {
            List<URL> resources = ResourceUtil.getResources(scanDir + clazz.getName());
            for (URL resource : resources) {
                try {
                    BufferedReader input = new BufferedReader(new InputStreamReader(resource.openStream()));
                    String line;
                    while ((line = input.readLine()) != null) {
                        String[] str = line.trim().split("=");
                        if (str.length > 1) {
                            String key = str[0], className = str[1];
                            map.put(key, Class.forName(className));
                        }
                    }
                } catch (Exception e) {
                    log.error("spi resource load error: {}", e.getMessage(), e);
                }
            }
        }
        loadMap.put(clazz.getName(), map);
        return map;
    }

    public static <T> T getInstance(Class<?> clazz, String key) {
        String clazzName = clazz.getName();
        Map<String, Class<?>> classMap = loadMap.get(clazzName);
        if (classMap == null) {
            throw new RuntimeException(String.format("SpiLoader 未加载 %s 类型", clazzName));
        }
        if (!classMap.containsKey(key)) {
            throw new RuntimeException(String.format("SpiLoader 的 %s 不存在 %s 的类型", clazzName, key));
        }
        Class<?> aClass = classMap.get(key);
        String aClassName = aClass.getName();
        if (!instanceCache.containsKey(aClassName)) {
            try {
                instanceCache.put(key, aClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                log.error("{} failed to new instance", aClassName);
                throw new RuntimeException(e);
            }
        }
        return (T) instanceCache.get(key);
    }
}
