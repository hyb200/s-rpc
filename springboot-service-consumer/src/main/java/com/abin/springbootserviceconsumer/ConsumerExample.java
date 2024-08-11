package com.abin.springbootserviceconsumer;

import com.abin.rpcspringbootstarter.annotation.RpcReference;
import com.abin.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class ConsumerExample {

    @RpcReference
    private UserService userService;

    public void test() {
        for (int i = 0; i < 3; i++) {
            String rpcResp = userService.getUserInfo("ikun", 18);
            System.out.println(rpcResp);
        }
    }
}
