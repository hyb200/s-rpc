package com.abin.rpcspringbootstarter.annotation;

import com.abin.core.constant.RpcConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务消费者注解（用于注册服务）
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RpcReference {

    /**
     * 服务接口类
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 服务版本号
     * @return
     */
    String serviceVersion() default RpcConstant.DEFAULT_SERVICE_VERSION;

    /**
     * 负载均衡
     * @return
     */
    String loadBalancer() default "polling";

    /**
     * 模拟调用
     * @return
     */
    boolean mock() default false;
}
