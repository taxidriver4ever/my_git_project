package org.example.tiktokproject.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.tiktokproject.pojo.NormalResult;
import org.example.tiktokproject.pojo.NormalUser;

import java.util.List;

@Mapper
public interface LoginAndRegisterMapper {
    public String selectUserNameByEmail(String userEmail);
    public void insertUserByEmailAndName(String userEmail, String userName);
    public String selectUserNameByUserEmailAndPassword(String userEmail,String userPassword);
    public List<NormalUser> selectUser();
}
