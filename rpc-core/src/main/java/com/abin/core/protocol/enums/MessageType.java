package com.abin.core.protocol.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum MessageType {

    REQUEST(0),
    RESPONSE(1),
    HEART_BEAT(2),
    OTHERS(3);

    private final int key;

    MessageType(int key) {
        this.key = key;
    }

    private static final Map<Integer, MessageType> cache;

    static {
        cache = Arrays.stream(MessageType.values()).collect(Collectors.toMap(MessageType::getKey, Function.identity()));
    }

    public static MessageType getEnumByKey(int key) {
        MessageType messageType = cache.get(key);
        if (Objects.isNull(messageType)) {
            throw new IllegalArgumentException("unknown protocol message type: " + key);
        }
        return messageType;
    }
}
