package com.example.rabbitmq._06_ttl_queue;

import com.example.rabbitmq.config._01_ttl_config.DelayedQueueConfig;
import com.example.rabbitmq.config._01_ttl_config.TtlQueueConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Kavin
 * @date 2021-09-23 15:46:38
 */
@Slf4j
@Component
public class DeadLetterQueueConsumer {

    /**
     * RabbitListener(queues = TtlQueueConfig.DEAD_LETTER_QUEUE)，监听死信队列 QD
     *
     * @param message 接受到的消息
     */
    @RabbitListener(queues = TtlQueueConfig.DEAD_LETTER_QUEUE)
    public void receivedD(Message message) {
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
        String format = dateFormat.format(new Date());
        log.info("<== 当前时间：{}，死信队列收到信息：{}", format, msg);
    }

    @RabbitListener(queues = DelayedQueueConfig.DELAYED_QUEUE_NAME)
    public void receiveDelayedQueue(Message message) {
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
        String format = dateFormat.format(new Date());
        log.info("<== 当前时间：{},收到延时队列的消息：{}", format, msg);
    }

}
