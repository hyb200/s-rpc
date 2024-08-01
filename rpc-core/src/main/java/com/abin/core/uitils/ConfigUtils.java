package com.abin.core.uitils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.setting.yaml.YamlUtil;
import com.abin.core.config.RpcConfig;

import java.util.Map;

public class ConfigUtils {

    private static final String BASENAME = "application";

    private static final String YML = ".yml";
    
    private static final String PROPERTIES = ".properties";

    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        return loadConfig(tClass, prefix, "");
    }

    /**
     * 加载配置对象，支持多环境
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String env) {
        boolean exist = FileUtil.exist("application.yml");
        if (exist) {
            return loadFromYml(tClass, prefix, env);
        } else
            return loadFromProperties(tClass, prefix, env);
    }

    public static <T> T loadFromProperties(Class<T> tClass, String prefix, String env) {
        String fileName = buildFileName(env, PROPERTIES);
        Props props = new Props(fileName);
        return props.toBean(tClass, prefix);
    }

    public static <T> T loadFromYml(Class<T> tClass, String prefix, String env) {
        String fileName = buildFileName(env, YML);
        T config = null;
        Map<String, Object> map = YamlUtil.loadByPath(fileName);
        if (map.get(prefix) != null) {
            config = (T) BeanUtil.toBean(map.get(prefix), tClass);
        }
        return config;
    }

    private static String buildFileName(String env, String fileSuffix) {
        StringBuilder sb = new StringBuilder(BASENAME);
        if (StrUtil.isNotBlank(env)) {
            sb.append("-").append(env);
        }
        sb.append(fileSuffix);
        return sb.toString();
    }
}
