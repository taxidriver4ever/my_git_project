package org.example.tiktokproject.controller;

import jakarta.annotation.Resource;
import org.example.tiktokproject.pojo.EmailWithCode;
import org.example.tiktokproject.pojo.NameWithUUID;
import org.example.tiktokproject.pojo.NormalResult;
import org.example.tiktokproject.pojo.NormalUser;
import org.example.tiktokproject.service.LoginAndRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/user")
public class LoginAndRegisterController {

    @Resource
    private LoginAndRegisterService loginAndRegisterService;

    @PostMapping("/SendOTP")
    public NormalResult SendOTP(@RequestBody NormalUser user) {
        String userEmail = user.getUserEmail();
        System.out.println(userEmail);
        loginAndRegisterService.SendOTP(userEmail);
        return NormalResult.success();
    }

    @PostMapping("/LoginOrRegister")
    public NormalResult LoginOrRegister(@RequestBody EmailWithCode emailWithCode) {
        NameWithUUID nameWithUUID = loginAndRegisterService.LoginOrRegister(emailWithCode);
        if (nameWithUUID == null) return NormalResult.error("验证码错误");
        return NormalResult.success(nameWithUUID);
    }

    @PostMapping("/LoginByPassword")
    public NormalResult LoginByPassword(@RequestBody NormalUser user) {
        NameWithUUID nameWithUUID = loginAndRegisterService.loginByPassword(user.getUserEmail(), user.getUserPassword());
        if(nameWithUUID == null) return NormalResult.error("该用户无设置密码或不存在");
        return NormalResult.success();
    }
}
