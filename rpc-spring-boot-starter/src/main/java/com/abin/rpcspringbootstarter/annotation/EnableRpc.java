package com.abin.rpcspringbootstarter.annotation;

import com.abin.rpcspringbootstarter.bootstrap.RpcConsumerBootstrap;
import com.abin.rpcspringbootstarter.bootstrap.RpcInitBootstrap;
import com.abin.rpcspringbootstarter.bootstrap.RpcProviderBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用 RPC
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootstrap.class, RpcProviderBootstrap.class, RpcConsumerBootstrap.class})
public @interface EnableRpc {

    boolean needServer() default true;
}
