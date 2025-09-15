package org.example.tiktokproject.service;

import org.example.tiktokproject.pojo.EmailWithCode;
import org.example.tiktokproject.pojo.NameWithUUID;

public interface LoginAndRegisterService {
    public void SendOTP(String userEmail);
    public NameWithUUID LoginOrRegister(EmailWithCode emailWithCode);
    public NameWithUUID loginByPassword(String userEmail,String userPassword);
}
