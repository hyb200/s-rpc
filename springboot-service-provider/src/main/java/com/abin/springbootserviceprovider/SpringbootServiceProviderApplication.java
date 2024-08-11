package com.abin.springbootserviceprovider;

import com.abin.rpcspringbootstarter.annotation.EnableRpc;
import com.abin.rpcspringbootstarter.bootstrap.RpcConsumerBootstrap;
import com.abin.rpcspringbootstarter.bootstrap.RpcInitBootstrap;
import com.abin.rpcspringbootstarter.bootstrap.RpcProviderBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;


@SpringBootApplication
@EnableRpc
public class SpringbootServiceProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootServiceProviderApplication.class, args);
    }

}
