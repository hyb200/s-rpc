package com.abin.rpcspringbootstarter.bootstrap;

import com.abin.core.RpcApplication;
import com.abin.core.config.RegistryConfig;
import com.abin.core.config.RpcConfig;
import com.abin.core.model.ServiceMetaInfo;
import com.abin.core.registry.LocalRegistry;
import com.abin.core.registry.Registry;
import com.abin.core.registry.RegistryFactory;
import com.abin.rpcspringbootstarter.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 服务提供者启动
 * 获取所有包含 @RpcService 注解的类，利用 Spring 的特性监听 Bean 的加载
 */
public class RpcProviderBootstrap implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if (rpcService != null) {
            //  注册服务
            //  1.获取服务基本信息
            Class<?> interfaceClass = rpcService.interfaceClass();
            if (interfaceClass == void.class) {
                interfaceClass = beanClass.getInterfaces()[0];
            }
            String serviceName = interfaceClass.getName();
            String serviceVersion = rpcService.serviceVersion();
            //  2.注册服务
            //  本地注册
            LocalRegistry.register(serviceName, beanClass);

            final RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());

            ServiceMetaInfo metaInfo = ServiceMetaInfo.builder()
                    .serviceName(serviceName)
                    .host(rpcConfig.getHost())
                    .port(rpcConfig.getPort())
                    .build();

            registry.register(metaInfo);
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

}
