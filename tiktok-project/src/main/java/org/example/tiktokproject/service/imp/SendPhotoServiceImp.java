package org.example.tiktokproject.service.imp;

import org.example.tiktokproject.pojo.PhotoAndVideo;
import org.example.tiktokproject.service.SendPhotoService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SendPhotoServiceImp implements SendPhotoService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void SendToPython(PhotoAndVideo photoAndVideo) {
        String exchange = "direct.SendPhoto.exchange";
        String routingKey = "SendPhoto";
        rabbitTemplate.convertAndSend(exchange, routingKey, photoAndVideo);
    }
}
