package com.example.rabbitmq._07_publish_confirm;

import com.example.rabbitmq.config._02_publish_confirm_config.ConfirmConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConfirmConsumer {

    @RabbitListener(queues = ConfirmConfig.CONFIRM_QUEUE_NAME)
    public void receiveMsg(Message message) {
        String msg = new String(message.getBody());
        log.info("<== 接收到routing key为：{} 的队列的消息:{}", ConfirmConfig.CONFIRM_QUEUE_KEY, msg);
    }

}
