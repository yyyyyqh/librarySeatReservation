package com.yqh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqh.entity.User;
import com.yqh.entity.dto.RegisterDTO;

public interface IUserService extends IService<User> {

    /**
     * 用户登录业务逻辑
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户对象，若失败则抛出异常或返回null
     */
    User login(String username, String password);


    void register(RegisterDTO registerDTO);
}