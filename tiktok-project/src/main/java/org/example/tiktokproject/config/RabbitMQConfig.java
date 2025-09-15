package org.example.tiktokproject.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("direct.SendPhoto.exchange", true, false);
    }

    @Bean
    public Queue queue() {
        return new Queue("SendPhoto.direct.queue", true, false, false);
    }

    @Bean
    public Binding binding(DirectExchange directExchange, Queue queue) {
        return BindingBuilder.bind(queue).to(directExchange).with("SendPhoto");
    }

    @Bean
    public DirectExchange directOTPExchange() {
        return new DirectExchange("direct.SendOTP.exchange", true, false);
    }

    @Bean
    public Queue OTPQueue() {
        return new Queue("SendOTP.direct.queue", true, false, false);
    }

    @Bean
    public Binding bindingOTP(DirectExchange directOTPExchange, Queue OTPQueue) {
        // 修正：使用 directOTPExchange 而不是 directExchange
        return BindingBuilder.bind(OTPQueue).to(directOTPExchange).with("SendOTP");
    }

    @Bean
    public MessageConverter messageConverter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        // 设置允许的类模式
        List<String> list = new ArrayList<>();
        list.add("org.example.tiktokproject.pojo.*");
        list.add("java.util.*");
        list.add("java.lang.*");
        converter.setAllowedListPatterns(list);
        return converter;
    }
}