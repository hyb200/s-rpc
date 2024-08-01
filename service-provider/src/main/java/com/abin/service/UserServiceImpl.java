package com.abin.service;

import com.abin.model.User;

public class UserServiceImpl implements UserService{

    @Override
    public String getUserInfo(String name, Integer age) {
        return new User(name, age).toString();
    }
}
