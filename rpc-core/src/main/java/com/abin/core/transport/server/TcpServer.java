package com.abin.core.transport.server;

public interface TcpServer {

    /**
     * 初始化
     */
    void init(int port) throws InterruptedException;
}
