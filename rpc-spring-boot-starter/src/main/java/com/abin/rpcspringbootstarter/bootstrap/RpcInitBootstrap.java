package com.abin.rpcspringbootstarter.bootstrap;

import com.abin.core.RpcApplication;
import com.abin.core.config.RpcConfig;
import com.abin.core.transport.server.NettyTcpServer;
import com.abin.rpcspringbootstarter.annotation.EnableRpc;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 框架启动
 */
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {

    /**
     * Spring 初始化时执行，初始化框架
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        boolean needServer = (boolean) importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName()).get("needServer");
        RpcApplication.init();

        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        if (needServer) {
            new Thread(() -> new NettyTcpServer().init(Integer.parseInt(rpcConfig.getPort()))).start();
        }
    }
}
