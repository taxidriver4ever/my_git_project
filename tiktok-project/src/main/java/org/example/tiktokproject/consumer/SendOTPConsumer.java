package org.example.tiktokproject.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.example.tiktokproject.pojo.DetectionResponse;
import org.example.tiktokproject.pojo.PhotoAndVideo;
import org.example.tiktokproject.pojo.RecognitionRequest;
import org.example.tiktokproject.pojo.Video;
import org.example.tiktokproject.repository.LoginAndRegisterRedis;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RabbitListener(queues = "SendOTP.direct.queue")
public class SendOTPConsumer {

    @Autowired
    private JavaMailSender mailSender;

    @Resource
    private LoginAndRegisterRedis loginAndRegisterRedis;

    @RabbitHandler
    public void receiveMessage(String userEmail,
                               Channel channel,
                               org.springframework.amqp.core.Message amqpMessage) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag(); // 获取消息的唯一标签

        boolean success = false;
        Random random = new Random();
        for(int i = 0;i<6;i++)
            stringBuilder.append(random.nextInt(10));
        MimeMessagePreparator preparation = getMimeMessagePreparator(userEmail, stringBuilder);
        mailSender.send(preparation);
        System.out.println("邮件发送成功至: " + userEmail);
        success = true; // 标记处理成功
        loginAndRegisterRedis.storeOTP(stringBuilder.toString() , userEmail);
        channel.basicAck(deliveryTag, success);
    }

    private static MimeMessagePreparator getMimeMessagePreparator(String userEmail, StringBuilder stringBuilder) {
        String message = stringBuilder.toString();
        return new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                mimeMessage.setRecipient(Message.RecipientType.TO,
                        new InternetAddress(userEmail));
                mimeMessage.setFrom(new InternetAddress("3887768494@qq.com"));
                mimeMessage.setText("您的验证码是" + message + "(有效期为5分钟)");
                mimeMessage.setSubject("验证码通知");
            }
        };
    }
}
