package cn.hdu.liu.service;

import cn.hdu.liu.obj.User;

public interface UserService {
    User authenticate(String username, String password);
    User findById(Integer id);
}
