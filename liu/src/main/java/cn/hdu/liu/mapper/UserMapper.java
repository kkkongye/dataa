package cn.hdu.liu.mapper;

import cn.hdu.liu.obj.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {
    @Insert("INSERT INTO users (username, password, role) VALUES (#{username}, #{password}, #{role})")
    void insert(User user);

    @Select("SELECT id, username, password, role, created_at FROM users")
    List<User> selectAll();


    @Select("SELECT * FROM users WHERE username = #{username} AND password = #{password}")
    User findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);


    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Integer id);
}