package com.abin.core.protocol;

public class ProtocolConstant {

    private ProtocolConstant() {}

    public static final int MESSAGE_HEADER_LENGTH = 17;

    public static final byte PROTOCOL_MAGIC = 0x1;

    public static final byte PROTOCOL_VERSION = 0x1;

    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

}
