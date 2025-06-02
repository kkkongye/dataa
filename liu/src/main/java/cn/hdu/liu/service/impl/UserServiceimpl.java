package cn.hdu.liu.service.impl;

import cn.hdu.liu.mapper.UserMapper;
import cn.hdu.liu.obj.User;
import cn.hdu.liu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User authenticate(String username, String password) {
        return userMapper.findByUsernameAndPassword(username, password);
    }

    @Override
    public User findById(Integer id) {
        return userMapper.findById(id);
    }
}