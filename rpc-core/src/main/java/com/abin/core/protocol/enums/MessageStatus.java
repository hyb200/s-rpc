package com.abin.core.protocol.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 协议消息状态枚举
 */
@Getter
public enum MessageStatus {
    OK("ok", 20),
    BAD_REQUEST("badRequest", 40),
    BAD_RESPONSE("badResponse", 50);

    private final String text;

    private final int val;

    MessageStatus(String text, int val) {
        this.text = text;
        this.val = val;
    }

    private static final Map<Integer, MessageStatus> cache;

    static {
        cache = Arrays.stream(MessageStatus.values()).collect(Collectors.toMap(MessageStatus::getVal, Function.identity()));
    }

    public static MessageStatus of(int val) {
        MessageStatus messageStatus = cache.get(val);
        if (Objects.isNull(messageStatus)) {
            throw new IllegalArgumentException("unknown protocol message status: " + val);
        }
        return messageStatus;
    }
}
