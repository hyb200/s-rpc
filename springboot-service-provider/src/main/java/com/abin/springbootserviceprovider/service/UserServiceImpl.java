package com.abin.springbootserviceprovider.service;

import com.abin.model.User;
import com.abin.rpcspringbootstarter.annotation.RpcService;
import com.abin.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RpcService
public class UserServiceImpl implements UserService {

    @Override
    public String getUserInfo(String name, Integer age) {
        return new User(name, age).toString();
    }
}
