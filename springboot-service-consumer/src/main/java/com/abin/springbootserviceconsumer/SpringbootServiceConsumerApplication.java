package com.abin.springbootserviceconsumer;

import com.abin.rpcspringbootstarter.annotation.EnableRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@EnableRpc(needServer = false)
public class SpringbootServiceConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootServiceConsumerApplication.class, args);
    }

}
