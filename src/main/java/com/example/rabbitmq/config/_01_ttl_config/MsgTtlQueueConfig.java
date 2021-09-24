package com.example.rabbitmq.config._01_ttl_config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MsgTtlQueueConfig {

    public static final String QUEUE_C = "QC";
    public static final String QUEUE_C_ROUTING_KEY = "XC";


    @Bean
    public Queue queueC() {
        return QueueBuilder.durable(QUEUE_C)
                           .deadLetterExchange(TtlQueueConfig.Y_DEAD_LETTER_EXCHANGE)
                           .deadLetterRoutingKey(TtlQueueConfig.DEAD_LETTER_QUEUE_ROUTING_KEY)
                           .build();
    }

    @Bean
    public Binding queueCBindingX(Queue queueC, DirectExchange xExchange) {
        return BindingBuilder.bind(queueC).to(xExchange).with(QUEUE_C_ROUTING_KEY);
    }

}
