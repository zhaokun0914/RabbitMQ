package com.example.rabbitmq._06_ttl_queue._01_base;

import com.example.rabbitmq.config.TtlQueueConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Kavin
 * @date 2021-09-23 15:46:46
 */
@Slf4j
@RestController
@RequestMapping("ttl")
public class ProducerController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RequestMapping("sendMsg/{message}")
    public void producer(@PathVariable String message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
        String format = dateFormat.format(new Date());
        log.info("==> 当前时间：{}，发送一条信息给两个TTL队列：{}", format, message);
        rabbitTemplate.convertAndSend(TtlQueueConfig.X_EXCHANGE, TtlQueueConfig.QUEUE_A_ROUTING_KEY, "消息发送给 TTL 为10s的队列" + message);
        rabbitTemplate.convertAndSend(TtlQueueConfig.X_EXCHANGE, TtlQueueConfig.QUEUE_B_ROUTING_KEY, "消息发送给 TTL 为40s的队列" + message);
    }

}
