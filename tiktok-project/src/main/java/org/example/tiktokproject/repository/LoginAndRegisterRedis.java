package org.example.tiktokproject.repository;

public interface LoginAndRegisterRedis {
    public void storeOTP(String otp, String userEmail);
    public boolean verifyOTP(String otp, String userEmail);
    public void storeUserNameWithUUID(String userName, String uuid);
    public boolean verifyUserNameWithUUID(String userName, String uuid);
}
