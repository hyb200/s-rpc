package com.abin.core.server;

public interface TcpServer {

    /**
     * 初始化
     */
    void init(int port) throws InterruptedException;
}
