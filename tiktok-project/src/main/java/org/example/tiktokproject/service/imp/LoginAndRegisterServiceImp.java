package org.example.tiktokproject.service.imp;

import jakarta.annotation.Resource;
import org.example.tiktokproject.mapper.LoginAndRegisterMapper;
import org.example.tiktokproject.pojo.EmailWithCode;
import org.example.tiktokproject.pojo.NameWithUUID;
import org.example.tiktokproject.repository.LoginAndRegisterRedis;
import org.example.tiktokproject.service.LoginAndRegisterService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LoginAndRegisterServiceImp implements LoginAndRegisterService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private LoginAndRegisterRedis loginAndRegisterRedis;

    @Resource
    private LoginAndRegisterMapper loginAndRegisterMapper;

    @Override
    public void SendOTP(String userEmail) {
        rabbitTemplate.convertAndSend("direct.SendOTP.exchange", "SendOTP", userEmail);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public NameWithUUID LoginOrRegister(EmailWithCode emailWithCode) {
        if(!loginAndRegisterRedis.verifyOTP(emailWithCode.getCode(),emailWithCode.getUserEmail())) return null;
        String userName = loginAndRegisterMapper.selectUserNameByEmail(emailWithCode.getUserEmail());

        NameWithUUID nameWithUUID = new NameWithUUID();
        String stringUUID = UUID.randomUUID().toString();
        if(userName==null) {
            String stringName = UUID.randomUUID().toString();
            loginAndRegisterMapper.insertUserByEmailAndName(emailWithCode.getUserEmail(), stringName);
            nameWithUUID.setName(stringName);
        }
        else nameWithUUID.setName(userName);
        nameWithUUID.setUuid(stringUUID);

        loginAndRegisterRedis.storeUserNameWithUUID(nameWithUUID.getName(), nameWithUUID.getUuid());
        return nameWithUUID;
    }

    @Override
    public NameWithUUID loginByPassword(String userEmail, String userPassword) {
        String userName = loginAndRegisterMapper.selectUserNameByUserEmailAndPassword(userEmail, userPassword);
        System.out.println(userName);
        if(userName==null) return null;
        else {
            NameWithUUID nameWithUUID = new NameWithUUID();
            nameWithUUID.setName(userName);
            nameWithUUID.setUuid(UUID.randomUUID().toString());
            loginAndRegisterRedis.storeUserNameWithUUID(nameWithUUID.getName(), nameWithUUID.getUuid());
            return nameWithUUID;
        }
    }


}
