package com.abin.core.protocol.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 协议消息序列化器枚举
 */
@Getter
public enum MessageSerializer {
    JDK(0, "jdk"),
    KRYO(1, "kryo"),
    ;

    private final int key;
    private final String val;

    MessageSerializer(int key, String val) {
        this.key = key;
        this.val = val;
    }

    private static final Map<Integer, MessageSerializer> keyCache;

    private static final Map<String, MessageSerializer> ValCache;


    static {
        keyCache = Arrays.stream(MessageSerializer.values()).collect(Collectors.toMap(MessageSerializer::getKey, Function.identity()));

        ValCache = Arrays.stream(MessageSerializer.values()).collect(Collectors.toMap(MessageSerializer::getVal, Function.identity()));
    }

    public static MessageSerializer of(int val) {
        MessageSerializer messageSerializer = keyCache.get(val);
        if (Objects.isNull(messageSerializer)) {
            throw new IllegalArgumentException("unknown protocol message status: " + val);
        }
        return messageSerializer;
    }

    public static MessageSerializer getEnumByKey(int key) {
        MessageSerializer messageSerializer = keyCache.get(key);
        if (Objects.isNull(messageSerializer)) {
            throw new IllegalArgumentException("unknown protocol message serializer key: " + key);
        }
        return messageSerializer;
    }

    public static MessageSerializer getEnumByValue(String val) {
        if (ObjectUtil.isEmpty(val)) {
            throw new NullPointerException("protocol message serializer value can not be empty");
        }
        MessageSerializer messageSerializer = ValCache.get(val);
        if (Objects.isNull(messageSerializer)) {
            throw new IllegalArgumentException("unknown protocol message serializer value: " + val);
        }
        return messageSerializer;
    }
}
