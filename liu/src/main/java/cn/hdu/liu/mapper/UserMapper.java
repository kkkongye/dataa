package cn.hdu.liu.mapper;

import cn.hdu.liu.obj.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {
    @Insert("INSERT INTO users (username, password, roll) VALUES (#{username}, #{password}, #{roll})")
    void insert(User user);

    @Select("SELECT id, username, password, roll, created_at FROM users")
    List<User> selectAll();
}