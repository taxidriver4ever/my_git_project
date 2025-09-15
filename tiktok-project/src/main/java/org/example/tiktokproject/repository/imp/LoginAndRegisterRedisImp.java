package org.example.tiktokproject.repository.imp;

import jakarta.annotation.Resource;
import org.example.tiktokproject.repository.LoginAndRegisterRedis;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Repository
public class LoginAndRegisterRedisImp implements LoginAndRegisterRedis {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void storeOTP(String otp, String userEmail) {
        redisTemplate.opsForValue().set("user:OTP:" + userEmail, otp, 5, TimeUnit.MINUTES);
    }

    @Override
    public boolean verifyOTP(String otp, String userEmail) {
        return Objects.equals(redisTemplate.opsForValue().get("user:OTP:" + userEmail), otp);
    }

    @Override
    public void storeUserNameWithUUID(String userName, String uuid) {
        redisTemplate.opsForValue().set("user:UUID:" + userName, uuid);
    }

    @Override
    public boolean verifyUserNameWithUUID(String userName, String uuid) {
        return Objects.requireNonNull(redisTemplate.opsForValue().get("user:UUID:" + userName)).equals(uuid);
    }
}
