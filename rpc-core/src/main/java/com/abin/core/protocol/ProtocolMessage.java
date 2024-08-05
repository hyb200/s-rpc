package com.abin.core.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolMessage<T> {

    private Header header;

    private T body;

    @Data
    public static class Header {

        private byte magic;

        private byte version;

        private byte codec;

        private byte type;

        private byte status;

        private long requestId;

        private int bodyLength;
    }
}
