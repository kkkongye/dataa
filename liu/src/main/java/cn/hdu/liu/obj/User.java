package cn.hdu.liu.obj;

import java.sql.Timestamp;

public class User {
    private Integer id;
    private String username;
    private String password;
    private String roll;
    private Timestamp createdAt;


    public String getRoll(){
        return roll;
    }

    public void setRoll(String roll){
        this.roll = roll;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}